package com.msa.identity.integration;

import com.msa.identity.application.port.MemberSyncPort;
import com.msa.identity.config.MemberServiceProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class MemberSyncClient implements MemberSyncPort {

    private static final Logger log = LoggerFactory.getLogger(MemberSyncClient.class);

    private final RestClient restClient;

    public MemberSyncClient(MemberServiceProperties properties, RestClient.Builder builder) {
        this.restClient = builder.baseUrl(properties.getUrl()).build();
    }

    @Override
    public void syncMember(String email, String name) {
        try {
            restClient.post()
                    .uri("/api/v1/members/sync")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new MemberSyncRequest(email, name))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("멤버 동기화 실패: {}", e.getMessage());
        }
    }

    private record MemberSyncRequest(String email, String name) {}
}
