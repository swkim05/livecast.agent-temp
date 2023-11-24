package livecast.agent.model.message.agent;

import livecast.agent.model.message.LCErrorReason;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LCMessageErrorResponse {
    private LCErrorReason reason;
}
