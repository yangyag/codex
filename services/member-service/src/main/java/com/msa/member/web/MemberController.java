package com.msa.member.web;

import com.msa.member.application.MemberService;
import com.msa.member.web.request.MemberCreateRequest;
import com.msa.member.web.request.MemberStatusUpdateRequest;
import com.msa.member.web.response.MemberSummaryResponse;
import com.msa.member.web.response.PageResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<MemberSummaryResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String q
    ) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<MemberSummaryResponse> result = memberService.list(q, pageable)
                .map(m -> new MemberSummaryResponse(
                        m.getId(),
                        m.getEmail(),
                        m.getName(),
                        m.getStatus().name(),
                        m.getCreatedAt()
                ));
        PageResponse<MemberSummaryResponse> response = new PageResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<MemberSummaryResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody MemberStatusUpdateRequest request
    ) {
        var updated = memberService.updateStatus(id.toString(), request);
        return ResponseEntity.ok(new MemberSummaryResponse(
                updated.getId(),
                updated.getEmail(),
                updated.getName(),
                updated.getStatus().name(),
                updated.getCreatedAt()
        ));
    }

    @PostMapping("/sync")
    public ResponseEntity<MemberSummaryResponse> syncMember(@Valid @RequestBody MemberCreateRequest request) {
        var saved = memberService.upsert(request);
        return ResponseEntity.ok(new MemberSummaryResponse(
                saved.getId(),
                saved.getEmail(),
                saved.getName(),
                saved.getStatus().name(),
                saved.getCreatedAt()
        ));
    }
}
