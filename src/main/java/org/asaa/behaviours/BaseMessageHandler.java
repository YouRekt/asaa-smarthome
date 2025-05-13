package org.asaa.behaviours;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import org.asaa.agents.SpringAwareAgent;
import org.asaa.util.Util;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public abstract class BaseMessageHandler extends CyclicBehaviour {

    public BaseMessageHandler(Agent agent) {
        super(agent);
    }

    public void processMsg(ACLMessage msg) {
        ((SpringAwareAgent)myAgent).agentCommunicationController.sendMessage(myAgent.getName(), String.format("[In] [%s] -> [%s <%s>] -> [%s]%s",
                msg.getSender().getLocalName(),
                Util.ConvertACLPerformativeToString(msg.getPerformative()),
                msg.getConversationId(),
                StreamSupport.stream(Spliterators.spliteratorUnknownSize(msg.getAllReceiver(), Spliterator.ORDERED), false).map(aid -> ((AID) aid).getLocalName()).collect(Collectors.joining(", ")),
                msg.getContent() == null ? "" : String.format(": %s", msg.getContent())));

        switch (msg.getPerformative()) {
            case ACLMessage.ACCEPT_PROPOSAL -> handleAcceptProposal(msg);
            case ACLMessage.AGREE -> handleAgree(msg);
            case ACLMessage.CANCEL -> handleCancel(msg);
            case ACLMessage.CFP -> handleCfp(msg);
            case ACLMessage.CONFIRM -> handleConfirm(msg);
            case ACLMessage.DISCONFIRM -> handleDisconfirm(msg);
            case ACLMessage.FAILURE -> handleFailure(msg);
            case ACLMessage.INFORM -> handleInform(msg);
            case ACLMessage.INFORM_IF -> handleInformIf(msg);
            case ACLMessage.INFORM_REF -> handleInformRef(msg);
            case ACLMessage.NOT_UNDERSTOOD -> handleNotUnderstood(msg);
            case ACLMessage.PROPOSE -> handlePropose(msg);
            case ACLMessage.QUERY_IF -> handleQueryIf(msg);
            case ACLMessage.QUERY_REF -> handleQueryRef(msg);
            case ACLMessage.REFUSE -> handleRefuse(msg);
            case ACLMessage.REJECT_PROPOSAL -> handleRejectProposal(msg);
            case ACLMessage.REQUEST -> handleRequest(msg);
            case ACLMessage.REQUEST_WHEN -> handleRequestWhen(msg);
            case ACLMessage.REQUEST_WHENEVER -> handleRequestWhenever(msg);
            case ACLMessage.SUBSCRIBE -> handleSubscribe(msg);
            case ACLMessage.PROXY -> handleProxy(msg);
            case ACLMessage.PROPAGATE -> handlePropagate(msg);
            case ACLMessage.UNKNOWN -> handleUnknown(msg);
            default -> block();
        }
    }

    protected void handleAcceptProposal(ACLMessage msg) {
    }

    protected void handleAgree(ACLMessage msg) {
    }

    protected void handleCancel(ACLMessage msg) {
    }

    protected void handleCfp(ACLMessage msg) {
    }

    protected void handleConfirm(ACLMessage msg) {
    }

    protected void handleDisconfirm(ACLMessage msg) {
    }

    protected void handleFailure(ACLMessage msg) {
    }

    protected void handleInform(ACLMessage msg) {
    }

    protected void handleInformIf(ACLMessage msg) {
    }

    protected void handleInformRef(ACLMessage msg) {
    }

    protected void handleNotUnderstood(ACLMessage msg) {
    }

    protected void handlePropose(ACLMessage msg) {
    }

    protected void handleQueryIf(ACLMessage msg) {
    }

    protected void handleQueryRef(ACLMessage msg) {
    }

    protected void handleRefuse(ACLMessage msg) {
    }

    protected void handleRejectProposal(ACLMessage msg) {
    }

    protected void handleRequest(ACLMessage msg) {
    }

    protected void handleRequestWhen(ACLMessage msg) {
    }

    protected void handleRequestWhenever(ACLMessage msg) {
    }

    protected void handleSubscribe(ACLMessage msg) {
    }

    protected void handleProxy(ACLMessage msg) {
    }

    protected void handlePropagate(ACLMessage msg) {
    }

    protected void handleUnknown(ACLMessage msg) {
    }

}
