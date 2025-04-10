package org.asaa.environment;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Environment {
    private static Environment instance;

    private final Map<String, Area> areas;

    private Environment() {
        this.areas = new HashMap<>();
    }

    public static synchronized Environment getInstance() {
        if (instance == null) {
            instance = new Environment();
        }
        return instance;
    }

    public void addArea(String name, Area area) {
        areas.put(name, area);
    }

    public Area getArea(String name) {
        return areas.get(name);
    }

    public Set<String> getAllAreaNames() {
        return areas.keySet();
    }
}
