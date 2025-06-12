package org.asaa.agents.appliances;

import jade.core.behaviours.WakerBehaviour;
import lombok.Getter;
import org.asaa.agents.SmartApplianceAgent;
import org.asaa.behaviours.appliances.AwaitEnableBehaviour;
import org.asaa.behaviours.appliances.DishwasherAgent.MessageHandlerBehaviour;
import org.asaa.behaviours.appliances.RequestPowerBehaviour;
import org.asaa.tasks.appliances.DishwasherAgent.WashDishesTask;

@Getter
public final class DishwasherAgent extends SmartApplianceAgent {
    private final long fullWashTime = 30000;
    private final long updateDelay = 200;
    private final double noninterruptibleStartPercent = 0.75;
    private final double noninterruptibleEndPercent = 0.4;

    @Override
    protected void setup() {
        idleDraw = 5;
        activeDraw = 275;
        priority = 150;
        isFreezable = true;

        super.setup();

        runnables.add(() -> this.addBehaviour(new WakerBehaviour(this, 5000) {
            @Override
            protected void onWake() {
                requestStartTask(new WashDishesTask((DishwasherAgent)myAgent, updateDelay, noninterruptibleStartPercent, noninterruptibleEndPercent, fullWashTime));
            }
        }));

        addBehaviour(new MessageHandlerBehaviour(this));

        addBehaviour(new RequestPowerBehaviour(this, idleDraw, priority, "enable-passive", ""));

        addBehaviour(new AwaitEnableBehaviour(this, awaitEnablePeriod, runnables, behaviours));
    }

    @Override
    protected String responseDefaultMsgContent() {
        return String.valueOf(isWorking);
    }
}
