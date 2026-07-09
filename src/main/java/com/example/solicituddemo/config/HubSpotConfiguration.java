package com.example.solicituddemo.config;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.solicituddemo.service.HubSpotClient;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableConfigurationProperties(HubSpotProperties.class)
public class HubSpotConfiguration {

    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    HttpClient httpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    @Bean
    HubSpotClient hubSpotClient(HttpClient httpClient, ObjectMapper objectMapper, HubSpotProperties properties) {
        return new HubSpotClient(httpClient, objectMapper, properties);
    }
}