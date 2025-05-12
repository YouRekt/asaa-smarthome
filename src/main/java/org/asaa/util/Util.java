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
        agent.sendMessage(msg);
    }

    public static String ConvertACLPerformativeToString(int performative) {
        return switch (performative) {
            case ACLMessage.ACCEPT_PROPOSAL -> "ACCEPT_PROPOSAL";
            case ACLMessage.AGREE -> "AGREE";
            case ACLMessage.CANCEL -> "CANCEL";
            case ACLMessage.CFP -> "CFP";
            case ACLMessage.CONFIRM -> "CONFIRM";
            case ACLMessage.DISCONFIRM -> "DISCONFIRM";
            case ACLMessage.FAILURE -> "FAILURE";
            case ACLMessage.INFORM -> "INFORM";
            case ACLMessage.INFORM_IF -> "INFORM_IF";
            case ACLMessage.INFORM_REF -> "INFORM_REF";
            case ACLMessage.NOT_UNDERSTOOD -> "NOT_UNDERSTOOD";
            case ACLMessage.PROPOSE -> "PROPOSE";
            case ACLMessage.QUERY_IF -> "QUERY_IF";
            case ACLMessage.QUERY_REF -> "QUERY_REF";
            case ACLMessage.REFUSE -> "REFUSE";
            case ACLMessage.REJECT_PROPOSAL -> "REJECT_PROPOSAL";
            case ACLMessage.REQUEST -> "REQUEST";
            case ACLMessage.REQUEST_WHEN -> "REQUEST_WHEN";
            case ACLMessage.REQUEST_WHENEVER -> "REQUEST_WHENEVER";
            case ACLMessage.SUBSCRIBE -> "SUBSCRIBE";
            case ACLMessage.PROXY -> "PROXY";
            case ACLMessage.PROPAGATE -> "PROPAGATE";
            case ACLMessage.UNKNOWN -> "UNKNOWN";
            default -> "ERR_UNK_PERF";
        };
    }
}
