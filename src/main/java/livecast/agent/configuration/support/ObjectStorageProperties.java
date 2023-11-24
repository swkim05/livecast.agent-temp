package livecast.agent.configuration.support;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "training.object-storage")
@Data
public class ObjectStorageProperties {
    private String configFile;
    private String regionId;
    private String nameSpace;
    private String bucketName;
    private String accessUrl;
    private int accessExpireDays;
    private String accessExpireDaysTimeZone;
}
