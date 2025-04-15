package org.asaa.agents.appliances;

import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import org.asaa.agents.SmartApplianceAgent;
import org.asaa.behaviours.appliance.HandleMessageBehaviour;
import org.asaa.environment.Environment;

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

public class ACAgent extends SmartApplianceAgent {

    private final Double targetTemperature = 21.0;
    private final Double threshold = 0.5; // +/- margin before action (e.g., ±0.5°C)
    private final Double coolingRate = 0.1;
    private boolean isCooling = false;

    @Override
    protected void setup() {
        super.setup();

        while(!findTemperatureSensor())
        {
            logger.info("Looking for temperature sensor");
        }

        addBehaviour(new HandleMessageBehaviour(this) {
            @Override
            protected void handleInform(ACLMessage msg) {
                Double temperature = Double.valueOf(msg.getContent());
                if (temperature > targetTemperature) {
                    isCooling = true;
                    Environment.getInstance().getArea(areaName).setAttribute("temperature", temperature - coolingRate);
                    requestTemperature();
                } else {
                    logger.info("Finished cooling");
                    isCooling = false;
                }
            }
        });

        addBehaviour(new TickerBehaviour(this, 10000) {
            @Override
            protected void onTick() {
                if (!isCooling) {
                    requestTemperature();
                }
            }
        });
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
                return false;
            }
        } catch (FIPAException e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    private void requestTemperature() {
        AID tempSensor = subscribedSensors.get("TemperatureSensorAgent").getFirst();
        if (tempSensor != null) {
            ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
            request.addReceiver(tempSensor);
            request.setReplyByDate(new Date(System.currentTimeMillis() + 9000));
            send(request);
        }
    }

    @Override
    protected void handleTrigger() {
        isCooling = !isCooling;
    }

    @Override
    protected String responseMsgContent() {
        return String.valueOf(isCooling);
    }
}
