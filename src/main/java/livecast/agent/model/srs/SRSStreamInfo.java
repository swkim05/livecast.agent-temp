package livecast.agent.model.srs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class SRSStreamInfo {
    private String id;
    private String name;
    private String vhost;
    private String app;
    private String tcUrl;
    private String url;
    private long live_ms;
    private long clients;
    private long frames;
    private long send_bytes;
    private long recv_bytes;
    private SRSPublishInfo publish;
    private SRSVideoInfo video;
    private SRSAudioInfo audio;
    private SRSKBpsInfo kbps;
}
