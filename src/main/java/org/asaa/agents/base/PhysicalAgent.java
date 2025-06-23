package org.asaa.agents.base;

import jade.core.AID;
import lombok.Getter;
import lombok.Setter;
import org.asaa.environment.Area;
import org.slf4j.MDC;

@Getter
public abstract class PhysicalAgent extends SpringAwareAgent {
    protected AID coordinatorAgent;
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
        coordinatorAgent = findAgent("CoordinatorAgent", "", true);
    }

    @Override
    protected void takeDown() {
        MDC.clear();
        super.takeDown();
    }

    protected Area getArea() {
        return environmentService.getArea(areaName);
    }
}
