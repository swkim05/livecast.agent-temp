package livecast.agent.configuration.support;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "livecast.application")
@Data
public class ApplicationProperties {
    private String encoding;
    private int networkConnTimeout;
    private int networkReadTimeout;
    private int maxWaitingTimeout;
}
