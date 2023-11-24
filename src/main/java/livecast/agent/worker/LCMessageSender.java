package livecast.agent.worker;

import livecast.agent.controller.AgentMessageController;
import livecast.agent.model.message.LCErrorReason;
import livecast.agent.model.message.LivecastMessageType;
import livecast.agent.model.message.LivecastObjectMessageContainer;
import livecast.agent.model.message.agent.LCMessageErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class LCMessageSender {
    private static final Logger log = LoggerFactory.getLogger(LCMessageSender.class);
    private static final String NoTransactionMessageId = "none";

    private SimpMessagingTemplate template;

    public LCMessageSender(SimpMessagingTemplate template) {
        this.template = template;
    }

    public void sendEventMessage(String roomId, Object message, LivecastMessageType type) {
        final String destination = "/topic/" + roomId;
        final LivecastObjectMessageContainer container = LivecastObjectMessageContainer.builder()
                .from(AgentMessageController.AgentId)
                .type(type)
                .messageId(NoTransactionMessageId)
                .message(message)
                .build();

        log.debug("SendingEventMessage : destination={}, container={}", destination, container);
        this.template.convertAndSend(destination, container);
    }

    public void sendEventMessage(String roomId, String sessionId, Object message, LivecastMessageType type) {
        final String destination = "/topic/" + roomId;
        final LivecastObjectMessageContainer container = LivecastObjectMessageContainer.builder()
                .from(AgentMessageController.AgentId)
                .type(type)
                .messageId(NoTransactionMessageId)
                .message(message)
                .build();

        log.debug("SendingEventMessage : sessionId={}, destination={}, container={}", sessionId, destination, container);
        this.template.convertAndSendToUser(sessionId, destination, container, createTransactionHeaders(sessionId));
    }

    public void sendTransactionMessage(String roomId, String sessionId, String messageId, Object message, LivecastMessageType type) {
        final String destination = "/topic/" + roomId;
        final LivecastObjectMessageContainer container = LivecastObjectMessageContainer.builder()
                .from(AgentMessageController.AgentId)
                .type(type)
                .messageId(messageId)
                .message(message)
                .build();

        log.debug("SendingTransactionMessage : sessionId={}, destination={}, container={}", sessionId, destination, container);
        this.template.convertAndSendToUser(sessionId, destination, container, createTransactionHeaders(sessionId));
    }

    public void sendTransactionErrorMessage(String roomId, String sessionId, String messageId, LCErrorReason reason) {
        final String destination = "/topic/" + roomId;
        final LivecastObjectMessageContainer container = LivecastObjectMessageContainer.builder()
                .from(AgentMessageController.AgentId)
                .type(LivecastMessageType.ErrorResponse)
                .messageId(messageId)
                .message(new LCMessageErrorResponse(reason))
                .build();

        log.debug("SendingErrorMessage : sessionId={}, destination={}, container={}", sessionId, destination, container);
        this.template.convertAndSendToUser(sessionId, destination, container, createTransactionHeaders(sessionId));
    }

    private MessageHeaders createTransactionHeaders(String sessionId) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        return headerAccessor.getMessageHeaders();
    }
}
