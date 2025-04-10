package org.asaa.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import lombok.Getter;
import org.asaa.behaviours.sensor.HandleMessageBehaviour;
import org.asaa.environment.Area;
import org.asaa.environment.Environment;

import java.util.ArrayList;
import java.util.List;

public abstract class SensorAgent extends Agent {
    protected String areaName;
    @Getter
    protected List<AID> subscribers = new ArrayList<AID>();

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            this.areaName = (String) args[0];
        } else {
            this.areaName = "default-area";
        }

        System.out.printf("[%s] initialized in area: %s%n", getLocalName(), areaName);

        addBehaviour(new HandleMessageBehaviour(this) {
            @Override
            protected void handleRequest(ACLMessage msg) {
                respondToRequest(msg);
            }
        });

    }

    protected Area getMyArea() {
        Environment env = Environment.getInstance();
        return env.getArea(areaName);
    }

    public void trigger() {
        addBehaviour(new OneShotBehaviour(this) {
            @Override
            public void action() {
                subscribers.forEach(SensorAgent.this::handleTrigger);
            }
        });
    }

    protected abstract void handleTrigger(AID subscriber);

    protected abstract void respondToRequest(ACLMessage msg);
}
