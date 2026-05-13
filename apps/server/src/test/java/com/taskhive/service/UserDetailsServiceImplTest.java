package com.taskhive.service;

import com.taskhive.model.User;
import com.taskhive.model.enums.Role;
import com.taskhive.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock UserRepository userRepository;

    @InjectMocks UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsername_사용자존재_UserDetails반환() {
        User user = User.builder()
                .email("user@test.com")
                .name("테스터")
                .password("encoded-password")
                .build();

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername("user@test.com");

        assertThat(result.getUsername()).isEqualTo("user@test.com");
        assertThat(result.getPassword()).isEqualTo("encoded-password");
        assertThat(result.getAuthorities()).isNotEmpty();
    }

    @Test
    void loadUserByUsername_사용자없음_예외발생() {
        when(userRepository.findByEmail("none@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("none@test.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void loadUserByUsername_ADMIN역할_권한포함() {
        User admin = User.builder()
                .email("admin@test.com")
                .name("관리자")
                .password("encoded")
                .role(Role.ADMIN)
                .build();

        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));

        UserDetails result = userDetailsService.loadUserByUsername("admin@test.com");

        assertThat(result.getAuthorities()).anyMatch(a -> a.getAuthority().contains("ADMIN"));
    }
}
