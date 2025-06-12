package org.asaa.behaviours.appliances;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import org.asaa.agents.SmartApplianceAgent;

public class RelinquishPowerBehaviour extends OneShotBehaviour {
    private final SmartApplianceAgent agent;
    private final int amount;
    private final String convId;

    public RelinquishPowerBehaviour(SmartApplianceAgent agent, int amount, String convId) {
        super(agent);
        this.agent = agent;
        this.amount = amount;
        this.convId = convId;
    }

    @Override
    public void action() {
        ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
        inform.addReceiver(agent.getCoordinatorAgent());
        inform.setConversationId(convId);
        inform.setContent(Integer.toString(amount));
        agent.getLogger().info("Sent INFORM for {}W, convId={}", amount, convId);
        agent.environmentService.addPerformedTask();
        agent.sendMessage(inform);
        if (convId.equals("disable-active") || convId.equals("disable-active-cfp"))
            agent.setWorking(false);
        else if (convId.equals("disable-passive") || convId.equals("disable-passive-cfp"))
            agent.setEnabled(false);
        else
            agent.getLogger().warn("Invalid convId {}", convId);
    }
}
