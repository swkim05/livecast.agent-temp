package livecast.agent.model.message.client;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LCMessageUserHandUpRequest {
    private String liveId;

    private boolean handUp;
}
