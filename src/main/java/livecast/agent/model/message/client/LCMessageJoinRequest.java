package livecast.agent.model.message.client;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LCMessageJoinRequest {
    private String roomId;
    private String entranceCode;
    private String name;
    private boolean force;
}
