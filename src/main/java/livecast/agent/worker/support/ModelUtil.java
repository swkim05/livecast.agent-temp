package livecast.agent.worker.support;

import livecast.agent.model.LCLiveUser;
import livecast.agent.model.LCRoomUser;
import livecast.agent.model.LCUserMediaState;
import livecast.agent.model.support.LCRoomUserType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ModelUtil {
    private Object lock;
    private Map<LCRoomUserType, Integer> liveNumberMap;

    public ModelUtil() {
        this.lock = new Object();
        this.liveNumberMap = new HashMap<>();
    }

    private int getLiveNumber(LCRoomUserType userType) {
        int liveNumber = 0;

        synchronized (lock) {
            if(liveNumberMap.containsKey(userType)) {
                liveNumber = liveNumberMap.get(userType);
                liveNumberMap.put(userType, liveNumber + 1);
            } else {
                liveNumberMap.put(userType, 1);
            }
        }

        return liveNumber;
    }

    public boolean isStaticLiveUser(LCRoomUserType userType) {
        if( (userType != null) && (!userType.equals(LCRoomUserType.Guest) && !userType.equals(LCRoomUserType.Operator)) ) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isOperatingUser(LCRoomUserType userType) {
        if( (userType != null) && (userType.equals(LCRoomUserType.Owner) || userType.equals(LCRoomUserType.Operator)) ) {
            return true;
        } else {
            return false;
        }
    }

    public void clearLiveUserState(LCLiveUser user) {
        user.setJoined(false);
        user.setJoinDatetime(null);

        this.clearUserMediaState(user.getMediaState());
//        this.clearUserMicState(user.getMicState());
//        this.clearUserCamState(user.getCamState());
//        this.clearUserAudioState(user.getAudioState());

        user.setMuted(true);
        user.setHandUp(false);
        // Chat 상태는 유지
        //user.setCanChat(false);
        // Block 상태는 유지
        //user.setBlocked(false);
    }

    private void clearUserMediaState(LCUserMediaState state) {
        if(state != null) {
            state.setPublished(false);
            state.setPublishId(null);
            state.setMicPermission(false);
            state.setMicOn(false);
            state.setCamPermission(false);
            state.setCamOn(false);
            state.setCamMirror(false);
            state.setAudioOn(false);
        }
    }

//    private void clearUserMicState(OntactUserMicState state) {
//        if(state != null) {
//            state.setPermission(false);
//            state.setOn(false);
//            state.setPublished(false);
//            state.setPublishId(null);
//            state.setMuted(true);
//        }
//    }
//
//    private void clearUserCamState(OntactUserCamState state) {
//        if(state != null) {
//            state.setPermission(false);
//            state.setOn(false);
//            state.setPublished(false);
//            state.setPublishId(null);
//        }
//    }
//
//    private void clearUserAudioState(OntactUserAudioState state) {
//        if(state != null) {
//            state.setPermission(false);
//            state.setOn(false);
//        }
//    }

    public List<LCLiveUser> convertToLiveUsers(List<LCRoomUser> roomUsers) {
        return roomUsers.stream()
                .map((user) -> this.convertToLiveUser(user))
                .collect(Collectors.toList());
    }

    public LCLiveUser convertToLiveUser(LCRoomUser user) {
        final LCRoomUserType userType = user.getType();
        final int liveNumber = this.getLiveNumber(userType != null ? userType : LCRoomUserType.None);
        final String liveId = String.format("%s-%d", userType != null ? userType.name() : LCRoomUserType.None.name(), liveNumber);
        final String name = this.isStaticLiveUser(userType) ? user.getName() : String.format("%s-%d", user.getName(), liveNumber);

        return LCLiveUser.builder()
                .sessionId(null)
                .liveId(liveId)
                .appId(null)
                .userId(Long.toString(user.getId()))
                .historyId(0)
                .name(name)
                .type(user.getType())
//                .extras(user.getExtras())
                .joined(false)
                .joinDatetime(null)
                .mediaState(LCUserMediaState.builder()
                        .published(false)
                        .publishId(null)
                        .micPermission(false)
                        .micOn(false)
                        .camPermission(false)
                        .camOn(false)
                        .camMirror(false)
                        .audioOn(false)
                        .build())
//                .micState(OntactUserMicState.builder()
//                        .permission(false)
//                        .on(false)
//                        .published(false)
//                        .publishId(null)
//                        .muted(true)
//                        .build())
//                .camState(OntactUserCamState.builder()
//                        .permission(false)
//                        .on(false)
//                        .published(false)
//                        .publishId(null)
//                        .build())
//                .audioState(OntactUserAudioState.builder()
//                        .permission(false)
//                        .on(false)
//                        .build())
                .muted(true)
                .handUp(false)
                .canChat(true)
                .blocked(false)
                .build();
    }

//    public OntactRoom convertToRoom(OTLRoom room) {
//        return OntactRoom.builder()
//                .id(room.getId())
//                .title(room.getTitle())
//                .name(room.getName())
//                .comment(room.getComment())
//                .type(room.getType())
//                .startDatetime(room.getStartDatetime())
//                .endDatetime(room.getEndDatetime())
//                .reserveStartDatetime(room.getReserveStartDatetime())
//                .reserveEndDatetime(room.getReserveEndDatetime())
//                .timeZone(room.getTimeZone())
//                .maxUsers(room.getMaxUsers())
//                .canChat(room.isCanChat())
//                .canRecord(room.isCanRecord())
//                .canRecordGuest(room.isCanRecordGuest())
//                .entranceCode(room.getEntranceCode())
//                .titleImageUrl(room.getTitleImageUrl())
//                .notifyUrl(room.getNotifyUrl())
//                .clientId(room.getClientId())
//                .autoActivation(room.isAutoActivation())
//                .joinAnyTime(room.isJoinAnytime())
//                .productOpenType(OTLRoomProductOpenType.NewWindow)
//                .products(room.getProducts())
//                .extras(room.getExtras())
//                .createdDatetime(room.getCreatedDatetime())
//                .updatedDatetime(room.getUpdatedDatetime())
//                .build();
//    }
}
