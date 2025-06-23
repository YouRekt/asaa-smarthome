package org.asaa.agents.coordinators;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import lombok.Getter;
import lombok.Setter;
import org.asaa.agents.base.SpringAwareAgent;
import org.asaa.behaviours.coordinators.CoordinatorAgent.AgentScanningBehaviour;
import org.asaa.behaviours.coordinators.CoordinatorAgent.MessageHandlerBehaviour;
import org.asaa.behaviours.coordinators.CoordinatorAgent.PowerNegotiationBehaviour;
import org.asaa.environment.Area;
import org.asaa.util.Util;
import org.slf4j.MDC;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public final class CoordinatorAgent extends SpringAwareAgent {
    @Setter
    private PowerNegotiationBehaviour powerNegotiationBehaviour;
    private final Map<Area, Map<String, List<AID>>> physicalAgents = new HashMap<>();
    private final Map<AID, Integer> appliancesAwaitingPower = new HashMap<>();
    private final Queue<ACLMessage> pendingCfpQueue = new LinkedList<>();
    @Setter
    private boolean cfpInProgress = false;

    @Override
    protected void setup() {
        super.setup();

        MDC.put("agent", "Coordinator");
        MDC.put("area", "----------");

        logger.info("Initialized");

        register("");

        addBehaviour(new AgentScanningBehaviour(this, 5000));

        addBehaviour(new MessageHandlerBehaviour(this));
    }

    @Override
    protected void takeDown() {
        MDC.clear();
        super.takeDown();
    }

    private List<AID> getAgentListAIDForArea(String agentClass, String areaStr) {
        Area area = environmentService.getArea(areaStr);
        Map<String, List<AID>> agentsInArea = physicalAgents.get(area);

        if (agentsInArea == null) {
            return Collections.emptyList();
        }

        List<AID> agents = agentsInArea.get(agentClass);
        if (agents == null || agents.isEmpty()) {
            return Collections.emptyList();
        }

        return agents;
    }

    public void performMorningRoutine() {
        /*
        TODO: Implement functionality:
        - Check if human is home
        - Based on the day of the week maybe do different stuff
        - Some common functionalities:
            * Start making coffee
            * Notify the user about today's weather and upcoming events
            * Open the blinds
            * Play morning playlist
            * Perform resource check (for simplicity now just check the fridge and maybe order missing items)
         */
        List<AID> receivers = new ArrayList<>();

        List<AID> fridgeAgents = getAgentListAIDForArea("FridgeAgent", "kitchen");
        AID fridgeAgent = (!fridgeAgents.isEmpty()) ? fridgeAgents.getFirst() : null;
        if (fridgeAgent == null) {
            logger.warn("Morning Routine | Fridge agent not found in kitchen");
            agentCommunicationController.sendError(getLocalName(), "Fridge agent not found in kitchen", false);
        }
        receivers.add(fridgeAgent);

        List<AID> coffeeAgents = getAgentListAIDForArea("CoffeeMachineAgent", "kitchen");
        AID coffeeAgent = (!coffeeAgents.isEmpty()) ? coffeeAgents.getFirst() : null;
        if (coffeeAgent == null) {
            logger.warn("Morning Routine | Coffee agent not found in kitchen");
            agentCommunicationController.sendError(getLocalName(), "Coffee agent not found in kitchen", false);
        }
        receivers.add(coffeeAgent);

        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        receivers.forEach(msg::addReceiver);
        msg.setConversationId("action-morning");
        sendMessage(msg);
    }

    public void toggleRandomLight(String message) {
        Area area = environmentService.getArea(message);
        if (area == null) {
            getLogger().warn("toggleRandomLight | Area was null");
            return;
        }
        AID selectedBulb = Util.getRandomEntry(physicalAgents.get(area).get("SmartLightbulbAgent"));
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(selectedBulb);
        msg.setConversationId("toggle");
        msg.setContent(Long.toString(ThreadLocalRandom.current().nextLong(3000L, 15001L)));
        sendMessage(msg);
    }
}


