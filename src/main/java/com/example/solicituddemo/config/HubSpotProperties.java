package com.example.solicituddemo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hubspot")
public record HubSpotProperties(String apiToken) {
}