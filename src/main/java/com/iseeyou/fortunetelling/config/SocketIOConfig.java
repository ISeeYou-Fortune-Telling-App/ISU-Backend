package com.iseeyou.fortunetelling.config;

import com.corundumstudio.socketio.SocketIOServer;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class SocketIOConfig {
    @Value("${socketio.port}")
    private int socketIOPort;

    @Value("${socketio.host}")
    private String socketIOHost;

    @Bean
    public SocketIOServer socketIOServer() {
        com.corundumstudio.socketio.Configuration config =
                new com.corundumstudio.socketio.Configuration();
        config.setPort(socketIOPort);
        config.setHostname(socketIOHost);
        config.setOrigin("*");
        config.setMaxHttpContentLength(1024 * 1024 * 100); // 100MB max file

        SocketIOServer server = new SocketIOServer(config);
        return server;
    }
}
