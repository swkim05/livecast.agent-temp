package livecast.agent.worker;

import livecast.agent.model.*;
import livecast.agent.model.message.LCErrorReason;
import livecast.agent.model.message.LivecastMessageType;
import livecast.agent.model.message.agent.*;
import livecast.agent.model.support.LCForbiddenWord;
import livecast.agent.model.support.LCRoomRecordState;
import livecast.agent.model.support.LCViewType;
import livecast.agent.worker.support.ModelUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

public class LCRoomMessageSender extends LCMessageSender {
    private static final Logger log = LoggerFactory.getLogger(LCRoomMessageSender.class);


    private ModelUtil modelConverter;
    private String roomId;

    public LCRoomMessageSender(SimpMessagingTemplate template, String roomId) {
        super(template);
        this.modelConverter = new ModelUtil();
        this.roomId = roomId;
    }

    public void sendEvent(Object message, LivecastMessageType messageType) {
        sendEventMessage(roomId, message, messageType);
    }

    public void sendJoinResponse(String sessionId, String messageId, LCErrorReason reason) {
        sendJoinResponse(sessionId, messageId, reason, null, null, null, null, null, null, null, null);
    }

    public void sendJoinResponse(String sessionId, String messageId, LCErrorReason reason,
                                 LCRoom room, String roomPin, LCLiveUser liveUser, List<LCLiveUser> liveUsers, List<LCForbiddenWord> forbiddenWords, LCRoomState roomState, LCViewType viewType, Object viewState) {
        final LCMessageJoinResponse message = LCMessageJoinResponse.builder()
                .accepted(reason.equals(LCErrorReason.OK))
                .reason(reason)
                .room(room)
                .roomPin(roomPin)
                .liveUser(liveUser)
                .liveUsers(liveUsers)
                .forbiddenWords(forbiddenWords)
                .roomState(roomState)
                .viewType(viewType)
                .viewState(viewState)
                .build();

        sendTransactionMessage(roomId, sessionId, messageId, message, LivecastMessageType.JoinResponse);
    }

    public void sendOperationResult(String sessionId, String messageId, LCErrorReason reason) {
        final LCMessageOperationResult message = LCMessageOperationResult.builder()
                .roomId(roomId)
                .success((reason != null) && (reason.equals(LCErrorReason.OK)) ? true : false)
                .reason(reason != null ? reason : LCErrorReason.Unknown)
                .build();

        sendTransactionMessage(roomId, sessionId, messageId, message, LivecastMessageType.OperationResult);
    }

    public void sendWaintingUsersEventToOperatingUser(List<LCWaitingUser> waitingUsers, List<LCLiveUser> liveUsers) {
        final LCMessageWaitingUsersEvent message = LCMessageWaitingUsersEvent.builder()
                .roomId(roomId)
                .users(waitingUsers)
                .build();

        liveUsers.stream()
                .filter(user -> user.isJoined() && modelConverter.isOperatingUser(user.getType()))
                .forEach(user ->
                        sendEventMessage(roomId, user.getSessionId(), message, LivecastMessageType.WaitingUsersEvent)
                );
    }

    public void sendJoinableEvent() {
        final LCMessageJoinableEvent message = LCMessageJoinableEvent.builder()
                .roomId(roomId)
                .build();

        sendEventMessage(roomId, message, LivecastMessageType.JoinableEvent);
    }

    public void sendJoinableEvent(String sessionId) {
        final LCMessageJoinableEvent message = LCMessageJoinableEvent.builder()
                .roomId(roomId)
                .build();

        sendEventMessage(roomId, sessionId, message, LivecastMessageType.JoinableEvent);
    }

    public void sendExitEvent(LCErrorReason reason) {
        final LCMessageExitEvent message = LCMessageExitEvent.builder()
                .roomId(roomId)
                .reason(reason)
                .build();

        sendEventMessage(roomId, message, LivecastMessageType.ExitEvent);
    }

    public void sendExitEvent(String sessionId, LCErrorReason reason) {
        final LCMessageExitEvent message = LCMessageExitEvent.builder()
                .roomId(roomId)
                .reason(reason)
                .build();

        sendEventMessage(roomId, sessionId, message, LivecastMessageType.ExitEvent);
    }

    public void sendUserAddedEvent(LCLiveUser liveUser) {
        final LCMessageUserAddedEvent message = LCMessageUserAddedEvent.builder()
                .roomId(roomId)
                .liveUser(liveUser)
                .build();

        sendEventMessage(roomId, message, LivecastMessageType.UserAddedEvent);
    }

    public void sendUserJoinedEvent(LCLiveUser liveUser) {
        final LCMessageUserJoinedEvent message = LCMessageUserJoinedEvent.builder()
                .roomId(roomId)
                .liveUser(liveUser)
                .build();

        sendEventMessage(roomId, message, LivecastMessageType.UserJoinedEvent);
    }

    public void sendUserExitedEvent(LCLiveUser liveUser) {
        final LCMessageUserExitedEvent message = LCMessageUserExitedEvent.builder()
                .roomId(roomId)
                .liveUser(liveUser)
                .build();

        sendEventMessage(roomId, message, LivecastMessageType.UserExitedEvent);
    }

    public void sendChatMessageEvent(LCLiveUser fromLiveUser, long chatNum, String chatMessage) {
        final LCMessageChatMessageEvent message = LCMessageChatMessageEvent.builder()
                .fromLiveId(fromLiveUser.getLiveId())
                .chatNum(chatNum)
                .message(chatMessage)
                .build();

        sendEventMessage(roomId, message, LivecastMessageType.ChatMessageEvent);
    }

    public void sendRoomStateChangedEvent(LCRoomState state) {
        final LCMessageRoomStateChangedEvent message = LCMessageRoomStateChangedEvent.builder()
                .roomId(roomId)
                .state(state)
                .build();

        sendEventMessage(roomId, message, LivecastMessageType.RoomStateChangedEvent);
    }

    public void sendViewStateChangedEvent(LCViewType viewType, Object viewState) {
        final LCMessageViewStateChangedEvent message = LCMessageViewStateChangedEvent.builder()
                .roomId(roomId)
                .viewType(viewType)
                .viewState(viewState)
                .build();

        sendEventMessage(roomId, message, LivecastMessageType.ViewStateChangedEvent);
    }

    public void sendUserStateChangedEvent(LCLiveUser liveUser) {
        final LCMessageUserStateChangedEvent message = LCMessageUserStateChangedEvent.builder()
                .liveId(liveUser.getLiveId())
                .state(LCUserState.builder()
                        .muted(liveUser.isMuted())
                        .handUp(liveUser.isHandUp())
                        .canChat(liveUser.isCanChat())
                        .blocked(liveUser.isBlocked())
                        .build())
                .build();

        sendEventMessage(roomId, message, LivecastMessageType.UserStateChangedEvent);
    }

    public void sendUserMediaStateChangedEvent(LCLiveUser liveUser) {
        final LCMessageUserMediaStateChangedEvent message = LCMessageUserMediaStateChangedEvent.builder()
                .liveId(liveUser.getLiveId())
                .state(liveUser.getMediaState())
                .build();

        sendEventMessage(roomId, message, LivecastMessageType.UserMediaStateChangedEvent);
    }

//    public void sendUserCamStateChangedEvent(OntactLiveUser liveUser) {
//        final OntactMessageUserCamStateChangedEvent message = OntactMessageUserCamStateChangedEvent.builder()
//                .liveId(liveUser.getLiveId())
//                .state(liveUser.getCamState())
//                .build();
//
//        sendEventMessage(roomId, message, LivecastMessageType.UserCamStateChangedEvent);
//    }
//
//    public void sendUserMicStateChangedEvent(OntactLiveUser liveUser) {
//        final OntactMessageUserMicStateChangedEvent message = OntactMessageUserMicStateChangedEvent.builder()
//                .liveId(liveUser.getLiveId())
//                .state(liveUser.getMicState())
//                .build();
//
//        sendEventMessage(roomId, message, LivecastMessageType.UserMicStateChangedEvent);
//    }
//
//    public void sendUserAudioStateChangedEvent(OntactLiveUser liveUser) {
//        final OntactMessageUserAudioStateChangedEvent message = OntactMessageUserAudioStateChangedEvent.builder()
//                .liveId(liveUser.getLiveId())
//                .state(liveUser.getAudioState())
//                .build();
//
//        sendEventMessage(roomId, message, LivecastMessageType.UserAudioStateChangedEvent);
//    }

    public void sendUserChatDeleteEvent(String from, String num) {
        final LCMessageUserChatDeleteEvent message = LCMessageUserChatDeleteEvent.builder()
                .from(from)
                .num(num)
                .build();

        sendEventMessage(roomId, message, LivecastMessageType.UserChatDeleteEvent);
    }

    public void sendRecordingEvent(int activationId, int recordId, LCRoomRecordState state, LocalDateTime datetime, ZoneId timeZone, String downloadUrl) {
        final LCMessageRecordingEvent message = LCMessageRecordingEvent.builder()
                .roomId(roomId)
                .activationId(activationId)
                .recordId(recordId)
                .state(state)
                .datetime(datetime)
                .timeZone(timeZone)
                .downloadUrl(downloadUrl)
                .build();

        sendEventMessage(roomId, message, LivecastMessageType.RecordingEvent);
    }
}
