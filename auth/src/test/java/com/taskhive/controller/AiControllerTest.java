package com.taskhive.controller;

import com.taskhive.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"spring.profiles.active=test"})
@AutoConfigureMockMvc
class AiControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean EmailService emailService;

    @Test
    void capabilities_인증없이_호출가능하고_NoopProvider_반환() throws Exception {
        mockMvc.perform(get("/api/ai/capabilities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false))
                .andExpect(jsonPath("$.provider").value("none"))
                .andExpect(jsonPath("$.cloudProvider").value(false));
    }
}
