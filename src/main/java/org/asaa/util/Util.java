package org.asaa.util;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

public class Util {
    public static int AWAIT_ENABLE_BLOCK_TIME = 1000;
    public static void SendMessage(Agent agent, String content, AID receiver, int performative, String conversationId)
    {
        ACLMessage msg = new ACLMessage(performative);
        msg.addReceiver(receiver);
        msg.setContent(content);
        msg.setConversationId(conversationId);
        agent.send(msg);
    }
}
