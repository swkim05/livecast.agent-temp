package livecast.agent.repository.mapper;

import livecast.agent.model.BaseUser;
import livecast.agent.model.support.BaseUserType;

import java.util.List;

public interface UserMapper {
    BaseUser selectUser(String id);
    List<BaseUser> selectUsersWhereType(BaseUserType type);
    int insertUser(BaseUser account);
}
