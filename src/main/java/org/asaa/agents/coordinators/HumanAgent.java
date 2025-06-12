package org.asaa.agents.coordinators;

import jade.core.behaviours.TickerBehaviour;
import lombok.Getter;
import lombok.Setter;
import org.asaa.agents.PhysicalAgent;
import org.asaa.behaviours.human.MessageHandlerBehaviour;
import org.asaa.services.HumanCommunicationService;
import org.asaa.tasks.Task;
import org.asaa.util.SpringContext;

public final class HumanAgent extends PhysicalAgent {
    private final HumanCommunicationService humanCommunicationService = SpringContext.get().getBean(HumanCommunicationService.class);
    @Setter
    @Getter
    private Task currentTask = null;

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

    }

    @Override
    protected String responseDefaultMsgContent() {
        return "I am currently in " + areaName;
    }
}
