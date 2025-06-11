package org.asaa.agents;

import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import lombok.Getter;
import lombok.Setter;
import org.asaa.environment.Area;
import org.asaa.exceptions.InvalidServiceSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Arrays;

public abstract class PhysicalAgent extends SpringAwareAgent {
    @Getter
    protected Logger logger;
    @Getter
    protected AID coordinatorAgent;
    /* Priority sheet:
    0   <= p < 100 - awaits callback upon being turned off while working
    100 <= p < 200 - default priority sorting (lower - lower priority - turns off first)
    200 <= p < 300 - isEnabled but not working, lowest prio turn off (low energy save anyway)
     */
    @Getter
    @Setter
    protected int priority = 0;
    @Getter
    protected String areaName;

    @Override
    protected void setup() {
        super.setup();

        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            this.areaName = (String) args[0];
        } else {
            this.areaName = "default-area";
        }

        MDC.put("agent", this.getLocalName());
        MDC.put("area", areaName);

        logger = LoggerFactory.getLogger(getLocalName());
        logger.info("Initialized in area: {}", areaName);

        registerBaseAgent();
        findCoordinatorAgent();
    }

    @Override
    protected void takeDown() {
        MDC.clear();
        super.takeDown();
    }

    private void registerBaseAgent() {
        if (getLocalName().equals("Human"))
            return;
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
            if (!coordinatorAgent.getLocalName().equals("Coordinator")) {
                logger.error("Coordinator agent was not of the expected type!!! Found: {}", coordinatorAgent.getLocalName());
                agentCommunicationController.sendError(getName(), "Fatal: Coordinator agent was not of expected type");
            }
            logger.info("Found coordinator agent");
        } catch (FIPAException e) {
            throw new InvalidServiceSpecification(e);
        }
    }

    protected Area getArea() {
        return environmentService.getArea(areaName);
    }

    public void trigger() {
        handleTrigger();
    }

    protected abstract void handleTrigger();

    protected void respond(ACLMessage msg) {
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.INFORM);
        reply.setContent(responseDefaultMsgContent());
        reply.setConversationId("def-reply");
        sendMessage(reply);
    }

    protected abstract String responseDefaultMsgContent();
}
