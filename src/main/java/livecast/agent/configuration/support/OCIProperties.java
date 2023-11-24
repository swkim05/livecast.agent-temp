package livecast.agent.configuration.support;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "training.oci.api")
@Data
public class OCIProperties {
    private String user;
    private String fingerprint;
    private String tenancy;
}
