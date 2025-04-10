package org.asaa.environment;

import java.util.Random;

public class Simulator {
    public static void startSimulation() {
        new Thread(() -> {
            Random rand = new Random();
            Environment env = Environment.getInstance();
            Area kitchen = env.getArea("kitchen");

            while (true) {
                try {
                    Thread.sleep(20000); // every 3 seconds
                    double newTemp = 18 + rand.nextDouble() * 6; // random temp 18–24
                    kitchen.setAttribute("temperature", newTemp);
                    System.out.println("[Simulator] Kitchen temperature updated to: " + newTemp);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
