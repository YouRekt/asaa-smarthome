package org.asaa.agents.appliances;

import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import lombok.Getter;
import lombok.Setter;
import org.asaa.agents.SmartApplianceAgent;
import org.asaa.behaviours.appliances.ACAgent.MessageHandlerBehaviour;
import org.asaa.behaviours.appliances.ACAgent.ModeAutoBehaviour;
import org.asaa.behaviours.appliances.AwaitEnableBehaviour;
import org.asaa.behaviours.appliances.RequestPowerBehaviour;

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

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
            while (!findTemperatureSensor()) {
                logger.info("Looking for temperature sensor");
            }
        });

        behaviours.put("ModeAutoBehaviour", new ModeAutoBehaviour(this));

        addBehaviour(new MessageHandlerBehaviour(this));

        addBehaviour(new RequestPowerBehaviour(this, idleDraw, priority, "enable-passive", ""));

        addBehaviour(new AwaitEnableBehaviour(this, awaitEnablePeriod, runnables, behaviours));
    }

    private boolean findTemperatureSensor() {
        final Property property = new Property();
        property.setName("areaName");
        property.setValue(areaName);

        final ServiceDescription sd = new ServiceDescription();
        sd.addProperties(property);
        sd.setType("TemperatureSensorAgent");

        try {
            final DFAgentDescription dfd = new DFAgentDescription();
            dfd.addServices(sd);
            Optional<AID> sensor = Arrays.stream(DFService.search(this, dfd)).map(DFAgentDescription::getName).findFirst();
            if (sensor.isPresent()) {
                subscribeSensor(sensor.get(), "TemperatureSensorAgent");
            } else {
                logger.warn("No TemperatureSensorAgent found");
                agentCommunicationController.sendError(getName(), "No TemperatureSensorAgent found");
                return false;
            }
        } catch (FIPAException e) {
            throw new RuntimeException(e);
        }
        return true;
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

    @Override
    protected String responseDefaultMsgContent() {
        return String.valueOf(isWorking);
    }
}
