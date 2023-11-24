package livecast.agent.model.support;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LCForbiddenWord {
    private long id;
    private long userId;
    private long clientId;
    private String clientName;
    private String contents;
    private LocalDateTime createdDatetime;
    private LocalDateTime updatedDatetime;
}
