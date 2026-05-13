package com.taskhive.service;

import com.taskhive.dto.*;
import com.taskhive.exception.BusinessException;
import com.taskhive.exception.ErrorCode;
import com.taskhive.model.*;
import com.taskhive.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository memberRepository;
    private final TaskStatusHistoryRepository statusHistoryRepository;
    private final WebhookDeliveryService webhookDeliveryService;
    private final LabelRepository labelRepository;

    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAllByDeletedAtIsNull().stream()
                .map(TaskResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getFilteredTasks(Task.Status status, Task.Priority priority, String search, Long labelId) {
        return taskRepository.findFiltered(status, priority, search, labelId).stream()
                .map(TaskResponse::from)
                .toList();
    }

    public TaskResponse getTask(Long id, String requesterEmail) {
        Task task = taskRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));
        checkProjectMembership(task, requesterEmail);
        return TaskResponse.from(task);
    }

    @Transactional
    public TaskResponse createTask(TaskRequest request, String requesterEmail) {
        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : Task.Status.TODO)
                .priority(request.getPriority() != null ? request.getPriority() : Task.Priority.MEDIUM)
                .dueDate(request.getDueDate())
                .build();

        if (request.getProjectId() != null) {
            Project project = projectRepository.findByIdAndDeletedAtIsNull(request.getProjectId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
            User requester = findUserByEmail(requesterEmail);
            if (!memberRepository.existsByProjectIdAndUserId(project.getId(), requester.getId())) {
                throw new BusinessException(ErrorCode.NOT_PROJECT_MEMBER);
            }
            task.setProject(project);
        }
        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            task.setAssignee(assignee);
        }
        TaskResponse response = TaskResponse.from(taskRepository.save(task));
        if (task.getProject() != null) {
            webhookDeliveryService.deliverAsync(task.getProject().getId(), "task.created", response);
        }
        return response;
    }

    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request, String requesterEmail) {
        Task task = taskRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));
        checkProjectMembership(task, requesterEmail);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        if (request.getStatus() != null && request.getStatus() != task.getStatus()) {
            statusHistoryRepository.save(TaskStatusHistory.builder()
                    .task(task)
                    .fromStatus(task.getStatus().name())
                    .toStatus(request.getStatus().name())
                    .changedBy(requesterEmail)
                    .changedAt(LocalDateTime.now())
                    .build());
            task.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());
        TaskResponse response = TaskResponse.from(taskRepository.save(task));
        if (task.getProject() != null) {
            webhookDeliveryService.deliverAsync(task.getProject().getId(), "task.updated", response);
        }
        return response;
    }

    @Transactional
    public void deleteTask(Long id, String requesterEmail) {
        Task task = taskRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));
        checkProjectMembership(task, requesterEmail);
        Long projectId = task.getProject() != null ? task.getProject().getId() : null;
        task.setDeletedAt(LocalDateTime.now());
        if (projectId != null) {
            webhookDeliveryService.deliverAsync(projectId, "task.deleted", id);
        }
    }

    @Transactional
    public void addLabelToTask(Long taskId, Long labelId, String requesterEmail) {
        Task task = taskRepository.findByIdAndDeletedAtIsNull(taskId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));
        checkProjectMembership(task, requesterEmail);
        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LABEL_NOT_FOUND));
        task.getLabels().add(label);
    }

    @Transactional
    public void removeLabelFromTask(Long taskId, Long labelId, String requesterEmail) {
        Task task = taskRepository.findByIdAndDeletedAtIsNull(taskId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));
        checkProjectMembership(task, requesterEmail);
        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LABEL_NOT_FOUND));
        task.getLabels().remove(label);
    }

    private void checkProjectMembership(Task task, String requesterEmail) {
        if (task.getProject() == null) return;
        User requester = findUserByEmail(requesterEmail);
        if (!memberRepository.existsByProjectIdAndUserId(task.getProject().getId(), requester.getId())) {
            throw new BusinessException(ErrorCode.NOT_PROJECT_MEMBER);
        }
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
