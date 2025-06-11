package org.asaa.behaviours.human;

import jade.lang.acl.ACLMessage;
import org.asaa.agents.coordinators.HumanAgent;
import org.asaa.behaviours.BaseMessageHandlerBehaviour;
import org.asaa.util.Util;

public class MessageHandlerBehaviour extends BaseMessageHandlerBehaviour {
    protected final HumanAgent humanAgent;

    public MessageHandlerBehaviour(HumanAgent humanAgent) {
        super(humanAgent);
        this.humanAgent = humanAgent;
    }

    @Override
    public void action() {
        final ACLMessage msg = myAgent.receive();

        if (msg != null) {
            humanAgent.getLogger().info("Received message, perf={}, convId={}, content={}", msg.getPerformative(), Util.ConvertStringToACLPerformative(msg.getConversationId()), msg.getContent());
            // Here we can add a specialized switch if needed (default -> processMsg(msg);)
            super.processMsg(msg);
        } else {
            block();
        }
    }
}
