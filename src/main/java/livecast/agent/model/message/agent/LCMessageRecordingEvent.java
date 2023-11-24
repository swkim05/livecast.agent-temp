package livecast.agent.model.message.agent;

import livecast.agent.model.support.LCRoomRecordState;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LCMessageRecordingEvent {
    private String roomId;
    private int activationId;
    private int recordId;

    private LCRoomRecordState state;
    private LocalDateTime datetime;
    private ZoneId timeZone;
    private String downloadUrl;
}
