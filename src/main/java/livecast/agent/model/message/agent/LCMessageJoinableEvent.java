package livecast.agent.model.message.agent;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LCMessageJoinableEvent {
    private String roomId;
}
