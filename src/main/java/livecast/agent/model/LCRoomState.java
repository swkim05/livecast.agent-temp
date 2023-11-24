package livecast.agent.model;

import livecast.agent.model.support.LCRoomRecordState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LCRoomState {
    private boolean activated;
    private int activationId;
    private boolean freeJoin;

    private boolean started;
    private LocalDateTime startDatetime;
    private ZoneId timeZone;

    private LCRoomRecordState recordState;
    private int recordId;
    private LocalDateTime recordStartDatetime;
    private LocalDateTime recordEndDatetime;
    private ZoneId recordTimeZone;
}
