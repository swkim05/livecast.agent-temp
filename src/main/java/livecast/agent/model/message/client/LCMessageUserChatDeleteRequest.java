package livecast.agent.model.message.client;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LCMessageUserChatDeleteRequest {
    private String from;
    private String num;
}
