package com.taskhive.service;

import com.taskhive.config.JwtUtil;
import com.taskhive.dto.*;
import com.taskhive.model.User;
import com.taskhive.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtUtil jwtUtil;
    @Mock AuthenticationManager authenticationManager;

    @InjectMocks AuthService authService;

    @Test
    void register_정상_저장후JWT반환() {
        RegisterRequest req = new RegisterRequest();
        req.setName("홍길동");
        req.setEmail("test@example.com");
        req.setPassword("password123");

        User saved = User.builder()
                .email("test@example.com")
                .name("홍길동")
                .password("encoded")
                .build();

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any())).thenReturn(saved);
        when(jwtUtil.generateToken("test@example.com")).thenReturn("jwt-token");

        AuthResponse response = authService.register(req);

        assertThat(response.getToken()).isNotNull();
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        verify(userRepository).save(any());
    }

    @Test
    void register_중복이메일_예외발생() {
        RegisterRequest req = new RegisterRequest();
        req.setName("홍길동");
        req.setEmail("dup@example.com");
        req.setPassword("password123");

        when(userRepository.existsByEmail("dup@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already in use");
    }

    @Test
    void login_정상_JWT반환() {
        AuthRequest req = new AuthRequest();
        req.setEmail("test@example.com");
        req.setPassword("password123");

        User user = User.builder()
                .email("test@example.com")
                .name("홍길동")
                .password("encoded")
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("test@example.com")).thenReturn("jwt-token");

        AuthResponse response = authService.login(req);

        assertThat(response.getToken()).isNotNull();
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void getMe_정상_사용자정보반환() {
        User user = User.builder()
                .email("test@example.com")
                .name("홍길동")
                .password("encoded")
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        AuthResponse response = authService.getMe("test@example.com");

        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getName()).isEqualTo("홍길동");
        assertThat(response.getToken()).isNull();
    }

    @Test
    void getMe_없는이메일_예외발생() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getMe("unknown@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void login_잘못된자격증명_예외발생() {
        AuthRequest req = new AuthRequest();
        req.setEmail("test@example.com");
        req.setPassword("wrong");

        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any());

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BadCredentialsException.class);
    }
}
