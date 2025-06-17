package org.asaa.util;

import lombok.Getter;

@Getter
public class AgentConfig {
    // Getters
    private final String agentName;
    private final String packageName;
    private final String className;
    private final Object[] args;

    public AgentConfig(String agentName, String packageName, String className) {
        this(agentName, packageName, className, new Object[]{});
    }

    public AgentConfig(String agentName, String packageName, String className, Object[] args) {
        this.agentName = agentName;
        this.packageName = packageName;
        this.className = className;
        this.args = args;
    }
}
