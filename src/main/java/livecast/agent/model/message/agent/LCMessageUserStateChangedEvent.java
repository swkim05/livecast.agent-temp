package livecast.agent.model.message.agent;

import livecast.agent.model.LCUserState;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LCMessageUserStateChangedEvent {
    private String liveId;

    private LCUserState state;
}
