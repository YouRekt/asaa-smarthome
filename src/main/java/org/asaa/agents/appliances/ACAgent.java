package org.asaa.agents.appliances;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import lombok.Getter;
import lombok.Setter;
import org.asaa.agents.SmartApplianceAgent;
import org.asaa.behaviours.appliances.ACAgent.MessageHandlerBehaviour;
import org.asaa.behaviours.appliances.ACAgent.ModeAutoBehaviour;
import org.asaa.behaviours.appliances.AwaitEnableBehaviour;
import org.asaa.behaviours.appliances.RequestPowerBehaviour;

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

        super.setup();

        runnables.add(() -> {
            AID sensor;
            while ((sensor = findAgent("Temperature Sensor", areaName)) == null) {
                logger.info("Looking for temperature sensor");
            }
            subscribeSensor(sensor, "TemperatureSensorAgent");
        });

        behaviours.put("ModeAutoBehaviour", new ModeAutoBehaviour(this));

        addBehaviour(new MessageHandlerBehaviour(this));

        addBehaviour(new RequestPowerBehaviour(this, idleDraw, priority, "enable-passive", ""));

        addBehaviour(new AwaitEnableBehaviour(this, awaitEnablePeriod, runnables, behaviours));
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
