package org.asaa.agents.coordinators;

import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import lombok.Getter;
import org.asaa.agents.SpringAwareAgent;
import org.asaa.behaviours.coordinator.AgentScanningBehaviour;
import org.asaa.behaviours.coordinator.HandleMessageBehaviour;
import org.asaa.environment.Area;
import org.asaa.exceptions.InvalidServiceSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Getter
public final class CoordinatorAgent extends SpringAwareAgent {
    private final Map<Area, Map<String, List<AID>>> physicalAgents = new HashMap<>();
    @Getter
    private final static Logger logger = LoggerFactory.getLogger("Coordinator");

    @Override
    protected void setup() {
        super.setup();
        logger.info("Initialized");

        registerCoordinatorAgent();

        addBehaviour(new AgentScanningBehaviour(this, 5000));
        addBehaviour(new HandleMessageBehaviour(this));
    }

    private void registerCoordinatorAgent() {
        final ServiceDescription sd = new ServiceDescription();
        sd.setType(getClass().getSimpleName());
        sd.setName(getLocalName());
        sd.setOwnership(getName());

        try {
            final DFAgentDescription dfd = new DFAgentDescription();
            dfd.addServices(sd);
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            throw new InvalidServiceSpecification(e);
        }
    }

    private AID getFridgeAIDForArea(String area) {
        Area kitchenArea = environmentService.getArea(area);
        Map<String, List<AID>> agentsInArea = physicalAgents.get(kitchenArea);

        if (agentsInArea != null) {
            List<AID> fridgeAgents = agentsInArea.get("FridgeAgent");
            if (fridgeAgents != null && !fridgeAgents.isEmpty()) {
                return fridgeAgents.getFirst();
            }
        }

        return null;
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
        AID fridgeAgent = getFridgeAIDForArea("kitchen");
        if (fridgeAgent == null) {
            logger.warn("Morning Routine | Fridge agent not found in kitchen");
            return;
        }

        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(fridgeAgent);
        msg.setConversationId("get-missing-items");
        send(msg);
    }
}


