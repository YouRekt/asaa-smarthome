package org.asaa.behaviours.coordinator;

import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import org.asaa.agents.coordinators.CoordinatorAgent;
import org.asaa.behaviours.coordinator.HandleMessageBehaviour;

public class AwaitResponsesBehaviour extends TickerBehaviour {
    private final CoordinatorAgent coordinatorAgent;
    private final HandleMessageBehaviour parent;
    private boolean done = false;

    public AwaitResponsesBehaviour(CoordinatorAgent coordinatorAgent, long period, HandleMessageBehaviour parent) {
        super(coordinatorAgent, period);
        this.coordinatorAgent = coordinatorAgent;
        this.parent = parent;
    }

    @Override
    protected void onTick() {
        CoordinatorAgent.getLogger().info("AwaitResponsesBehaviour onTick - received responses: {}, sent: {}", parent.getCfpInformResponses(), parent.getCfpSentProposals());
        boolean timeout = System.currentTimeMillis() - parent.getCfpInformStartTime() > 3000;
        if (parent.getCfpInformResponses() >= parent.getCfpSentProposals()) {
            if (timeout)
                CoordinatorAgent.getLogger().error("AwaitResponsesBehaviour timeout, proceeding with relief anyways");
            int requiredPower = Integer.parseInt(parent.getCfpMessage().getContent().split(",")[0]);
            parent.environmentService.modifyPowerConsumption(requiredPower);
            ACLMessage origReply = parent.getCfpMessage().createReply();
            origReply.setPerformative(ACLMessage.AGREE);
            origReply.setContent("Enable " + (parent.isCfpPassive() ? "passive" : "active") + " approved after relief - " + requiredPower + "W");
            coordinatorAgent.send(origReply);
            done = true;
        }
        if (done)
            coordinatorAgent.removeBehaviour(this);
    }
}
