package org.asaa.agents.appliances;

import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import org.asaa.agents.SmartApplianceAgent;
import org.asaa.behaviours.appliance.AwaitEnableBehaviour;
import org.asaa.behaviours.appliance.HandleMessageBehaviour;
import org.asaa.behaviours.appliance.RelinquishPowerBehaviour;
import org.asaa.behaviours.appliance.RequestPowerBehaviour;

public final class DishwasherAgent extends SmartApplianceAgent {
    private final long fullWashTime = 30000;
    private long remainingWashTime = fullWashTime;
    private long washStartTime;
    private WakerBehaviour washBehaviour;

    @Override
    protected void setup() {
        idleDraw = 5;
        activeDraw = 275;
        priority = 150;
        isFreezable = true;

        super.setup();

        addBehaviour(new HandleMessageBehaviour(this) {
            @Override
            protected void handleInform(ACLMessage msg) {
                switch (msg.getConversationId()) {
                    case "enable-callback":
                        if (washBehaviour == null && remainingWashTime > 0) {
                            String replyWith = "req-" + System.currentTimeMillis();
                            smartApplianceAgent.onPowerGrantedCallbacks.put(replyWith, () -> beginWash());
                            addBehaviour(new RequestPowerBehaviour(smartApplianceAgent, activeDraw, priority, "enable-active", replyWith));
                        }
                        break;
                    default:
                        break;
                }
                super.handleInform(msg);
            }

            @Override
            protected void handleRequest(ACLMessage msg) {
                if (!isWorking) {
                    remainingWashTime = fullWashTime;
                    String replyWith = "req-" + System.currentTimeMillis();
                    smartApplianceAgent.onPowerGrantedCallbacks.put(replyWith, () -> beginWash());
                    addBehaviour(new RequestPowerBehaviour(smartApplianceAgent, activeDraw, priority, "enable-active", replyWith));
                }
            }

            @Override
            protected void handleAcceptProposal(ACLMessage msg) {
                switch (msg.getConversationId()) {
                    case "power-relief":
                        if (washBehaviour != null) {
                            long elapsed = System.currentTimeMillis() - washStartTime;
                            remainingWashTime = Math.max(0, remainingWashTime - elapsed);
                            myAgent.removeBehaviour(washBehaviour);
                            washBehaviour = null;
                            logger.info("Wash paused, {}ms left", remainingWashTime);
                            super.handleAcceptProposal(msg);
                        } else {
                            super.handleAcceptProposal(msg);
                        }
                        break;
                    default:
                        break;
                }
            }
        });

        addBehaviour(new RequestPowerBehaviour(this, idleDraw, priority, "enable-passive", ""));

        addBehaviour(new AwaitEnableBehaviour(this, awaitEnablePeriod, runnables, behaviours));
    }

    private void beginWash() {
        if (remainingWashTime <= 0) {
            logger.info("Nothing to do: no remaining time");
            return;
        }
        washStartTime = System.currentTimeMillis();

        washBehaviour = new WakerBehaviour(this, remainingWashTime) {
            @Override
            protected void onWake() {
                myAgent.addBehaviour(new RelinquishPowerBehaviour((SmartApplianceAgent) myAgent, activeDraw, "disable-active"));
                remainingWashTime = 0;
                logger.info("Wash complete!");
            }
        };
        this.addBehaviour(washBehaviour);
        logger.info("Wash started/resumed for {}ms", remainingWashTime);
    }

    @Override
    protected String responseDefaultMsgContent() {
        return String.valueOf(isWorking);
    }
}
