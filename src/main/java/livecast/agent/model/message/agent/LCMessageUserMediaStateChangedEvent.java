package livecast.agent.model.message.agent;

import livecast.agent.model.LCUserMediaState;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LCMessageUserMediaStateChangedEvent {
    private String liveId;

    private LCUserMediaState state;
}
