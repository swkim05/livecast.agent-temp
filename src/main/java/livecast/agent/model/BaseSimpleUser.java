package livecast.agent.model;

import livecast.agent.model.support.BaseUserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseSimpleUser implements Serializable {
    public static final long serialVersionUID = 1L;

    private String id;
    private String clientId;
    private String email;
    private String name;
    private BaseUserType type;
    private boolean enabled;
    private LocalDateTime createdDatetime;
    private LocalDateTime updatedDatetime;
}
