package org.asaa.agents.appliances;

import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import org.asaa.agents.SmartApplianceAgent;
import org.asaa.behaviours.appliance.AwaitEnableBehaviour;
import org.asaa.behaviours.appliance.HandleMessageBehaviour;
import org.asaa.behaviours.appliance.RelinquishPowerBehaviour;
import org.asaa.behaviours.appliance.RequestPowerBehaviour;

public final class CoffeeMachineAgent extends SmartApplianceAgent {

    @Override
    protected void setup() {
        idleDraw = 5;
        activeDraw = 120;
        priority = 10;

        super.setup();

        addBehaviour(new HandleMessageBehaviour(this) {
            @Override
            protected void handleRequest(ACLMessage msg) {
                if (!isWorking) {
                    String replyWith = "req-" + System.currentTimeMillis();
                    smartApplianceAgent.onPowerGrantedCallbacks.put(replyWith, () -> makeCoffee());
                    addBehaviour(new RequestPowerBehaviour(smartApplianceAgent, activeDraw, priority, "enable-active", replyWith));
                } else {
                    logger.warn("Currently making coffee, can not respond to request");
                }
            }
        });
        addBehaviour(new RequestPowerBehaviour(this, idleDraw, priority, "enable-passive", ""));
        addBehaviour(new AwaitEnableBehaviour(this, () -> {

        }));
    }

    private void makeCoffee() {
        logger.info("Making coffee");
        addBehaviour(new WakerBehaviour(this, 10000) {
            @Override
            protected void onWake() {
                logger.info("Coffee made! Enjoy");
                addBehaviour(new RelinquishPowerBehaviour((SmartApplianceAgent) myAgent, activeDraw, "disable-active"));
            }
        });
    }

    @Override
    protected String responseDefaultMsgContent() {
        return String.valueOf(isWorking);
    }
}
