package com.iseeyou.fortunetelling.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

@Configuration
@Slf4j
public class AIConfig {

    @Bean
    public RestTemplate restTemplate() {
        log.info("Configuring RestTemplate for AI service calls");

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000); // 10 seconds connection timeout
        factory.setReadTimeout(300000);    // 30 seconds read timeout

        RestTemplate restTemplate = new RestTemplate(factory);

        // Add error handler to log detailed errors
        restTemplate.setErrorHandler(new org.springframework.web.client.ResponseErrorHandler() {
            @Override
            public boolean hasError(org.springframework.http.client.ClientHttpResponse response)
                    throws java.io.IOException {
                return response.getStatusCode().is4xxClientError() ||
                       response.getStatusCode().is5xxServerError();
            }

            @Override
            public void handleError(org.springframework.http.client.ClientHttpResponse response)
                    throws java.io.IOException {
                log.error("RestTemplate error - Status: {}, Headers: {}",
                    response.getStatusCode(), response.getHeaders());
                throw new RuntimeException("HTTP Error: " + response.getStatusCode());
            }
        });

        log.info("RestTemplate configured successfully with timeouts: connect=10s, read=30s");
        return restTemplate;
    }

    @Bean
    public WebClient webClient() {
        log.info("Configuring WebClient for AI streaming calls");
        return WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10MB
                .build();
    }
}
