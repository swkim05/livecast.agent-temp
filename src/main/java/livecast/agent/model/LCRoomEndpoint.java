package livecast.agent.model;

import livecast.agent.model.support.LCRoomEndpointType;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LCRoomEndpoint {
    private String roomId;
    private LCRoomEndpointType type;
    private String endpoint;
    private LocalDateTime createdDatetime;
    private LocalDateTime updatedDatetime;
}
