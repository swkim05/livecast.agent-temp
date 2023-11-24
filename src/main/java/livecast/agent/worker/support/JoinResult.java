package livecast.agent.worker.support;

import livecast.agent.model.LCLiveUser;
import livecast.agent.model.message.LCErrorReason;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class JoinResult {
    private LCErrorReason reason;
    private LCLiveUser liveUser;
}
