package com.taskhive.service;

import com.taskhive.dto.CommentRequest;
import com.taskhive.dto.CommentResponse;
import com.taskhive.exception.BusinessException;
import com.taskhive.exception.ErrorCode;
import com.taskhive.model.Comment;
import com.taskhive.model.Task;
import com.taskhive.model.User;
import com.taskhive.repository.CommentRepository;
import com.taskhive.repository.TaskRepository;
import com.taskhive.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock CommentRepository commentRepository;
    @Mock TaskRepository taskRepository;
    @Mock UserRepository userRepository;

    @InjectMocks CommentService commentService;

    private Task buildTask(Long id) {
        Task task = Task.builder().title("태스크").status(Task.Status.TODO).priority(Task.Priority.MEDIUM).build();
        task.setId(id);
        return task;
    }

    private User buildUser(Long id, String email) {
        User user = User.builder().email(email).name("사용자").password("pw").build();
        user.setId(id);
        return user;
    }

    private Comment buildComment(Long id, String content, User author, Task task) {
        Comment comment = Comment.builder()
                .content(content)
                .author(author)
                .task(task)
                .build();
        comment.setId(id);
        return comment;
    }

    @Test
    void getComments_태스크없음_예외발생() {
        when(taskRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.getComments(99L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void getComments_댓글없음_빈리스트() {
        when(taskRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(buildTask(1L)));
        when(commentRepository.findByTaskIdOrderByCreatedAtAsc(1L)).thenReturn(List.of());

        assertThat(commentService.getComments(1L)).isEmpty();
    }

    @Test
    void getComments_댓글있음_목록반환() {
        Task task = buildTask(1L);
        User author = buildUser(1L, "author@test.com");
        Comment comment = buildComment(1L, "댓글 내용", author, task);

        when(taskRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(task));
        when(commentRepository.findByTaskIdOrderByCreatedAtAsc(1L)).thenReturn(List.of(comment));

        List<CommentResponse> result = commentService.getComments(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("댓글 내용");
        assertThat(result.get(0).getAuthorEmail()).isEqualTo("author@test.com");
    }

    @Test
    void addComment_태스크없음_예외발생() {
        when(taskRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.addComment(99L, new CommentRequest(), "user@test.com"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void addComment_사용자없음_예외발생() {
        when(taskRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(buildTask(1L)));
        when(userRepository.findByEmail("none@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.addComment(1L, new CommentRequest(), "none@test.com"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void addComment_정상저장_댓글반환() {
        Task task = buildTask(1L);
        User author = buildUser(1L, "author@test.com");
        CommentRequest req = new CommentRequest();
        req.setContent("새 댓글");

        Comment saved = buildComment(1L, "새 댓글", author, task);

        when(taskRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(task));
        when(userRepository.findByEmail("author@test.com")).thenReturn(Optional.of(author));
        when(commentRepository.save(any())).thenReturn(saved);

        CommentResponse result = commentService.addComment(1L, req, "author@test.com");

        assertThat(result.getContent()).isEqualTo("새 댓글");
        assertThat(result.getAuthorEmail()).isEqualTo("author@test.com");
        verify(commentRepository).save(any());
    }

    @Test
    void deleteComment_댓글없음_예외발생() {
        when(commentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.deleteComment(99L, "user@test.com"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void deleteComment_본인아님_FORBIDDEN예외() {
        Task task = buildTask(1L);
        User author = buildUser(1L, "author@test.com");
        Comment comment = buildComment(1L, "댓글", author, task);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.deleteComment(1L, "other@test.com"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN);
                });
    }

    @Test
    void deleteComment_본인_정상삭제() {
        Task task = buildTask(1L);
        User author = buildUser(1L, "author@test.com");
        Comment comment = buildComment(1L, "댓글", author, task);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        commentService.deleteComment(1L, "author@test.com");

        verify(commentRepository).deleteById(1L);
    }
}
