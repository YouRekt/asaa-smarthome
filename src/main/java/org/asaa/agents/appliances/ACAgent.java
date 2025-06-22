package org.asaa.agents.appliances;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import lombok.Getter;
import lombok.Setter;
import org.asaa.agents.base.SmartApplianceAgent;
import org.asaa.behaviours.appliances.ACAgent.MessageHandlerBehaviour;
import org.asaa.behaviours.appliances.ACAgent.ModeAutoBehaviour;

import java.util.Date;

@Getter
public final class ACAgent extends SmartApplianceAgent {
    @Setter
    private Double currentTemperature;
    private final Double targetTemperature = 21.0;
    private final Double coolingRate = 0.2;

    @Override
    protected void setup() {
        idleDraw = 10;
        activeDraw = 190;
        priority = 120;

        runnables.add(() -> {
            AID sensor;
            while ((sensor = findAgent("TemperatureSensorAgent", areaName, true)) == null) {
                logger.info("Looking for temperature sensor");
            }
            subscribeSensor(sensor, "TemperatureSensorAgent");
        });

        behaviours.put("ModeAutoBehaviour", new ModeAutoBehaviour(this));

        super.setup();

        addBehaviour(new MessageHandlerBehaviour(this));
    }

    public void requestTemperature() {
        AID tempSensor = subscribedSensors.get("TemperatureSensorAgent").getFirst();
        if (tempSensor != null) {
            ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
            request.addReceiver(tempSensor);
            request.setReplyByDate(new Date(System.currentTimeMillis() + 9000));
            sendMessage(request);
        }
    }

}
