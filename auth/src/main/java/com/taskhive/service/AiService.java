package com.taskhive.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskhive.dto.AiTaskRequest;
import com.taskhive.dto.BrainDumpItem;
import com.taskhive.dto.CommentRequest;
import com.taskhive.dto.CommentResponse;
import com.taskhive.dto.EstimateResponse;
import com.taskhive.dto.FilterParseResponse;
import com.taskhive.dto.PrioritizeItem;
import com.taskhive.dto.StandupResponse;
import com.taskhive.dto.TaskRequest;
import com.taskhive.dto.TaskResponse;
import com.taskhive.exception.BusinessException;
import com.taskhive.exception.ErrorCode;
import com.taskhive.model.Task;
import com.taskhive.model.TaskActivity;
import com.taskhive.model.User;
import com.taskhive.repository.CommentRepository;
import com.taskhive.repository.ProjectMemberRepository;
import com.taskhive.repository.TaskActivityRepository;
import com.taskhive.repository.TaskRepository;
import com.taskhive.repository.UserRepository;
import com.taskhive.service.ai.AiProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final AiProvider aiProvider;
    private final ObjectMapper objectMapper;
    private final TaskRepository taskRepository;
    private final CommentRepository commentRepository;
    private final TaskActivityRepository taskActivityRepository;
    private final CommentService commentService;
    private final TaskService taskService;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public List<StandupResponse> generateStandup(Long projectId, String requesterEmail) {
        if (!aiProvider.isAvailable()) {
            throw new BusinessException(ErrorCode.AI_UNAVAILABLE);
        }

        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, requester.getId())) {
            throw new BusinessException(ErrorCode.NOT_PROJECT_MEMBER);
        }

        LocalDateTime since = LocalDateTime.now().minusHours(24);
        List<TaskActivity> activities = taskActivityRepository.findByProjectIdAndOccurredAtAfter(projectId, since);

        if (activities.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, List<TaskActivity>> byActor = activities.stream()
                .collect(Collectors.groupingBy(TaskActivity::getActorEmail, LinkedHashMap::new, Collectors.toList()));

        List<StandupResponse> result = new ArrayList<>();
        for (Map.Entry<String, List<TaskActivity>> entry : byActor.entrySet()) {
            String actorEmail = entry.getKey();
            List<TaskActivity> userActivities = entry.getValue();

            User actor = userRepository.findByEmail(actorEmail).orElse(null);
            Long userId = actor != null ? actor.getId() : null;
            String name = actor != null ? actor.getName() : actorEmail;

            String activityLines = userActivities.stream()
                    .map(a -> "- [" + a.getAction() + "] " + (a.getTaskTitle() != null ? a.getTaskTitle() : "") +
                              (a.getDetail() != null && !a.getDetail().isBlank() ? ": " + a.getDetail() : ""))
                    .collect(Collectors.joining("\n"));

            String prompt = """
                    You are a standup assistant. Summarize the following team member's task activities from the last 24 hours in Korean in 1-2 sentences.
                    Focus on what they worked on and any notable changes. Be concise and direct.

                    Team member: %s
                    Activities:
                    %s

                    Respond with a concise summary only, no extra formatting.
                    """.formatted(name, activityLines);

            String summary;
            try {
                summary = aiProvider.generate(prompt);
                if (summary == null || summary.isBlank()) {
                    summary = "활동 요약을 생성할 수 없습니다.";
                }
            } catch (Exception e) {
                log.warn("스탠드업 요약 생성 실패 ({}): {}", actorEmail, e.getMessage());
                throw new BusinessException(ErrorCode.AI_UNAVAILABLE);
            }

            result.add(new StandupResponse(userId, name, summary));
        }

        return result;
    }

    public CommentResponse summarizeTask(Long taskId, String requesterEmail) {
        if (!aiProvider.isAvailable()) {
            throw new BusinessException(ErrorCode.AI_UNAVAILABLE);
        }

        Task task = taskRepository.findByIdAndDeletedAtIsNull(taskId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));

        List<String> commentLines = commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId).stream()
                .map(c -> "- [" + c.getAuthor().getEmail() + "] " + c.getContent())
                .collect(Collectors.toList());

        List<String> activityLines = taskActivityRepository.findByTaskIdOrderByOccurredAtDesc(taskId).stream()
                .limit(10)
                .map(a -> "- [" + a.getAction() + "] " + (a.getDetail() != null ? a.getDetail() : ""))
                .collect(Collectors.toList());

        String prompt = """
                You are a task management assistant. Summarize the following task in Korean in 2-3 sentences.
                Focus on the current state, key discussion points, and any important activities.

                Task title: %s
                Task description: %s
                Task status: %s
                Task priority: %s

                Comments:
                %s

                Recent activities:
                %s

                Respond with a concise summary only, no extra formatting.
                """.formatted(
                task.getTitle(),
                task.getDescription() != null ? task.getDescription() : "(없음)",
                task.getStatus(),
                task.getPriority(),
                commentLines.isEmpty() ? "(없음)" : String.join("\n", commentLines),
                activityLines.isEmpty() ? "(없음)" : String.join("\n", activityLines)
        );

        String summaryText;
        try {
            summaryText = aiProvider.generate(prompt);
            if (summaryText == null || summaryText.isBlank()) {
                summaryText = "AI 요약을 생성할 수 없습니다.";
            }
        } catch (Exception e) {
            log.warn("AI 요약 생성 실패: {}", e.getMessage());
            summaryText = "AI 요약을 생성할 수 없습니다.";
        }

        CommentRequest commentRequest = new CommentRequest();
        commentRequest.setContent("[AI 요약] " + summaryText);
        return commentService.addComment(taskId, commentRequest, requesterEmail);
    }

    public TaskRequest generateTask(AiTaskRequest request) {
        if (!aiProvider.isAvailable()) {
            return fallback(request);
        }

        String today = LocalDate.now().toString();
        String prompt = """
                You are a task management assistant. Extract structured task information from the user's description.
                Today's date is %s.
                Respond ONLY with valid JSON in this exact format:
                {"title": "...", "description": "...", "priority": "LOW|MEDIUM|HIGH", "dueDate": "YYYY-MM-DD or null"}

                For dueDate: parse natural language date hints (e.g. "이번 주 금요일", "내일", "다음 주", "tomorrow", "next week", "this Friday") relative to today's date.
                If no date is mentioned, set dueDate to null.

                User description: %s
                """.formatted(today, request.getDescription());

        try {
            String responseText = aiProvider.generate(prompt);
            if (responseText == null || responseText.isBlank()) {
                return fallback(request);
            }

            JsonNode parsed = objectMapper.readTree(responseText);

            TaskRequest task = new TaskRequest();
            task.setTitle(parsed.path("title").asText("AI 생성 태스크"));
            task.setDescription(parsed.path("description").asText(request.getDescription()));
            task.setPriority(parsePriority(parsed.path("priority").asText("MEDIUM")));
            task.setProjectId(request.getProjectId());

            String dueDateStr = parsed.path("dueDate").asText(null);
            if (dueDateStr != null && !dueDateStr.isBlank() && !dueDateStr.equalsIgnoreCase("null")) {
                try {
                    task.setDueDate(LocalDate.parse(dueDateStr));
                } catch (Exception ex) {
                    log.warn("dueDate 파싱 실패, null 유지: {}", dueDateStr);
                }
            }

            return task;
        } catch (Exception e) {
            log.warn("AI 응답 파싱 실패, 기본값 반환: {}", e.getMessage());
            return fallback(request);
        }
    }

    public FilterParseResponse parseFilter(String query) {
        if (!aiProvider.isAvailable()) {
            throw new BusinessException(ErrorCode.AI_UNAVAILABLE);
        }

        String today = LocalDate.now().toString();
        String prompt = """
                You are a task filter assistant. Today is %s.
                Parse the user's natural language query into filter parameters.
                Respond ONLY with valid JSON:
                {"status": "TODO|IN_PROGRESS|DONE|null", "priority": "LOW|MEDIUM|HIGH|null", "dueDateBefore": "YYYY-MM-DD|null"}

                Query: %s
                """.formatted(today, query);

        try {
            String responseText = aiProvider.generate(prompt);
            if (responseText == null || responseText.isBlank()) {
                return new FilterParseResponse(null, null, null);
            }

            JsonNode parsed = objectMapper.readTree(responseText);

            String status = nullableText(parsed.path("status"));
            String priority = nullableText(parsed.path("priority"));
            String dueDateBefore = nullableText(parsed.path("dueDateBefore"));

            return new FilterParseResponse(status, priority, dueDateBefore);
        } catch (Exception e) {
            log.warn("AI 필터 파싱 실패, 기본값 반환: {}", e.getMessage());
            return new FilterParseResponse(null, null, null);
        }
    }

    public List<BrainDumpItem> breakdown(String text, Long projectId) {
        if (!aiProvider.isAvailable()) {
            throw new BusinessException(ErrorCode.AI_UNAVAILABLE);
        }

        String prompt = """
                You are a task management assistant. Break down the following brain dump text into a list of actionable tasks.
                Respond ONLY with a valid JSON array in this exact format:
                [{"title": "...", "description": "...", "priority": "LOW|MEDIUM|HIGH"}, ...]

                Each item must have a clear, concise title and a brief description.
                Assign priority based on urgency and importance.

                Brain dump text:
                %s
                """.formatted(text);

        try {
            String responseText = aiProvider.generate(prompt);
            if (responseText == null || responseText.isBlank()) {
                return new ArrayList<>();
            }

            JsonNode parsed = objectMapper.readTree(responseText);
            if (!parsed.isArray()) {
                log.warn("브레인덤프 파싱 실패: 배열이 아님");
                return new ArrayList<>();
            }

            List<BrainDumpItem> items = new ArrayList<>();
            for (JsonNode node : parsed) {
                String title = node.path("title").asText("");
                String description = node.path("description").asText("");
                String priority = node.path("priority").asText("MEDIUM");
                if (!title.isBlank()) {
                    items.add(new BrainDumpItem(title, description, priority));
                }
            }
            return items;
        } catch (Exception e) {
            log.warn("브레인덤프 파싱 실패, 빈 리스트 반환: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<TaskResponse> createTasksFromBreakdown(List<BrainDumpItem> items, Long projectId, String requesterEmail) {
        List<TaskResponse> created = new ArrayList<>();
        for (BrainDumpItem item : items) {
            TaskRequest taskRequest = new TaskRequest();
            taskRequest.setTitle(item.title());
            taskRequest.setDescription(item.description());
            taskRequest.setPriority(parsePriority(item.priority()));
            taskRequest.setProjectId(projectId);
            created.add(taskService.createTask(taskRequest, requesterEmail));
        }
        return created;
    }

    public List<PrioritizeItem> prioritizeTasks(Long projectId, String requesterEmail) {
        if (!aiProvider.isAvailable()) {
            throw new BusinessException(ErrorCode.AI_UNAVAILABLE);
        }

        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, requester.getId())) {
            throw new BusinessException(ErrorCode.NOT_PROJECT_MEMBER);
        }

        List<Task> todoTasks = taskRepository.findByProjectIdAndStatusAndDeletedAtIsNull(projectId, Task.Status.TODO);
        if (todoTasks.isEmpty()) {
            return new ArrayList<>();
        }

        String taskList = todoTasks.stream()
                .map(t -> "- id=%d title=%s priority=%s".formatted(t.getId(), t.getTitle(), t.getPriority()))
                .collect(Collectors.joining("\n"));

        String prompt = """
                You are a task management assistant. Prioritize the following TODO tasks for a project.
                Respond ONLY with a valid JSON array in this exact format:
                [{"taskId": <number>, "reason": "<brief reason in Korean>"}, ...]

                Order by highest priority first. Include all tasks.

                Tasks:
                %s
                """.formatted(taskList);

        try {
            String responseText = aiProvider.generate(prompt);
            if (responseText == null || responseText.isBlank()) {
                return new ArrayList<>();
            }

            JsonNode parsed = objectMapper.readTree(responseText);
            if (!parsed.isArray()) {
                log.warn("우선순위화 파싱 실패: 배열이 아님");
                return new ArrayList<>();
            }

            List<PrioritizeItem> items = new ArrayList<>();
            for (JsonNode node : parsed) {
                long taskId = node.path("taskId").asLong(0);
                String reason = node.path("reason").asText("");
                if (taskId > 0) {
                    items.add(new PrioritizeItem(taskId, reason));
                }
            }
            return items;
        } catch (Exception e) {
            log.warn("우선순위화 파싱 실패, 빈 리스트 반환: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<TaskResponse> getBlockers(Long projectId, String requesterEmail) {
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, requester.getId())) {
            throw new BusinessException(ErrorCode.NOT_PROJECT_MEMBER);
        }

        LocalDateTime cutoff = LocalDateTime.now().minusDays(14);
        return taskRepository.findBlockers(projectId, cutoff).stream()
                .map(TaskResponse::from)
                .collect(Collectors.toList());
    }

    public EstimateResponse estimate(String title, String description) {
        if (!aiProvider.isAvailable()) {
            throw new BusinessException(ErrorCode.AI_UNAVAILABLE);
        }

        String today = LocalDate.now().toString();
        String prompt = """
                You are a task estimation assistant. Estimate the effort and duration for the following task.
                Today's date is %s.
                Respond ONLY with valid JSON in this exact format:
                {"effort": "S|M|L", "estimatedDays": <integer>, "suggestedDueDate": "YYYY-MM-DD"}

                effort: S = small (1-2 days), M = medium (3-7 days), L = large (8+ days)
                estimatedDays: number of working days needed
                suggestedDueDate: today + estimatedDays (skip weekends)

                Task title: %s
                Task description: %s
                """.formatted(today, title, description != null ? description : "");

        try {
            String responseText = aiProvider.generate(prompt);
            if (responseText == null || responseText.isBlank()) {
                return new EstimateResponse("M", 3, LocalDate.now().plusDays(3).toString());
            }

            JsonNode parsed = objectMapper.readTree(responseText);
            String effort = parsed.path("effort").asText("M");
            int estimatedDays = parsed.path("estimatedDays").asInt(3);
            String suggestedDueDate = nullableText(parsed.path("suggestedDueDate"));
            if (suggestedDueDate == null) {
                suggestedDueDate = LocalDate.now().plusDays(estimatedDays).toString();
            }
            return new EstimateResponse(effort, estimatedDays, suggestedDueDate);
        } catch (Exception e) {
            log.warn("AI 공수 추정 파싱 실패, 기본값 반환: {}", e.getMessage());
            return new EstimateResponse("M", 3, LocalDate.now().plusDays(3).toString());
        }
    }

    private String nullableText(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) return null;
        String text = node.asText(null);
        if (text == null || text.isBlank() || text.equalsIgnoreCase("null")) return null;
        return text;
    }

    private TaskRequest fallback(AiTaskRequest request) {
        TaskRequest task = new TaskRequest();
        task.setTitle(request.getDescription().length() > 50
                ? request.getDescription().substring(0, 50) + "..."
                : request.getDescription());
        task.setDescription(request.getDescription());
        task.setPriority(Task.Priority.MEDIUM);
        task.setProjectId(request.getProjectId());
        return task;
    }

    private Task.Priority parsePriority(String value) {
        try {
            return Task.Priority.valueOf(value.toUpperCase());
        } catch (Exception e) {
            return Task.Priority.MEDIUM;
        }
    }
}
