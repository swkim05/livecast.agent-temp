package livecast.agent.model.message.agent;

import livecast.agent.model.support.LCViewType;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LCMessageViewStateChangedEvent {
    private String roomId;

    private LCViewType viewType;
    private Object viewState;
}
