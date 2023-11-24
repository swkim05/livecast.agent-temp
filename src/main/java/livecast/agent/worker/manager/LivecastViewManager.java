package livecast.agent.worker.manager;

import livecast.agent.model.LCLiveUser;
import livecast.agent.model.LCUserMediaState;
import livecast.agent.model.support.LCViewType;
import livecast.agent.model.view.LCViewState;
import livecast.agent.worker.LCRoomMessageSender;
import livecast.agent.worker.support.ModelUtil;

import java.util.List;

public class LivecastViewManager extends LCViewManagerAdapter{
    private boolean mainPublished;
    private String mainLiveId;
    private String mainFeedId;
    private String mainName;

    private boolean mainMirror;

    private LCLiveUser presentationUser;

    public LivecastViewManager(LCViewType viewType, String roomId, boolean activated, List<LCLiveUser> liveUsers, ModelUtil modelUtil, LCRoomMessageSender messageSender) {
        super(viewType, roomId, activated, liveUsers, modelUtil, messageSender);

        this.mainPublished = false;
        this.mainLiveId = null;
        this.mainFeedId = null;
        this.mainName = null;
        this.mainMirror = false;
        this.presentationUser = null;
    }

    @Override
    public Object getViewState() {
        return LCViewState.builder()
                .mainPublished(mainPublished)
                .mainLiveId(mainLiveId)
                .mainFeedId(mainFeedId)
                .mainName(mainName)
                .mainMirror(mainMirror)
                .presentationUser(presentationUser)
                .build();
    }

    @Override
    public void onUserExited(LCLiveUser liveUser) {
        super.onUserExited(liveUser);

        if((liveUser != null) && (this.containsLiveUser(liveUser))) {
            boolean stateChanged = false;

            if((this.mainPublished) && (this.mainLiveId != null) && (this.mainLiveId.equals(liveUser.getLiveId()))) {
                this.mainPublished = false;
                this.mainLiveId = null;
                this.mainFeedId = null;
                this.mainName = null;

                stateChanged = true;
            }

            if((this.presentationUser != null) && (this.presentationUser.getLiveId().equals(liveUser.getLiveId()))) {
                this.presentationUser = null;

                stateChanged = true;
            }

            if(stateChanged) {
                this.sendViewStateChangedEvent();
            }
        }
    }

    @Override
    public void onUserPresentation(LCLiveUser liveUser, boolean presentation) {
        super.onUserPresentation(liveUser, presentation);

        if(liveUser != null) {
            if(presentation) {
                this.presentationUser = liveUser;
                this.sendViewStateChangedEvent();

            } else if((presentationUser != null) && (presentationUser.getLiveId().equals(liveUser.getLiveId()))) {
                this.presentationUser = null;
                this.sendViewStateChangedEvent();
            }
        }
    }

    @Override
    public void onUserMediaStateChanged(LCLiveUser liveUser, LCUserMediaState newState, LCUserMediaState oldState) {
        super.onUserMediaStateChanged(liveUser, newState, oldState);

        if((liveUser != null) && (this.containsLiveUser(liveUser))) {
            if(this.isOperatingUser(liveUser)) {
                if(newState.isPublished()) {
                    this.mainPublished = true;
                    this.mainLiveId = liveUser.getLiveId();
                    this.mainFeedId = newState.getPublishId();
                    this.mainName = liveUser.getName();
                    this.mainMirror = newState.isCamMirror();

                    this.sendViewStateChangedEvent();
                } else if((!newState.isPublished()) && (this.mainPublished) && (this.mainLiveId != null) && (this.mainLiveId.equals(liveUser.getLiveId()))) {
                    this.mainPublished = false;
                    this.mainLiveId = null;
                    this.mainFeedId = null;
                    this.mainName = null;
                    this.mainMirror = false;

                    this.sendViewStateChangedEvent();
                }
            }

            if((this.presentationUser != null) && (this.presentationUser.getLiveId().equals(liveUser.getLiveId()))) {
                this.sendViewStateChangedEvent();
            }
        }
    }
}
