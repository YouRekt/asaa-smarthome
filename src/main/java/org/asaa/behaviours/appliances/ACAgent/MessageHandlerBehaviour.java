package org.asaa.behaviours.appliances.ACAgent;

import jade.lang.acl.ACLMessage;
import org.asaa.agents.appliances.ACAgent;
import org.asaa.tasks.appliances.ACAgent.CoolingTask;

public class MessageHandlerBehaviour extends org.asaa.behaviours.appliances.base.MessageHandlerBehaviour {
    private final ACAgent agent;

    public MessageHandlerBehaviour(ACAgent agent) {
        super(agent);
        this.agent = agent;
    }

    @Override
    protected void handleInform(ACLMessage msg) {
        switch (msg.getConversationId()) {

            case "cooling-task": {
                if (agent.getCurrentTaskBehaviour() == null || agent.getCurrentTaskBehaviour().done()) {
                    CoolingTask task = new CoolingTask(agent, agent.getCoolingRate(), agent.getTargetTemperature());
                    agent.getTaskBehaviourQueue().add(task);
                    agent.getLogger().info("Cooling task added to queue.");
                } else {
                    agent.getLogger().warn("Cooling task already running.");
                    agent.agentCommunicationController.sendError(agent.getLocalName(), "Cooling task already running.");
                }
                break;
            }

            case "def-reply": {
                double updatedTemp = Double.parseDouble(msg.getContent());
                agent.setCurrentTemperature(updatedTemp);
                break;
            }

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
