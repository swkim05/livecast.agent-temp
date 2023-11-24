package livecast.agent.model;

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
public class LCRoomNotify {
    public enum State {
        Activated, Started, Deactivated,
    }

    private String roomId;
    private int activationId;
    private State state;
    private ZoneId timeZone;
    private LocalDateTime time;
}
