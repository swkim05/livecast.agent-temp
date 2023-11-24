package livecast.agent.model.message.agent;

import livecast.agent.model.LCRoomState;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LCMessageRoomStateChangedEvent {
    private String roomId;
    private LCRoomState state;
}
