package org.asaa.behaviours.coordinator;

import jade.lang.acl.ACLMessage;
import org.asaa.agents.coordinators.CoordinatorAgent;
import org.asaa.behaviours.BaseMessageHandler;
import org.asaa.environment.Environment;

public class HandleMessageBehaviour extends BaseMessageHandler {
    protected final CoordinatorAgent coordinatorAgent;

    public HandleMessageBehaviour(CoordinatorAgent coordinatorAgent) {
        super(coordinatorAgent);

        this.coordinatorAgent = coordinatorAgent;
    }

    @Override
    public void action() {
        final ACLMessage msg = coordinatorAgent.receive();

        if (msg != null) {
            // Here we can add a specialized switch if needed (default -> processMsg(msg);)
            switch (msg.getConversationId()) {
                case "routine-morning" -> coordinatorAgent.performMorningRoutine();
                default -> super.processMsg(msg);
            }
        } else {
            block();
        }
    }

    @Override
    protected void handleCfp(ACLMessage msg) {
        var availablePower = Environment.getInstance().getPowerAvailability();
        int requiredPower;
        switch (msg.getConversationId()) {
            case "enable-passive":
                requiredPower = Integer.parseInt(msg.getContent());
                if (availablePower >= requiredPower) {
                    Environment.getInstance().modifyPowerConsumption(requiredPower);
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.AGREE);
                    reply.setContent("Enable passive approved - " + requiredPower + "W");
                    coordinatorAgent.send(reply);
                } else {
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("Enable active refused - " + requiredPower + "W");
                    coordinatorAgent.send(reply);
                }
                break;
            case "enable-active":
                requiredPower = Integer.parseInt(msg.getContent());
                if (availablePower >= requiredPower) {
                    Environment.getInstance().modifyPowerConsumption(requiredPower);
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.AGREE);
                    reply.setContent("Enable active approved - " + requiredPower + "W");
                    coordinatorAgent.send(reply);
                } else {
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("Enable active refused - " + requiredPower + "W");
                    coordinatorAgent.send(reply);
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void handleInform(ACLMessage msg) {
        switch (msg.getConversationId()) {
            case "disable-active":
                var returnedPower = Integer.parseInt(msg.getContent());
                Environment.getInstance().modifyPowerConsumption(-returnedPower);
                break;
            default:
                break;
        }
    }
}
