package livecast.agent.model;

import livecast.agent.model.support.LCRoomDeactivationType;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LCRoomActivation {
    private String roomId;
    private int activationId;
    private LocalDateTime activatedDatetime;
    private LocalDateTime startedDatetime;
    private LocalDateTime deactivatedDatetime;
    private ZoneId timeZone;
    private LCRoomDeactivationType deactivateType;
    private LocalDateTime createdDatetime;
    private LocalDateTime updatedDatetime;
}
