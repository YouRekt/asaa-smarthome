package org.asaa.behaviours.appliances.DishwasherAgent;

import jade.lang.acl.ACLMessage;
import org.asaa.agents.appliances.DishwasherAgent;
import org.asaa.tasks.appliances.DishwasherAgent.WashDishesTask;

public class MessageHandlerBehaviour extends org.asaa.behaviours.appliances.base.MessageHandlerBehaviour {
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
//                if (agent.getCurrentTask() != null) {
//                    agent.getCurrentTask().resume();
//                }
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
                // TODO: Implement using new system
//                if (agent.getCurrentTask() == null) {
//                    new WashDishesTask(agent, agent.getUpdateDelay(), agent.getNoninterruptibleStartPercent(), agent.getNoninterruptibleEndPercent(), agent.getFullWashTime()).start();
//                }
                if (agent.getCurrentTaskBehaviour() == null || agent.getCurrentTaskBehaviour().done()) {
                    WashDishesTask task = new WashDishesTask(agent, 30000, 0.7,  0.3);
                    agent.getTaskBehaviourQueue().add(task);
                    agent.getLogger().info("Wash dishes task added to queue");
                } else {
                    agent.getLogger().warn("Wash dishes task is already running");
                    agent.agentCommunicationController.sendError(agent.getLocalName(), "Wash dishes task is already running", false);
                }
                break;
            default:
                super.handleRequest(msg);
                break;
        }
    }
}
