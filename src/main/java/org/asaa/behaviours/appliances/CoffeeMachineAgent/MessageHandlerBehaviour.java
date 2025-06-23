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
                if (agent.getCurrentTaskBehaviour() == null || agent.getCurrentTaskBehaviour().done()) {
                    MakeCoffeeTask task = new MakeCoffeeTask(agent, 10000);
                    agent.getTaskBehaviourQueue().add(task);
                    agent.getLogger().info("Make Coffee Task added to queue");
                } else {
                    agent.getLogger().warn("Make Coffee Task already running");
                    agent.agentCommunicationController.sendError(agent.getLocalName(), "Make Coffee Task already running", false);
                }
                break;
            default:
                super.handleRequest(msg);
                break;
        }
    }
}
