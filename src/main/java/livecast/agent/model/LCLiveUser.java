package livecast.agent.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import livecast.agent.model.support.LCRoomUserType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LCLiveUser {
    @JsonIgnore
    private String sessionId;

    private String liveId;
    private String appId;
    private String userId;
    private int historyId;
    private String name;
    private LCRoomUserType type;
//    private List<OTLExtra> extras;

    private boolean joined;
    private LocalDateTime joinDatetime;

    private LCUserMediaState mediaState;

    private boolean muted;
    private boolean handUp;
    private boolean canChat;
    private boolean blocked;
}
