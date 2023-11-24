package livecast.agent.model.message.client;

import livecast.agent.model.LCUserMediaState;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LCMessageUserMediaStateReport {
    private LCUserMediaState state;
}
