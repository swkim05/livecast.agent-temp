package livecast.agent.model.message;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import livecast.agent.util.JsonStringRawDeserializer;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LivecastStringMessageContainer {
    private String from;
    private String to;
    private LivecastMessageType type;
    private String messageId;

    @JsonDeserialize(using = JsonStringRawDeserializer.class)
    private String message;
}
