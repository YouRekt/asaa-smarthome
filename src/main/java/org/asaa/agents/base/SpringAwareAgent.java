package org.asaa.agents.base;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import lombok.Getter;
import org.asaa.exceptions.InvalidServiceSpecification;
import org.asaa.services.EnvironmentService;
import org.asaa.controllers.AgentCommunicationController;
import org.asaa.controllers.AgentPresenceController;
import org.asaa.util.SpringContext;
import org.asaa.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public abstract class SpringAwareAgent extends Agent {
    public EnvironmentService environmentService;
    public AgentCommunicationController agentCommunicationController;
    public AgentPresenceController agentPresenceController;
    @Getter
    protected Logger logger;

    @Override
    protected void setup() {
        super.setup();

        logger = LoggerFactory.getLogger(getLocalName());

        ApplicationContext context = SpringContext.get();
        if (context != null) {
            environmentService = context.getBean(EnvironmentService.class);
            agentCommunicationController = context.getBean(AgentCommunicationController.class);
            agentPresenceController = context.getBean(AgentPresenceController.class);
        } else {
            System.err.println("Spring ApplicationContext is null!");
        }
    }

    public final void sendMessage(ACLMessage msg) {
        if (msg.getConversationId() == null)
            msg.setConversationId("");
        if (msg.getContent() == null)
            msg.setContent("");
        send(msg);
        agentCommunicationController.sendMessage(
                msg.getSender().getLocalName(),
                StreamSupport.stream(Spliterators.spliteratorUnknownSize(new Util.IteratorAdapter<AID>(msg.getAllReceiver(), AID.class), Spliterator.ORDERED), false).map(AID::getLocalName).collect(Collectors.toList()),
                Util.ConvertACLPerformativeToString(msg.getPerformative()),
                msg.getConversationId(),
                msg.getContent(),
                true
                );
    }

    public final void register(String areaName) {
        if (getLocalName().equals("Human"))
            return;

        final ServiceDescription sd = new ServiceDescription();
        sd.setType(getClass().getSimpleName());
        sd.setName(getLocalName());
        sd.setOwnership(getName());

        if (!areaName.isEmpty()) {
            final Property property = new Property();
            property.setName("areaName");
            property.setValue(areaName);
            sd.addProperties(property);
        }

        try {
            final DFAgentDescription dfd = new DFAgentDescription();
            dfd.addServices(sd);
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            throw new InvalidServiceSpecification(e);
        }
    }

    protected List<AID> findAgents(String agentName, String areaName, boolean byType) {
        final ServiceDescription sd = new ServiceDescription();
        if (byType) {
            sd.setType(agentName);
        } else {
            sd.setName(agentName);
        }

        if (!areaName.isEmpty()) {
            final Property property = new Property();
            property.setName("areaName");
            property.setValue(areaName);

            sd.addProperties(property);
        }

        try {
            final DFAgentDescription dfd = new DFAgentDescription();
            dfd.addServices(sd);

            return Arrays.stream(DFService.search(this, dfd)).map(DFAgentDescription::getName).toList();
        } catch (FIPAException e) {
            throw new InvalidServiceSpecification(e);
        }
    }

    protected AID findAgent(String agentName, String areaName, boolean byType) {
        AID chosenAgent = (findAgents(agentName, areaName, byType) == null) ? null : findAgents(agentName, areaName, byType).getFirst();
        if (chosenAgent == null) {
            logger.warn("No agent {} found", byType ? "of type " + agentName : agentName);
            agentCommunicationController.sendError(getName(), "No" + agentName + "found");
            return null;
        }
        if (!byType && !chosenAgent.getLocalName().equals(agentName)) {
            logger.error("Agent was not of the expected type!!! Found: {}, should be {}", chosenAgent.getLocalName(), agentName);
            agentCommunicationController.sendError(getName(), "Fatal: Agent was not of the expected type");
            return null;
        }
        logger.info("Found {} agent", agentName);
        return chosenAgent;
    }
}
