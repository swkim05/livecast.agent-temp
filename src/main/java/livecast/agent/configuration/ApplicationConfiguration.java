package livecast.agent.configuration;

import livecast.agent.configuration.support.OCIProperties;
import livecast.agent.configuration.support.RestTemplateProperties;
import livecast.agent.util.DateTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

import java.security.cert.X509Certificate;

@Slf4j
@Configuration
@EnableAsync
public class ApplicationConfiguration implements AsyncConfigurer {

    private OCIProperties ociProperties;

    @Autowired
    public ApplicationConfiguration(OCIProperties ociProperties) {
        this.ociProperties = ociProperties;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateProperties restTemplateProperties) throws Exception {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        HttpClient client = HttpClientBuilder.create()
                .setMaxConnTotal(restTemplateProperties.getClientMaxConnTotal())
                .setMaxConnPerRoute(restTemplateProperties.getClientMaxConnPerRoute())
                .setSSLHostnameVerifier(new NoopHostnameVerifier())
                .setSSLContext(SSLContexts.custom()
                        .loadTrustMaterial(null, (X509Certificate[] x509Certificates, String s) -> true)
                        .build()
                )
                .build();

        factory.setHttpClient(client);
        factory.setConnectTimeout(restTemplateProperties.getFactoryConnTimeout());
        factory.setReadTimeout(restTemplateProperties.getFactoryReadTimeout());

        return new RestTemplate(factory);
    }

    @Bean
    public DateTimeUtil dateTimeUtil() {
        return new DateTimeUtil();
    }
}
