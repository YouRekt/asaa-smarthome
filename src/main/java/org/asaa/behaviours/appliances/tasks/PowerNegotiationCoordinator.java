package org.asaa.behaviours.appliances.tasks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class PowerNegotiationCoordinator {

    // Sensible impact score ranges (eyeball-friendly values)
    // Base impact: 10-100 (task type importance)
    // Action penalties: 5-50 (disruption cost)
    // Priority modifiers: ±50 (priority differences)
    // Final range: ~10-200 points

    private static final double ALMOST_DONE_THRESHOLD_MINUTES = 3.0;
    private static final double ALMOST_DONE_WAIT_PENALTY = 30.0; // High penalty for interrupting almost-done tasks
    private static final double BASE_SCHEDULING_BENEFIT = 100.0; // Should be higher than typical interruption costs

    // Task type base impact scores (higher = more important, harder to interrupt)
    private static final Map<TaskInfo.Type, Double> TASK_TYPE_IMPACT = Map.of(
            TaskInfo.Type.CRITICAL_SAFETY, 100.0,
            TaskInfo.Type.USER_COMFORT, 60.0,
            TaskInfo.Type.MAINTENANCE, 40.0,
            TaskInfo.Type.OPTIMIZATION, 20.0,
            TaskInfo.Type.ENTERTAINMENT, 10.0
    );

    // Action base costs (higher = more disruptive)
    private static final Map<PowerProposal.Action, Double> ACTION_COSTS = Map.of(
            PowerProposal.Action.PAUSE, 15.0,
            PowerProposal.Action.INTERRUPT, 40.0
    );

    public NegotiationResult negotiatePowerAllocation(PowerRequest request, List<PowerProposal> proposals, int availablePower) {

        // First, check if we should delay/schedule the request instead
        if (shouldScheduleRequest(request, proposals, availablePower)) {
            return new NegotiationResult(
                    NegotiationResult.Outcome.SCHEDULE_LATER,
                    calculateOptimalScheduleTime(request, proposals, availablePower),
                    new ArrayList<>()
            );
        }

        // Calculate scores for all proposals
        List<ScoredProposal> scoredProposals = scoreProposals(request, proposals);

        // Sort by score (lower is better - less impact)
        scoredProposals.sort(Comparator.comparingDouble(ScoredProposal::getScore));

        // Select an optimal combination
        return selectOptimalProposalCombination(request, scoredProposals, availablePower);
    }

    private boolean shouldScheduleRequest(PowerRequest request, List<PowerProposal> proposals, int availablePower) {
        // Don't schedule if it's urgent or can't be scheduled
        if (request.getUrgency() == PowerRequest.Urgency.IMMEDIATE || !request.isCanBeScheduled()) {
            return false;
        }

        // Check if any current tasks will finish soon
        long nearTermFinishTime = proposals.stream()
                .filter(p -> p.getCurrentTask() != null)
                .mapToLong(p -> p.getCurrentTask().getEstimatedRemainingTime())
                .filter(time -> time > 0 && time < request.getMaxWaitTime())
                .min()
                .orElse(Long.MAX_VALUE);

        // If we can wait and there are tasks finishing soon, consider scheduling
        if (nearTermFinishTime < request.getMaxWaitTime()) {
            // Calculate if waiting is better than interrupting
            double actualInterruptionCost = calculateActualInterruptionCost(request, proposals, availablePower);
            double schedulingBenefit = calculateSchedulingBenefit(request, nearTermFinishTime);

            return schedulingBenefit > actualInterruptionCost;
        }

        return false;
    }

    private List<ScoredProposal> scoreProposals(PowerRequest request, List<PowerProposal> proposals) {
        List<ScoredProposal> scoredProposals = new ArrayList<>();

        for (PowerProposal proposal : proposals) {
            double score = calculateProposalScore(request, proposal);
            scoredProposals.add(new ScoredProposal(proposal, score));
        }

        return scoredProposals;
    }

    private double calculateProposalScore(PowerRequest request, PowerProposal proposal) {
        TaskInfo currentTask = proposal.getCurrentTask();
        TaskInfo requestTask = request.getTaskInfo();

        // If no current task (idle agent), very low impact
        if (currentTask == null) {
            return 5.0; // Idle agents are always best choice
        }

        double score = 0.0;

        // 1. Base task type impact (higher = more important = harder to interrupt)
        score += TASK_TYPE_IMPACT.getOrDefault(currentTask.getType(), 30.0);

        // 2. Action cost (higher = more disruptive)
        score += ACTION_COSTS.getOrDefault(proposal.getAction(), 25.0);

        // 3. Priority difference (favor interrupting lower priority tasks)
        double priorityDiff = requestTask.getPriority() - currentTask.getPriority();
        if (priorityDiff > 0) {
            score -= priorityDiff * 0.5; // Reduce score for lower priority current tasks
        } else {
            score += Math.abs(priorityDiff) * 0.5; // Increase score for higher priority current tasks
        }

        // 4. "Almost done" penalty - heavily penalize interrupting tasks almost finished
        double timeRemainingMinutes = currentTask.getEstimatedRemainingTime() / 60000.0;
        if (timeRemainingMinutes > 0 && timeRemainingMinutes <= ALMOST_DONE_THRESHOLD_MINUTES) {
            // The closer to completion, the higher the penalty
            double completionPercentage = 1.0 - (timeRemainingMinutes / ALMOST_DONE_THRESHOLD_MINUTES);
            score += ALMOST_DONE_WAIT_PENALTY * completionPercentage;
        }

        // 5. Urgency of requesting task (reduce score for urgent requests)
        score -= getUrgencyMultiplier(request.getUrgency()) * 10.0;

        // 6. Action feasibility penalties
        if (proposal.getAction() == PowerProposal.Action.PAUSE && !currentTask.isPausable()) {
            score += 200.0; // Massive penalty for impossible actions
        }
        if (proposal.getAction() == PowerProposal.Action.INTERRUPT && !currentTask.isInterruptible()) {
            score += 200.0; // Massive penalty for impossible actions
        }

        return Math.max(score, 1.0); // Minimum score of 1
    }

    private double getUrgencyMultiplier(PowerRequest.Urgency urgency) {
        return switch (urgency) {
            case IMMEDIATE -> 3.0;
            case HIGH -> 2.0;
            case NORMAL -> 1.0;
            case LOW -> 0.5;
        };
    }

    private NegotiationResult selectOptimalProposalCombination(PowerRequest request, List<ScoredProposal> scoredProposals, double availablePower) {
        double powerNeeded = request.getPowerAmount() - availablePower;
        List<PowerProposal> selectedProposals = new ArrayList<>();
        double powerToBeFreed = 0.0;

        // Greedy selection of best proposals until we have enough power
        for (ScoredProposal scoredProposal : scoredProposals) {
            if (powerToBeFreed >= powerNeeded) {
                break;
            }

            PowerProposal proposal = scoredProposal.getProposal();

            // Check if this proposal is acceptable
            if (isProposalAcceptable(request, proposal)) {
                selectedProposals.add(proposal);
                powerToBeFreed += proposal.getPowerAmount();
            }
        }

        // Determine outcome
        if (powerToBeFreed >= powerNeeded) {
            return new NegotiationResult(NegotiationResult.Outcome.ACCEPT, 0, selectedProposals);
        } else {
            return new NegotiationResult(NegotiationResult.Outcome.REFUSE, 0, new ArrayList<>());
        }
    }

    private boolean isProposalAcceptable(PowerRequest request, PowerProposal proposal) {
        TaskInfo currentTask = proposal.getCurrentTask();

        // Idle agents are always acceptable
        if (currentTask == null) {
            return true;
        }

        TaskInfo requestTask = request.getTaskInfo();

        // Never interrupt critical safety tasks unless the request is also critical safety
        if (currentTask.getType() == TaskInfo.Type.CRITICAL_SAFETY &&
                requestTask.getType() != TaskInfo.Type.CRITICAL_SAFETY) {
            return false;
        }

        // Don't interrupt non-interruptible tasks unless absolutely critical
        if (proposal.getAction() == PowerProposal.Action.INTERRUPT &&
                !currentTask.isInterruptible() &&
                request.getUrgency() != PowerRequest.Urgency.IMMEDIATE) {
            return false;
        }

        // Don't pause non-pausable tasks
        return proposal.getAction() != PowerProposal.Action.PAUSE || currentTask.isPausable();
    }

    /**
     * Calculate the actual cost of the interruption plan that would be executed
     * (Fixed version - only considers agents that would actually be selected)
     */
    private double calculateActualInterruptionCost(PowerRequest request, List<PowerProposal> proposals, double availablePower) {
        List<ScoredProposal> scoredProposals = scoreProposals(request, proposals);
        scoredProposals.sort(Comparator.comparingDouble(ScoredProposal::getScore));

        double powerNeeded = request.getPowerAmount() - availablePower;
        double powerToBeFreed = 0.0;
        double totalCost = 0.0;

        // Calculate cost of the agents we'd actually interrupt
        for (ScoredProposal scoredProposal : scoredProposals) {
            if (powerToBeFreed >= powerNeeded) {
                break;
            }

            if (isProposalAcceptable(request, scoredProposal.getProposal())) {
                totalCost += scoredProposal.getScore();
                powerToBeFreed += scoredProposal.getProposal().getPowerAmount();
            }
        }

        return totalCost;
    }

    private double calculateSchedulingBenefit(PowerRequest request, long waitTimeMs) {
        double waitTimeMinutes = waitTimeMs / 60000.0;
        double urgencyPenalty = getUrgencyMultiplier(request.getUrgency()) * waitTimeMinutes * 5.0;

        // Base benefit minus penalty for waiting
        return Math.max(0, BASE_SCHEDULING_BENEFIT - urgencyPenalty);
    }

    private long calculateOptimalScheduleTime(PowerRequest request, List<PowerProposal> proposals, int availablePower) {
        double powerNeeded = request.getPowerAmount() - availablePower;

        // Sort tasks by completion time
        List<PowerProposal> sortedByTime = proposals.stream()
                .filter(p -> p.getCurrentTask() != null && p.getCurrentTask().getEstimatedRemainingTime() > 0)
                .sorted(Comparator.comparingLong(p -> p.getCurrentTask().getEstimatedRemainingTime()))
                .toList();

        double accumulatedPower = 0;
        for (PowerProposal proposal : sortedByTime) {
            accumulatedPower += proposal.getPowerAmount();
            if (accumulatedPower >= powerNeeded) {
                return proposal.getCurrentTask().getEstimatedRemainingTime() + 150;
            }
        }

        return request.getMaxWaitTime(); // Fallback if not enough power even after all tasks
    }
}

// Helper class
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