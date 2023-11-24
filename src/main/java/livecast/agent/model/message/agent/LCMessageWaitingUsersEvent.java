package livecast.agent.model.message.agent;

import livecast.agent.model.LCWaitingUser;
import lombok.*;

import java.util.Collection;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LCMessageWaitingUsersEvent {
    private String roomId;
    private Collection<LCWaitingUser> users;
}
