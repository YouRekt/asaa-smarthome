package org.asaa.agents;

import jade.core.Agent;

import java.util.List;

public abstract class SmartApplianceAgent extends Agent {
    protected String areaName;
    protected List<SensorAgent> sensors;

    @Override
    protected void setup() {

    }
}
