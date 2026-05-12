package com.taskhive.aspect;

import com.taskhive.dto.TaskResponse;
import com.taskhive.model.TaskActivity;
import com.taskhive.repository.TaskActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class TaskActivityAspect {

    private final TaskActivityRepository taskActivityRepository;

    @AfterReturning(
            pointcut = "execution(* com.taskhive.service.TaskService.createTask(..))",
            returning = "result")
    public void afterCreate(Object result) {
        if (result instanceof TaskResponse task) {
            save(task.getId(), task.getTitle(), "CREATED", null);
        }
    }

    @AfterReturning(
            pointcut = "execution(* com.taskhive.service.TaskService.updateTask(..))",
            returning = "result")
    public void afterUpdate(Object result) {
        if (result instanceof TaskResponse task) {
            save(task.getId(), task.getTitle(), "UPDATED",
                    "상태: " + (task.getStatus() != null ? task.getStatus() : "-"));
        }
    }

    @AfterReturning("execution(* com.taskhive.service.TaskService.deleteTask(..))")
    public void afterDelete(JoinPoint jp) {
        Object[] args = jp.getArgs();
        if (args.length > 0 && args[0] instanceof Long id) {
            save(id, null, "DELETED", null);
        }
    }

    @AfterReturning(
            pointcut = "execution(* com.taskhive.service.CommentService.addComment(..))",
            returning = "result")
    public void afterComment(JoinPoint jp, Object result) {
        Object[] args = jp.getArgs();
        if (args.length > 0 && args[0] instanceof Long taskId) {
            save(taskId, null, "COMMENTED", null);
        }
    }

    private void save(Long taskId, String taskTitle, String action, String detail) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth != null ? auth.getName() : "system";
            taskActivityRepository.save(TaskActivity.builder()
                    .taskId(taskId)
                    .taskTitle(taskTitle)
                    .actorEmail(email)
                    .action(action)
                    .detail(detail)
                    .occurredAt(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.warn("활동 로그 저장 실패: {}", e.getMessage());
        }
    }
}
