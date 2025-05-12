package org.asaa.util;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.DynamicConverter;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import org.asaa.services.EnvironmentService;

public class Util {
    public static int AWAIT_ENABLE_BLOCK_TIME = 1000;

    public static void SendMessage(Agent agent, String content, AID receiver, int performative, String conversationId) {
        ACLMessage msg = new ACLMessage(performative);
        msg.addReceiver(receiver);
        msg.setContent(content);
        msg.setConversationId(conversationId);
        agent.send(msg);
    }
}
