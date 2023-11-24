package livecast.agent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import livecast.agent.configuration.support.ApplicationProperties;
import livecast.agent.exception.ErrorCode;
import livecast.agent.exception.LCException;
import livecast.agent.model.*;
import livecast.agent.model.message.LCErrorReason;
import livecast.agent.model.message.LivecastMessageType;
import livecast.agent.repository.AgentRepository;
import livecast.agent.util.DateTimeUtil;
import livecast.agent.worker.LCMessageSender;
import livecast.agent.worker.LCRoomMessageSender;
import livecast.agent.worker.RoomAgent;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@Service
public class AgentService {
    private static final int SecretLen = 6;
    private static final int PinLen = 6;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    private static class JanusCreateResult {
        private boolean created;
        private String secret;
        private String pin;
    }

    private ApplicationProperties appProperties;
    private RoomService roomService;
    private RoomRecordService recordService;
    private RoomNotifier notifier;
    private ObjectMapper objectMapper;
    private LCMessageSender messageSender;
    private DateTimeUtil dateTimeUtil;
    private SimpMessagingTemplate template;
    private RestTemplate restTemplate;

    private Object lock;
    private AgentRepository roomAgentRepository;
    private AgentRepository sessionAgentRepository;

    @Autowired
    public AgentService(ApplicationProperties appProperties, RoomService roomService, RoomRecordService recordService, RoomNotifier notifier, ObjectMapper objectMapper, LCMessageSender messageSender, DateTimeUtil dateTimeUtil, SimpMessagingTemplate template, RestTemplate restTemplate) {
        this.appProperties = appProperties;
        this.roomService = roomService;
        this.recordService = recordService;
        this.notifier = notifier;
        this.objectMapper = objectMapper;
        this.messageSender = messageSender;
        this.dateTimeUtil = dateTimeUtil;
        this.template = template;
        this.restTemplate = restTemplate;

        this.lock = new Object();
        this.roomAgentRepository = new AgentRepository();
        this.sessionAgentRepository = new AgentRepository();
    }

    @Scheduled(cron="0 5,15,25,35,45,55 * * * *")
    public void checkDestroyRoomAgent() {
        log.debug("Checking destory room agent...");

        synchronized (lock) {
            final List<String> keyList = roomAgentRepository.keyList();
            for (String roomId : keyList) {
                final RoomAgent agent = roomAgentRepository.get(roomId);
                if (agent != null) {
                    log.debug("Checking room : roomId={}", roomId);

                    final boolean isJoinAnyTime = agent.isJoinAnyTime();
                    final ZoneId roomTimeZone = agent.getTimeZone();
                    final LocalDateTime roomEndTime = agent.getReserveEndDatetime();
                    final ZonedDateTime zoneNow = ZonedDateTime.now();
                    final LocalDateTime now = dateTimeUtil.toZoneLocalDateTime(zoneNow, roomTimeZone);

                    final boolean isTimeOver = isJoinAnyTime ? false : now.isAfter(roomEndTime);
                    final int connectedUserCount = agent.getConnectedUserCount();
                    final int noTimeoutWaitingUserCount = agent.getNoTimeoutWaitingUserCount();
                    log.info("RoomAgent state : roomId={}, timeOver={}, connectedUserCount={}, noTimeoutWaitingUserCount={}", roomId, isTimeOver, connectedUserCount, noTimeoutWaitingUserCount);

                    if (isTimeOver && ((connectedUserCount > 0) || (noTimeoutWaitingUserCount > 0))) {
                        log.info("Deactivating Room : roomId={}", roomId);

                        agent.deactivateRoom(LCErrorReason.TimeOver);
                    } else if ((connectedUserCount < 1) && (noTimeoutWaitingUserCount < 1)) {
                        log.info("Destroying RoomAgent : roomId={}", roomId);
                        agent.sendExitEvent(LCErrorReason.NoSuchRoom);

                        destroyRoomAgent(roomId);
                    }
                } else {
                    log.debug("Checking room : No RoomAgent, roomId={}", roomId);
                }
            }
        }
    }

    public void onUserConnected(String roomId, String sessionId, LCWaitingUser waitingUser) {
        final LCRoom room = roomService.getRoom(roomId);
        if(room != null) {
            synchronized (lock) {
                RoomAgent agent = roomAgentRepository.get(roomId);

                if (agent == null) {
//                    agent = createRoomAgent(room, waitingUser.getUserId());
                    throw new LCException(ErrorCode.CanNotFoundRoom, "Can not found room...");
                }

                sessionAgentRepository.add(sessionId, agent);
                log.debug("Session added to repository : sessionId={}, count={}", sessionId, sessionAgentRepository.size());

                agent.onUserConnected(sessionId, waitingUser);
            }
        }
    }

    public void startRoomAgent(String liveCode) {
        final LCRoom room = roomService.getRoom(liveCode);
        if(room != null) {
            synchronized (lock) {
                RoomAgent agent = roomAgentRepository.get(liveCode);

                if (agent == null) {
                    agent = createRoomAgentWithOutUser(room);
                }

                if(agent != null){
                    log.debug("createRoomAgentWithOutUser success !");
                }

//                sessionAgentRepository.add(sessionId, agent);
//                log.debug("Session added to repository : count={}", sessionAgentRepository.size());

//                agent.onUserConnected(sessionId, waitingUser); -> 분리 되어야 함.
            }
        }
    }

    public void onUserDisconnected(String sessionId) {
        synchronized (lock) {
            final RoomAgent agent = sessionAgentRepository.get(sessionId);

            if (agent != null) {
                sessionAgentRepository.remove(sessionId);
                log.debug("Session removed from repository : sessionId={}, count={}", sessionId, sessionAgentRepository.size());

                agent.onUserDisconnected(sessionId);
            }
        }
    }

    public void handleTransactionMessage(String roomId, String messageId, Object message, LivecastMessageType type, String fromSessionId, String fromLiveId) {
        synchronized (lock) {
            final RoomAgent agent = roomAgentRepository.get(roomId);

            if (agent != null) {
                agent.onTransactionMessage(messageId, message, type, fromSessionId, fromLiveId);
            } else {
                messageSender.sendTransactionErrorMessage(roomId, fromSessionId, messageId, LCErrorReason.NoSuchRoom);
            }
        }
    }


    private RoomAgent createRoomAgent(LCRoom room, String userId) {
        RoomAgent agent = roomAgentRepository.get(room.getCode());
        if(agent == null) {
            final List<LCRoomUser> roomUsers = roomService.getRoomUsers(room.getCode());
            final List<LCRoomEndpoint> endpoints = roomService.getRoomEndpoints(room.getCode());
            final String secret = RandomStringUtils.randomAlphanumeric(SecretLen);
            final String pin = RandomStringUtils.randomAlphanumeric(PinLen);

            agent = new RoomAgent(restTemplate, appProperties, roomService, recordService, new LCRoomMessageSender(template, room.getCode()), notifier, objectMapper, dateTimeUtil, room, endpoints, secret, pin, roomUsers);
            if (room.isAutoActivation()) {
                final LCRoomActivation activation = agent.activateRoom(userId);

                if(activation == null) {
                    throw new LCException(ErrorCode.CanNotActivateRoom, "RoomActivation error");
                }
            }

            roomAgentRepository.add(room.getCode(), agent);
            log.info("RoomAgent added to repository : roomId={}, count={}", room.getCode(), roomAgentRepository.size());
        }

        return agent;
    }

    private RoomAgent createRoomAgentWithOutUser(LCRoom room) {
        RoomAgent agent = roomAgentRepository.get(room.getCode());
        if(agent == null) {
            final List<LCRoomUser> roomUsers = roomService.getRoomUsers(room.getCode());
            final List<LCRoomEndpoint> endpoints = roomService.getRoomEndpoints(room.getCode());
            final String secret = RandomStringUtils.randomAlphanumeric(SecretLen);
            final String pin = RandomStringUtils.randomAlphanumeric(PinLen);

            agent = new RoomAgent(restTemplate, appProperties, roomService, recordService, new LCRoomMessageSender(template, room.getCode()), notifier, objectMapper, dateTimeUtil, room, endpoints, secret, pin, roomUsers);
            if (room.isAutoActivation()) {
//                final LCRoomActivation activation = agent.activateRoom(userId);
//
//                if(activation == null) {
//                    throw new LCException(ErrorCode.CanNotActivateRoom, "RoomActivation error");
//                }
            }

            roomAgentRepository.add(room.getCode(), agent);
            log.info("RoomAgent added to repository : roomId={}, count={}", room.getCode(), roomAgentRepository.size());
        }

        return agent;
    }

    private void destroyRoomAgent(String roomId) {
        final RoomAgent agent = roomAgentRepository.get(roomId);
        if(agent != null) {
            roomAgentRepository.remove(roomId);
            log.info("RoomAgent removed from repository : roomId={}", roomId);
        } else {
            log.info("RoomAgent already destroyed : roomId={}", roomId);
        }
    }
}
