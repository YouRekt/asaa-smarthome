package org.asaa.services;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import org.asaa.dto.ConfigDTO;
import org.asaa.exceptions.AgentContainerException;
import org.asaa.exceptions.JadePlatformInitializationException;
import org.asaa.util.AgentConfig;
import org.asaa.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

@Service
public class JadeService {
    private static final ExecutorService jadeExecutor = Executors.newCachedThreadPool();
    private static final Logger logger = LoggerFactory.getLogger(JadeService.class);
    private ContainerController container;
    private final List<AgentConfig> agentConfigs = new ArrayList<>();

    public void configureAgents(List<AgentConfig> configs) {
        this.agentConfigs.clear();

        // Always add core agents first
        addCoreAgents();

        // Add configured agents
        this.agentConfigs.addAll(configs);
    }

    public void configureAgentsFromDTO(List<ConfigDTO.AgentEntry> agentEntries) {
        List<AgentConfig> configs = new ArrayList<>();

        for (ConfigDTO.AgentEntry entry : agentEntries) {
            String packageName = getPackageNameFromAgentName(entry.getName());
            String className = getClassNameFromAgentName(entry.getName());
            Object[] args = new Object[]{entry.getArea()};

            configs.add(new AgentConfig(entry.getAid(), packageName, className, args));
        }

        configureAgents(configs);
    }

    private void addCoreAgents() {
        // Core system agents that should always run
        agentConfigs.add(new AgentConfig("Coordinator", "coordinators", "CoordinatorAgent"));
        agentConfigs.add(new AgentConfig("Human", "coordinators", "HumanAgent", new Object[]{"Kitchen"}));
        agentConfigs.add(new AgentConfig("Scheduler", "coordinators", "SchedulerAgent"));
    }

    private String getPackageNameFromAgentName(String agentName) {
        String lowerName = agentName.toLowerCase();
        if (lowerName.contains("sensor")) {
            return "sensors";
        } else {
            return "appliances";
        }
    }

    private String getClassNameFromAgentName(String agentName) {
        switch (agentName) {
            case "Temperature Sensor" -> {
                return "TemperatureSensorAgent";
            }
            case "Motion Sensor" -> {
                return "MotionSensorAgent";
            }
            case "AC Unit" -> {
                return "ACAgent";
            }
            case "Coffee Machine" -> {
                return "CoffeeMachineAgent";
            }
            case "Dishwasher" -> {
                return "DishwasherAgent";
            }
            case "Fridge" -> {
                return "FridgeAgent";
            }
            case "Smart Lightbulb" -> {
                return "SmartLightbulbAgent";
            }
            default -> {
                logger.error("Agent class for name \"{}\" not found", agentName);
                throw new RuntimeException();
            }
        }
    }

    private void runAgents(final ContainerController container) {
//        runAgent(container, "Coordinator", "coordinators", "CoordinatorAgent");
//        try {
//            TimeUnit.SECONDS.sleep(1);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        runAgent(container, "Human", "coordinators", "HumanAgent", new Object[]{"kitchen"});
//        runAgent(container, "Scheduler", "coordinators", "SchedulerAgent");
//        runAgent(container, "Temperature Sensor", "sensors", "TemperatureSensorAgent", new Object[]{"kitchen"});
//        runAgent(container, "Motion Sensor", "sensors", "MotionSensorAgent", new Object[]{"kitchen"});
//        runAgent(container, "Smart Lightbulb 1", "appliances", "SmartLightbulbAgent", new Object[]{"kitchen"});
//        runAgent(container, "Smart Lightbulb 2", "appliances", "SmartLightbulbAgent", new Object[]{"bathroom"});
//        runAgent(container, "Smart Lightbulb 3", "appliances", "SmartLightbulbAgent", new Object[]{"bedroom 1"});
//        runAgent(container, "Smart Lightbulb 4", "appliances", "SmartLightbulbAgent", new Object[]{"bedroom 2"});
//        runAgent(container, "Smart Lightbulb 5", "appliances", "SmartLightbulbAgent", new Object[]{"bedroom 3"});
//        runAgent(container, "Smart Lightbulb 6", "appliances", "SmartLightbulbAgent", new Object[]{"before room"});

        for (AgentConfig config : agentConfigs) {
            runAgent(container, config.getAgentName(), config.getPackageName(),
                    config.getClassName(), config.getArgs());

            // Add small delay between agent startups if needed
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Agent startup delay interrupted");
            }
        }

//        runAgent(container, "AC", "appliances", "ACAgent", new Object[]{"kitchen"});
//        runAgent(container, "Fridge", "appliances", "FridgeAgent", new Object[]{"kitchen"});
//        runAgent(container, "Coffee", "appliances", "CoffeeMachineAgent", new Object[]{"kitchen"});
//        runAgent(container, "Dishwasher", "appliances", "DishwasherAgent", new Object[]{"kitchen"});
    }

    public static void runGUI(final ContainerController mainContainer) {
        try {
            final AgentController guiAgent = mainContainer.createNewAgent("rma", "jade.tools.rma.rma", new Object[0]);
            guiAgent.start();
        } catch (final StaleProxyException e) {
            throw new AgentContainerException("GUIAgent", e);
        }
    }

    public static void runAgent(final ContainerController mainContainer, final String agentName, final String packageName, final String className) {
        try {
            final String path = format("org.asaa.agents.%s.%s", packageName, className);
            final AgentController agent = mainContainer.createNewAgent(agentName, path, new Object[]{});
            agent.start();
        } catch (final StaleProxyException e) {
            throw new AgentContainerException(agentName, e);
        }
    }

    public static void runAgent(final ContainerController mainContainer, final String agentName, final String packageName, final String className, final Object[] args) {
        try {
            final String path = format("org.asaa.agents.%s.%s", packageName, className);
            final AgentController agent = mainContainer.createNewAgent(agentName, path, args);
            agent.start();
        } catch (final StaleProxyException e) {
            throw new AgentContainerException(agentName, e);
        }
    }

    public synchronized void start() {
        final jade.core.Runtime runtime = Runtime.instance();

        if (container == null) {
            final Profile profile = new ProfileImpl();
            profile.setParameter(Profile.MTPS, "");

            try {
                container = jadeExecutor.submit(() -> runtime.createMainContainer(profile)).get();

                runGUI(container);
                if (agentConfigs.isEmpty()) {
                    addDefaultAgents();
                }
                Util.ContainerIP = container.getPlatformName();
                logger.info("Platform Name {}", Util.ContainerIP);
                runAgents(container);

            } catch (final InterruptedException | ExecutionException e) {
                logger.error(e.getMessage());
                throw new JadePlatformInitializationException(e);
            }
        }
    }

    private void addDefaultAgents() {
        addCoreAgents();
        agentConfigs.add(new AgentConfig("Temperature Sensor", "sensors", "TemperatureSensorAgent", new Object[]{"kitchen"}));
        agentConfigs.add(new AgentConfig("Motion Sensor", "sensors", "MotionSensorAgent", new Object[]{"kitchen"}));
        agentConfigs.add(new AgentConfig("Smart Lightbulb 1", "appliances", "SmartLightbulbAgent", new Object[]{"kitchen"}));
        agentConfigs.add(new AgentConfig("Smart Lightbulb 2", "appliances", "SmartLightbulbAgent", new Object[]{"kitchen"}));
        agentConfigs.add(new AgentConfig("Smart Lightbulb 3", "appliances", "SmartLightbulbAgent", new Object[]{"kitchen"}));
        agentConfigs.add(new AgentConfig("Smart Lightbulb 4", "appliances", "SmartLightbulbAgent", new Object[]{"kitchen"}));
        agentConfigs.add(new AgentConfig("Smart Lightbulb 5", "appliances", "SmartLightbulbAgent", new Object[]{"kitchen"}));
        agentConfigs.add(new AgentConfig("Smart Lightbulb 6", "appliances", "SmartLightbulbAgent", new Object[]{"kitchen"}));
    }

    public synchronized void stop() {
        if (container != null) {
            try {
                container.kill();
                container = null;
                agentConfigs.clear();
            } catch (StaleProxyException e) {
                logger.error(e.getMessage());
                throw new RuntimeException(e);
            }
        } else {
            logger.warn("Agent container is null");
        }
    }
}
