package livecast.agent.model;

import livecast.agent.model.support.BaseUserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseUser {
    private String id;
    private String clientId;
    private String email;
    private String password;
    private String name;
    private BaseUserType type;
    private boolean enabled;
    private LocalDateTime createdDatetime;
    private LocalDateTime updatedDatetime;
}
