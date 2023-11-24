package livecast.agent.controller;

import livecast.agent.service.AgentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/api/v1/rooms")
public class RoomController {
    private AgentService agentService;

    @Autowired
    public RoomController(AgentService agentService){
        this.agentService = agentService;
    }

    @GetMapping ("/{liveCode}/agent")
    public void startRoomAgent(HttpServletRequest httpRequest, @PathVariable String liveCode) {
        agentService.startRoomAgent(liveCode);
    }
}
