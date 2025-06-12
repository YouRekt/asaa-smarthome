package org.asaa.behaviours.appliances;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import org.asaa.agents.SmartApplianceAgent;

public class RequestPowerBehaviour extends OneShotBehaviour {
    private final SmartApplianceAgent agent;
    private final int amount;
    private final int priority;
    private final String convId;
    private final String replyWith;

    public RequestPowerBehaviour(SmartApplianceAgent agent, int amount, int priority, String convId, String replyWith) {
        super(agent);
        this.agent = agent;
        this.amount = amount;
        this.priority = priority;
        this.convId = convId;
        this.replyWith = replyWith;
    }

    @Override
    public void action() {
        ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
        cfp.addReceiver(agent.getCoordinatorAgent());
        cfp.setConversationId(convId);
        cfp.setContent(amount + "," + priority);
        cfp.setReplyWith(replyWith);
        agent.getLogger().info("Sent CFP for {}W, prio={}, convId={}", amount, priority, convId);
        agent.environmentService.addPerformedTask();
        agent.sendMessage(cfp);
    }
}
