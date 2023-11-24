package livecast.agent.model.transfer;

import livecast.agent.model.LCRoom;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LCRoomTransfer {
    private boolean result;
    private LCRoom data;
    private String message;
    private Integer code;
}
