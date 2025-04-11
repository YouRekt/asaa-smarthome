package org.asaa.agents.sensors;

import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import org.asaa.agents.SensorAgent;
import org.asaa.environment.Area;

import java.util.List;

public class MotionSensorAgent extends SensorAgent {
    private boolean previousState;

    @Override
    protected void setup() {
        super.setup();
        previousState = getHumanPresence();
        addBehaviour(new TickerBehaviour(this, 100) {
            @Override
            protected void onTick() {
                if (getHumanPresence() != previousState) {
                    trigger();
                    previousState = getHumanPresence();
                }
            }
        });
    }

    private boolean getHumanPresence() {
        Area area = getMyArea();
        return (boolean) area.getAttribute("human");
    }

    private String getHumanPresenceString() {
        return String.valueOf(getHumanPresence());
    }

    @Override
    protected void respond(ACLMessage msg) {
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.INFORM);
        reply.setContent(getHumanPresenceString());
        send(reply);
    }

    @Override
    protected void handleTrigger(final List<AID> subscribers) {
        final ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setContent(getHumanPresenceString());
        subscribers.forEach(msg::addReceiver);
        send(msg);
    }
}
