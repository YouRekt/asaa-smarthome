package org.asaa.agents.appliances;

import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import lombok.Getter;
import lombok.Setter;
import org.asaa.agents.SmartApplianceAgent;
import org.asaa.behaviours.appliances.AwaitEnableBehaviour;
import org.asaa.behaviours.appliances.DishwasherAgent.MessageHandlerBehaviour;
import org.asaa.behaviours.appliances.RelinquishPowerBehaviour;
import org.asaa.behaviours.appliances.RequestPowerBehaviour;

@Getter
public final class DishwasherAgent extends SmartApplianceAgent {
    private final long fullWashTime = 30000;
    @Setter
    private long remainingWashTime = fullWashTime;
    private long washStartTime;
    @Setter
    private WakerBehaviour washBehaviour;

    @Override
    protected void setup() {
        idleDraw = 5;
        activeDraw = 275;
        priority = 150;
        isFreezable = true;

        super.setup();

        runnables.add(() -> {
            this.addBehaviour(new WakerBehaviour(this, 5000) {
                @Override
                protected void onWake() {
                    remainingWashTime = fullWashTime;
                    String replyWith = "req-" + System.currentTimeMillis();
                    ((SmartApplianceAgent)myAgent).onPowerGrantedCallbacks.put(replyWith, () -> performWash(false));
                    addBehaviour(new RequestPowerBehaviour((SmartApplianceAgent) myAgent, activeDraw, priority, "enable-active", replyWith));
                }
            });
        });

        addBehaviour(new MessageHandlerBehaviour(this));

        addBehaviour(new RequestPowerBehaviour(this, idleDraw, priority, "enable-passive", ""));

        addBehaviour(new AwaitEnableBehaviour(this, awaitEnablePeriod, runnables, behaviours));
    }

    public void performWash(boolean isResumed) {
        if (remainingWashTime <= 0) {
            logger.info("Nothing to do: no remaining time");
            return;
        }
        washStartTime = System.currentTimeMillis();

        washBehaviour = new WakerBehaviour(this, remainingWashTime) {
            @Override
            protected void onWake() {
                remainingWashTime = 0;
                logger.info("Wash complete!");
                environmentService.addPerformedTask();
                myAgent.addBehaviour(new RelinquishPowerBehaviour((SmartApplianceAgent) myAgent, activeDraw, "disable-active"));
            }
        };
        this.addBehaviour(washBehaviour);
        logger.info("Wash {} for {}ms", (isResumed ? "resumed" : "started"), remainingWashTime);
    }

    @Override
    protected void handleToggle() {
        if (washBehaviour != null) {
            long elapsed = System.currentTimeMillis() - washStartTime;
            remainingWashTime = Math.max(0, remainingWashTime - elapsed);
            this.removeBehaviour(washBehaviour);
            washBehaviour = null;
            logger.info("Wash paused, {}ms left", remainingWashTime);
        }
        super.handleToggle();
    }

    @Override
    protected String responseDefaultMsgContent() {
        return String.valueOf(isWorking);
    }
}
