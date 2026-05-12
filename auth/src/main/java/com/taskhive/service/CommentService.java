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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public List<CommentResponse> getComments(Long taskId) {
        taskRepository.findByIdAndDeletedAtIsNull(taskId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));
        return commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId).stream()
                .map(CommentResponse::from)
                .toList();
    }

    @Transactional
    public CommentResponse addComment(Long taskId, CommentRequest request, String authorEmail) {
        Task task = taskRepository.findByIdAndDeletedAtIsNull(taskId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));
        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Comment comment = Comment.builder()
                .content(request.getContent())
                .task(task)
                .author(author)
                .build();
        return CommentResponse.from(commentRepository.save(comment));
    }

    @Transactional
    public void deleteComment(Long commentId, String requesterEmail) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));
        if (!comment.getAuthor().getEmail().equals(requesterEmail)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        commentRepository.deleteById(commentId);
    }
}
