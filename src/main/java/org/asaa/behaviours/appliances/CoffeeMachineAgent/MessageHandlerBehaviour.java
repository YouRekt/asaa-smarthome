package org.asaa.behaviours.appliances.CoffeeMachineAgent;

import jade.lang.acl.ACLMessage;
import org.asaa.agents.appliances.CoffeeMachineAgent;
import org.asaa.behaviours.appliances.RequestPowerBehaviour;

public class MessageHandlerBehaviour extends org.asaa.behaviours.appliances.MessageHandlerBehaviour {
    private final CoffeeMachineAgent agent;

    public MessageHandlerBehaviour(CoffeeMachineAgent agent) {
        super(agent);
        this.agent = agent;
    }

    @Override
    protected void handleRequest(ACLMessage msg) {
        switch (msg.getConversationId()) {
            case "action-morning":
                if (!agent.isWorking()) {
                    String replyWith = "req-" + System.currentTimeMillis();
                    agent.onPowerGrantedCallbacks.put(replyWith, agent::makeCoffee);
                    agent.addBehaviour(new RequestPowerBehaviour(agent, agent.getActiveDraw(), agent.getPriority(), "enable-active", replyWith));
                } else {
                    agent.getLogger().warn("Currently making coffee, can not respond to request");
                    agent.agentCommunicationController.sendError(agent.getName(), "Currently making coffee, can not respond to request");
                }
                break;
            default:
                super.handleRequest(msg);
                break;
        }
    }
}
