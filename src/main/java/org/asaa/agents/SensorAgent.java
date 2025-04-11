package org.asaa.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asaa.behaviours.sensor.HandleMessageBehaviour;
import org.asaa.environment.Area;
import org.asaa.environment.Environment;
import org.asaa.exceptions.InvalidServiceSpecification;

import java.util.ArrayList;
import java.util.List;

//TODO: Add failure handling
public abstract class SensorAgent extends Agent {
    protected String areaName;
    @Getter
    protected List<AID> subscribers = new ArrayList<>();
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

        logger.info("Sensor initialized in area {}", areaName);

        registerSensor();

        addBehaviour(new HandleMessageBehaviour(this) {
            @Override
            protected void handleInform(ACLMessage msg) {
                trigger();
            }

            @Override
            protected void handleRequest(ACLMessage msg) {
                logger.info("Responding to {}'s request", msg.getSender().getLocalName());
                respond(msg);
            }
        });
    }

    private void registerSensor() {
        final ServiceDescription sd = new ServiceDescription();
        sd.setType("sensor");
        sd.setName(getLocalName());
        sd.setOwnership(getLocalName());

        try {
            final DFAgentDescription dfd = new DFAgentDescription();
            dfd.addServices(sd);
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            throw new InvalidServiceSpecification(e);
        }
    }

    protected Area getMyArea() {
        Environment env = Environment.getInstance();
        return env.getArea(areaName);
    }

    public void trigger() {
        logger.info("I have been triggered! Sending trigger sensor information to all subscribers");
        handleTrigger(subscribers);
    }

    protected abstract void handleTrigger(final List<AID> subscribers);

    protected abstract void respond(ACLMessage msg);
}
