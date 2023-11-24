package livecast.agent.model.srs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class SRSVideoInfo {
    private String codec;
    private String profile;
    private String level;
    private long width;
    private long height;
}
