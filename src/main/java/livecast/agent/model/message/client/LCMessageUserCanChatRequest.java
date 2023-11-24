package livecast.agent.model.message.client;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LCMessageUserCanChatRequest {
    private String liveId;

    private boolean canChat;
}
