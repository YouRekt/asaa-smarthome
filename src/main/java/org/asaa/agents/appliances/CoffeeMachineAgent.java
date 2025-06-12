package org.asaa.agents.appliances;

import jade.core.behaviours.WakerBehaviour;
import org.asaa.agents.SmartApplianceAgent;
import org.asaa.behaviours.appliances.AwaitEnableBehaviour;
import org.asaa.behaviours.appliances.CoffeeMachineAgent.MessageHandlerBehaviour;
import org.asaa.behaviours.appliances.RelinquishPowerBehaviour;
import org.asaa.behaviours.appliances.RequestPowerBehaviour;

public final class CoffeeMachineAgent extends SmartApplianceAgent {
    private WakerBehaviour makingCoffeeBehaviour = null;

    @Override
    protected void setup() {
        idleDraw = 5;
        activeDraw = 120;
        priority = 100;
        isInterruptible = false;

        super.setup();

        addBehaviour(new MessageHandlerBehaviour(this));

        addBehaviour(new RequestPowerBehaviour(this, idleDraw, priority, "enable-passive", ""));

        addBehaviour(new AwaitEnableBehaviour(this, awaitEnablePeriod, runnables, behaviours));
    }

    public void makeCoffee() {
        logger.info("Making coffee");
        makingCoffeeBehaviour = new WakerBehaviour(this, 10000) {
            @Override
            protected void onWake() {
                logger.info("Coffee made! Enjoy");
                environmentService.addPerformedTask();
                addBehaviour(new RelinquishPowerBehaviour((SmartApplianceAgent) myAgent, activeDraw, "disable-active"));
            }
        };
        addBehaviour(makingCoffeeBehaviour);
    }

    @Override
    protected String responseDefaultMsgContent() {
        return String.valueOf(isWorking);
    }
}
