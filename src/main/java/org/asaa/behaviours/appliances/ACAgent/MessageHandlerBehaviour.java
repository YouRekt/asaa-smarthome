package org.asaa.behaviours.appliances.ACAgent;

import jade.lang.acl.ACLMessage;
import org.asaa.agents.appliances.ACAgent;
import org.asaa.behaviours.appliances.TaskBehaviour;
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
                    agent.getLogger().info("Cooling task added to queue");
                } else {
                    agent.getLogger().warn("Cooling task already running");
                    agent.agentCommunicationController.sendError(agent.getLocalName(), "Cooling task already running", false);
                }
                break;
            }

            case "def-reply": {
                double updatedTemp = Double.parseDouble(msg.getContent());
                agent.setCurrentTemperature(updatedTemp);

                agent.getLogger().info("Received updated temperature: {}°C", updatedTemp);

                // Check if a cooling task should be started
                if (updatedTemp > agent.getTargetTemperature()) {
                    TaskBehaviour<?> current = agent.getCurrentTaskBehaviour();

                    if (current == null || current.done()) {
                        CoolingTask task = new CoolingTask(agent, agent.getCoolingRate(), agent.getTargetTemperature());
                        agent.getTaskBehaviourQueue().add(task);
                        agent.getLogger().info("CoolingTask added to queue due to high temperature.");
                    } else if (current instanceof CoolingTask) {
                        agent.getLogger().debug("CoolingTask is already running.");
                        // Optionally trigger a state update if needed
                    }
                }

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
