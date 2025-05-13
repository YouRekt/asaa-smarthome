package org.asaa.environment;

import java.util.Random;

public class TemperatureSimulator {
    private static final Random random = new Random();

    public static double simulateRoomTemperature(double currentTemp, int hourOfDay, int peakSunHour, double minDailyTemp, double maxDailyTemp) {
        hourOfDay = Math.max(0, Math.min(hourOfDay, 23));

        // Calculate temperature curve using a sine wave approximation
        double range = maxDailyTemp - minDailyTemp;
        double peakOffset = (peakSunHour - 6 + 24) % 24; // assuming sunrise ~6AM
        double relativeHour = (hourOfDay - peakOffset + 24) % 24;
        double baseTemp = minDailyTemp + range * Math.sin(Math.PI * relativeHour / 24);

        // Add a small random fluctuation to simulate real-world instability
        double noise = (random.nextDouble() - 0.5) * 0.5; // ±0.25°C

        // Gently adjust toward the base temp to simulate inertia
        double adjustment = (baseTemp - currentTemp) * 0.1;

        return Math.round((currentTemp + adjustment + noise) * 100.0) / 100.0;
    }
}
