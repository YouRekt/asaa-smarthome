package org.asaa.behaviours.coordinator;

import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import org.asaa.agents.coordinators.CoordinatorAgent;

import java.util.*;

public class ReliefNegotiationBehaviour extends TickerBehaviour {
    private final CoordinatorAgent coordinatorAgent;
    private final HandleMessageBehaviour parent;
    private boolean done = false;
    public ReliefNegotiationBehaviour(CoordinatorAgent coordinatorAgent, long period, HandleMessageBehaviour parent) {
        super(coordinatorAgent, period);
        this.coordinatorAgent = coordinatorAgent;
        this.parent = parent;
    }

    @Override
    protected void onTick() {
        CoordinatorAgent.getLogger().info("ReliefNegotiationBehaviour onTick - received responses: {}, sent: {}", parent.getCfpResponses(), parent.getCfpSent());
        boolean timeout = System.currentTimeMillis() - parent.getCfpStartTime() > 3000;
        if (timeout || parent.getCfpResponses() >= parent.getCfpSent()) {
            List<Map.Entry<AID, Integer>> sorted = new ArrayList<>(parent.getCfpProposals().entrySet());
            sorted.sort(Comparator.comparing(e -> coordinatorAgent.getPriority(e.getKey())));
            int gathered = 0;
            Set<AID> accepted = new HashSet<>();
            for (var e : sorted) {
                if (gathered >= parent.getCfpShortage())
                    break;
                gathered += e.getValue();
                accepted.add(e.getKey());
            }

            ACLMessage origReply = parent.getCfpMessage().createReply();
            int requiredPower = Integer.parseInt(parent.getCfpMessage().getContent().split(",")[0]);
            if (gathered < parent.getCfpShortage()) {
                origReply.setPerformative(ACLMessage.REFUSE);
                origReply.setContent("Enable " + (parent.isCfpPassive() ? "passive" : "active") + " refused even after proposed relief - " + requiredPower + "W");
                coordinatorAgent.send(origReply);
            } else {
                for (var e : parent.getCfpProposals().entrySet()) {
                    ACLMessage reply = new ACLMessage(accepted.contains(e.getKey()) ? ACLMessage.ACCEPT_PROPOSAL : ACLMessage.REJECT_PROPOSAL);
                    reply.addReceiver(e.getKey());
                    reply.setConversationId("power-relief");
                    reply.setContent(Integer.toString(e.getValue()));
                    coordinatorAgent.send(reply);
                    parent.setCfpSentProposals(parent.getCfpSentProposals() + (accepted.contains(e.getKey()) ? 1 : 0));
                }
                parent.setCfpInformStartTime(System.currentTimeMillis());
                coordinatorAgent.addBehaviour(new AwaitResponsesBehaviour(coordinatorAgent, 250, parent));
            }
            done = true;
        }
        if (done)
            coordinatorAgent.removeBehaviour(this);
    }
}
