package livecast.agent.repository;

import livecast.agent.worker.RoomAgent;

import java.util.*;

public class AgentRepository {
    private Map<String, RoomAgent> agentMap;

    public AgentRepository() {
        this.agentMap = new HashMap<>();
    }

    public List<String> keyList() {
        final Set<String> keySet = agentMap.keySet();
        if(keySet != null) {
            return new ArrayList<>(keySet);
        } else {
            return new ArrayList<>();
        }
    }

    public boolean exists(String key) {
        return agentMap.containsKey(key);
    }

    public int size() {
        return agentMap.size();
    }

    public RoomAgent get(String key) {
        return agentMap.get(key);
    }

    public void add(String key, RoomAgent agent) {
        agentMap.put(key, agent);
    }

    public void remove(String key) {
        agentMap.remove(key);
    }
}
