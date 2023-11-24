package livecast.agent.model.message.agent;

import livecast.agent.model.LCLiveUser;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LCMessageUserExitedEvent {
    private String roomId;
    private LCLiveUser liveUser;
}
