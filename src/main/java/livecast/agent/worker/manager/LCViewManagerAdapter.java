package livecast.agent.worker.manager;

import livecast.agent.model.LCLiveUser;
import livecast.agent.model.LCUserMediaState;
import livecast.agent.model.support.LCViewType;
import livecast.agent.worker.LCRoomMessageSender;
import livecast.agent.worker.support.ModelUtil;

import java.util.List;

public class LCViewManagerAdapter extends LCViewManager{
    public LCViewManagerAdapter(LCViewType viewType, String roomId, boolean activated, List<LCLiveUser> liveUsers, ModelUtil modelUtil, LCRoomMessageSender messageSender) {
        super(viewType, roomId, activated, liveUsers, modelUtil, messageSender);
    }
    @Override
    public Object getViewState() {
        return null;
    }

    @Override
    protected void onActivated() {}

    @Override
    protected void onDeactivated() {}

    @Override
    public void onUserAdded(LCLiveUser liveUser) {}

    @Override
    public void onUserJoined(LCLiveUser liveUser) {}

    @Override
    public void onUserExited(LCLiveUser liveUser) {
        clearUserState(liveUser);
    }

//    @Override
//    public void onUserMutedChanged(OntactLiveUser liveUser) {}

    @Override
    public void onUserHandUpChanged(LCLiveUser liveUser) {}

    @Override
    public void onUserPresentation(LCLiveUser liveUser, boolean presentation) {}

    @Override
    public void onUserMediaStateChanged(LCLiveUser liveUser, LCUserMediaState newState, LCUserMediaState oldState) {}

//    @Override
//    public void onUserCamStateChanged(OntactLiveUser liveUser, OntactUserCamState camState) {}
//
//    @Override
//    public void onUserMicStateChanged(OntactLiveUser liveUser, OntactUserMicState micState) {}
//
//    @Override
//    public void onUserAudioStateChanged(OntactLiveUser liveUser, OntactUserAudioState audioState) {}
}
