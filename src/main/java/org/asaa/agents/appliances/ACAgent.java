package org.asaa.agents.appliances;

import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import org.asaa.agents.SmartApplianceAgent;
import org.asaa.behaviours.appliance.AwaitEnableBehaviour;
import org.asaa.behaviours.appliance.HandleMessageBehaviour;
import org.asaa.behaviours.appliance.RelinquishPowerBehaviour;
import org.asaa.behaviours.appliance.RequestPowerBehaviour;

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

public final class ACAgent extends SmartApplianceAgent {
    private final Double targetTemperature = 21.0;
    private final Double threshold = 0.5; // +/- margin before action (e.g., ±0.5°C)
    private final Double coolingRate = 0.1;

    @Override
    protected void setup() {
        super.setup();

        idleDraw = 10;
        activeDraw = 190;

        addBehaviour(new HandleMessageBehaviour(this) {
            @Override
            protected void handleInform(ACLMessage msg) {
                if (!isEnabled) {
                    logger.debug("Ignoring INFORM because not enabled");
                    return;
                }

                double temperature = Double.parseDouble(msg.getContent());
                if (temperature > targetTemperature) {
                    if (!isWorking) {
                        String replyWith = "req-" + System.currentTimeMillis();
                        smartApplianceAgent.onPowerGrantedCallbacks.put(replyWith, () -> performCooling(temperature));
                        addBehaviour(new RequestPowerBehaviour(smartApplianceAgent, activeDraw, "enable-active", replyWith));
                    } else performCooling(temperature);
                } else {
                    if (isWorking) {
                        logger.info("Finished cooling");
                        addBehaviour(new RelinquishPowerBehaviour(smartApplianceAgent, activeDraw, "disable-active"));
                    }
                    isWorking = false;
                }
            }
        });
        addBehaviour(new RequestPowerBehaviour(this, idleDraw, "enable-passive", ""));
        addBehaviour(new AwaitEnableBehaviour(this, () -> {
            while (!findTemperatureSensor()) {
                logger.info("Looking for temperature sensor");
            }

            addBehaviour(new TickerBehaviour(this, 10000) {
                @Override
                protected void onTick() {
                    if (!isWorking) {
                        requestTemperature();
                    }
                }
            });
        }));
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

    private void performCooling(double temperature) {
        addBehaviour(new WakerBehaviour(this, 1000) {
            @Override
            protected void onWake() {
                environmentService.getArea(areaName).setAttribute("temperature", temperature - coolingRate);
                requestTemperature();
            }
        });
        isWorking = true;
    }

    @Override
    protected void handleTrigger() {
        isWorking = !isWorking;
    }

    @Override
    protected String responseMsgContent() {
        return String.valueOf(isWorking);
    }
}
