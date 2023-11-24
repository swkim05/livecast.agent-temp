package livecast.agent.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;


@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {
    private TaskScheduler messageBrokerTaskScheduler;

    @Autowired
    public void setMessageBrokerTaskScheduler(TaskScheduler taskScheduler) {
        this.messageBrokerTaskScheduler = taskScheduler;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/agent").addInterceptors(new HandshakeInterceptor() {
            @Override
            public boolean beforeHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Map<String, Object> attributes) throws Exception {
                // Set ip attribute to WebSocket session
                if (serverHttpRequest instanceof ServletServerHttpRequest) {
                    ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) serverHttpRequest;
                    String ipAddress = servletRequest.getServletRequest().getHeader("X-FORWARDED-FOR");
                    if (ipAddress == null) {
                        ipAddress = servletRequest.getServletRequest().getRemoteAddr();
                    }
                    attributes.put("remoteAddr", ipAddress);
                    log.debug("address 1 : {}", ipAddress);
                } else {
                    attributes.put("remoteAddr", serverHttpRequest.getRemoteAddress());
                    log.debug("address 2 : {}", serverHttpRequest.getRemoteAddress());
                }

                return true;
            }

            @Override
            public void afterHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Exception e) {

            }
        }).setAllowedOrigins("*");

        log.debug("WebSocket endpoint added : /agent");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.setApplicationDestinationPrefixes("/agent");
        config.enableSimpleBroker("/topic")
                .setHeartbeatValue(new long[] {25 * 1000, 25 * 1000})
                .setTaskScheduler(messageBrokerTaskScheduler);

    }
}
