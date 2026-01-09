package com.msa.identity.web;

import com.msa.identity.application.UserQueryService;
import com.msa.identity.web.response.PageResponse;
import com.msa.identity.web.response.UserSummaryResponse;
import com.msa.identity.application.AdminUserService;
import com.msa.identity.web.request.UserStatusUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/users")
@Validated
public class AdminUserController {

    private final UserQueryService userQueryService;
    private final AdminUserService adminUserService;

    public AdminUserController(UserQueryService userQueryService, AdminUserService adminUserService) {
        this.userQueryService = userQueryService;
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<UserSummaryResponse>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<UserSummaryResponse> result = userQueryService.listUsers(pageable)
                .map(user -> new UserSummaryResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getRole().name(),
                        user.getStatus().name(),
                        user.getCreatedAt()
                ));

        PageResponse<UserSummaryResponse> response = new PageResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{email}/status")
    public ResponseEntity<UserSummaryResponse> updateStatus(
            @PathVariable String email,
            @org.springframework.web.bind.annotation.RequestBody @jakarta.validation.Valid UserStatusUpdateRequest request
    ) {
        var updated = adminUserService.updateStatus(email, request.status());
        return ResponseEntity.ok(new UserSummaryResponse(
                updated.getId(),
                updated.getEmail(),
                updated.getRole().name(),
                updated.getStatus().name(),
                updated.getCreatedAt()
        ));
    }
}
