package com.msa.gateway.security;

import java.util.Arrays;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "gateway.security")
public class GatewaySecurityProperties {

    private List<String> openPaths = Arrays.asList(
            "/api/v1/auth/login",
            "/api/v1/auth/signup",
            "/api/v1/members/sync",
            "/actuator/**");
    private List<String> adminPaths = Arrays.asList(
            "/api/v1/members/**",
            "/api/v1/admin/**");

    public List<String> getOpenPaths() {
        return openPaths;
    }

    public void setOpenPaths(List<String> openPaths) {
        this.openPaths = openPaths;
    }

    public List<String> getAdminPaths() {
        return adminPaths;
    }

    public void setAdminPaths(List<String> adminPaths) {
        this.adminPaths = adminPaths;
    }
}
