package livecast.agent.model;

import livecast.agent.model.support.LCActivityType;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LCRoomActivationUserHistoryActivity {
    private String roomId;
    private int activationId;
    private long userId;
    private int historyId;
    private int productId;
    private int activityId;
    private LCActivityType activityType;
    private LocalDateTime activityDatetime;
    private ZoneId activityTimeZone;
    private String activityData;
    private LocalDateTime createdDatetime;
    private LocalDateTime updatedDatetime;
}
