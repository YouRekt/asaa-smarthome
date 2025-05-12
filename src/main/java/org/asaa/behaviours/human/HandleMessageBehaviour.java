package org.asaa.behaviours.human;

import jade.lang.acl.ACLMessage;
import org.asaa.agents.coordinators.HumanAgent;
import org.asaa.behaviours.BaseMessageHandler;

public class HandleMessageBehaviour extends BaseMessageHandler {
    protected final HumanAgent humanAgent;

    public HandleMessageBehaviour(HumanAgent humanAgent) {
        super(humanAgent);
        this.humanAgent = humanAgent;
    }

    @Override
    public void action() {
        final ACLMessage msg = myAgent.receive();

        if (msg != null) {
            humanAgent.logger.info("Received message, perf={}, convId={}, content={}", msg.getPerformative(), msg.getConversationId(), msg.getContent());
            // Here we can add a specialized switch if needed (default -> processMsg(msg);)
            super.processMsg(msg);
        } else {
            block();
        }
    }
}
