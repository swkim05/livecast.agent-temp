package livecast.agent.model.message.client;

import livecast.agent.model.support.LCRoomRecordState;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LCMessageRecordingReport {
    private String roomId;
    private int activationId;
    private int recordId;

    private LCRoomRecordState state;
    private String downloadUrl;
}
