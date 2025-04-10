package org.asaa;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.ContainerController;
import org.asaa.environment.Area;
import org.asaa.environment.Environment;
import org.asaa.environment.Simulator;
import org.asaa.exceptions.JadePlatformInitializationException;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.asaa.JADEEngine.runAgent;
import static org.asaa.JADEEngine.runGUI;

public class Engine {
    private static final ExecutorService jadeExecutor = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        final Runtime runtime = Runtime.instance();
        final Profile profile = new ProfileImpl();
        Environment env = Environment.getInstance();
        Area kitchen = new Area("kitchen");
        kitchen.setAttribute("temperature", 20.0); // Initial value
        env.addArea("kitchen", kitchen);

        Simulator.startSimulation();


        try {
            final ContainerController container = jadeExecutor.submit(() -> runtime.createMainContainer(profile)).get();

            runGUI(container);
            runAgent(container, "Temperature Sensor", "TemperatureSensorAgent", new Object[]{"kitchen"});
        } catch (final InterruptedException | ExecutionException e) {
            throw new JadePlatformInitializationException(e);
        }
    }
}
