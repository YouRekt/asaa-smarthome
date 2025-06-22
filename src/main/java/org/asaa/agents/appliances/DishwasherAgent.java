package org.asaa.agents.appliances;

import lombok.Getter;
import org.asaa.agents.base.SmartApplianceAgent;
import org.asaa.behaviours.appliances.DishwasherAgent.MessageHandlerBehaviour;

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

        // TODO: Implement using the new system
//        runnables.add(() -> this.addBehaviour(new WakerBehaviour(this, 5000) {
//            @Override
//            protected void onWake() {
//                new WashDishesTask((DishwasherAgent)myAgent, updateDelay, noninterruptibleStartPercent, noninterruptibleEndPercent, fullWashTime).start();
//            }
//        }));

        super.setup();

        addBehaviour(new MessageHandlerBehaviour(this));
    }

}
