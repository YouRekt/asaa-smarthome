package org.asaa.behaviours.appliances.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import org.asaa.agents.base.SmartApplianceAgent;
import org.asaa.behaviours.appliances.tasks.PowerRequest;

public class RequestPowerBehaviour extends OneShotBehaviour {
    private final PowerRequest powerRequest;
    private final SmartApplianceAgent agent;
    private final String convId;

    public RequestPowerBehaviour(SmartApplianceAgent agent, String convId, PowerRequest powerRequest) {
        super(agent);
        this.agent = agent;
        this.convId = convId;
        this.powerRequest = powerRequest;
    }

    @Override
    public void action() {
        ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
        req.addReceiver(agent.getCoordinatorAgent());
        ObjectMapper mapper = new ObjectMapper();
        try {
            req.setContent(mapper.writeValueAsString(powerRequest));
        } catch (JsonProcessingException e) {
            agent.getLogger().error("{}@action: JsonProcessingException {}", this.getClass().getSimpleName(), e.getMessage());
        }
        req.setConversationId(convId);
        agent.getLogger().info("Sent CFP for {}: {}W, prio={}, convId={}", powerRequest.getTaskInfo() == null ? "enable-passive" : powerRequest.getTaskInfo().getTaskId(), powerRequest.getPowerAmount(), powerRequest.getTaskInfo() == null ? "0" : powerRequest.getTaskInfo().getPriority(), convId);
        agent.sendMessage(req);
    }
}
