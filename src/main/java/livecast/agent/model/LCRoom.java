package livecast.agent.model;

import livecast.agent.model.support.LCRoomStatus;
import lombok.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LCRoom { // = live_broadcast
    private String code;
    private String onAirYn;
    private String channelCode;
    private String hostUserid;
    private String title;
    private String notification;
    private String requiredLoginYn;
    private ZonedDateTime liveStartDt;
    private String duration;
    private ZonedDateTime realStartDt;
    private ZonedDateTime realEndDt;
    private ZonedDateTime regDt;
    private String regUserid;
    private ZonedDateTime updDt;
    private String updUserid;
    private String description;
    private String hostUsername;
    private String hostNickname;
    private Integer maxConcurrent;
    private Integer currentConcurrent;

    private boolean autoActivation;
    private ZoneId timeZone; // 사용 안할수도
    private String notifyUrl;
    private LCRoomStatus status;
}
