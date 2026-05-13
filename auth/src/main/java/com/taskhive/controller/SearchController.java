package com.taskhive.controller;

import com.taskhive.dto.SearchResult;
import com.taskhive.model.User;
import com.taskhive.repository.ProjectMemberRepository;
import com.taskhive.repository.ProjectRepository;
import com.taskhive.repository.TaskRepository;
import com.taskhive.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository memberRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<SearchResult>> search(@RequestParam String q, Authentication auth) {
        if (q == null || q.isBlank()) return ResponseEntity.ok(List.of());

        User user = userRepository.findByEmail(auth.getName()).orElseThrow();
        List<Long> projectIds = memberRepository.findProjectIdsByUserId(user.getId());

        List<SearchResult> results = new ArrayList<>();

        if (!projectIds.isEmpty()) {
            taskRepository.searchByKeyword(q, projectIds).stream()
                    .map(t -> new SearchResult("task", t.getId(), t.getTitle(),
                            t.getProject() != null ? t.getProject().getName() : ""))
                    .forEach(results::add);

            projectRepository.searchByKeyword(q, projectIds).stream()
                    .map(p -> new SearchResult("project", p.getId(), p.getName(),
                            p.getDescription() != null ? p.getDescription() : ""))
                    .forEach(results::add);
        }

        return ResponseEntity.ok(results.subList(0, Math.min(results.size(), 20)));
    }
}
