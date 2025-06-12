package org.asaa.agents;

import jade.core.AID;
import lombok.Getter;
import lombok.Setter;
import org.asaa.environment.Area;
import org.slf4j.MDC;

@Getter
public abstract class PhysicalAgent extends SpringAwareAgent {
    protected AID coordinatorAgent;
    /* Priority sheet:
    0   <= p < 100 - awaits callback upon being turned off while working
    100 <= p < 200 - default priority sorting (lower - lower priority - turns off first)
    200 <= p < 300 - isEnabled but not working, lowest prio turn off (low energy save anyway)
     */
    @Setter
    protected int priority = 0;
    protected String areaName;

    @Override
    protected void setup() {
        super.setup();

        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            this.areaName = (String) args[0];
        } else {
            this.areaName = "default-area";
        }

        MDC.put("agent", this.getLocalName());
        MDC.put("area", areaName);

        logger.info("Initialized in area: {}", areaName);

        register(areaName);
        coordinatorAgent = findAgent("Coordinator", "");
    }

    @Override
    protected void takeDown() {
        MDC.clear();
        super.takeDown();
    }

    protected Area getArea() {
        return environmentService.getArea(areaName);
    }

    public void trigger() {
        handleTrigger();
    }

    protected abstract void handleTrigger();

}
