package org.asaa.behaviours;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asaa.agents.PhysicalAgent;

public abstract class BaseMessageHandler extends CyclicBehaviour {
    protected final Logger logger;

    public BaseMessageHandler(PhysicalAgent physicalAgent) {
        super(physicalAgent);

        logger = LogManager.getLogger(physicalAgent.getLocalName());
    }

    public void processMsg(ACLMessage msg) {
        switch (msg.getPerformative()) {
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

    protected void handleAgree(ACLMessage msg) {};

    protected void handleCancel(ACLMessage msg) {};

    protected void handleCfp(ACLMessage msg) {};

    protected void handleConfirm(ACLMessage msg) {};

    protected void handleDisconfirm(ACLMessage msg) {};

    protected void handleFailure(ACLMessage msg) {};

    protected void handleInform(ACLMessage msg) {};

    protected void handleInformIf(ACLMessage msg) {};

    protected void handleInformRef(ACLMessage msg) {};

    protected void handleNotUnderstood(ACLMessage msg) {};

    protected void handlePropose(ACLMessage msg) {};

    protected void handleQueryIf(ACLMessage msg) {};

    protected void handleQueryRef(ACLMessage msg) {};

    protected void handleRefuse(ACLMessage msg) {};

    protected void handleRejectProposal(ACLMessage msg) {};

    protected void handleRequest(ACLMessage msg) {};

    protected void handleRequestWhen(ACLMessage msg) {};

    protected void handleRequestWhenever(ACLMessage msg) {};

    protected void handleSubscribe(ACLMessage msg) {};

    protected void handleProxy(ACLMessage msg) {};

    protected void handlePropagate(ACLMessage msg) {};

    protected void handleUnknown(ACLMessage msg) {};
}
