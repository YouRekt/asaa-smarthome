package org.asaa.behaviours.coordinator;

import jade.lang.acl.ACLMessage;
import org.asaa.agents.SmartApplianceAgent;
import org.asaa.agents.coordinators.CoordinatorAgent;
import org.asaa.behaviours.BaseMessageHandler;

public abstract class HandleMessageBehaviour extends BaseMessageHandler {
    protected final CoordinatorAgent coordinatorAgent;

    public HandleMessageBehaviour(CoordinatorAgent coordinatorAgent) {
        super(coordinatorAgent);

        this.coordinatorAgent = coordinatorAgent;
    }

    @Override
    public void action() {
        final ACLMessage msg = myAgent.receive();

        if (msg != null) {
            // Here we can add a specialized switch if needed (default -> processMsg(msg);)
            super.processMsg(msg);
        } else {
            block();
        }
    }
}
