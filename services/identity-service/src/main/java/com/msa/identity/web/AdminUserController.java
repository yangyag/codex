package com.msa.identity.web;

import com.msa.identity.application.UserQueryService;
import com.msa.identity.web.response.PageResponse;
import com.msa.identity.web.response.UserSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final UserQueryService userQueryService;

    public AdminUserController(UserQueryService userQueryService) {
        this.userQueryService = userQueryService;
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
}
