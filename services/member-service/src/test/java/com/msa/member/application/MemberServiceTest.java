package com.msa.member.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.msa.member.application.command.SearchMembersCommand;
import com.msa.member.application.command.SyncMemberCommand;
import com.msa.member.application.command.UpdateMemberStatusCommand;
import com.msa.member.domain.Member;
import com.msa.member.domain.MemberFactory;
import com.msa.member.domain.MemberRepository;
import com.msa.member.domain.MemberStatus;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    private MemberService memberService;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(memberRepository, new MemberFactory());
    }

    @Test
    void list_uses_findAll_when_query_blank() {
        var pageable = PageRequest.of(0, 10);
        Page<Member> page = new PageImpl<>(java.util.List.of());
        given(memberRepository.findAll(pageable)).willReturn(page);

        var result = memberService.list(new SearchMembersCommand(null, pageable));

        assertThat(result).isEqualTo(page);
        verify(memberRepository).findAll(pageable);
        verify(memberRepository, never()).search(any(), any());
    }

    @Test
    void list_uses_search_when_query_present() {
        var pageable = PageRequest.of(0, 10);
        Page<Member> page = new PageImpl<>(java.util.List.of());
        given(memberRepository.search(eq("alpha"), eq(pageable))).willReturn(page);

        var result = memberService.list(new SearchMembersCommand("alpha", pageable));

        assertThat(result).isEqualTo(page);
        verify(memberRepository).search("alpha", pageable);
    }

    @Test
    void updateStatus_updates_member_status() throws Exception {
        Member member = new Member("user@example.com", "User", MemberStatus.ACTIVE);
        UUID id = UUID.randomUUID();
        var idField = Member.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(member, id);
        given(memberRepository.findById(id)).willReturn(Optional.of(member));

        var updated = memberService.updateStatus(new UpdateMemberStatusCommand(id, "BLOCKED"));

        assertThat(updated.getStatus()).isEqualTo(MemberStatus.BLOCKED);
    }

    @Test
    void updateStatus_throws_when_not_found() {
        UUID id = UUID.randomUUID();
        given(memberRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.updateStatus(new UpdateMemberStatusCommand(id, "BLOCKED")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void upsert_returns_existing_when_present() {
        Member existing = new Member("exist@example.com", "Existing", MemberStatus.ACTIVE);
        given(memberRepository.findByEmail(existing.getEmail())).willReturn(Optional.of(existing));

        var result = memberService.upsert(new SyncMemberCommand(existing.getEmail(), "Existing"));

        assertThat(result).isEqualTo(existing);
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    void upsert_saves_new_when_absent() {
        given(memberRepository.findByEmail("new@example.com")).willReturn(Optional.empty());
        given(memberRepository.save(any(Member.class))).willAnswer(invocation -> invocation.getArgument(0));

        var result = memberService.upsert(new SyncMemberCommand("new@example.com", "NewUser"));

        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(result.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        verify(memberRepository).save(any(Member.class));
    }
}
