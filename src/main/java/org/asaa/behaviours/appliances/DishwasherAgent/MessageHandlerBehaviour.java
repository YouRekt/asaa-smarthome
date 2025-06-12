package org.asaa.behaviours.appliances.DishwasherAgent;

import jade.lang.acl.ACLMessage;
import org.asaa.agents.appliances.DishwasherAgent;
import org.asaa.behaviours.appliances.RequestPowerBehaviour;

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
                if (agent.getWashBehaviour() == null && agent.getRemainingWashTime() > 0) {
                    String replyWith = "req-" + System.currentTimeMillis();
                    agent.onPowerGrantedCallbacks.put(replyWith, () -> agent.performWash(true));
                    agent.addBehaviour(new RequestPowerBehaviour(agent, agent.getActiveDraw(), agent.getPriority(), "enable-active", replyWith));
                }
                break;
            default:
                break;
        }
        super.handleInform(msg);
    }

    @Override
    protected void handleRequest(ACLMessage msg) {
        if (!agent.isWorking()) {
            agent.setRemainingWashTime(agent.getFullWashTime());
            String replyWith = "req-" + System.currentTimeMillis();
            agent.onPowerGrantedCallbacks.put(replyWith, () -> agent.performWash(false));
            agent.addBehaviour(new RequestPowerBehaviour(agent, agent.getActiveDraw(), agent.getPriority(), "enable-active", replyWith));
        }
    }

    @Override
    protected void handleAcceptProposal(ACLMessage msg) {
        switch (msg.getConversationId()) {
            case "power-relief":
                if (agent.getWashBehaviour() != null) {
                    long elapsed = System.currentTimeMillis() - agent.getWashStartTime();
                    agent.setRemainingWashTime(Math.max(0, agent.getRemainingWashTime() - elapsed));
                    agent.removeBehaviour(agent.getWashBehaviour());
                    agent.setWashBehaviour(null);
                    agent.getLogger().info("Wash paused, {}ms left", agent.getRemainingWashTime());
                    super.handleAcceptProposal(msg);
                } else {
                    super.handleAcceptProposal(msg);
                }
                break;
            default:
                break;
        }
    }
}
