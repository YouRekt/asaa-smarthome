package org.asaa.behaviours.appliances.tasks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class PowerNegotiationCoordinator {

    private static final double PAUSE_PREFERENCE_MULTIPLIER = 0.5;
    private static final double INTERRUPT_PENALTY_MULTIPLIER = 2.0;
    private static final double TIME_URGENCY_FACTOR = 0.1;

    public NegotiationResult negotiatePowerAllocation(PowerRequest request,
                                                      List<PowerProposal> proposals,
                                                      double availablePower) {

        // First, check if we should delay/schedule the request instead
        if (shouldScheduleRequest(request, proposals)) {
            return new NegotiationResult(NegotiationResult.Outcome.SCHEDULE_LATER,
                    calculateOptimalScheduleTime(request, proposals),
                    new ArrayList<>());
        }

        // Calculate scores for all proposals
        List<ScoredProposal> scoredProposals = scoreProposals(request, proposals);

        // Sort by score (lower is better - less impact)
        scoredProposals.sort(Comparator.comparingDouble(ScoredProposal::getScore));

        // Select optimal combination
        return selectOptimalProposalCombination(request, scoredProposals, availablePower);
    }

    private boolean shouldScheduleRequest(PowerRequest request, List<PowerProposal> proposals) {
        // Don't schedule if it's urgent or can't be scheduled
        if (request.getUrgency() == PowerRequest.Urgency.IMMEDIATE || !request.isCanBeScheduled()) {
            return false;
        }

        // Check if any current tasks will finish soon
        long nearTermFinishTime = proposals.stream()
                .mapToLong(p -> p.getCurrentTask().getEstimatedRemainingTime())
                .filter(time -> time > 0 && time < request.getMaxWaitTime())
                .min()
                .orElse(Long.MAX_VALUE);

        // If we can wait and there are tasks finishing soon, consider scheduling
        if (nearTermFinishTime < request.getMaxWaitTime()) {
            // Calculate if waiting is better than interrupting/pausing
            double currentInterruptionCost = calculateTotalInterruptionCost(request, proposals);
            double schedulingBenefit = calculateSchedulingBenefit(request, nearTermFinishTime);

            return schedulingBenefit > currentInterruptionCost;
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
        double score = 0.0;
        TaskInfo currentTask = proposal.getCurrentTask();
        TaskInfo requestTask = request.getTaskInfo();

        // Base priority difference (negative if proposal task has higher priority)
        double priorityDifference = requestTask.getPriority() - currentTask.getPriority();
        score += priorityDifference * 10; // Weight priority heavily

        // Action type penalty
        switch (proposal.getAction()) {
            case PAUSE:
                if (currentTask.isPausable()) {
                    score += 5 * PAUSE_PREFERENCE_MULTIPLIER; // Prefer pausable tasks
                } else {
                    score += 50; // High penalty if not actually pausable
                }
                break;
            case INTERRUPT:
                score += 20 * INTERRUPT_PENALTY_MULTIPLIER; // Heavy penalty for interruption
                if (!currentTask.isInterruptible()) {
                    score += 100; // Extremely high penalty if not interruptible
                }
                break;
            case RESCHEDULE:
                score += 8; // Moderate penalty for rescheduling
                break;
        }

        // Task type considerations
        score += calculateTaskTypeScore(currentTask.getType(), requestTask.getType());

        // Time considerations
        double timeRemaining = currentTask.getEstimatedRemainingTime();
        if (timeRemaining > 0) {
            // Prefer tasks that are almost done (less waste)
            score += (timeRemaining / 60000.0) * TIME_URGENCY_FACTOR; // Convert to minutes
        }

        // Impact score from the proposal itself
        score += proposal.getImpactScore();

        // Urgency of the requesting task
        score -= getUrgencyMultiplier(request.getUrgency()) * 5;

        return score;
    }

    private double calculateTaskTypeScore(TaskInfo.Type currentType, TaskInfo.Type requestType) {
        // Define task type hierarchy
        Map<TaskInfo.Type, Integer> typeImportance = Map.of(
                TaskInfo.Type.CRITICAL_SAFETY, 100,
                TaskInfo.Type.USER_COMFORT, 60,
                TaskInfo.Type.MAINTENANCE, 40,
                TaskInfo.Type.OPTIMIZATION, 20,
                TaskInfo.Type.ENTERTAINMENT, 10
        );

        int currentImportance = typeImportance.getOrDefault(currentType, 30);
        int requestImportance = typeImportance.getOrDefault(requestType, 30);

        // Negative score if we're interrupting something more important
        return (currentImportance - requestImportance) * 0.5;
    }

    private double getUrgencyMultiplier(PowerRequest.Urgency urgency) {
        switch (urgency) {
            case IMMEDIATE: return 3.0;
            case HIGH: return 2.0;
            case NORMAL: return 1.0;
            case LOW: return 0.5;
            default: return 1.0;
        }
    }

    private NegotiationResult selectOptimalProposalCombination(PowerRequest request,
                                                               List<ScoredProposal> scoredProposals,
                                                               double availablePower) {
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
        if (proposal.getAction() == PowerProposal.Action.PAUSE && !currentTask.isPausable()) {
            return false;
        }

        return true;
    }

    private double calculateTotalInterruptionCost(PowerRequest request, List<PowerProposal> proposals) {
        return proposals.stream()
                .mapToDouble(p -> calculateProposalScore(request, p))
                .sum();
    }

    private double calculateSchedulingBenefit(PowerRequest request, long waitTime) {
        double urgencyPenalty = getUrgencyMultiplier(request.getUrgency()) * (waitTime / 60000.0);
        return Math.max(0, 50 - urgencyPenalty); // Base benefit minus urgency penalty
    }

    private long calculateOptimalScheduleTime(PowerRequest request, List<PowerProposal> proposals) {
        // Find the earliest time when enough power might be available
        return proposals.stream()
                .mapToLong(p -> p.getCurrentTask().getEstimatedRemainingTime())
                .filter(time -> time > 0)
                .min()
                .orElse(request.getMaxWaitTime());
    }
}

// Helper classes
class ScoredProposal {
    private final PowerProposal proposal;
    private final double score;

    public ScoredProposal(PowerProposal proposal, double score) {
        this.proposal = proposal;
        this.score = score;
    }

    public PowerProposal getProposal() { return proposal; }
    public double getScore() { return score; }
}

