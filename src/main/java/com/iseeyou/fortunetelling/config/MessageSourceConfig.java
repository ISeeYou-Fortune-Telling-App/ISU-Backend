package com.iseeyou.fortunetelling.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.nio.charset.StandardCharsets;

@Configuration
public class MessageSourceConfig {

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();

        // Set the base name for message files
        messageSource.setBasename("classpath:locales/messages");

        // Set default encoding
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());

        // Cache messages for 1 hour in production
        messageSource.setCacheSeconds(3600);

        // Fallback to system locale if message not found
        messageSource.setFallbackToSystemLocale(true);

        // Use code as default message if not found
        messageSource.setUseCodeAsDefaultMessage(false);

        return messageSource;
    }
}

