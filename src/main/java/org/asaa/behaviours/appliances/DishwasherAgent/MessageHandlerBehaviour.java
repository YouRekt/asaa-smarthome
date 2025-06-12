package org.asaa.behaviours.appliances.DishwasherAgent;

import jade.lang.acl.ACLMessage;
import org.asaa.agents.appliances.DishwasherAgent;
import org.asaa.tasks.appliances.DishwasherAgent.WashDishesTask;

public class MessageHandlerBehaviour extends org.asaa.behaviours.appliances.MessageHandlerBehaviour {
    private final DishwasherAgent agent;

    public MessageHandlerBehaviour(DishwasherAgent agent) {
        super(agent);
        this.agent = agent;
    }

    @Override
    protected void handleInform(ACLMessage msg) {
        switch (msg.getConversationId()) {
            case "enable-callback":
                agent.getLogger().info("Received enable-callback message");
                if (agent.getCurrentTask() != null) {
                    agent.getCurrentTask().resume();
                }
                break;
            default:
                super.handleInform(msg);
                break;
        }
    }

    @Override
    protected void handleRequest(ACLMessage msg) {
        switch (msg.getConversationId()) {
            case "wash-dishes-task":
                if (agent.getCurrentTask() == null) {
                    agent.requestStartTask(new WashDishesTask(agent, agent.getUpdateDelay(), agent.getNoninterruptibleStartPercent(), agent.getNoninterruptibleEndPercent(), agent.getFullWashTime()));
                }
                break;
            default:
                super.handleRequest(msg);
                break;
        }
    }

    @Override
    protected void handleAcceptProposal(ACLMessage msg) {
        switch (msg.getConversationId()) {
            case "power-relief":
                if (agent.getCurrentTask() != null) {
                    agent.getCurrentTask().pause();
                    super.handleAcceptProposal(msg);
                } else {
                    super.handleAcceptProposal(msg);
                }
                break;
            default:
                super.handleAcceptProposal(msg);
                break;
        }
    }
}
