package com.msa.member.application;

import com.msa.member.domain.Member;
import com.msa.member.domain.MemberRepository;
import com.msa.member.domain.MemberStatus;
import com.msa.member.web.request.MemberStatusUpdateRequest;
import com.msa.member.web.request.MemberCreateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public Page<Member> list(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return memberRepository.findAll(pageable);
        }
        return memberRepository.search(query, pageable);
    }

    @Transactional
    public Member updateStatus(String id, MemberStatusUpdateRequest request) {
        Member member = memberRepository.findById(java.util.UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("멤버를 찾을 수 없습니다: " + id));
        member.updateStatus(MemberStatus.valueOf(request.status()));
        return member;
    }

    @Transactional
    public Member upsert(MemberCreateRequest request) {
        return memberRepository.findByEmail(request.email())
                .orElseGet(() -> memberRepository.save(
                        new Member(request.email(), request.name(), MemberStatus.ACTIVE)
                ));
    }
}
