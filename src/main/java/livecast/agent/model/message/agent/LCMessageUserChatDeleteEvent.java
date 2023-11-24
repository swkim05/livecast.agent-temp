package livecast.agent.model.message.agent;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LCMessageUserChatDeleteEvent {
    private String from;
    private String num;
}
