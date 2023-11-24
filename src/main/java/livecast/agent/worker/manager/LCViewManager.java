package livecast.agent.worker.manager;

import livecast.agent.model.LCLiveUser;
import livecast.agent.model.LCUserMediaState;
import livecast.agent.model.support.LCViewType;
import livecast.agent.worker.LCRoomMessageSender;
import livecast.agent.worker.support.ModelUtil;

import java.util.List;
import java.util.Optional;

public abstract class LCViewManager {
    private ModelUtil modelUtil;
    private LCRoomMessageSender messageSender;

    private LCViewType viewType;
    private boolean activated;
    private String roomId;
    private List<LCLiveUser> liveUsers;

    public LCViewManager(LCViewType viewType, String roomId, boolean activated, List<LCLiveUser> liveUsers, ModelUtil modelUtil, LCRoomMessageSender messageSender) {
        this.modelUtil = modelUtil;
        this.messageSender = messageSender;

        this.viewType = viewType;
        this.roomId = roomId;
        this.activated = activated;
        this.liveUsers = liveUsers;
    }

    public abstract Object getViewState();

    protected abstract void onActivated();
    protected abstract void onDeactivated();

    public abstract void onUserAdded(LCLiveUser liveUser);
    public abstract void onUserJoined(LCLiveUser liveUser);
    public abstract void onUserExited(LCLiveUser liveUser);

    //    public abstract void onUserMutedChanged(LCLiveUser liveUser);
    public abstract void onUserHandUpChanged(LCLiveUser liveUser);
    public abstract void onUserPresentation(LCLiveUser liveUser, boolean presentation);
    public abstract void onUserMediaStateChanged(LCLiveUser liveUser, LCUserMediaState newState, LCUserMediaState oldState);
//    public abstract void onUserCamStateChanged(LCLiveUser liveUser, OntactUserCamState camState);
//    public abstract void onUserMicStateChanged(LCLiveUser liveUser, OntactUserMicState micState);
//    public abstract void onUserAudioStateChanged(LCLiveUser liveUser, OntactUserAudioState audioState);


    public LCViewType getViewType() {
        return this.viewType;
    }

    public String getRoomId() {
        return this.roomId;
    }

    public List<LCLiveUser> getLiveUsers() {
        return this.liveUsers;
    }

    public LCLiveUser getLiveUser(String appId) {
        final Optional<LCLiveUser> liveUserOptional = this.liveUsers.stream().filter(u -> u.getAppId().equals(appId)).findFirst();
        if(liveUserOptional.isPresent()) {
            return liveUserOptional.get();
        } else {
            return null;
        }
    }

    public boolean containsLiveUser(LCLiveUser liveUser) {
        boolean isContain = false;

        if(this.liveUsers != null) {
            if (liveUser != null) {
                final String appId = liveUser.getAppId();
                if (appId != null) {
                    final Optional<LCLiveUser> liveUserOptional = this.liveUsers.stream().filter(u -> (u.getAppId() != null) && u.getAppId().equals(appId)).findFirst();

                    isContain = liveUserOptional.isPresent();
                }
            }
        }

        return isContain;
    }

    public void clearUserState(LCLiveUser user) {
        this.modelUtil.clearLiveUserState(user);
    }

    public void activated() {
        this.activated = true;

        this.onActivated();
    }

    public void deactivated() {
        this.activated = false;

        this.onDeactivated();
    }

    public boolean isOperatingUser(LCLiveUser liveUser) {
        if(liveUser != null) {
            return modelUtil.isOperatingUser(liveUser.getType());
        } else {
            return false;
        }
    }

    public void sendViewStateChangedEvent() {
        if(this.activated) {
            this.messageSender.sendViewStateChangedEvent(this.getViewType(), this.getViewState());
        }
    }

    public void sendUserStateChangedEvent(LCLiveUser liveUser) {
        if(this.activated) {
            this.messageSender.sendUserStateChangedEvent(liveUser);
        }
    }
}
