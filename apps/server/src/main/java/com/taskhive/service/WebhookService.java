package com.taskhive.service;

import com.taskhive.dto.WebhookRequest;
import com.taskhive.dto.WebhookResponse;
import com.taskhive.exception.BusinessException;
import com.taskhive.exception.ErrorCode;
import com.taskhive.model.Project;
import com.taskhive.model.ProjectWebhook;
import com.taskhive.repository.ProjectMemberRepository;
import com.taskhive.repository.ProjectRepository;
import com.taskhive.repository.ProjectWebhookRepository;
import com.taskhive.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WebhookService {

    private final ProjectWebhookRepository webhookRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public WebhookResponse create(Long projectId, WebhookRequest req, String requesterEmail) {
        checkMembership(projectId, requesterEmail);
        validateSsrf(req.url());

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        String events = req.events() != null && !req.events().isBlank()
                ? req.events()
                : "task.created,task.updated,task.deleted";

        ProjectWebhook webhook = ProjectWebhook.builder()
                .project(project)
                .url(req.url())
                .secret(req.secret())
                .events(events)
                .enabled(true)
                .consecutiveFailures(0)
                .build();
        return WebhookResponse.from(webhookRepository.save(webhook));
    }

    public List<WebhookResponse> list(Long projectId, String requesterEmail) {
        checkMembership(projectId, requesterEmail);
        return webhookRepository.findByProjectId(projectId).stream()
                .map(WebhookResponse::from)
                .toList();
    }

    @Transactional
    public void delete(Long projectId, Long webhookId, String requesterEmail) {
        checkMembership(projectId, requesterEmail);
        ProjectWebhook webhook = webhookRepository.findById(webhookId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WEBHOOK_NOT_FOUND));
        if (!webhook.getProject().getId().equals(projectId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        webhookRepository.delete(webhook);
    }

    private void checkMembership(Long projectId, String email) {
        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND)).getId();
        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new BusinessException(ErrorCode.NOT_PROJECT_MEMBER);
        }
    }

    static void validateSsrf(String urlStr) {
        try {
            URL url = new URL(urlStr);
            String host = url.getHost();
            if (host == null || host.isBlank()) {
                throw new BusinessException(ErrorCode.INVALID_INPUT);
            }

            // 명시적 로컬호스트 체크
            if (host.equalsIgnoreCase("localhost") || host.equals("0.0.0.0")) {
                throw new BusinessException(ErrorCode.SSRF_BLOCKED);
            }

            InetAddress addr = InetAddress.getByName(host);
            if (addr.isLoopbackAddress() || addr.isSiteLocalAddress()
                    || addr.isLinkLocalAddress() || addr.isAnyLocalAddress()) {
                throw new BusinessException(ErrorCode.SSRF_BLOCKED);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        } catch (Exception e) {
            // DNS 해석 실패 등 → 허용 (외부 도메인일 가능성)
        }
    }
}
