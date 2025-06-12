package org.asaa.behaviours.appliances.CoffeeMachineAgent;

import jade.lang.acl.ACLMessage;
import org.asaa.agents.appliances.CoffeeMachineAgent;
import org.asaa.tasks.appliances.CoffeeMachineAgent.MakeCoffeeTask;

public class MessageHandlerBehaviour extends org.asaa.behaviours.appliances.MessageHandlerBehaviour {
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
                if (agent.getCurrentTask() == null) {
                    agent.requestStartTask(new MakeCoffeeTask(agent));
                } else {
                    agent.getLogger().warn("{}@request: Make Coffee Task already running", msg.getConversationId());
                    agent.agentCommunicationController.sendError(agent.getName(), msg.getConversationId() + "@request: Make Coffee Task already running");
                }
                break;
            default:
                super.handleRequest(msg);
                break;
        }
    }
}
