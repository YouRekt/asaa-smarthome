package org.asaa.util;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.DynamicConverter;
import org.asaa.services.EnvironmentService;

public class SimulationTimeConverter extends DynamicConverter<ILoggingEvent> {
    @Override
    public String convert(ILoggingEvent event) {
        return SpringContext.get().getBean(EnvironmentService.class).getFormattedSimulationTime();
    }
}