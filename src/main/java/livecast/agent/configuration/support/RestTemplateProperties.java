package livecast.agent.configuration.support;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "livecast.rest-template")
@Data
public class RestTemplateProperties {
    private int factoryReadTimeout;
    private int factoryConnTimeout;
    private int clientMaxConnTotal;
    private int clientMaxConnPerRoute;
}
