package org.asaa.environment;

import lombok.Getter;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Area {
    @Getter
    private final String name;
    private final Map<String, Object> attributes;

    public Area(String name) {
        this.name = name;
        this.attributes = new ConcurrentHashMap<>();
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public Map<String, Object> getAllAttributes() {
        return Collections.unmodifiableMap(attributes);
    }
}
