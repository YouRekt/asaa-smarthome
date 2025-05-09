package org.asaa.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.asaa.environment.Area;
import org.asaa.environment.Environment;
import org.asaa.exceptions.InvalidServiceSpecification;

import java.util.Arrays;

public abstract class PhysicalAgent extends Agent {
    protected String areaName;
    protected Logger logger;
    public AID coordinatorAgent;

    @Override
    protected void setup() {
        logger = LogManager.getLogger(getLocalName());
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            this.areaName = (String) args[0];
        } else {
            this.areaName = "default-area";
        }
        ThreadContext.put("area", areaName);

        logger.info("Initialized in area: {}", areaName);

        registerBaseAgent();
        findCoordinatorAgent();
    }

    private void registerBaseAgent() {
        final Property p = new Property();
        p.setName("areaName");
        p.setValue(areaName);

        final ServiceDescription sd = new ServiceDescription();
        sd.setType(getClass().getSimpleName());
        sd.setName(getLocalName());
        sd.setOwnership(getName());
        sd.addProperties(p);

        try {
            final DFAgentDescription dfd = new DFAgentDescription();
            dfd.addServices(sd);
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            throw new InvalidServiceSpecification(e);
        }
    }

    private void findCoordinatorAgent() {
        final ServiceDescription sd = new ServiceDescription();
        sd.setType("CoordinatorAgent");

        try {
            final DFAgentDescription dfd = new DFAgentDescription();
            dfd.addServices(sd);
            coordinatorAgent = Arrays.stream(DFService.search(this, dfd)).map(DFAgentDescription::getName).toList().getFirst();
            if (!coordinatorAgent.getLocalName().equals("Coordinator"))
                logger.warn("Coordinator agent was not of the expected type!!! Found: {}", coordinatorAgent.getLocalName());
            logger.info("Found coordinator agent");
        } catch (FIPAException e) {
            throw new InvalidServiceSpecification(e);
        }
    }

    protected Area getArea() {
        return Environment.getInstance().getArea(areaName);
    }

    public void trigger() {
        logger.info("I have been triggered!");
        handleTrigger();
    }

    protected abstract void handleTrigger();

    protected void respond(ACLMessage msg) {
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.INFORM);
        reply.setContent(responseMsgContent());
        send(reply);
    }

    protected abstract String responseMsgContent();
}
