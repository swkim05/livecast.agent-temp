package livecast.agent.model.srs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class SRSAudioInfo {
    private String code;
    private long sample_rate;
    private long channel;
    private String profile;
}
