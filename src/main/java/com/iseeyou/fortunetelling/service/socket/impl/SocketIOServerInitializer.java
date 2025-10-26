package com.iseeyou.fortunetelling.service.socket.impl;

import com.corundumstudio.socketio.SocketIOServer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SocketIOServerInitializer {

    private final SocketIOServer socketIOServer;

    @PostConstruct
    public void start() {
        socketIOServer.start();
        log.info("Socket.IO server started on port: {}",
                socketIOServer.getConfiguration().getPort());
    }

    @PreDestroy
    public void stop() {
        if (socketIOServer != null) {
            socketIOServer.stop();
            log.info("Socket.IO server stopped");
        }
    }

}
