package com.taskhive.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskhive.model.ProjectWebhook;
import com.taskhive.repository.ProjectWebhookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookDeliveryService {

    private static final int MAX_FAILURES = 5;

    private final ProjectWebhookRepository webhookRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Async
    public CompletableFuture<Void> deliver(Long projectId, String eventType, Object payload) {
        List<ProjectWebhook> webhooks = webhookRepository.findByProjectIdAndEnabledTrue(projectId);
        for (ProjectWebhook webhook : webhooks) {
            if (!webhook.getEvents().contains(eventType)) continue;
            try {
                String body = objectMapper.writeValueAsString(Map.of(
                        "event", eventType,
                        "timestamp", Instant.now().toString(),
                        "payload", payload
                ));
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                if (webhook.getSecret() != null && !webhook.getSecret().isBlank()) {
                    headers.set("X-TaskHive-Signature", "sha256=" + hmacSha256(webhook.getSecret(), body));
                }
                restTemplate.exchange(webhook.getUrl(), HttpMethod.POST,
                        new HttpEntity<>(body, headers), String.class);

                // 성공 시 연속 실패 카운터 초기화
                if (webhook.getConsecutiveFailures() > 0) {
                    webhook.setConsecutiveFailures(0);
                    webhookRepository.save(webhook);
                }
            } catch (Exception e) {
                int failures = webhook.getConsecutiveFailures() + 1;
                webhook.setConsecutiveFailures(failures);
                if (failures >= MAX_FAILURES) {
                    webhook.setEnabled(false);
                    log.warn("웹훅 {}회 연속 실패 → 비활성화: {}", failures, webhook.getUrl());
                }
                webhookRepository.save(webhook);
                log.warn("웹훅 전달 실패 [{}]: {}", webhook.getUrl(), e.getMessage());
            }
        }
        return CompletableFuture.completedFuture(null);
    }

    public static String hmacSha256(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("HMAC-SHA256 계산 실패", e);
        }
    }
}
