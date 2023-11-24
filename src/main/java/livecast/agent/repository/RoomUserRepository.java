package livecast.agent.repository;

import livecast.agent.model.LCRoomUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
@Repository
public class RoomUserRepository {
//    private RoomUserMapper mapper;

    @Autowired
    public RoomUserRepository(
//            RoomUserMapper mapper
    ) {
//        this.mapper = mapper;
    }

    public List<LCRoomUser> selectRoomUsers(String roomId) {
        return new ArrayList<>();
    }
    public LCRoomUser selectRoomUser(long userId) {
//        return mapper.selectRoomUser(userId);
        return new LCRoomUser();
    }
}
