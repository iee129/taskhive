package com.taskhive.controller;

import com.taskhive.dto.TokenCreateRequest;
import com.taskhive.dto.TokenCreateResponse;
import com.taskhive.dto.TokenListResponse;
import com.taskhive.service.PersonalAccessTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/settings/tokens")
@RequiredArgsConstructor
public class PersonalAccessTokenController {

    private final PersonalAccessTokenService patService;

    @PostMapping
    public ResponseEntity<TokenCreateResponse> create(@Valid @RequestBody TokenCreateRequest req,
                                                       Authentication auth) {
        return ResponseEntity.ok(patService.createToken(auth.getName(), req.name()));
    }

    @GetMapping
    public ResponseEntity<List<TokenListResponse>> list(Authentication auth) {
        return ResponseEntity.ok(patService.listTokens(auth.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revoke(@PathVariable Long id, Authentication auth) {
        patService.revokeToken(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
