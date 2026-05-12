package com.taskhive.config;

import com.taskhive.service.DataSeederService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("demo")
@RequiredArgsConstructor
public class DemoSeederRunner implements CommandLineRunner {

    private final DataSeederService dataSeederService;

    @Override
    public void run(String... args) {
        log.info("Demo profile: running seed...");
        String result = dataSeederService.seed();
        log.info("Seed result: {}", result);
    }
}
