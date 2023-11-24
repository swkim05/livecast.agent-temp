package livecast.agent.repository;

import livecast.agent.model.LCRoomActivation;
import livecast.agent.model.LCRoomActivationUserHistory;
import livecast.agent.model.LCRoomActivationUserHistoryActivity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class RoomActivationRepository {
//    private RoomActivationMapper mapper;

    @Autowired
    public RoomActivationRepository(
//            RoomActivationMapper mapper
    ) {
//        this.mapper = mapper;
    }

    public LCRoomActivation selectRoomActivation(String roomId, int activationId) {
//        return mapper.selectRoomActivation(roomId, activationId);
        return new LCRoomActivation();
    }

    public LCRoomActivationUserHistory selectRoomActivationUserHistory(String roomId, int activationId, long userId, int historyId) {
//        return mapper.selectRoomActivationUserHistory(roomId, activationId, userId, historyId);
        return new LCRoomActivationUserHistory();
    }

    public int insertRoomActivation(LCRoomActivation roomActivation) {
//        return mapper.insertRoomActivation(roomActivation);
        return 1;
    }

    public int insertRoomActivationUserHistory(LCRoomActivationUserHistory userHistory) {
//        return mapper.insertRoomActivationUserHistory(userHistory);
        return 1;
    }

    public int insertRoomActivationUserHistoryActivity(LCRoomActivationUserHistoryActivity activity) {
//        return mapper.insertRoomActivationUserHistoryActivity(activity);
        return 1;
    }

    public int updateRoomActivationStartedDatetime(LCRoomActivation roomActivation) {
//        return mapper.updateRoomActivationStartedDatetime(roomActivation);
        return 1;
    }

    public int updateRoomActivationDeactivatedDatetime(LCRoomActivation roomActivation) {
//        return mapper.updateRoomActivationDeactivatedDatetime(roomActivation);
        return 1;
    }

    public int updateRoomActivationUserHistoryExitDatetime(LCRoomActivationUserHistory userHistory) {
//        return mapper.updateRoomActivationUserHistoryExitDatetime(userHistory);
        return 1;
    }
}
