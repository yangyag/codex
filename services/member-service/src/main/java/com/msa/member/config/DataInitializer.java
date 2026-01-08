package com.msa.member.config;

import com.msa.member.domain.Member;
import com.msa.member.domain.MemberRepository;
import com.msa.member.domain.MemberStatus;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer {

    private final MemberRepository memberRepository;

    public DataInitializer(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @PostConstruct
    @Transactional
    public void seedMembers() {
        if (memberRepository.count() >= 100) {
            return;
        }
        List<Member> batch = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            String email = String.format("member%03d@example.com", i);
            if (memberRepository.existsByEmail(email)) {
                continue;
            }
            Member m = new Member(email, "사용자" + i, MemberStatus.ACTIVE);
            batch.add(m);
        }
        if (!batch.isEmpty()) {
            memberRepository.saveAll(batch);
        }
    }
}
