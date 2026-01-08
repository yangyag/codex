package com.msa.member.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, UUID> {
    boolean existsByEmail(String email);
    Optional<Member> findByEmail(String email);

    @Query("select m from Member m where (:q is null or lower(m.email) like lower(concat('%', :q, '%')) or lower(m.name) like lower(concat('%', :q, '%')))")
    Page<Member> search(@Param("q") String query, Pageable pageable);
}
