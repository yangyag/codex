package com.msa.identity.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "member.service")
public class MemberServiceProperties {
    /**
     * member-service base URL (e.g., http://localhost:8082)
     */
    private String url = "http://localhost:8082";

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
