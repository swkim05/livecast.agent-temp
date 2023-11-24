package livecast.agent.model.message.client;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LCMessageUserPublishKickoffRequest {
    private String publishId;
}
