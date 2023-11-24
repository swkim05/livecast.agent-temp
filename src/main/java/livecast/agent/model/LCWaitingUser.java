package livecast.agent.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import livecast.agent.model.support.LCRoomUserType;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LCWaitingUser {
    private String sessionId;
    private String appId;
    private String userId;
    private String name;
    private LCRoomUserType type;
    private LocalDateTime connectDatetime;
    @JsonIgnore
    private String userAgent;
    @JsonIgnore
    private boolean joinable;
}
