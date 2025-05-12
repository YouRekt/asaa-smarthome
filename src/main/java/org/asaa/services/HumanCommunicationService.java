package org.asaa.services;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import lombok.Getter;
import org.asaa.dto.ACLMessageDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static org.asaa.util.Util.ConvertStringToACLPerformative;

@Service
public class HumanCommunicationService {

    @Getter
    private final List<ACLMessage> receivedMessages = new ArrayList<ACLMessage>();

    public void receiveMessage(ACLMessageDTO aclMessageDTO) {
        ACLMessage msg = new ACLMessage(ConvertStringToACLPerformative(aclMessageDTO.performative()));
        msg.addReceiver(new AID(aclMessageDTO.aid(), true));
        msg.setContent(aclMessageDTO.message());

        receivedMessages.add(msg);
    }
}
