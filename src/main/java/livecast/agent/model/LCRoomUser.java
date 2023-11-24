package livecast.agent.model;

import livecast.agent.model.support.LCRoomUserType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LCRoomUser {
    private long id;
    private String roomId;
    private String appId;
    private LCRoomUserType type;
    private String name;
    private String nickName;
    private String pinCode;
//    private List<OTLExtra> extras;
    private LocalDateTime createdDatetime;
    private LocalDateTime updatedDatetime;
}
