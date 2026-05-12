package com.taskhive.service;

import com.taskhive.model.*;
import com.taskhive.model.enums.ProjectMemberRole;
import com.taskhive.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataSeederService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public String seed() {
        if (userRepository.existsByEmail("test@example.com")) {
            log.info("Seed data already exists, skipping");
            return "already_seeded";
        }

        User owner = userRepository.save(User.builder()
                .name("Test User")
                .email("test@example.com")
                .password(passwordEncoder.encode("Test1234!"))
                .emailVerified(true)
                .build());

        User member = userRepository.save(User.builder()
                .name("Member User")
                .email("member@example.com")
                .password(passwordEncoder.encode("Test1234!"))
                .emailVerified(true)
                .build());

        Project project = projectRepository.save(Project.builder()
                .name("Demo Project")
                .description("테스트용 데모 프로젝트")
                .owner(owner)
                .build());

        projectMemberRepository.save(ProjectMember.builder()
                .project(project).user(owner).role(ProjectMemberRole.OWNER).build());
        projectMemberRepository.save(ProjectMember.builder()
                .project(project).user(member).role(ProjectMemberRole.MEMBER).build());

        taskRepository.save(Task.builder().title("로그인 API 구현").project(project)
                .status(Task.Status.TODO).priority(Task.Priority.HIGH).build());
        taskRepository.save(Task.builder().title("프론트엔드 레이아웃").project(project)
                .status(Task.Status.TODO).priority(Task.Priority.MEDIUM).build());
        taskRepository.save(Task.builder().title("DB 스키마 설계").project(project)
                .status(Task.Status.IN_PROGRESS).priority(Task.Priority.HIGH).build());
        taskRepository.save(Task.builder().title("Swagger 문서화").project(project)
                .status(Task.Status.IN_PROGRESS).priority(Task.Priority.LOW).build());
        taskRepository.save(Task.builder().title("배포 환경 구성").project(project)
                .status(Task.Status.DONE).priority(Task.Priority.MEDIUM).build());

        log.info("Seed data created: 2 users, 1 project, 5 tasks");
        return "seeded";
    }
}
