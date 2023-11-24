package livecast.agent.model.message;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LivecastObjectMessageContainer {
    private String from;
    private LivecastMessageType type;
    private String messageId;

    private Object message;
}
