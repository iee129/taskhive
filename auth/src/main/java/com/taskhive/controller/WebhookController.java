package com.taskhive.controller;

import com.taskhive.dto.WebhookRequest;
import com.taskhive.dto.WebhookResponse;
import com.taskhive.service.WebhookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookService webhookService;

    @PostMapping
    public ResponseEntity<WebhookResponse> create(@PathVariable Long projectId,
                                                   @Valid @RequestBody WebhookRequest req,
                                                   Authentication auth) {
        return ResponseEntity.ok(webhookService.create(projectId, req, auth.getName()));
    }

    @GetMapping
    public ResponseEntity<List<WebhookResponse>> list(@PathVariable Long projectId,
                                                       Authentication auth) {
        return ResponseEntity.ok(webhookService.list(projectId, auth.getName()));
    }

    @DeleteMapping("/{webhookId}")
    public ResponseEntity<Void> delete(@PathVariable Long projectId,
                                        @PathVariable Long webhookId,
                                        Authentication auth) {
        webhookService.delete(projectId, webhookId, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
