package org.asaa.services;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import org.asaa.exceptions.AgentContainerException;
import org.asaa.exceptions.JadePlatformInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.String.format;

@Service
public class JadeService {
    private static final ExecutorService jadeExecutor = Executors.newCachedThreadPool();
    private static final Logger logger = LoggerFactory.getLogger(JadeService.class);
    private ContainerController container;

    private static void runAgents(final ContainerController container) {
        runAgent(container, "Coordinator", "coordinators", "CoordinatorAgent");
        runAgent(container, "Scheduler", "coordinators", "SchedulerAgent");
        runAgent(container, "Temperature Sensor", "sensors", "TemperatureSensorAgent", new Object[]{"kitchen"});
        runAgent(container, "Motion Sensor", "sensors", "MotionSensorAgent", new Object[]{"kitchen"});
        runAgent(container, "Smart Lightbulb", "appliances", "SmartLightbulbAgent", new Object[]{"kitchen"});
        runAgent(container, "AC", "appliances", "ACAgent", new Object[]{"kitchen"});
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

//                runGUI(container);
                runAgents(container);

            } catch (final InterruptedException | ExecutionException e) {
                logger.error(e.getMessage());
                throw new JadePlatformInitializationException(e);
            }
        }
    }

    public synchronized void stop() {
        if (container != null) {
            try {
                container.kill();
            } catch (StaleProxyException e) {
                logger.error(e.getMessage());
                throw new RuntimeException(e);
            }
        } else {
            logger.warn("Agent container is null");
        }
    }
}
