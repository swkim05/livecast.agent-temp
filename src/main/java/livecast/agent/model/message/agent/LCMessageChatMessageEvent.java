package livecast.agent.model.message.agent;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LCMessageChatMessageEvent {
    private String fromLiveId;
    private long chatNum;
    private String message;
}
