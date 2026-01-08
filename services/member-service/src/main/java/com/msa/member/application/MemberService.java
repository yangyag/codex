package com.msa.member.application;

import com.msa.member.application.command.SearchMembersCommand;
import com.msa.member.application.command.SyncMemberCommand;
import com.msa.member.application.command.UpdateMemberStatusCommand;
import com.msa.member.application.port.MemberUseCase;
import com.msa.member.domain.Member;
import com.msa.member.domain.MemberFactory;
import com.msa.member.domain.MemberRepository;
import com.msa.member.domain.MemberStatus;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService implements MemberUseCase {

    private final MemberRepository memberRepository;
    private final MemberFactory memberFactory;

    public MemberService(MemberRepository memberRepository, MemberFactory memberFactory) {
        this.memberRepository = memberRepository;
        this.memberFactory = memberFactory;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Member> list(SearchMembersCommand command) {
        if (command.query() == null || command.query().isBlank()) {
            return memberRepository.findAll(command.pageable());
        }
        return memberRepository.search(command.query(), command.pageable());
    }

    @Override
    @Transactional
    public Member updateStatus(UpdateMemberStatusCommand command) {
        UUID id = command.memberId();
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("멤버를 찾을 수 없습니다: " + id));
        member.updateStatus(MemberStatus.valueOf(command.status()));
        return member;
    }

    @Override
    @Transactional
    public Member upsert(SyncMemberCommand command) {
        return memberRepository.findByEmail(command.email())
                .orElseGet(() -> memberRepository.save(
                        memberFactory.createActiveMember(command.email(), command.name())
                ));
    }
}
