package com.taskhive.repository;

import com.taskhive.model.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class TaskRepositoryTest {

    @Autowired TaskRepository taskRepository;

    private Task saveTask(String title, Task.Status status, Task.Priority priority, boolean deleted) {
        Task task = Task.builder()
                .title(title)
                .status(status)
                .priority(priority)
                .build();
        if (deleted) task.setDeletedAt(java.time.LocalDateTime.now());
        return taskRepository.save(task);
    }

    @Test
    void findAllByDeletedAtIsNull_삭제된태스크제외() {
        saveTask("활성태스크", Task.Status.TODO, Task.Priority.MEDIUM, false);
        saveTask("삭제된태스크", Task.Status.TODO, Task.Priority.MEDIUM, true);

        List<Task> result = taskRepository.findAllByDeletedAtIsNull();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("활성태스크");
    }

    @Test
    void findByIdAndDeletedAtIsNull_존재하는태스크() {
        Task saved = saveTask("태스크", Task.Status.TODO, Task.Priority.MEDIUM, false);

        Optional<Task> result = taskRepository.findByIdAndDeletedAtIsNull(saved.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("태스크");
    }

    @Test
    void findByIdAndDeletedAtIsNull_삭제된태스크_빈결과() {
        Task saved = saveTask("삭제태스크", Task.Status.TODO, Task.Priority.MEDIUM, true);

        Optional<Task> result = taskRepository.findByIdAndDeletedAtIsNull(saved.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void countByDeletedAtIsNull_삭제제외카운트() {
        saveTask("태스크1", Task.Status.TODO, Task.Priority.MEDIUM, false);
        saveTask("태스크2", Task.Status.TODO, Task.Priority.MEDIUM, false);
        saveTask("삭제됨", Task.Status.TODO, Task.Priority.MEDIUM, true);

        assertThat(taskRepository.countByDeletedAtIsNull()).isEqualTo(2L);
    }

    @Test
    void countByStatusAndDeletedAtIsNull_상태별카운트() {
        saveTask("TODO1", Task.Status.TODO, Task.Priority.MEDIUM, false);
        saveTask("TODO2", Task.Status.TODO, Task.Priority.MEDIUM, false);
        saveTask("진행중", Task.Status.IN_PROGRESS, Task.Priority.MEDIUM, false);
        saveTask("완료(삭제)", Task.Status.DONE, Task.Priority.MEDIUM, true);

        assertThat(taskRepository.countByStatusAndDeletedAtIsNull(Task.Status.TODO)).isEqualTo(2L);
        assertThat(taskRepository.countByStatusAndDeletedAtIsNull(Task.Status.IN_PROGRESS)).isEqualTo(1L);
        assertThat(taskRepository.countByStatusAndDeletedAtIsNull(Task.Status.DONE)).isZero();
    }

    @Test
    void countByPriorityAndDeletedAtIsNull_우선순위별카운트() {
        saveTask("높음1", Task.Status.TODO, Task.Priority.HIGH, false);
        saveTask("높음2", Task.Status.TODO, Task.Priority.HIGH, false);
        saveTask("보통", Task.Status.TODO, Task.Priority.MEDIUM, false);
        saveTask("낮음(삭제)", Task.Status.TODO, Task.Priority.LOW, true);

        assertThat(taskRepository.countByPriorityAndDeletedAtIsNull(Task.Priority.HIGH)).isEqualTo(2L);
        assertThat(taskRepository.countByPriorityAndDeletedAtIsNull(Task.Priority.MEDIUM)).isEqualTo(1L);
        assertThat(taskRepository.countByPriorityAndDeletedAtIsNull(Task.Priority.LOW)).isZero();
    }

    @Test
    void findFiltered_상태필터() {
        saveTask("TODO태스크", Task.Status.TODO, Task.Priority.MEDIUM, false);
        saveTask("진행중태스크", Task.Status.IN_PROGRESS, Task.Priority.MEDIUM, false);

        List<Task> result = taskRepository.findFiltered(Task.Status.TODO, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("TODO태스크");
    }

    @Test
    void findFiltered_우선순위필터() {
        saveTask("HIGH태스크", Task.Status.TODO, Task.Priority.HIGH, false);
        saveTask("LOW태스크", Task.Status.TODO, Task.Priority.LOW, false);

        List<Task> result = taskRepository.findFiltered(null, Task.Priority.HIGH, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("HIGH태스크");
    }

    @Test
    void findFiltered_키워드검색() {
        saveTask("Spring Boot 프로젝트", Task.Status.TODO, Task.Priority.MEDIUM, false);
        saveTask("React 작업", Task.Status.TODO, Task.Priority.MEDIUM, false);

        List<Task> result = taskRepository.findFiltered(null, null, "spring");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).containsIgnoringCase("spring");
    }

    @Test
    void findFiltered_null필터_전체반환() {
        saveTask("태스크1", Task.Status.TODO, Task.Priority.MEDIUM, false);
        saveTask("태스크2", Task.Status.IN_PROGRESS, Task.Priority.HIGH, false);

        List<Task> result = taskRepository.findFiltered(null, null, null);

        assertThat(result).hasSize(2);
    }

    @Test
    void findFiltered_삭제된태스크제외() {
        saveTask("활성", Task.Status.TODO, Task.Priority.MEDIUM, false);
        saveTask("삭제됨", Task.Status.TODO, Task.Priority.MEDIUM, true);

        List<Task> result = taskRepository.findFiltered(null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("활성");
    }

    @Test
    void findFiltered_복합필터() {
        saveTask("HIGH IN_PROGRESS", Task.Status.IN_PROGRESS, Task.Priority.HIGH, false);
        saveTask("HIGH TODO", Task.Status.TODO, Task.Priority.HIGH, false);
        saveTask("LOW IN_PROGRESS", Task.Status.IN_PROGRESS, Task.Priority.LOW, false);

        List<Task> result = taskRepository.findFiltered(Task.Status.IN_PROGRESS, Task.Priority.HIGH, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("HIGH IN_PROGRESS");
    }
}
