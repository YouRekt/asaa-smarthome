package org.asaa.agents.sensors;

import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import org.asaa.agents.SensorAgent;

public final class MotionSensorAgent extends SensorAgent {
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
        return (boolean) getArea().getAttribute("human");
    }

    private String getHumanPresenceString() {
        return String.valueOf(getHumanPresence());
    }

    @Override
    protected String responseMsgContent() {
        return getHumanPresenceString();
    }

    @Override
    protected void handleTrigger() {
        final ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setContent(getHumanPresenceString());
        subscribers.forEach(msg::addReceiver);
        send(msg);
    }
}
