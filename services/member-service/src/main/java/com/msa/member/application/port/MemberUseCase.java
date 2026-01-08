package com.msa.member.application.port;

import com.msa.member.application.command.SearchMembersCommand;
import com.msa.member.application.command.SyncMemberCommand;
import com.msa.member.application.command.UpdateMemberStatusCommand;
import com.msa.member.domain.Member;
import org.springframework.data.domain.Page;

public interface MemberUseCase {
    Page<Member> list(SearchMembersCommand command);
    Member updateStatus(UpdateMemberStatusCommand command);
    Member upsert(SyncMemberCommand command);
}
