package org.asaa.behaviours.appliances.tasks;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class NegotiationResult {
    private final Outcome outcome;
    private final long scheduleTime; // When to schedule if outcome is SCHEDULE_LATER
    private final List<PowerProposal> acceptedProposals;

    public enum Outcome {
        ACCEPT,         // Accept the request and selected proposals
        REFUSE,         // Refuse the request (not enough power can be freed)
        SCHEDULE_LATER  // Schedule the request for later time
    }
}
