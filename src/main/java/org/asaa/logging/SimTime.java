//package org.asaa.logging;
//
//import org.apache.logging.log4j.core.LogEvent;
//import org.apache.logging.log4j.core.config.plugins.Plugin;
//import org.apache.logging.log4j.core.config.plugins.PluginFactory;
//import org.apache.logging.log4j.core.pattern.ConverterKeys;
//import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
//import org.apache.logging.log4j.core.pattern.PatternConverter;
//import org.asaa.environment.Environment;
//
//@Plugin(name = "SimTime", category = PatternConverter.CATEGORY)
//@ConverterKeys({"simTime"})
//public class SimTime extends LogEventPatternConverter {
//
//    protected SimTime(String name, String style) {
//        super(name, style);
//    }
//
//    @PluginFactory
//    public static SimTime newInstance(String[] options) {
//        return new SimTime("simTime", "simTime");
//    }
//
//    @Override
//    public void format(LogEvent event, StringBuilder toAppendTo) {
//        toAppendTo.append(Environment.getSimulationTimeString());
//    }
//}
