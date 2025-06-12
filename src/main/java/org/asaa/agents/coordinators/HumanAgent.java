package org.asaa.agents.coordinators;

import jade.core.behaviours.TickerBehaviour;
import org.asaa.agents.PhysicalAgent;
import org.asaa.behaviours.human.MessageHandlerBehaviour;
import org.asaa.services.HumanCommunicationService;
import org.asaa.util.SpringContext;

public final class HumanAgent extends PhysicalAgent {
    private final HumanCommunicationService humanCommunicationService = SpringContext.get().getBean(HumanCommunicationService.class);

    @Override
    protected void setup() {
        super.setup();

        addBehaviour(new MessageHandlerBehaviour(this));

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
        logger.info("HumanAgent handleTrigger");
    }
}
