package livecast.agent.configuration;

import livecast.agent.configuration.support.TomcatProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ContainerConfiguration {
    @Bean
    public TomcatServletWebServerFactory servletContainer(TomcatProperties tomcatProperties) {
        log.info("Tomcat properties : {}", tomcatProperties);

        return new TomcatServletWebServerFactory() {
            @Override
            protected void customizeConnector(Connector connector) {
                super.customizeConnector(connector);

                connector.setProperty("maxThreads", 			tomcatProperties.getMaxThreads());
                connector.setProperty("minSpareThreads", 		tomcatProperties.getMinSpareThreads());
                connector.setProperty("maxConnections",		    tomcatProperties.getMaxConnections());
                connector.setProperty("connectionLinger", 	    tomcatProperties.getConnectionLingers());
                connector.setProperty("connectionTimeout", 	    tomcatProperties.getConnectionTimeout());
                connector.setProperty("keepAliveTimeout", 	    tomcatProperties.getKeepAliveTimeout());
                connector.setProperty("maxKeepAliveRequests",   tomcatProperties.getMaxKeepAliveRequests());
                connector.setProperty("server", 				tomcatProperties.getServerInfo());
            }
        };
    }
}
