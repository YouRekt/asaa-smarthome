package org.asaa.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asaa.behaviours.sensor.HandleMessageBehaviour;
import org.asaa.environment.Area;
import org.asaa.environment.Environment;

import java.util.ArrayList;
import java.util.List;

public abstract class SensorAgent extends Agent {
    protected String areaName;
    @Getter
    protected List<AID> subscribers = new ArrayList<AID>();
    protected Logger logger;

    @Override
    protected void setup() {
        logger = LogManager.getLogger(getLocalName());
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            this.areaName = (String) args[0];
        } else {
            this.areaName = "default-area";
        }

        logger.info("initialized in area {}", areaName);

        addBehaviour(new HandleMessageBehaviour(this) {
            @Override
            protected void handleRequest(ACLMessage msg) {
                logger.info("Responding to {}'s request", msg.getSender().getLocalName());
                respond(msg);
            }
        });
    }

    protected Area getMyArea() {
        Environment env = Environment.getInstance();
        return env.getArea(areaName);
    }

    public void trigger() {
        logger.info("sending trigger information to all subscribers");
        handleTrigger(subscribers);
    }

    protected abstract void handleTrigger(final List<AID> subscribers);

    protected abstract void respond(ACLMessage msg);
}
