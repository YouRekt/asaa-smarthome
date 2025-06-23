package org.asaa.util;

import jade.lang.acl.ACLMessage;

import java.util.*;

public class Util {
    public static String ContainerIP = null;

    public static String ConvertACLPerformativeToString(int performative) {
        return switch (performative) {
            case ACLMessage.ACCEPT_PROPOSAL -> "ACCEPT_PROPOSAL";
            case ACLMessage.AGREE -> "AGREE";
            case ACLMessage.CANCEL -> "CANCEL";
            case ACLMessage.CFP -> "CFP";
            case ACLMessage.CONFIRM -> "CONFIRM";
            case ACLMessage.DISCONFIRM -> "DISCONFIRM";
            case ACLMessage.FAILURE -> "FAILURE";
            case ACLMessage.INFORM -> "INFORM";
            case ACLMessage.INFORM_IF -> "INFORM_IF";
            case ACLMessage.INFORM_REF -> "INFORM_REF";
            case ACLMessage.NOT_UNDERSTOOD -> "NOT_UNDERSTOOD";
            case ACLMessage.PROPOSE -> "PROPOSE";
            case ACLMessage.QUERY_IF -> "QUERY_IF";
            case ACLMessage.QUERY_REF -> "QUERY_REF";
            case ACLMessage.REFUSE -> "REFUSE";
            case ACLMessage.REJECT_PROPOSAL -> "REJECT_PROPOSAL";
            case ACLMessage.REQUEST -> "REQUEST";
            case ACLMessage.REQUEST_WHEN -> "REQUEST_WHEN";
            case ACLMessage.REQUEST_WHENEVER -> "REQUEST_WHENEVER";
            case ACLMessage.SUBSCRIBE -> "SUBSCRIBE";
            case ACLMessage.PROXY -> "PROXY";
            case ACLMessage.PROPAGATE -> "PROPAGATE";
            case ACLMessage.UNKNOWN -> "UNKNOWN";
            default -> "ERR_UNK_PERF";
        };
    }

    public static int ConvertStringToACLPerformative(String performative) {
        return switch (performative) {
            case "ACCEPT_PROPOSAL" -> ACLMessage.ACCEPT_PROPOSAL;
            case "AGREE" -> ACLMessage.AGREE;
            case "CANCEL" -> ACLMessage.CANCEL;
            case "CFP" -> ACLMessage.CFP;
            case "CONFIRM" -> ACLMessage.CONFIRM;
            case "DISCONFIRM" -> ACLMessage.DISCONFIRM;
            case "FAILURE" -> ACLMessage.FAILURE;
            case "INFORM" -> ACLMessage.INFORM;
            case "INFORM_IF" -> ACLMessage.INFORM_IF;
            case "INFORM_REF" -> ACLMessage.INFORM_REF;
            case "NOT_UNDERSTOOD" -> ACLMessage.NOT_UNDERSTOOD;
            case "PROPOSE" -> ACLMessage.PROPOSE;
            case "QUERY_IF" -> ACLMessage.QUERY_IF;
            case "QUERY_REF" -> ACLMessage.QUERY_REF;
            case "REFUSE" -> ACLMessage.REFUSE;
            case "REJECT_PROPOSAL" -> ACLMessage.REJECT_PROPOSAL;
            case "REQUEST" -> ACLMessage.REQUEST;
            case "REQUEST_WHEN" -> ACLMessage.REQUEST_WHEN;
            case "REQUEST_WHENEVER" -> ACLMessage.REQUEST_WHENEVER;
            case "SUBSCRIBE" -> ACLMessage.SUBSCRIBE;
            case "PROXY" -> ACLMessage.PROXY;
            case "PROPAGATE" -> ACLMessage.PROPAGATE;
            default -> ACLMessage.UNKNOWN;
        };
    }

    public static <K, V> Map.Entry<K, V> getRandomEntry(Map<K, V> map) {
        if (map.isEmpty())
            return null;

        List<Map.Entry<K, V>> entries = new ArrayList<>(map.entrySet());
        int randomIndex = new Random().nextInt(entries.size());
        return entries.get(randomIndex);
    }

    public static <T> T getRandomEntry(List<T> list) {
        if (list == null || list.isEmpty())
            return null;

        int randomIndex = new Random().nextInt(list.size());
        return list.get(randomIndex);
    }

    public static class IteratorAdapter<T> implements Iterator<T> {
        private final jade.util.leap.Iterator it;
        private final Class<T> type;

        public IteratorAdapter(jade.util.leap.Iterator it, Class<T> type) {
            this.it = it;
            this.type = type;
        }

        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        @Override
        public T next() {
            Object obj = it.next();
            if (!type.isInstance(obj)) {
                throw new ClassCastException("Expected type: " + type + ", but got: " + obj.getClass());
            }
            return type.cast(obj);
        }
    }
}
