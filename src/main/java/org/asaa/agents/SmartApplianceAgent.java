package org.asaa.agents;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import lombok.Getter;
import org.asaa.behaviours.appliance.HandleMessageBehaviour;

import java.util.List;

@Getter
public abstract class SmartApplianceAgent extends PhysicalAgent {
    protected List<AID> followedSensors;

    @Override
    protected void setup() {
        super.setup();

        addBehaviour(new HandleMessageBehaviour(this) {
            @Override
            protected void handleInform(ACLMessage msg) {
                trigger();
            }
        });
    }
}
