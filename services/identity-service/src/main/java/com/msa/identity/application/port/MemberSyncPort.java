package com.msa.identity.application.port;

public interface MemberSyncPort {
    void syncMember(String email, String name);
}
