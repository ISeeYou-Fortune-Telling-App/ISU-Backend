package com.iseeyou.fortunetelling.service.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.security.core.Authentication;

public class LogoutSuccessEvent extends ApplicationEvent {

    private final Authentication authentication;

    public LogoutSuccessEvent(Object source, Authentication authentication) {
        super(source);
        this.authentication = authentication;
    }

    public Authentication getAuthentication() {
        return authentication;
    }

}
