package livecast.agent.service;

import livecast.agent.model.*;
import livecast.agent.model.support.LCActivityType;
import livecast.agent.model.support.LCRoomDeactivationType;
import livecast.agent.model.support.LCRoomStatus;
import livecast.agent.model.support.LCRoomUserType;
import livecast.agent.repository.RoomActivationRepository;
import livecast.agent.repository.RoomRepository;
import livecast.agent.repository.RoomUserRepository;
import livecast.agent.util.DateTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class RoomService {
    private RoomRepository roomRepository;
    private RoomUserRepository roomUserRepository;
    private RoomActivationRepository roomActivationRepository;
    private RoomNotifier roomNotifier;
    private DateTimeUtil dateTimeUtil;

    @Autowired
    public RoomService(RoomRepository roomRepository, RoomUserRepository roomUserRepository, RoomActivationRepository roomActivationRepository, RoomNotifier roomNotifier, DateTimeUtil dateTimeUtil) {
        this.roomRepository = roomRepository;
        this.roomUserRepository = roomUserRepository;
        this.roomActivationRepository = roomActivationRepository;
        this.roomNotifier = roomNotifier;
        this.dateTimeUtil = dateTimeUtil;
    }

    public LCRoom getRoom(String liveCode) {
        return roomRepository.selectRoom(liveCode).getData();
//        return new LCRoom();
    }

    public List<LCRoomUser> getRoomUsers(String roomId) {
//        return roomUserRepository.selectRoomUsers(roomId);
        return new ArrayList<>();
    }

    public LCRoomUser getRoomUser(String userIdStr) {
        LCRoomUser user = null;

        try {
            long userId = Long.parseLong(userIdStr);
            user = roomUserRepository.selectRoomUser(userId);
        } catch(Exception e) {
            user = null;
        }

        return user;
    }

    public LCRoomUser convertToRoomUser(LCWaitingUser waitingUser, String liveCode){
        // TODO : type, nickName 넣는 부분 변경 필요
        return LCRoomUser.builder()
                .id(Long.parseLong(waitingUser.getUserId()))
                .roomId(liveCode)
                .appId(waitingUser.getAppId())
                .type(waitingUser.getType())
                .name(waitingUser.getName())
                .nickName(waitingUser.getName())
                .pinCode("")
                .build();
    }

    public List<LCRoomEndpoint> getRoomEndpoints(String roomId) {
        return roomRepository.selectRoomEndpoints(roomId);
    }

//    public LCRoomActivation activateRoom(LCRoom room, String activationUserId, ZoneId timeZone) {
    public LCRoomActivation activateRoom(LCRoom room, String activationUserId) {
        final LocalDateTime now = LocalDateTime.now();
        final ZonedDateTime zoneNow = ZonedDateTime.now();
//        final LocalDateTime activatedTime = dateTimeUtil.toZoneLocalDateTime(zoneNow, timeZone);

        final LCRoomActivation activation = LCRoomActivation.builder()
                .roomId(room.getCode())
                .activationId(0)
                .activatedDatetime(now)
                .deactivatedDatetime(null)
//                .timeZone(timeZone)
                .deactivateType(null)
                .createdDatetime(now)
                .updatedDatetime(now)
                .build();

//        final LCRoomActivation activationResult = updateRoomStatusAndInsertRoomActivation(activation); // activation 을 insert 하면서 activationId 값이 셋팅되어 리턴.
        if(activation != null) {
            roomNotifier.sendRoomStateChanged(room.getNotifyUrl(), room.getCode(), activation.getActivationId(), LCRoomNotify.State.Activated, ZoneId.systemDefault(), now); // getNotifyUrl 은 station 임
            log.info("Room Activated : roomId={}, activationId={}, activationUser={}", room.getCode(), activation.getActivationId(), activationUserId);
        }

        return activation;
    }

    public LCRoomActivation startRoom(LCRoom room, int activationId) {
        final LCRoomActivation roomActivation = roomActivationRepository.selectRoomActivation(room.getCode(), activationId);
        if(roomActivation != null) {
            final LocalDateTime now = LocalDateTime.now();
            final ZonedDateTime zoneNow = ZonedDateTime.now();
            final LocalDateTime startedTime = dateTimeUtil.toZoneLocalDateTime(zoneNow, roomActivation.getTimeZone());

            roomActivation.setStartedDatetime(startedTime);
            roomActivation.setUpdatedDatetime(now);

            final LCRoomActivation activationResult = updateRoomStatusAndUpdateRoomActivationStartDatetime(roomActivation);
            if(activationResult != null) {
                roomNotifier.sendRoomStateChanged(room.getNotifyUrl(), room.getCode(), activationId, LCRoomNotify.State.Started, roomActivation.getTimeZone(), startedTime);
                log.info("Room started : roomId={}, activationId={}", room.getCode(), activationId);
            }

            return activationResult;
        } else {
            log.warn("Room started but can not found room activation : roomId={}, activationId={}", room.getCode(), activationId);

            return null;
        }
    }

    public LCRoomActivation deactivateRoom(LCRoom room, int activationId, LCRoomDeactivationType deactivationType) {
        final LCRoomActivation roomActivation = roomActivationRepository.selectRoomActivation(room.getCode(), activationId);
        if(roomActivation != null) {
            final LocalDateTime now = LocalDateTime.now();
            final ZonedDateTime zoneNow = ZonedDateTime.now();
            final LocalDateTime deactivatedTime = dateTimeUtil.toZoneLocalDateTime(zoneNow, roomActivation.getTimeZone());

            roomActivation.setDeactivatedDatetime(deactivatedTime);
            roomActivation.setDeactivateType(deactivationType);
            roomActivation.setUpdatedDatetime(now);

            final LCRoomActivation activationResult = updateRoomStatusAndUpdateRoomActivationDeactivateDatetime(roomActivation);
            if(activationResult != null) {
                roomNotifier.sendRoomStateChanged(room.getNotifyUrl(), room.getCode(), activationId, LCRoomNotify.State.Deactivated, roomActivation.getTimeZone(), deactivatedTime);
                log.info("Room Deactivated : roomId={}, activationId={}, deactivationType={}", room.getCode(), activationId, deactivationType);
            }

            return activationResult;
        } else {
            log.warn("Room Deactivated but can not found room activation : roomId={}, activationId={}, deactivationType={}", room.getCode(), activationId, deactivationType);

            return null;
        }
    }

    public LCRoomActivationUserHistory enterRoomUser(String roomId, int activationId, String appId, String name, String userIdStr, ZoneId timeZone, LocalDateTime enterTime) {
        final LocalDateTime now = LocalDateTime.now();

        final LCRoomActivationUserHistory userHistory = LCRoomActivationUserHistory.builder()
                .roomId(roomId)
                .activationId(activationId)
                .userId(Long.parseLong(userIdStr))
                .historyId(0)
                .appId(appId)
                .name(name)
                .enterDatetime(enterTime)
                .exitDatetime(null)
                .timeZone(timeZone)
                .createdDatetime(now)
                .updatedDatetime(now)
                .build();
        roomActivationRepository.insertRoomActivationUserHistory(userHistory);
        log.info("User Entered : roomId={}, activationId={}, appId={}, userId={}, historyId={}", roomId, activationId, appId, userIdStr, userHistory.getHistoryId());
        return userHistory;
    }

    public LCRoomActivationUserHistory exitRoomUser(String roomId, int activationId, String appId, String userIdStr, int historyId) {
        final LCRoomActivationUserHistory userHistory = roomActivationRepository.selectRoomActivationUserHistory(roomId, activationId, Long.parseLong(userIdStr), historyId);
        if(userHistory != null) {
            final LocalDateTime now = LocalDateTime.now();
            final ZonedDateTime zoneNow = ZonedDateTime.now();
            final LocalDateTime exitTime = dateTimeUtil.toZoneLocalDateTime(zoneNow, userHistory.getTimeZone());

            userHistory.setExitDatetime(exitTime);
            userHistory.setUpdatedDatetime(now);

            roomActivationRepository.updateRoomActivationUserHistoryExitDatetime(userHistory);
            log.info("User Exited : roomId={}, activationId={}, appId={}, userId={}, historyId={}", roomId, activationId, appId, userIdStr, historyId);
            return userHistory;
        } else {
            log.warn("User Exited but can not found user history : roomId={}, activationId={}, appId={}, userId={}, historyId={}", roomId, activationId, appId, userIdStr, historyId);
            return null;
        }
    }

    public LCRoomActivationUserHistoryActivity logUserHistoryActivity(String roomId, int activationId, String userIdStr, int historyId, LCActivityType activityType, String activityData, int productId) {
        final long userId = Long.parseLong(userIdStr);
        final LCRoomActivationUserHistory userHistory = roomActivationRepository.selectRoomActivationUserHistory(roomId, activationId, userId, historyId);
        if(userHistory != null) {
            final LocalDateTime now = LocalDateTime.now();
            final ZonedDateTime zoneNow = ZonedDateTime.now();
            final LocalDateTime activityTime = dateTimeUtil.toZoneLocalDateTime(zoneNow, userHistory.getTimeZone());
            final LCRoomActivationUserHistoryActivity activity = LCRoomActivationUserHistoryActivity.builder()
                    .roomId(roomId)
                    .activationId(activationId)
                    .userId(userId)
                    .historyId(historyId)
                    .productId(productId)
                    .activityId(0)
                    .activityType(activityType)
                    .activityDatetime(activityTime)
                    .activityTimeZone(userHistory.getTimeZone())
                    .activityData(activityData)
                    .createdDatetime(now)
                    .updatedDatetime(now)
                    .build();

            roomActivationRepository.insertRoomActivationUserHistoryActivity(activity);
            log.info("UserActivity saved : roomId={}, activationId={}, userId={}, historyId={}, activityType={}, activityData={}", roomId, activationId, userId, historyId, activityTime, activityData);
            return activity;
        } else {
            log.warn("UserActivity saving, but can not found user history : roomId={}, activationId={}, userId={}, historyId={}", roomId, activationId, userIdStr, historyId);
            return null;
        }
    }

    @Transactional
    protected LCRoomActivation updateRoomStatusAndInsertRoomActivation(LCRoomActivation activation) {
        final LCRoom room = roomRepository.selectRoom(activation.getRoomId()).getData();
        if((room != null) && (!room.getStatus().equals(LCRoomStatus.Canceled))) {
            roomRepository.updateRoomStatus(activation.getRoomId(), LCRoomStatus.Activated);
            roomActivationRepository.insertRoomActivation(activation);

            return activation;
        } else {
            log.warn("updateRoomStatusAndInsertRoomActivation error : Empty room or room status is canceled");

            return null;
        }
    }

    @Transactional
    protected LCRoomActivation updateRoomStatusAndUpdateRoomActivationStartDatetime(LCRoomActivation activation) {
        final LCRoom room = roomRepository.selectRoom(activation.getRoomId()).getData();
        if((room != null) && (!room.getStatus().equals(LCRoomStatus.Canceled))) {
            roomRepository.updateRoomStatus(activation.getRoomId(), LCRoomStatus.Started);
            roomActivationRepository.updateRoomActivationStartedDatetime(activation);

            return activation;
        } else {
            log.warn("UpdateRoomStatusAndUpdateRoomActivationStartDatetime error : Empty room or room status is canceled");

            return null;
        }
    }

    @Transactional
    protected LCRoomActivation updateRoomStatusAndUpdateRoomActivationDeactivateDatetime(LCRoomActivation activation) {
        final LCRoom room = roomRepository.selectRoom(activation.getRoomId()).getData();
        if((room != null) && (!room.getStatus().equals(LCRoomStatus.Canceled))) {
            roomRepository.updateRoomStatus(activation.getRoomId(), LCRoomStatus.Deactivated);
            roomActivationRepository.updateRoomActivationDeactivatedDatetime(activation);

            return activation;
        } else {
            log.warn("UpdateRoomStatusAndUpdateRoomActivationDeactivateDatetime error : Empty room or room status is canceled");
            return null;
        }
    }
}
