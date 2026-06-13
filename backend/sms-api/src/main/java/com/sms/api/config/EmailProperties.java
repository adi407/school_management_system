package com.sms.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.email")
public record EmailProperties(
    String from,
    String adminTo,
    String frontendUrl,
    boolean enabled
) {}
