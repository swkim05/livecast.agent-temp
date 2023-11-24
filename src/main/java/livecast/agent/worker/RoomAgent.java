package livecast.agent.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import livecast.agent.configuration.support.ApplicationProperties;
import livecast.agent.model.*;
import livecast.agent.model.message.LCErrorReason;
import livecast.agent.model.message.LivecastMessageType;
import livecast.agent.model.message.client.*;
import livecast.agent.model.srs.SRSStreamInfo;
import livecast.agent.model.srs.SRSStreamsResponse;
import livecast.agent.model.support.*;
import livecast.agent.service.RoomNotifier;
import livecast.agent.service.RoomRecordService;
import livecast.agent.service.RoomService;
import livecast.agent.srs.SRSHttpApiUtil;
import livecast.agent.util.DateTimeUtil;
import livecast.agent.worker.manager.LCViewManager;
import livecast.agent.worker.manager.LivecastViewManager;
import livecast.agent.worker.support.JoinResult;
import livecast.agent.worker.support.ModelUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class RoomAgent {
    private static final Logger log = LoggerFactory.getLogger(RoomAgent.class);

    private Object lock;
    private int liveNumber;
    private Map<String, LCWaitingUser> waitingUserSessionMap;

    private ModelUtil modelUtil;
    private ApplicationProperties appProperties;
    private RoomService roomService;
    private RoomRecordService recordService;
    private LCRoomMessageSender messageSender;
    private RoomNotifier notifier;
    private ObjectMapper objectMapper;
    private DateTimeUtil dateTimeUtil;
    private Map<LivecastMessageType, LCMessageHandler> handlerMap;

    private SRSHttpApiUtil apiUtil;

    // 기본 설정 관련
    private List<LCRoomUser> roomUsers;
    private List<LCRoomEndpoint> roomEndpoints;
    private LCRoom room;
    private String roomSecret;
    private String roomPin;
    private List<LCLiveUser> liveUsers;
    private List<LCViewType> views;
    private List<LCForbiddenWord> forbiddenWords;

    // 현재 상태 관련
    private LCRoomState roomState;
    private long chatNum;

    // 뷰 관련
    private Map<LCViewType, LCViewManager> viewManagerMap;
    private LCViewType currentViewType;

    private interface LCMessageHandler {
        void handleMessage(String messageId, Object msgObj, String sessionId, LCLiveUser liveUser);
    }

    public RoomAgent(RestTemplate restTemplate, ApplicationProperties appProperties, RoomService roomService, RoomRecordService recordService, LCRoomMessageSender messageSender, RoomNotifier notifier, ObjectMapper objectMapper, DateTimeUtil dateTimeUtil, LCRoom room, List<LCRoomEndpoint> roomEndpoints, String secret, String pin, List<LCRoomUser> roomUsers) {
        this.lock = new Object();
        this.waitingUserSessionMap = new HashMap<>();

        this.modelUtil = new ModelUtil();
        this.appProperties = appProperties;
        this.roomService = roomService;
        this.recordService = recordService;
        this.messageSender = messageSender;
        this.notifier = notifier;
        this.objectMapper = objectMapper;
        this.dateTimeUtil = dateTimeUtil;
        this.handlerMap = new HashMap<>();
        this.configureMessageHandler();
        this.initApiUtil(restTemplate, roomEndpoints);

        this.roomUsers = roomUsers;
        this.roomEndpoints = roomEndpoints;
//        this.room = modelUtil.convertToRoom(room);
        this.room = room;
        this.roomSecret = secret;
        this.roomPin = pin;
        this.liveUsers = modelUtil.convertToLiveUsers(roomUsers).stream()
                .filter((user) -> modelUtil.isStaticLiveUser(user.getType()))
                .collect(Collectors.toList());
        this.views = Arrays.asList(LCViewType.values());
        this.forbiddenWords = new ArrayList<>();

        this.liveNumber = 0;
        this.roomState = LCRoomState.builder()
                .activated(false)
                .activationId(-1)
                .freeJoin(room.isAutoActivation())
                .started(false)
                .startDatetime(null)
                .timeZone(room.getTimeZone())
                .recordState(LCRoomRecordState.None)
                .recordId(-1)
                .recordStartDatetime(null)
                .recordEndDatetime(null)
                .recordTimeZone(room.getTimeZone())
                .build();
        this.chatNum = 1;

        initViewManager();
    }

    private void initApiUtil(RestTemplate restTemplate, List<LCRoomEndpoint> roomEndpoints) {
        final Optional<LCRoomEndpoint> rtcApiEndpointOptional = roomEndpoints.stream().filter(ep -> (ep.getType() != null) && (ep.getType().equals(LCRoomEndpointType.RTCApi))).findFirst();
        if(rtcApiEndpointOptional.isPresent()) {
            final LCRoomEndpoint rtcApiEndpoint = rtcApiEndpointOptional.get();
            this.apiUtil = new SRSHttpApiUtil(restTemplate, rtcApiEndpoint.getEndpoint());
        } else {
            this.apiUtil = null;
        }
    }

    private void initViewManager() {
        final LivecastViewManager onandonViewManager = new LivecastViewManager(LCViewType.OnAndOn, room.getCode(), false, liveUsers, modelUtil, messageSender);
        this.viewManagerMap = new HashMap<>();
        this.viewManagerMap.put(LCViewType.OnAndOn, onandonViewManager);

        changeView(LCViewType.OnAndOn);
    }

    private LCViewManager getCurrentViewManager() {
        return viewManagerMap.get(currentViewType);
    }

    private Object getCurrentViewState() {
        final LCViewManager viewManager = viewManagerMap.get(currentViewType);
        if(viewManager != null) {
            return viewManager.getViewState();
        } else {
            return null;
        }
    }

    private List<LCViewManager> getViewManagers() {
        return viewManagerMap.values().stream().collect(Collectors.toList());
    }

    private boolean canEnterTime() {
        final ZoneId timeZone = room.getTimeZone();
        final LocalDateTime startTime = room.getRealStartDt().toLocalDateTime();
        final LocalDateTime endTime = room.getRealEndDt().toLocalDateTime();

        final ZonedDateTime zoneNow = ZonedDateTime.now();
        final LocalDateTime now = dateTimeUtil.toZoneLocalDateTime(zoneNow, timeZone);

        return now.isAfter(startTime) && now.isBefore(endTime);
    }

    private int countJoinedUser() {
        long count = liveUsers.stream().filter(u -> u.isJoined()).count();

        if(count > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        } else if(count < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        } else {
            return (int) count;
        }
    }

    private int countJoinedUser(LCRoomUserType... userTypes) {
        long count = liveUsers.stream().filter(u -> {
            final List<LCRoomUserType> userTypeList = Arrays.asList(userTypes);
            final long matchedUserTypeCount = userTypeList.stream().filter(t -> (u.getType() != null) && (u.getType().equals(t))).count();

            return u.isJoined() && (matchedUserTypeCount > 0);
        }).count();

        if(count > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        } else if(count < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        } else {
            return (int) count;
        }
    }

    public String getSecret() {
        return roomSecret;
    }

    public boolean isJoinAnyTime() {
        if(room != null) {
//            return room.isJoinAnyTime();
            return true;
        } else {
            return false;
        }
    }

    public ZoneId getTimeZone() {
        if(room != null) {
            return room.getTimeZone();
        } else {
            return null;
        }
    }
    public LocalDateTime getReserveEndDatetime() {
        if(room != null) {
            return room.getRealEndDt().toLocalDateTime();
        } else {
            return null;
        }
    }

    public int getConnectedUserCount() {
        synchronized (lock) {
            return countJoinedUser(LCRoomUserType.Owner, LCRoomUserType.Member, LCRoomUserType.Guest, LCRoomUserType.Operator);
        }
    }

    public int getNoTimeoutWaitingUserCount() {
        synchronized (lock) {
            int count = 0;

            final long timeoutMillis = appProperties.getMaxWaitingTimeout();
            final LocalDateTime now = LocalDateTime.now();
            for(Map.Entry<String, LCWaitingUser> entry : waitingUserSessionMap.entrySet()) {
                final LCWaitingUser waitingUser = entry.getValue();
                if(waitingUser != null) {
                    final LocalDateTime connectDatetime = waitingUser.getConnectDatetime();
                    if(connectDatetime != null) {
                        long amount = connectDatetime.until(now, ChronoUnit.MILLIS);
                        if(amount < timeoutMillis) {
                            count++;
                        }
                    }
                }
            }

            return count;
        }
    }

    public LCRoomActivation activateRoom(String userId) {
        synchronized (lock) {
            return doActivateRoom(userId, true);
        }
    }

    public void deactivateRoom(LCErrorReason reason) {
        synchronized (lock) {
            doDeactivateRoom(reason);
        }
    }

    public void onUserConnected(String sessionId, LCWaitingUser user) {
        synchronized (lock) {
            handleUserConnected(sessionId, user);
        }
    }

    public void onUserDisconnected(String sessionId) {
        synchronized (lock) {
            handleUserDisconnected(sessionId);
        }
    }

    public void sendExitEvent(LCErrorReason reason) {
        messageSender.sendExitEvent(reason);
    }

    public void onTransactionMessage(String messageId, Object message, LivecastMessageType type, String fromSessionId, String fromLiveId) {
        synchronized (lock) {
            log.debug("Received a room transaction message : roomCode={}, session={}, type={}, messageId={}, message={}", room.getCode(), fromSessionId, type, messageId, message);

            // 아무때나 입장가능하거나, 입장 가능 시각일때...
//            if(room.isJoinAnyTime() || canEnterTime()) {
            if(true || canEnterTime()) {
                final Optional<LCLiveUser> liveUserOptional = liveUsers.stream().filter((u) -> u.isJoined() && (u.getSessionId() != null) && (u.getSessionId().equals(fromSessionId))).findFirst();

                if(liveUserOptional.isPresent()) { // liveUsers -> static user(멤버등 정해진 참가자.) list. 이미 isJoined 인 static 유저의 요청일때.
                    final LCLiveUser liveUser = liveUserOptional.get();

                    if((liveUser.getLiveId() != null) && (liveUser.getLiveId().equals(fromLiveId))) {
                        processTransaction(messageId, message, type, fromSessionId, liveUser);
                    } else {
                        log.warn("BadRequest : roomId={}, sessionId={}, liveId={}", room.getCode(), fromSessionId, fromLiveId);
                        messageSender.sendTransactionErrorMessage(room.getCode(), fromSessionId, messageId, LCErrorReason.BadRequest);
                    }
                } else { // 최초 join 은 이 아래.
                    if(type.isJoinRequired()) {
                        log.warn("JoinRequired : roomId={}, sessionId={}, liveId={}", room.getCode(), fromSessionId, fromLiveId);
                        messageSender.sendTransactionErrorMessage(room.getCode(), fromSessionId, messageId, LCErrorReason.YouAreNotJoined);
                    } else { // 최초 join 요청시 여기를 타도록 되어있다.
                        processTransaction(messageId, message, type, fromSessionId, null);
                    }
                }
            } else {
                log.warn("NoTimeToEnter : roomId={}, sessionId={}, liveId={}", room.getCode(), fromSessionId, fromLiveId);
                messageSender.sendTransactionErrorMessage(room.getCode(), fromSessionId, messageId, LCErrorReason.NoTimeToEnter);
            }
        }
    }

    private void processTransaction(String messageId, Object message, LivecastMessageType type, String fromSessionId, LCLiveUser liveUser) {
        final boolean hasOpRole = (liveUser != null) && (modelUtil.isOperatingUser(liveUser.getType()));

        // Activation이 필요없거나, 방이 Activation 된 경우...
        if((!type.isRoomActivationRequired()) || this.roomState.isActivated()) {
            // 권한이 필요한 명령이 아니거나, 유저가 권한이 있으면...
            if ((!type.isOperatingRoleRequired()) || (hasOpRole)) {
                final LCMessageHandler messageHandler = handlerMap.get(type);

                if (messageHandler != null) {
                    log.debug("HandleMessage : roomId={}, message={}", room.getCode(), message);
                    messageHandler.handleMessage(messageId, message, fromSessionId, liveUser);
                } else {
                    log.warn("NotSupportedMessage : roomId={}, messageType={}, sessionId={}", room.getCode(), type, fromSessionId);
                    messageSender.sendTransactionErrorMessage(room.getCode(), fromSessionId, messageId, LCErrorReason.NotSupportedMessage);
                }
            } else {
                log.warn("NoAuthority : roomId={}, messageType={}, sessionId={}", room.getCode(), type, fromSessionId);
                messageSender.sendTransactionErrorMessage(room.getCode(), fromSessionId, messageId, LCErrorReason.YouCanNot);
            }
        } else {
            log.warn("NotActivatedRoom : roomId={}, messageType={}, sessionId={}", room.getCode(), type, fromSessionId);
            messageSender.sendTransactionErrorMessage(room.getCode(), fromSessionId, messageId, LCErrorReason.NotActivated);
        }

    }

    private void configureMessageHandler() {
        handlerMap.put(LivecastMessageType.JoinRequest, (messageId, msgObj, sessionId, liveUser) -> {
            final LCMessageJoinRequest message = (LCMessageJoinRequest) msgObj;
            log.debug("waitingUserSessionMap : {}", waitingUserSessionMap);
            log.debug("sessionId : {}", sessionId);
            final LCWaitingUser waiting = waitingUserSessionMap.get(sessionId);
            log.debug("waiting : {}", waiting);

            // 접속 기록이 있으면...
            if(waiting != null) {
                // 접속 가능한지 확인
                if (waiting.isJoinable()) {
                    log.debug("handleUserJoin start");
                    final JoinResult result = handleUserJoin(waiting, message.getEntranceCode(), message.getName(), message.isForce());
                    if(result != null) {
                        if((result.getReason() != null) && (result.getReason().equals(LCErrorReason.OK))) {
                            messageSender.sendJoinResponse(sessionId, messageId, result.getReason(), room, roomPin, result.getLiveUser(), liveUsers, forbiddenWords, roomState, currentViewType, getCurrentViewState());
                        } else {
                            messageSender.sendJoinResponse(sessionId, messageId, result.getReason());
                        }
                    } else {
                        messageSender.sendJoinResponse(sessionId, messageId, LCErrorReason.Unknown);
                    }
                } else {
                    messageSender.sendJoinResponse(sessionId, messageId, LCErrorReason.YouCanNotJoinYet);
                }
            } else {
                messageSender.sendJoinResponse(sessionId, messageId, LCErrorReason.NoSuchUser);
            }
        });
        handlerMap.put(LivecastMessageType.UserHandUpRequest, (messageId, msgObj, sessionId, liveUser) -> {
            final LCMessageUserHandUpRequest message = (LCMessageUserHandUpRequest) msgObj;

            if((liveUser.getLiveId() != null) && (message.getLiveId() != null) && (liveUser.getLiveId().equals(message.getLiveId()))) {
                this.updateUserHandUp(liveUser, message.isHandUp());

                messageSender.sendOperationResult(sessionId, messageId, LCErrorReason.OK);
            } else {
                messageSender.sendOperationResult(sessionId, messageId, LCErrorReason.BadRequest);
            }

        });
        handlerMap.put(LivecastMessageType.UserPresentationRequest, (messageId, msgObj, sessionId, liveUser) -> {
            final LCMessageUserPresentationRequest message = (LCMessageUserPresentationRequest) msgObj;
            final Optional<LCLiveUser> muteUserOptional = liveUsers.stream().filter(u -> (u.getLiveId() != null) && (u.getLiveId().equals(message.getLiveId()))).findFirst();

            if(muteUserOptional.isPresent()) {
                this.updateUserPresentation(muteUserOptional.get(), message.isPresentation());

                messageSender.sendOperationResult(sessionId, messageId, LCErrorReason.OK);
            } else {
                messageSender.sendOperationResult(sessionId, messageId, LCErrorReason.NoSuchUser);
            }

        });
        handlerMap.put(LivecastMessageType.UserCanChatRequest, (messageId, msgObj, sessionId, liveUser) -> {
            final LCMessageUserCanChatRequest message = (LCMessageUserCanChatRequest) msgObj;
            final Optional<LCLiveUser> userOptional = liveUsers.stream().filter(u -> (u.getLiveId() != null) && (u.getLiveId().equals(message.getLiveId()))).findFirst();

            if(userOptional.isPresent()) {
                this.updateUserCanChat(userOptional.get(), message.isCanChat());

                messageSender.sendOperationResult(sessionId, messageId, LCErrorReason.OK);
            } else {
                messageSender.sendOperationResult(sessionId, messageId, LCErrorReason.NoSuchUser);
            }
        });
        handlerMap.put(LivecastMessageType.UserBlockRequest, (messageId, msgObj, sessionId, liveUser) -> {
            final LCMessageUserBlockRequest message = (LCMessageUserBlockRequest) msgObj;
            final Optional<LCLiveUser> userOptional = liveUsers.stream().filter(u -> (u.getLiveId() != null) && (u.getLiveId().equals(message.getLiveId()))).findFirst();

            if(userOptional.isPresent()) {
                this.handleUserBlock(userOptional.get(), message.isBlocked());
            } else {
                messageSender.sendOperationResult(sessionId, messageId, LCErrorReason.NoSuchUser);
            }
        });
        handlerMap.put(LivecastMessageType.ChatMessageRequest, (messageId, msgObj, sessionId, liveUser) -> {
            final LCMessageChatMessageRequest message = (LCMessageChatMessageRequest) msgObj;
            final long chatNum = this.chatNum++;
            final String chatMessage = message.getMessage();

            messageSender.sendChatMessageEvent(liveUser, chatNum, chatMessage);
            messageSender.sendOperationResult(sessionId, messageId, LCErrorReason.OK);

        });
        handlerMap.put(LivecastMessageType.UserChatDeleteRequest, (messageId, msgObj, sessionId, liveUser) -> {
            final LCMessageUserChatDeleteRequest message = (LCMessageUserChatDeleteRequest) msgObj;

            messageSender.sendUserChatDeleteEvent(message.getFrom(), message.getNum());
            messageSender.sendOperationResult(sessionId, messageId, LCErrorReason.OK);
        });
        handlerMap.put(LivecastMessageType.PublishKickoffRequest, (messageId, msgObj, sessionId, liveUser) -> {
            final LCMessageUserPublishKickoffRequest message = (LCMessageUserPublishKickoffRequest) msgObj;

            if(this.apiUtil != null) {
                final SRSStreamsResponse streams = this.apiUtil.getStreams();
                if((streams != null) && (streams.getStreams() != null)) {
                    final String publishId = message.getPublishId();
                    final Optional<SRSStreamInfo> streamInfoOptional = streams.getStreams().stream().filter(s -> (s.getName() != null) && (s.getName().equals(publishId))).findFirst();
                    if(streamInfoOptional.isPresent()) {
                        final SRSStreamInfo streamInfo = streamInfoOptional.get();
                        final String cid = streamInfo.getPublish() != null ? streamInfo.getPublish().getCid() : null;
                        if((cid != null) && (cid.length() > 0)) {
                            final boolean kickoffResult = this.apiUtil.requestClientKickoff(cid);
                            log.debug("RequestClientKickoff : publishId={}, cid={}, result={}", publishId, cid, kickoffResult);
                        }
                    }
                }

                messageSender.sendOperationResult(sessionId, messageId, LCErrorReason.OK);
            } else {
                messageSender.sendOperationResult(sessionId, messageId, LCErrorReason.NotSupportedOperation);
            }
        });
        handlerMap.put(LivecastMessageType.UserMediaStateReport, (messageId, msgObj, sessionId, liveUser) -> {
            final LCMessageUserMediaStateReport message = (LCMessageUserMediaStateReport) msgObj;

            final LCUserMediaState oldState = liveUser.getMediaState();
            log.debug("UserMediaStateReport ... {}, {}", liveUser, message.getState());
            final LCUserMediaState newState = updateUserMediaState(liveUser, message.getState());
            if(newState != null) {
                for(LCViewManager vm : this.getViewManagers()) {
                    vm.onUserMediaStateChanged(liveUser, newState, oldState);
                }
            }

            if(newState != null) {
                messageSender.sendOperationResult(sessionId, messageId, LCErrorReason.OK);
            } else {
                messageSender.sendOperationResult(sessionId, messageId, LCErrorReason.Unknown);
            }

        });
//        handlerMap.put(LivecastMessageType.UserProductClickReport, (messageId, msgObj, sessionId, liveUser) -> {
//            final LCMessageUserProductClickReport message = (OntactMessageUserProductClickReport) msgObj;
//
//            try {
//                final OTLRoomActivationUserHistoryActivity activity = roomService.logUserHistoryActivity(room.getId(), roomState.getActivationId(), liveUser.getUserId(), liveUser.getHistoryId(), OTLActivityType.ProductClick, message.getPageUrl(), message.getProductId());
//
//                if(activity != null) {
//                    messageSender.sendOperationResult(sessionId, messageId, LCErrorReason.OK);
//                } else {
//                    messageSender.sendOperationResult(sessionId, messageId, LCErrorReason.Unknown);
//                }
//            } catch(Exception e) {
//                messageSender.sendOperationResult(sessionId, messageId, LCErrorReason.Unknown);
//            }
//        });
        handlerMap.put(LivecastMessageType.RecordingReport, (messageId, msgObj, sessionId, liveUser) -> {
            final LCMessageRecordingReport message = (LCMessageRecordingReport)  msgObj;
            final ZoneId timeZone = room.getTimeZone();
            final ZonedDateTime zonedNow = ZonedDateTime.now();
            final LocalDateTime now = dateTimeUtil.toZoneLocalDateTime(zonedNow, timeZone);

            this.roomState.setRecordState(message.getState());

            if((message.getState() != null) && (message.getState().equals(LCRoomRecordState.Recording))) {
                this.roomState.setRecordStartDatetime(now);
                this.roomState.setRecordTimeZone(room.getTimeZone());
            } else if((message.getState() != null) && (message.getState().equals(LCRoomRecordState.Completed))) {
                this.roomState.setRecordEndDatetime(now);
                this.roomState.setRecordTimeZone(room.getTimeZone());
            }

            messageSender.sendOperationResult(sessionId, messageId, LCErrorReason.OK);
            messageSender.sendRecordingEvent(roomState.getActivationId(), roomState.getRecordId(), message.getState(), now, timeZone, message.getDownloadUrl());
        });
        handlerMap.put(LivecastMessageType.SaveSceneRequest, (messageId, msgObj, sessionId, liveUser) -> {

            log.debug("SaveSceneRequest start...");
            log.debug("sessionId : {}", sessionId);
            log.debug("messageId : {}", messageId);
            log.debug("liveUser : {}", liveUser);
            log.debug("msgObj : {}", msgObj);
//            final LCMessageRecordingReport message = (LCMessageRecordingReport)  msgObj;
//            final ZoneId timeZone = room.getTimeZone();
//            final ZonedDateTime zonedNow = ZonedDateTime.now();
//            final LocalDateTime now = dateTimeUtil.toZoneLocalDateTime(zonedNow, timeZone);
//
//            this.roomState.setRecordState(message.getState());
//
//            if((message.getState() != null) && (message.getState().equals(LCRoomRecordState.Recording))) {
//                this.roomState.setRecordStartDatetime(now);
//                this.roomState.setRecordTimeZone(room.getTimeZone());
//            } else if((message.getState() != null) && (message.getState().equals(LCRoomRecordState.Completed))) {
//                this.roomState.setRecordEndDatetime(now);
//                this.roomState.setRecordTimeZone(room.getTimeZone());
//            }
//
//            messageSender.sendOperationResult(sessionId, messageId, LCErrorReason.OK);
//            messageSender.sendRecordingEvent(roomState.getActivationId(), roomState.getRecordId(), message.getState(), now, timeZone, message.getDownloadUrl());
        });
    }

    private LCRoomActivation doActivateRoom(String userId, boolean freeJoin) {
//        final LCRoomActivation activation = roomService.activateRoom(room, userId, room.getTimeZone());
        final LCRoomActivation activation = roomService.activateRoom(room, userId);

        if(activation != null) {
            final List<LCForbiddenWord> forbiddenWords = this.queryForbiddenWords(room.getCode());
            this.forbiddenWords = forbiddenWords;

            roomState.setActivated(true);
            roomState.setActivationId(activation.getActivationId());
            roomState.setFreeJoin(freeJoin);
            roomState.setStarted(false);
            roomState.setStartDatetime(null);

            final Collection<LCWaitingUser> waitingUsers = waitingUserSessionMap.values();
            for (LCWaitingUser user : waitingUsers) {
                user.setJoinable(freeJoin);
            }

            return activation;
        } else {
            return null;
        }
    }

    private void doStartRoom() {
        log.debug("doStartRoom start... {}");
//        final ZoneId timeZone = room.getTimeZone();
//        final ZonedDateTime zonedNow = ZonedDateTime.now();
//        final LocalDateTime now = dateTimeUtil.toZoneLocalDateTime(zonedNow, timeZone);

        roomState.setStarted(true);
        log.debug("point -2");
        roomState.setStartDatetime(LocalDateTime.now());
        log.debug("point -1");
        roomState.setTimeZone(ZoneId.systemDefault());
        log.debug("point 0 {} ", roomState);
//        roomService.startRoom(room, roomState.getActivationId());
        log.debug("point 1");
        messageSender.sendRoomStateChangedEvent(roomState);
        log.debug("point 2");
        messageSender.sendJoinableEvent();
        log.debug("point 3");

//        if(this.room.isCanRecord()) {
//            LCRoomRecordState currentRecordState = roomState.getRecordState();
//            if (currentRecordState.equals(LCRoomRecordState.None) || currentRecordState.equals(LCRoomRecordState.Error) || currentRecordState.equals(LCRoomRecordState.Completed)) {
//                this.startRecord(this.room.getId(), this.roomState.getActivationId(), this.room.getTimeZone(), LCRecordingMode.Portrait);
//            }
//        }
    }


    private void doDeactivateRoom(LCErrorReason reason) {
        final List<LCLiveUser> joinedUsers = liveUsers.stream().filter((user) -> user.isJoined()).collect(Collectors.toList());
        for(LCLiveUser user : joinedUsers) {
            try {
                roomService.exitRoomUser(room.getCode(), roomState.getActivationId(), user.getAppId(), user.getUserId(), user.getHistoryId());
            } catch(Exception e) {
                log.warn("Write room user exit log to db error : errorMessage={}", e.getMessage());
            }
        }

        final LCRoomDeactivationType type;
        if(reason.equals(LCErrorReason.Deactivated) || reason.equals(LCErrorReason.NoOwnerInRoom) || reason.equals(LCErrorReason.NobodyInRoom)) {
            type = LCRoomDeactivationType.Exit;
        } else if(reason.equals(LCErrorReason.TimeOver)) {
            type = LCRoomDeactivationType.TimedOut;
        } else {
            type = LCRoomDeactivationType.Unknown;
        }
        roomService.deactivateRoom(room, roomState.getActivationId(), type);

        roomState.setActivated(false);
        roomState.setActivationId(-1);
        roomState.setFreeJoin(room.isAutoActivation());
        roomState.setStarted(false);
        roomState.setStartDatetime(null);
        roomState.setRecordState(LCRoomRecordState.None);
        roomState.setRecordId(-1);
        roomState.setRecordStartDatetime(null);
        roomState.setRecordEndDatetime(null);
        roomState.setRecordTimeZone(room.getTimeZone());

        messageSender.sendExitEvent(reason);
    }

    private void handleUserConnected(String sessionId, LCWaitingUser waiting) {
        LCRoomUser roomUser = null;
        final Optional<LCRoomUser> roomUserOptional = roomUsers.stream().filter((u) -> Long.toString(u.getId()).equals(waiting.getUserId())).findFirst();
        if(roomUserOptional.isPresent()) {
            roomUser = roomUserOptional.get();
        } else {
//            final LCRoomUser newUser = roomService.getRoomUser(waiting.getUserId());
            final LCRoomUser newUser = roomService.convertToRoomUser(waiting, room.getCode());
            if(newUser != null) {
                roomUsers.add(newUser);

                roomUser = newUser;
            }
        }

        if(roomUser != null) {
//            if(modelUtil.isOperatingUser(roomUser.getType())) {
//                waiting.setJoinable(true);
//            } else if(roomState.isActivated() && roomState.isFreeJoin()) {
//                waiting.setJoinable(true);
//            }
            waiting.setJoinable(true); // 항상, 일단 조인 까지는 가능함.

            waitingUserSessionMap.put(sessionId, waiting);
//            if(!roomState.isFreeJoin()) {
//                messageSender.sendWaintingUsersEventToOperatingUser(waitingUserSessionMap.values().stream().collect(Collectors.toList()), liveUsers);
//            }
        }
    }

    private JoinResult handleUserJoin(LCWaitingUser waitingUser, String entranceCode, String name, boolean isForce) {
        final String sessionId = waitingUser.getSessionId();
        final String appId = waitingUser.getAppId();

        LCLiveUser liveUser = null;
        final Optional<LCRoomUser> roomUserOptional = roomUsers.stream()
                .filter((user) -> (Long.toString(user.getId()).equals(waitingUser.getUserId())))
                .findFirst();
        if(roomUserOptional.isPresent()) { // connect 할때 추가되므로, 참이어야 함
            final LCRoomUser roomUser = roomUserOptional.get();
            log.debug("roomUser = {}", roomUser);
            if(modelUtil.isStaticLiveUser(roomUser.getType())) {
                final Optional<LCLiveUser> liveUserOptional = liveUsers.stream()
                        .filter(user -> (user.getType() != null) && (user.getType().equals(roomUser.getType()))
                                && (user.getUserId() != null) && (user.getUserId().equals(waitingUser.getUserId()))
                        ).findFirst();
                if(liveUserOptional.isPresent()) {
                    liveUser = liveUserOptional.get();
                } else {
                    liveUser = modelUtil.convertToLiveUser(roomUserOptional.get());
                    liveUsers.add(liveUser);

                    messageSender.sendUserAddedEvent(liveUser);
                    for (LCViewManager vm : getViewManagers()) {
                        vm.onUserAdded(liveUser);
                    }
                }
            } else {
                final Optional<LCLiveUser> liveUserOptional = liveUsers.stream()
                        .filter(user -> (user.getType() != null) && (user.getType().equals(roomUser.getType()))
                                && (user.getAppId() != null) && (user.getAppId().equals(waitingUser.getAppId()))
                        ).findFirst();
                if(liveUserOptional.isPresent()) {
                    liveUser = liveUserOptional.get();
                } else {
                    liveUser = modelUtil.convertToLiveUser(roomUserOptional.get());
                    liveUsers.add(liveUser);

                    messageSender.sendUserAddedEvent(liveUser);
                    for (LCViewManager vm : getViewManagers()) {
                        vm.onUserAdded(liveUser);
                    }
                }
            }
        }

        if(liveUser == null) {
            return JoinResult.builder()
                    .reason(LCErrorReason.NoSuchUser)
                    .liveUser(null)
                    .build();
        }

        if(!roomState.isActivated()) {
            if(modelUtil.isOperatingUser(liveUser.getType())) {
                doActivateRoom(liveUser.getUserId(), true);
            } else {
                return JoinResult.builder()
                        .reason(LCErrorReason.NotActivated)
                        .liveUser(null)
                        .build();
            }
        }

        if(liveUser.isBlocked()) {
            return JoinResult.builder()
                    .reason(LCErrorReason.YouAreBlocked)
                    .liveUser(null)
                    .build();
        }

        if(liveUser.isJoined()) {
            if(isForce) {
                handleKickUser(liveUser.getSessionId());
            } else {
                return JoinResult.builder()
                        .reason(LCErrorReason.YouAreAlreadyJoined)
                        .liveUser(null)
                        .build();
            }
        }

        if((liveUser.getType() != null) && (!modelUtil.isOperatingUser(liveUser.getType()))) {
            log.debug("roomState : {}", roomState.isStarted());
            if(!roomState.isStarted()) {
                return JoinResult.builder()
                        .reason(LCErrorReason.YouCanNotJoinYet)
                        .liveUser(null)
                        .build();
            }

            final Optional<LCLiveUser> ownerOptional = liveUsers.stream().filter(u -> u.isJoined() && (u.getType() != null) && (u.getType().equals(LCRoomUserType.Owner))).findFirst();
            if(!ownerOptional.isPresent()) {
                return JoinResult.builder()
                        .reason(LCErrorReason.YouCanNotJoinYet)
                        .liveUser(null)
                        .build();
            }
        }

        // TODO : 현재 항상 0명으로 나와서 임시 주석 (getMaxConcurrent)
//        if((liveUser.getType() != null) && liveUser.getType().equals(LCRoomUserType.Guest)) {
//            log.debug("max user : {}", room.getCurrentConcurrent());
//            if(this.countJoinedUser(LCRoomUserType.Guest) >= room.getMaxConcurrent() - 3) {
//                return JoinResult.builder()
//                        .reason(LCErrorReason.TooManyUsers)
//                        .liveUser(null)
//                        .build();
//            }
//        }

//        if((liveUser.getType() != null) && (liveUser.getType().equals(LCRoomUserType.Owner)) && (room.getEntranceCode() != null) && (!room.getEntranceCode().equals(entranceCode))) {
//            return JoinResult.builder()
//                    .reason(LCErrorReason.CheckEntranceCode)
//                    .liveUser(null)
//                    .build();
//        }

        try {
            final ZoneId roomTimeZone = room.getTimeZone() != null ? room.getTimeZone() : ZoneId.systemDefault();
            final ZonedDateTime zonedNow = ZonedDateTime.now();
            final LocalDateTime roomTimeNow = dateTimeUtil.toZoneLocalDateTime(zonedNow, roomTimeZone);
            final String liveUserName = this.getLiveUserName(liveUser.getLiveId(), (name != null) && (name.length() > 0) ? name : liveUser.getName());
//            final OTLRoomActivationUserHistory userHistory = roomService.enterRoomUser(room.getId(), roomState.getActivationId(), appId, liveUserName, liveUser.getUserId(), roomTimeZone, roomTimeNow);

            liveUser.setSessionId(sessionId);
//            liveUser.setHistoryId(userHistory.getHistoryId());
            liveUser.setAppId(appId);
            liveUser.setName(liveUserName);
            liveUser.setJoined(true);
            liveUser.setJoinDatetime(roomTimeNow);

            messageSender.sendUserJoinedEvent(liveUser);
            for (LCViewManager vm : getViewManagers()) {
                vm.onUserJoined(liveUser);
            }

            waitingUserSessionMap.remove(sessionId);
            if (!roomState.isFreeJoin()) {
                messageSender.sendWaintingUsersEventToOperatingUser(waitingUserSessionMap.values().stream().collect(Collectors.toList()), liveUsers);
            }

            if ((roomState.isStarted()) && (liveUser.getType() != null) && (liveUser.getType().equals(LCRoomUserType.Owner))) {
                messageSender.sendJoinableEvent();
            }

            return JoinResult.builder()
                    .reason(LCErrorReason.OK)
                    .liveUser(liveUser)
                    .build();
        } catch (Exception e) {
            log.warn("Handle user join error : errorMessage={}", e.getMessage());

            return JoinResult.builder()
                    .reason(LCErrorReason.Unknown)
                    .liveUser(null)
                    .build();
        }
    }

    private String getLiveUserName(String liveId, String name) {
        if((name != null) && (name.length() > 0)) {
            final Optional<LCLiveUser> nameEqualsLiveUserOptional = liveUsers.stream()
                    .filter(u -> ((u.getLiveId() == null) && (u.getName() != null) && (u.getName().equals(name)))
                            || ((u.getLiveId() != null) && (!u.getLiveId().equals(liveId)) && (u.getName() != null) && (u.getName().equals(name))))
                    .findFirst();
            if (nameEqualsLiveUserOptional.isPresent()) {
                for(int i=0; i<room.getMaxConcurrent(); i++) {
                    final String newName = String.format("%s-%d", name, (i+1));
                    final Optional<LCLiveUser> newNameEqualsLiveUserOptional = liveUsers.stream()
                            .filter(u -> ((u.getLiveId() == null) && (u.getName() != null) && (u.getName().equals(newName)))
                                    || ((u.getLiveId() != null) && (!u.getLiveId().equals(liveId)) && (u.getName() != null) && (u.getName().equals(newName))))
                            .findFirst();
                    if(newNameEqualsLiveUserOptional.isPresent()) {
                        continue;
                    } else {
                        return newName;
                    }
                }

                return name;
            } else {
                return name;
            }
        } else {
            return "";
        }
    }

    private void handleKickUser(String sessionId) {
        messageSender.sendExitEvent(sessionId, LCErrorReason.YouAreKicked);
    }

    private void handleUserDisconnected(String sessionId) {
        final Optional<LCLiveUser> liveUserOptional = liveUsers.stream().filter((u) -> (u.getSessionId() != null) && (u.getSessionId().equals(sessionId))).findFirst();
        if(liveUserOptional.isPresent()) {
            final LCLiveUser liveUser = liveUserOptional.get();

            if(roomState.isActivated()) {
                try {
                    roomService.exitRoomUser(room.getCode(), roomState.getActivationId(), liveUser.getAppId(), liveUser.getUserId(), liveUser.getHistoryId());
                } catch(Exception e) {
                    log.warn("Write room user exit log to db error : errorMessage={}", e.getMessage());
                }
            }

            if(liveUser.isJoined()) {
                handleUserExit(liveUser);
            }
        }

        waitingUserSessionMap.remove(sessionId);
    }

    private void handleUserExit(LCLiveUser liveUser) {
        log.debug("HandleUserExit : liveUser={}", liveUser);

        modelUtil.clearLiveUserState(liveUser);
        messageSender.sendUserExitedEvent(liveUser);
        for(LCViewManager vm : getViewManagers()) {
            vm.onUserExited(liveUser);
        }

        if((liveUser.getType() != null) && (liveUser.getType().equals(LCRoomUserType.Owner))) {
            log.debug("HandleUserExit - Send exit event(NoOwnerInRoom) : Exit user is owner");
            messageSender.sendExitEvent(LCErrorReason.NoOwnerInRoom);
        }

        if(this.countJoinedUser(LCRoomUserType.Owner, LCRoomUserType.Member, LCRoomUserType.Guest, LCRoomUserType.Operator) < 1) {
            log.debug("HandleUserExit - Deactivating room : Nobody in room");
            doDeactivateRoom(LCErrorReason.NobodyInRoom);
        }
    }

    private void handleUserBlock(LCLiveUser liveUser, boolean block) {
        log.debug("HandleUserBlock : liveUser={}", liveUser);

        if(liveUser != null) {
            liveUser.setBlocked(block);

            if(block) {
                this.handleKickUser(liveUser.getSessionId());
            } else {
                messageSender.sendUserStateChangedEvent(liveUser);
            }
        }
    }

    private void updateUserHandUp(LCLiveUser liveUser, boolean handUp) {
        if(liveUser != null) {
            if(liveUser.isHandUp() != handUp) {
                liveUser.setHandUp(handUp);

                messageSender.sendUserStateChangedEvent(liveUser);
                for(LCViewManager vm : getViewManagers()) {
                    vm.onUserHandUpChanged(liveUser);
                }
            }
        }
    }

    private void updateUserPresentation(LCLiveUser liveUser, boolean presentation) {
        liveUser.setHandUp(false);
        messageSender.sendUserStateChangedEvent(liveUser);

        for(LCViewManager vm : getViewManagers()) {
            vm.onUserPresentation(liveUser, presentation);
        }
    }

    private void updateUserCanChat(LCLiveUser liveUser, boolean canChat) {
        if(liveUser != null) {
            liveUser.setCanChat(canChat);

            messageSender.sendUserStateChangedEvent(liveUser);
        }
    }

    private LCUserMediaState updateUserMediaState(LCLiveUser liveUser, LCUserMediaState state) {
        if((liveUser != null) && (state != null)) {
            liveUser.setMediaState(state);

            messageSender.sendUserMediaStateChangedEvent(liveUser);

            if((liveUser.getType() != null) && (liveUser.getType().equals(LCRoomUserType.Owner))) {
                if(state.isPublished() && (!roomState.isStarted())) {
                    this.doStartRoom();
                }
            }
        }

        return state;
    }

//    private OTLRoomRecord startRecord(String roomId, int activationId, ZoneId timeZone, OntactRecordingMode mode) {
//        OTLRoomRecord record = null;
//        try {
//            final OTLRoomRecordState newRecordState = OTLRoomRecordState.Requested;
//
//            record = recordService.createRecord(roomId, activationId, newRecordState, timeZone);
//            this.requestStartRecord(roomId, activationId, record.getRecordId(), mode);
//
//            this.roomState.setRecordState(newRecordState);
//            this.roomState.setRecordId(record.getRecordId());
//            return record;
//        } catch(Exception e) {
//            if(record != null) {
//                final OTLRoomRecordState newRecordState = OTLRoomRecordState.Error;
//
//                final OTLRoomRecordStateUpdate update = OTLRoomRecordStateUpdate.builder()
//                        .roomId(roomId)
//                        .activationId(activationId)
//                        .recordId(record.getRecordId())
//                        .state(newRecordState)
//                        .message("StartRecord http request error : " + e.getMessage())
//                        .build();
//
//                this.roomState.setRecordState(newRecordState);
//                this.roomState.setRecordId(record.getRecordId());
//
//                recordService.updateRecordState(update, timeZone);
//            }
//
//            log.warn("StartRecord error", e);
//
//            return null;
//        }
//    }

    private List<LCForbiddenWord> queryForbiddenWords(String roomId) {
        List<LCForbiddenWord> forbiddenWords = null;

//        final List<OTLExtra> roomExtras = room.getExtras();
//        if((roomExtras != null) && (roomExtras.size() > 0)) {
//            final Optional<OTLExtra> queryUrlExtraOptional = roomExtras.stream().filter((extra) -> (extra.getKey() != null) && (extra.getKey().equalsIgnoreCase("QueryUrl"))).findFirst();
//            if(queryUrlExtraOptional.isPresent()) {
//                forbiddenWords= notifier.getForbiddenWords(queryUrlExtraOptional.get().getValue(), roomId);
//            } else {
//                log.warn("QueryForbiddenWords : Empty QueryURL extra");
//            }
//        } else {
//            log.debug("QueryForbiddenWords : Empty room extras");
//        }

        return forbiddenWords;
    }

    private void requestStartRecord(String roomId, int activationId, int recordId, LCRecordingMode mode) {
        final Optional<LCRoomEndpoint> recordEndpointOptional = roomEndpoints.stream().filter((endpoint) -> endpoint.getType().equals(LCRoomEndpointType.Record)).findFirst();
        if(recordEndpointOptional.isPresent()) {
            notifier.requestStartRecord(recordEndpointOptional.get().getEndpoint(), roomId, activationId, recordId, mode);
        }
    }

    private void changeView(LCViewType viewType) {
        if(getCurrentViewManager() != null) {
            getCurrentViewManager().deactivated();
        }

        currentViewType = viewType;
        if(getCurrentViewManager() != null) {
            getCurrentViewManager().activated();
        }
    }
}
