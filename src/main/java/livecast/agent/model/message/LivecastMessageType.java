package livecast.agent.model.message;

import livecast.agent.model.message.agent.*;
import livecast.agent.model.message.client.*;

public enum LivecastMessageType {
    String                          (String.class,                                          false,  false,  false),
    JoinRequest                     (LCMessageJoinRequest.class,                        false,  false,  false),
    UserHandUpRequest               (LCMessageUserHandUpRequest.class,                  true,   true,   false),
    UserPresentationRequest         (LCMessageUserPresentationRequest.class,            true,   true,   true),
    //    UserMuteRequest                 (OntactMessageUserMuteRequest.class,                    true,   true,   true),
    UserCanChatRequest              (LCMessageUserCanChatRequest.class,                 true,   true,   true),
    UserBlockRequest                (LCMessageUserBlockRequest.class,                   true,   true,   true),
    ChatMessageRequest              (LCMessageChatMessageRequest.class,                 true,   true,   false),
    UserChatDeleteRequest           (LCMessageUserChatDeleteRequest.class,              true,   true,   false),
    PublishKickoffRequest           (LCMessageUserPublishKickoffRequest.class,          true,   true,   false),
    UserMediaStateReport            (LCMessageUserMediaStateReport.class,               true,   false,  false),
    //    UserCamStateReport              (OntactMessageUserCamStateReport.class,                 true,   false,  false),
//    UserMicStateReport              (OntactMessageUserMicStateReport.class,                 true,   false,  false),
//    UserAudioStateReport            (OntactMessageUserAudioStateReport.class,               true,   false,  false),
//    UserProductClickReport          (OntactMessageUserProductClickReport.class,             true,   false,  false),
    RecordingReport                 (LCMessageRecordingReport.class,                    false,  false,  false),


    // Agent Command
    JoinableEvent                   (LCMessageJoinableEvent.class,                      false,  false,  false),
    ExitEvent                       (LCMessageExitEvent.class,                          false,  false,  false),
    WaitingUsersEvent               (LCMessageWaitingUsersEvent.class,                  false,  false,  false),
    UserAddedEvent                  (LCMessageUserAddedEvent.class,                     false,  false,  false),
    UserJoinedEvent                 (LCMessageUserJoinedEvent.class,                    false,  false,  false),
    UserExitedEvent                 (LCMessageUserExitedEvent.class,                    false,  false,  false),
    ChatMessageEvent                (LCMessageChatMessageEvent.class,                   false,  false,  false),
    RoomStateChangedEvent           (LCMessageRoomStateChangedEvent.class,              false,  false,  false),
    ViewStateChangedEvent           (LCMessageViewStateChangedEvent.class,              false,  false,  false),
    UserStateChangedEvent           (LCMessageUserStateChangedEvent.class,              false,  false,  false),
    UserMediaStateChangedEvent      (LCMessageUserMediaStateChangedEvent.class,         false,  false,  false),
    //    UserCamStateChangedEvent        (OntactMessageUserCamStateChangedEvent.class,           false,  false,  false),
//    UserMicStateChangedEvent        (OntactMessageUserMicStateChangedEvent.class,           false,  false,  false),
//    UserAudioStateChangedEvent      (OntactMessageUserAudioStateChangedEvent.class,         false,  false,  false),
    UserChatDeleteEvent             (LCMessageUserChatDeleteEvent.class,                false,  false,  false),
    RecordingEvent                  (LCMessageRecordingEvent.class,                     false,  false,  false),

    JoinResponse                    (LCMessageJoinResponse.class,                       false,  false,  false),
    OperationResult                 (LCMessageOperationResult.class,                    false,  false,  false),
    ErrorResponse                   (LCMessageErrorResponse.class,                      false,  false,  false),


    SaveSceneRequest                   (LCMessageSaveSceneRequest.class,                      true,  false,  false);
    private Class<?> aClass;
    private boolean joinRequired;
    private boolean roomActivationRequired;
    private boolean operatingRoleRequired;

    LivecastMessageType(Class<?> aClass, boolean joinRequired, boolean roomActivationRequired, boolean operatingRoleRequired) {
        this.aClass = aClass;
        this.joinRequired = joinRequired;
        this.roomActivationRequired = roomActivationRequired;
        this.operatingRoleRequired = operatingRoleRequired;
    }

    public Class<?> getClassInfo() {
        return this.aClass;
    }

    public boolean isJoinRequired() {
        return joinRequired;
    }

    public boolean isRoomActivationRequired() {
        return roomActivationRequired;
    }

    public boolean isOperatingRoleRequired() {
        return operatingRoleRequired;
    }
}
