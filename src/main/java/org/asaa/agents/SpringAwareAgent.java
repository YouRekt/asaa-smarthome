package org.asaa.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import org.asaa.services.EnvironmentService;
import org.asaa.controllers.AgentCommunicationController;
import org.asaa.controllers.AgentPresenceController;
import org.asaa.util.SpringContext;
import org.asaa.util.Util;
import org.springframework.context.ApplicationContext;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public abstract class SpringAwareAgent extends Agent {
    public EnvironmentService environmentService;
    public AgentCommunicationController agentCommunicationController;
    public AgentPresenceController agentPresenceController;

    @Override
    protected void setup() {
        super.setup();
        ApplicationContext context = SpringContext.get();
        if (context != null) {
            environmentService = context.getBean(EnvironmentService.class);
            agentCommunicationController = context.getBean(AgentCommunicationController.class);
            agentPresenceController = context.getBean(AgentPresenceController.class);
//            System.out.println("Environment Service: " + environmentService);
        } else {
            System.err.println("Spring ApplicationContext is null!");
        }
    }

    public final void sendMessage(ACLMessage msg) {
        if (msg.getConversationId() == null)
            msg.setConversationId("NO_CONV_ID");
        if (msg.getContent() == null)
            msg.setContent("NO_CONTENT");
        send(msg);
        agentCommunicationController.sendMessage(getName(), String.format("[%s] -> [%s <%s>] -> [%s]%s",
                getLocalName(),
                Util.ConvertACLPerformativeToString(msg.getPerformative()),
                msg.getConversationId(),
                StreamSupport.stream(Spliterators.spliteratorUnknownSize(msg.getAllReceiver(), Spliterator.ORDERED), false).map(aid -> ((AID) aid).getLocalName()).collect(Collectors.joining(", ")),
                msg.getContent() == null ? "" : String.format(": %s", msg.getContent())));
    }
}
