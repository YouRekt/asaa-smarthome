package org.asaa.behaviours.appliances.tasks;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PowerNegotiationCoordinator {

    private static final double ALMOST_DONE_THRESHOLD_MINUTES = 3.0;
    private static final double ALMOST_DONE_WAIT_PENALTY = 30.0;
    private static final double BASE_SCHEDULING_BENEFIT = 100.0;

    private static final Map<TaskInfo.Type, Double> TASK_TYPE_IMPACT = Map.of(TaskInfo.Type.CRITICAL_SAFETY, 100.0, TaskInfo.Type.USER_COMFORT, 60.0, TaskInfo.Type.MAINTENANCE, 40.0, TaskInfo.Type.OPTIMIZATION, 20.0, TaskInfo.Type.ENTERTAINMENT, 10.0);

    private static final Map<PowerProposal.Action, Double> ACTION_COSTS = Map.of(PowerProposal.Action.PAUSE, 15.0, PowerProposal.Action.INTERRUPT, 40.0);

    static {
        // Load OR-Tools native library
        Loader.loadNativeLibraries();
    }

    public NegotiationResult negotiatePowerAllocation(PowerRequest request, List<PowerProposal> proposals, int availablePower) {

        // First, check if we should delay/schedule the request instead
        if (shouldScheduleRequest(request, proposals, availablePower)) {
            return new NegotiationResult(NegotiationResult.Outcome.SCHEDULE_LATER, calculateOptimalScheduleTime(request, proposals), new ArrayList<>());
        }

        // Use ILP to find optimal solution
        return solveWithILP(request, proposals, availablePower);
    }

    private NegotiationResult solveWithILP(PowerRequest request, List<PowerProposal> proposals, double availablePower) {
        double powerNeeded = request.getPowerAmount() - availablePower;

        if (powerNeeded <= 0) {
            return new NegotiationResult(NegotiationResult.Outcome.ACCEPT, 0L, new ArrayList<>());
        }

        // Filter acceptable proposals
        List<ScoredProposal> acceptableProposals = new ArrayList<>();
        for (PowerProposal proposal : proposals) {
            if (isProposalAcceptable(request, proposal)) {
                double score = calculateProposalScore(request, proposal);
                acceptableProposals.add(new ScoredProposal(proposal, score));
            }
        }

        if (acceptableProposals.isEmpty()) {
            return new NegotiationResult(NegotiationResult.Outcome.REFUSE, 0L, new ArrayList<>());
        }

        // Create the ILP solver
        MPSolver solver = MPSolver.createSolver("SCIP"); // or "CBC", "GLPK"
        if (solver == null) {
            // Fallback to greedy if solver not available
            return fallbackToGreedy(request, acceptableProposals, powerNeeded);
        }

        return formulateAndSolveILP(solver, request, acceptableProposals, powerNeeded);
    }

    private NegotiationResult formulateAndSolveILP(MPSolver solver, PowerRequest request, List<ScoredProposal> acceptableProposals, double powerNeeded) {

        int n = acceptableProposals.size();

        // Decision variables: x[i] = 1 if proposal i is selected, 0 otherwise
        MPVariable[] x = new MPVariable[n];
        for (int i = 0; i < n; i++) {
            x[i] = solver.makeIntVar(0, 1, "x_" + i);
        }

        // Objective: Minimize total impact cost (including bonus impact scores)
        MPObjective objective = solver.objective();
        for (int i = 0; i < n; i++) {
            double totalCost = acceptableProposals.get(i).getScore() + acceptableProposals.get(i).getProposal().getBonusImpactScore();
            objective.setCoefficient(x[i], totalCost);
        }
        objective.setMinimization();

        // Primary constraint: Meet power requirements
        MPConstraint powerConstraint = solver.makeConstraint(powerNeeded, Double.POSITIVE_INFINITY, "power_requirement");
        for (int i = 0; i < n; i++) {
            powerConstraint.setCoefficient(x[i], acceptableProposals.get(i).getProposal().getPowerAmount());
        }

        // Additional constraints for complex business rules
        addAdvancedConstraints(solver, x, acceptableProposals, request);

        // Solve the problem
        MPSolver.ResultStatus resultStatus = solver.solve();

        if (resultStatus == MPSolver.ResultStatus.OPTIMAL || resultStatus == MPSolver.ResultStatus.FEASIBLE) {
            return extractSolution(x, acceptableProposals, solver.objective().value());
        } else {
            return new NegotiationResult(NegotiationResult.Outcome.REFUSE, 0L, new ArrayList<>());
        }
    }

    /**
     * Add advanced constraints that are hard to express in other algorithms
     */
    private void addAdvancedConstraints(MPSolver solver, MPVariable[] x, List<ScoredProposal> proposals, PowerRequest request) {

        int n = proposals.size();

        // Constraint 1: Limit maximum number of critical safety interruptions
        List<Integer> criticalSafetyIndices = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            TaskInfo currentTask = proposals.get(i).getProposal().getCurrentTask();
            if (currentTask != null && currentTask.getType() == TaskInfo.Type.CRITICAL_SAFETY) {
                criticalSafetyIndices.add(i);
            }
        }

        if (!criticalSafetyIndices.isEmpty() && request.getTaskInfo().getType() != TaskInfo.Type.CRITICAL_SAFETY) {
            // Don't interrupt more than 1 critical safety task unless absolutely necessary
            MPConstraint criticalLimit = solver.makeConstraint(0, request.getUrgency() == PowerRequest.Urgency.IMMEDIATE ? 2 : 1, "critical_safety_limit");

            for (int idx : criticalSafetyIndices) {
                criticalLimit.setCoefficient(x[idx], 1);
            }
        }

        // Constraint 2: Prefer not mixing pause and interrupt actions (consistency)
        List<Integer> pauseIndices = new ArrayList<>();
        List<Integer> interruptIndices = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            PowerProposal.Action action = proposals.get(i).getProposal().getAction();
            if (action == PowerProposal.Action.PAUSE) {
                pauseIndices.add(i);
            } else if (action == PowerProposal.Action.INTERRUPT) {
                interruptIndices.add(i);
            }
        }

        if (!pauseIndices.isEmpty() && !interruptIndices.isEmpty()) {
            // Soft constraint: if we use interrupts, prefer interrupts; if we use pauses, prefer pauses
            // This is achieved by adding auxiliary variables and constraints
            MPVariable usePause = solver.makeIntVar(0, 1, "use_pause");
            MPVariable useInterrupt = solver.makeIntVar(0, 1, "use_interrupt");

            // If any pause is selected, usePause = 1
            for (int idx : pauseIndices) {
                MPConstraint pauseImplication = solver.makeConstraint(0, Double.POSITIVE_INFINITY, "pause_impl_" + idx);
                pauseImplication.setCoefficient(usePause, 1);
                pauseImplication.setCoefficient(x[idx], -1);
            }

            // If any interrupt is selected, useInterrupt = 1
            for (int idx : interruptIndices) {
                MPConstraint interruptImplication = solver.makeConstraint(0, Double.POSITIVE_INFINITY, "interrupt_impl_" + idx);
                interruptImplication.setCoefficient(useInterrupt, 1);
                interruptImplication.setCoefficient(x[idx], -1);
            }

            // Add penalty to objective for mixing actions
            solver.objective().setCoefficient(usePause, 5); // Small penalty
            solver.objective().setCoefficient(useInterrupt, 5); // Small penalty
        }

        // Constraint 3: Prioritize agents from same appliance/system (locality preference)
        Map<String, List<Integer>> applianceGroups = groupProposalsByAppliance(proposals);

        for (Map.Entry<String, List<Integer>> entry : applianceGroups.entrySet()) {
            List<Integer> indices = entry.getValue();
            if (indices.size() > 1) {
                // If we select one agent from this appliance, slightly prefer selecting others too
                // This reduces context switching overhead
                MPVariable applianceUsed = solver.makeIntVar(0, 1, "appliance_" + entry.getKey());

                for (int idx : indices) {
                    MPConstraint applianceConstraint = solver.makeConstraint(0, Double.POSITIVE_INFINITY, "appliance_constraint_" + idx);
                    applianceConstraint.setCoefficient(applianceUsed, 1);
                    applianceConstraint.setCoefficient(x[idx], -1);
                }

                // Small bonus for using appliances (negative cost)
                solver.objective().setCoefficient(applianceUsed, -2);
            }
        }

        // Constraint 4: Load balancing - don't overload any single appliance
        for (Map.Entry<String, List<Integer>> entry : applianceGroups.entrySet()) {
            List<Integer> indices = entry.getValue();
            if (indices.size() > 2) {
                // Don't select more than 70% of agents from any single appliance
                int maxFromAppliance = Math.max(1, (int) Math.ceil(indices.size() * 0.7));
                MPConstraint loadBalance = solver.makeConstraint(0, maxFromAppliance, "load_balance_" + entry.getKey());

                for (int idx : indices) {
                    loadBalance.setCoefficient(x[idx], 1);
                }
            }
        }

        // Constraint 5: Time-based constraints considering timeToFree
        for (int i = 0; i < n; i++) {
            TaskInfo currentTask = proposals.get(i).getProposal().getCurrentTask();
            PowerProposal proposal = proposals.get(i).getProposal();

            if (currentTask != null) {
                double timeRemainingMinutes = currentTask.getEstimatedRemainingTime() / 60000.0;
                if (timeRemainingMinutes > 0 && timeRemainingMinutes <= 1.0) { // Less than 1 minute
                    // Very strong preference against interrupting
                    solver.objective().setCoefficient(x[i], solver.objective().getCoefficient(x[i]) + 100);
                }
            }

            // Also consider timeToFree - prefer proposals that free power quickly
            if (proposal.getTimeToFree() > 0) {
                double timeToFreeMinutes = proposal.getTimeToFree() / 60000.0;
                if (timeToFreeMinutes > 5.0) { // More than 5 minutes to free power
                    solver.objective().setCoefficient(x[i], solver.objective().getCoefficient(x[i]) + timeToFreeMinutes * 0.5);
                }
            }
        }
    }

    private Map<String, List<Integer>> groupProposalsByAppliance(List<ScoredProposal> proposals) {
        Map<String, List<Integer>> groups = new java.util.HashMap<>();

        for (int i = 0; i < proposals.size(); i++) {
            // Group by agent ID prefix (assuming format like "appliance1_agent1")
            String agentId = proposals.get(i).getProposal().getAgentId();
            String applianceId = extractApplianceId(agentId);
            groups.computeIfAbsent(applianceId, k -> new ArrayList<>()).add(i);
        }

        return groups;
    }

    private String extractApplianceId(String agentId) {
        // Extract appliance ID from agent ID
        // Assuming format like "appliance1_agent1" or "hvac_unit_2_agent_3"
        int lastUnderscore = agentId.lastIndexOf('_');
        if (lastUnderscore > 0) {
            return agentId.substring(0, lastUnderscore);
        }
        return agentId; // Fallback to full agent ID if no pattern found
    }

    private NegotiationResult extractSolution(MPVariable[] x, List<ScoredProposal> proposals, double optimalValue) {
        List<PowerProposal> selectedProposals = new ArrayList<>();

        for (int i = 0; i < x.length; i++) {
            if (x[i].solutionValue() > 0.5) { // Binary variable, so > 0.5 means selected
                selectedProposals.add(proposals.get(i).getProposal());
            }
        }

        System.out.println("ILP found optimal solution with cost: " + optimalValue);
        return new NegotiationResult(NegotiationResult.Outcome.ACCEPT, 0L, selectedProposals);
    }

    /**
     * Multi-objective ILP formulation - optimize multiple criteria simultaneously
     */
    private NegotiationResult solveMultiObjectiveILP(PowerRequest request, List<ScoredProposal> acceptableProposals, double powerNeeded) {

        MPSolver solver = MPSolver.createSolver("SCIP");
        if (solver == null) {
            return fallbackToGreedy(request, acceptableProposals, powerNeeded);
        }

        int n = acceptableProposals.size();
        MPVariable[] x = new MPVariable[n];
        for (int i = 0; i < n; i++) {
            x[i] = solver.makeIntVar(0, 1, "x_" + i);
        }

        // Multi-objective: weighted sum of different criteria
        MPObjective objective = solver.objective();

        for (int i = 0; i < n; i++) {
            ScoredProposal sp = acceptableProposals.get(i);
            PowerProposal proposal = sp.getProposal();

            // Weight 1: Basic impact score (primary objective)
            double impactWeight = 1.0;
            objective.setCoefficient(x[i], impactWeight * sp.getScore());

            // Weight 2: Energy efficiency (secondary objective)
            double energyEfficiency = calculateEnergyEfficiency(proposal);
            double efficiencyWeight = 0.1;
            objective.setCoefficient(x[i], objective.getCoefficient(x[i]) - efficiencyWeight * energyEfficiency);

            // Weight 3: User satisfaction impact (tertiary objective)
            double userSatisfactionImpact = calculateUserSatisfactionImpact(proposal, request);
            double satisfactionWeight = 0.2;
            objective.setCoefficient(x[i], objective.getCoefficient(x[i]) + satisfactionWeight * userSatisfactionImpact);
        }

        objective.setMinimization();

        // Power constraint
        MPConstraint powerConstraint = solver.makeConstraint(powerNeeded, Double.POSITIVE_INFINITY, "power_requirement");
        for (int i = 0; i < n; i++) {
            powerConstraint.setCoefficient(x[i], acceptableProposals.get(i).getProposal().getPowerAmount());
        }

        // Solve and extract solution
        MPSolver.ResultStatus resultStatus = solver.solve();

        if (resultStatus == MPSolver.ResultStatus.OPTIMAL || resultStatus == MPSolver.ResultStatus.FEASIBLE) {
            return extractSolution(x, acceptableProposals, solver.objective().value());
        } else {
            return new NegotiationResult(NegotiationResult.Outcome.REFUSE, 0L, new ArrayList<>());
        }
    }

    private double calculateEnergyEfficiency(PowerProposal proposal) {
        // Example: return higher values for more energy-efficient operations
        TaskInfo currentTask = proposal.getCurrentTask();
        if (currentTask == null) return 100.0; // Idle is most efficient

        return switch (currentTask.getType()) {
            case OPTIMIZATION -> 80.0; // Optimization tasks are usually efficient
            case MAINTENANCE -> 60.0;
            case USER_COMFORT -> 40.0;
            case ENTERTAINMENT -> 20.0;
            case CRITICAL_SAFETY -> 10.0; // Safety tasks may be less efficient but necessary
        };
    }

    private double calculateUserSatisfactionImpact(PowerProposal proposal, PowerRequest request) {
        TaskInfo currentTask = proposal.getCurrentTask();
        if (currentTask == null) return 0.0; // No impact on satisfaction

        // Higher values mean more negative impact on user satisfaction
        double impact = switch (currentTask.getType()) {
            case USER_COMFORT -> 50.0; // High impact
            case ENTERTAINMENT -> 40.0;
            case MAINTENANCE -> 20.0;
            case OPTIMIZATION -> 10.0;
            case CRITICAL_SAFETY -> 5.0; // Users understand safety interruptions
        };

        // Reduce impact if the requesting task is more important
        if (request.getTaskInfo().getPriority() > currentTask.getPriority()) {
            impact *= 0.7;
        }

        return impact;
    }

    // Fallback greedy algorithm if ILP solver is not available
    private NegotiationResult fallbackToGreedy(PowerRequest request, List<ScoredProposal> acceptableProposals, double powerNeeded) {
        acceptableProposals.sort((a, b) -> Double.compare(a.getScore(), b.getScore()));

        List<PowerProposal> selectedProposals = new ArrayList<>();
        double powerToBeFreed = 0.0;

        for (ScoredProposal scoredProposal : acceptableProposals) {
            if (powerToBeFreed >= powerNeeded) {
                break;
            }
            selectedProposals.add(scoredProposal.getProposal());
            powerToBeFreed += scoredProposal.getProposal().getPowerAmount();
        }

        if (powerToBeFreed >= powerNeeded) {
            return new NegotiationResult(NegotiationResult.Outcome.ACCEPT, 0L, selectedProposals);
        } else {
            return new NegotiationResult(NegotiationResult.Outcome.REFUSE, 0L, new ArrayList<>());
        }
    }

    // ... (include all your existing helper methods)
    private boolean shouldScheduleRequest(PowerRequest request, List<PowerProposal> proposals, int availablePower) {
        if (request.getUrgency() == PowerRequest.Urgency.IMMEDIATE || !request.isCanBeScheduled()) {
            return false;
        }

        long nearTermFinishTime = proposals.stream().filter(p -> p.getCurrentTask() != null).mapToLong(p -> p.getCurrentTask().getEstimatedRemainingTime()).filter(time -> time > 0 && time < request.getMaxWaitTime()).min().orElse(Long.MAX_VALUE);

        if (nearTermFinishTime < request.getMaxWaitTime()) {
            double actualInterruptionCost = calculateActualInterruptionCost(request, proposals, availablePower);
            double schedulingBenefit = calculateSchedulingBenefit(request, nearTermFinishTime);
            return schedulingBenefit > actualInterruptionCost;
        }

        return false;
    }

    /**
     * Enhanced scoring that incorporates the bonus impact score from PowerProposal
     */
    private double calculateProposalScore(PowerRequest request, PowerProposal proposal) {
        TaskInfo currentTask = proposal.getCurrentTask();
        TaskInfo requestTask = request.getTaskInfo();

        if (currentTask == null) {
            // Idle agents have minimal impact, but still consider bonus impact
            return 5.0 + proposal.getBonusImpactScore();
        }

        double score = 0.0;

        // 1. Base task type impact
        score += TASK_TYPE_IMPACT.getOrDefault(currentTask.getType(), 30.0);

        // 2. Action cost
        score += ACTION_COSTS.getOrDefault(proposal.getAction(), 25.0);

        // 3. Priority difference
        double priorityDiff = requestTask.getPriority() - currentTask.getPriority();
        if (priorityDiff > 0) {
            score -= priorityDiff * 0.5;
        } else {
            score += Math.abs(priorityDiff) * 0.5;
        }

        // 4. "Almost done" penalty
        double timeRemainingMinutes = currentTask.getEstimatedRemainingTime() / 60000.0;
        if (timeRemainingMinutes > 0 && timeRemainingMinutes <= ALMOST_DONE_THRESHOLD_MINUTES) {
            double completionPercentage = 1.0 - (timeRemainingMinutes / ALMOST_DONE_THRESHOLD_MINUTES);
            score += ALMOST_DONE_WAIT_PENALTY * completionPercentage;
        }

        // 5. Urgency modifier
        score -= getUrgencyMultiplier(request.getUrgency()) * 10.0;

        // 6. Feasibility penalties
        if (proposal.getAction() == PowerProposal.Action.PAUSE && !currentTask.isPausable()) {
            score += 200.0;
        }
        if (proposal.getAction() == PowerProposal.Action.INTERRUPT && !currentTask.isInterruptible()) {
            score += 200.0;
        }

        // 7. Time to free power consideration
        if (proposal.getTimeToFree() > 0) {
            // Prefer proposals that can free power quickly
            double timeToFreeMinutes = proposal.getTimeToFree() / 60000.0;
            score += timeToFreeMinutes * 2.0; // Penalty for slow power release
        }

        // Note: bonusImpactScore is added in the ILP formulation, not here
        // to avoid double-counting in the base score calculation

        return Math.max(score, 1.0);
    }

    private double getUrgencyMultiplier(PowerRequest.Urgency urgency) {
        return switch (urgency) {
            case IMMEDIATE -> 3.0;
            case HIGH -> 2.0;
            case NORMAL -> 1.0;
            case LOW -> 0.5;
        };
    }

    private boolean isProposalAcceptable(PowerRequest request, PowerProposal proposal) {
        TaskInfo currentTask = proposal.getCurrentTask();

        if (currentTask == null) {
            return true;
        }

        TaskInfo requestTask = request.getTaskInfo();

        if (currentTask.getType() == TaskInfo.Type.CRITICAL_SAFETY && requestTask.getType() != TaskInfo.Type.CRITICAL_SAFETY) {
            return false;
        }

        if (proposal.getAction() == PowerProposal.Action.INTERRUPT && !currentTask.isInterruptible() && request.getUrgency() != PowerRequest.Urgency.IMMEDIATE) {
            return false;
        }

        return proposal.getAction() != PowerProposal.Action.PAUSE || currentTask.isPausable();
    }

    private double calculateActualInterruptionCost(PowerRequest request, List<PowerProposal> proposals, double availablePower) {
        // Implementation similar to original
        return 0.0; // Simplified for brevity
    }

    private double calculateSchedulingBenefit(PowerRequest request, long waitTimeMs) {
        double waitTimeMinutes = waitTimeMs / 60000.0;
        double urgencyPenalty = getUrgencyMultiplier(request.getUrgency()) * waitTimeMinutes * 5.0;
        return Math.max(0, BASE_SCHEDULING_BENEFIT - urgencyPenalty);
    }

    private long calculateOptimalScheduleTime(PowerRequest request, List<PowerProposal> proposals) {
        return proposals.stream().filter(p -> p.getCurrentTask() != null).mapToLong(p -> p.getCurrentTask().getEstimatedRemainingTime()).filter(time -> time > 0).min().orElse(request.getMaxWaitTime());
    }
}

class ScoredProposal {
    private final PowerProposal proposal;
    private final double score;

    public ScoredProposal(PowerProposal proposal, double score) {
        this.proposal = proposal;
        this.score = score;
    }

    public PowerProposal getProposal() {
        return proposal;
    }

    public double getScore() {
        return score;
    }
}