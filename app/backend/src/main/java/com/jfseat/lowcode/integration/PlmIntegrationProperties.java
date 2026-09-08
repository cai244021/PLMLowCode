package com.jfseat.lowcode.integration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "plm.integration")
public record PlmIntegrationProperties(
        String baseUrl,
        String securityContext,
        String loginTicket
) {
}
