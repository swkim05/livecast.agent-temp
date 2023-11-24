package livecast.agent.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import livecast.agent.model.LCWaitingUser;
import livecast.agent.model.message.LivecastMessageType;
import livecast.agent.model.message.LivecastStringMessageContainer;
import livecast.agent.model.support.LCRoomUserType;
import livecast.agent.service.AgentService;
import livecast.agent.worker.LCMessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class AgentMessageController {
    public static final String AgentId = "agent";

    private ObjectMapper objectMapper;
    private AgentService agentService;
    private LCMessageSender messageSender;

    @Autowired
    public AgentMessageController(ObjectMapper objectMapper, AgentService agentService, LCMessageSender messageSender) {
        this.objectMapper = objectMapper;
        this.agentService = agentService;
        this.messageSender = messageSender;
    }

//    @MessageMapping("/message")
//    public void handleMessage(String message) {
//        log.info("Received message: {}", message);
//    }

    @MessageMapping("{roomId}")
    public void handleMessageContainer(LivecastStringMessageContainer messageContainer, @DestinationVariable String roomId, @Header("simpSessionId") String sessionId) {
        log.debug("Message received : roomId={}, sessionId={}, container={}", roomId, sessionId, messageContainer);

        final String to = messageContainer.getTo();
        final String from = messageContainer.getFrom();
        final String messageId = messageContainer.getMessageId();

        try {
            if ((to != null) && ((to.equals(AgentId)) || (to.equals("all")))) {
                LivecastMessageType type = messageContainer.getType();;
                Object message = null;

                if (type != null) {
                    message = objectMapper.readValue(messageContainer.getMessage(), type.getClassInfo());
                } else {
                    type = LivecastMessageType.String;
                    message = messageContainer.getMessage();
                }
                log.debug("message = {}", message);
                log.debug("type = {}", type);

                agentService.handleTransactionMessage(roomId, messageId, message, type, sessionId, from);
            }
        } catch(Exception e) {
            log.warn("Message handling error", e);

//            messageSender.sendTransactionErrorMessage(roomId, sessionId, messageId, OntactErrorReason.Unknown);
        }
    }

    @EventListener(classes = {SessionConnectedEvent.class})
    public void handleSessionConnectedEvent(SessionConnectedEvent event) {
        log.info("handleSessionConnectedEvent !");
        try {
            final MessageHeaderAccessor connectHeaders = MessageHeaderAccessor.getAccessor((GenericMessage) event.getMessage().getHeaders().get("simpConnectMessage"), MessageHeaderAccessor.class);
            String sessionId = null;
            String remoteAddr = null;
            String roomId = null;
            String appId = null;
            String userId = null;
            String userName = null;
            String userAgent = null;
            String userType = null;

            try {
                sessionId = (String) connectHeaders.getHeader("simpSessionId");
                log.debug("sessionId = {} : ", sessionId);
            } catch(Exception e) {
                log.warn("Can not found session id", e);
            }

            try {
                final Map<String, String> attributes = (Map<String, String>) connectHeaders.getHeader("simpSessionAttributes");
                remoteAddr = attributes.get("remoteAddr");
                log.debug("sessionId = {} : ", remoteAddr);
            } catch(Exception e) {
                log.warn("Can not found remote address in attributes", e);
            }

            try {
                final Map<String, List<String>> nativeHeaders = (Map<String, List<String>>) connectHeaders.getHeader("nativeHeaders");

                try {
                    roomId = nativeHeaders.get("roomId").get(0);
                } catch (Exception e) {
                    log.warn("Can not found roomId", e);
                }
                try {
                    appId = nativeHeaders.get("roomUserAppId").get(0);
                } catch(Exception e) {
                    log.warn("Can not found user app id in headers", e);
                }
                try {
                    userId = nativeHeaders.get("roomUserId").get(0);
                } catch(Exception e) {
                    log.warn("Can not found user id in headers", e);
                }
                try {
                    userName = nativeHeaders.get("roomUserName").get(0);
                } catch (Exception e) {
                    log.warn("Can not found user name in headers", e);
                }
                try {
                    userAgent = nativeHeaders.get("roomUserAgent").get(0);
                } catch(Exception e) {
                    log.warn("Can not found user agent in headers", e);
                }
                try {
                    userType = nativeHeaders.get("userType").get(0);
                } catch(Exception e) {
                    log.warn("Can not found user agent in headers", e);
                }
            } catch(Exception e) {
                log.warn("Can not found native headers", e);
            }

            final LCWaitingUser user = LCWaitingUser.builder()
                    .sessionId(sessionId)
                    .appId(appId)
                    .userId(userId)
                    .name(userName)
                    .userAgent(userAgent)
                    .joinable(false)
                    .type(LCRoomUserType.valueOf(userType))
                    .connectDatetime(LocalDateTime.now())
                    .build();

            log.debug("SessionConnected : remoteAddr={}, sessionId={}, roomId={}, user={}", remoteAddr, sessionId, roomId, user);
            try {
//                connectionHistoryService.onUserConnected(LocalDateTime.now(), sessionId, roomId, user, remoteAddr);
            } catch(Exception e) {
                log.warn("SessionConnected but can not logging", e);
            }

            if((sessionId != null) && (roomId != null) && (userId != null)) {
                agentService.onUserConnected(roomId, sessionId, user);
            } else {
                log.warn("SessionConnected but can not found information");
            }
        } catch(Exception e) {
            log.debug("SessionConnected error", e);
        }
    }

    @EventListener(classes = {SessionDisconnectEvent.class })
    public void handleSessionDisconnectEvent(SessionDisconnectEvent event) {
        try {
            final String sessionId = event.getSessionId();
            final CloseStatus closeStatus = event.getCloseStatus();

            log.debug("SessionDisconnected : sessionId={}, closeCode={}", sessionId, closeStatus.getCode());
            try {
//                connectionHistoryService.onUserDisconnected(LocalDateTime.now(), sessionId, closeStatus.getCode());
            } catch (Exception e) {
                log.warn("SessionDisconnected but can not logging", e);
            }

            agentService.onUserDisconnected(sessionId);
        } catch(Exception e){
            log.warn("SessionDisconnected error", e);
        }
    }
}
