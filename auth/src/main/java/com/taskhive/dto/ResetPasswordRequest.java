package com.taskhive.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ResetPasswordRequest {
    @NotBlank
    private String token;
    @NotBlank @Size(min = 8)
    private String newPassword;
}
