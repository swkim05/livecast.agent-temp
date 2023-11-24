package livecast.agent.model;

import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LCRoomActivationUserHistory {
    private String roomId;
    private int activationId;
    private long userId;
    private int historyId;
    private String appId;
    private String name;
    private LocalDateTime enterDatetime;
    private LocalDateTime exitDatetime;
    private ZoneId timeZone;
    private LocalDateTime createdDatetime;
    private LocalDateTime updatedDatetime;
}
