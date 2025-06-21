package org.asaa.behaviours.appliances.CoffeeMachineAgent;

import jade.lang.acl.ACLMessage;
import org.asaa.agents.appliances.CoffeeMachineAgent;

public class MessageHandlerBehaviour extends org.asaa.behaviours.appliances.base.MessageHandlerBehaviour {
    private final CoffeeMachineAgent agent;

    public MessageHandlerBehaviour(CoffeeMachineAgent agent) {
        super(agent);
        this.agent = agent;
    }

    @Override
    protected void handleRequest(ACLMessage msg) {
        switch (msg.getConversationId()) {
            case "make-coffee-task":
            case "action-morning":
                // TODO: Implement using new system
//                if (agent.getCurrentTask() == null) {
//                    new MakeCoffeeTask(agent).start();
//                } else {
//                    agent.getLogger().warn("{}@request: Make Coffee Task already running", msg.getConversationId());
//                    agent.agentCommunicationController.sendError(agent.getLocalName(), msg.getConversationId() + "@request: Make Coffee Task already running");
//                }
                break;
            default:
                super.handleRequest(msg);
                break;
        }
    }
}
