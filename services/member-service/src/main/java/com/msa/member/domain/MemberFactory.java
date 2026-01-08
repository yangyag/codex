package com.msa.member.domain;

import org.springframework.stereotype.Component;

@Component
public class MemberFactory {

    public Member createActiveMember(String email, String name) {
        return new Member(email, name, MemberStatus.ACTIVE);
    }
}
