package org.asaa.agents.coordinators;

import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import org.asaa.agents.PhysicalAgent;
import org.asaa.behaviours.human.HandleMessageBehaviour;
import org.asaa.services.EnvironmentService;
import org.asaa.services.HumanCommunicationService;
import org.asaa.util.SpringContext;

public final class HumanAgent extends PhysicalAgent {
    private final HumanCommunicationService humanCommunicationService = SpringContext.get().getBean(HumanCommunicationService.class);

    @Override
    protected void setup() {
        super.setup();

        addBehaviour(new HandleMessageBehaviour(this));

        addBehaviour(new TickerBehaviour(this, 1000) {
            @Override
            public void onTick() {
                if (!humanCommunicationService.getReceivedMessages().isEmpty()) {
                    logger.info("Sent message");
                    ((HumanAgent)myAgent).sendMessage(humanCommunicationService.getReceivedMessages().removeFirst());
                }
            }
        });
    }

    @Override
    protected void handleTrigger() {

    }

    @Override
    protected String responseDefaultMsgContent() {
        return "I am currently in " + areaName;
    }
}
