package org.asaa.agents;

import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import org.asaa.environment.Area;
import org.asaa.exceptions.InvalidServiceSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PhysicalAgent extends SpringAwareAgent {
    protected String areaName;
    protected Logger logger;

    @Override
    protected void setup() {
        super.setup();
        logger = LoggerFactory.getLogger(getLocalName());
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            this.areaName = (String) args[0];
        } else {
            this.areaName = "default-area";
        }

        logger.info("Initialized in area: {}", areaName);

        registerBaseAgent();
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

    protected Area getArea() {
        if (environmentService == null) {
            logger.error("Environment service not set");
            return null;
        }
        return environmentService.getArea(areaName);
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
