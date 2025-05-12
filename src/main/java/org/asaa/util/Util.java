package org.asaa.util;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import org.asaa.agents.SpringAwareAgent;

public class Util {
    public static void SendMessage(SpringAwareAgent agent, String content, AID receiver, int performative, String conversationId) {
        ACLMessage msg = new ACLMessage(performative);
        msg.addReceiver(receiver);
        msg.setContent(content);
        msg.setConversationId(conversationId);
        agent.sendMessage(msg, false);
    }
}
