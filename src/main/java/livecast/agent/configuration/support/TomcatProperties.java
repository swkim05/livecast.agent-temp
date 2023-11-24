package livecast.agent.configuration.support;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="training.tomcat")
@Data
public class TomcatProperties {
    private String maxThreads;
    private String minSpareThreads;
    private String maxConnections;
    private String connectionLingers;
    private String connectionTimeout;
    private String keepAliveTimeout;
    private String maxKeepAliveRequests;
    private boolean allowOrigins;
    private String serverInfo;
}
