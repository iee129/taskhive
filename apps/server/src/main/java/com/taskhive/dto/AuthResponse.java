package com.taskhive.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {
    private String token;
    private String email;
    private String name;
}
