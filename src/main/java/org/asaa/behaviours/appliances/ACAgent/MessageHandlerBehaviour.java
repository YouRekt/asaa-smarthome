package org.asaa.behaviours.appliances.ACAgent;

import jade.lang.acl.ACLMessage;
import org.asaa.agents.appliances.ACAgent;
import org.asaa.tasks.appliances.ACAgent.CoolingTask;

public class MessageHandlerBehaviour extends org.asaa.behaviours.appliances.MessageHandlerBehaviour {
    private final ACAgent agent;

    public MessageHandlerBehaviour(ACAgent agent) {
        super(agent);
        this.agent = agent;
    }

    @Override
    protected void handleInform(ACLMessage msg) {
        switch (msg.getConversationId()) {
            case "cooling-task":
                if (agent.getCurrentTask() == null) {
                    agent.requestStartTask(new CoolingTask(agent, agent.getCoolingRate(), agent.getTargetTemperature()));
                } else {
                    agent.getLogger().warn("cooling-task@inform: Cooling Task already running");
                    agent.agentCommunicationController.sendError(agent.getName(), "cooling-task@inform: Cooling Task already running");
                }
                break;
            case "def-reply":
                agent.setCurrentTemperature(Double.parseDouble(msg.getContent()));
                if (agent.getCurrentTemperature() > agent.getTargetTemperature() && agent.getCurrentTask() == null) {
                    agent.requestStartTask(new CoolingTask(agent, agent.getCoolingRate(), agent.getTargetTemperature()));
                } else if (agent.getCurrentTask() != null) {
                    agent.getCurrentTask().wake();
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
            case "interrupt-task":
                agent.removeBehaviour(agent.getBehaviours().get("ModeAutoBehaviour"));
                super.handleRequest(msg);
                break;
            default:
                super.handleRequest(msg);
                break;
        }
    }
}
