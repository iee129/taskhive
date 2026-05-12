package com.taskhive.controller;

import com.taskhive.service.DataSeederService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dev")
@Profile("dev")
@RequiredArgsConstructor
public class DevController {

    private final DataSeederService dataSeederService;

    @PostMapping("/seed")
    public ResponseEntity<Map<String, String>> seed() {
        String result = dataSeederService.seed();
        return ResponseEntity.ok(Map.of("result", result));
    }
}
