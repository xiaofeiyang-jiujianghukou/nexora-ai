package com.nexora;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexora.user.dto.LoginRequest;
import com.nexora.user.dto.RegisterRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Auth 模块 API 集成测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String accessToken;
    private static String refreshToken;

    private static final String TEST_EMAIL = "test@nexora.ai";
    private static final String TEST_PASSWORD = "test123456";

    @Test
    @Order(1)
    void shouldRegisterSuccessfully() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail(TEST_EMAIL);
        req.setPassword(TEST_PASSWORD);
        req.setNickname("测试用户");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.userId").isNumber());
    }

    @Test
    @Order(2)
    void shouldRejectDuplicateEmail() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail(TEST_EMAIL);
        req.setPassword("anything");
        req.setNickname("重复");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(20004));
    }

    @Test
    @Order(3)
    void shouldLoginSuccessfully() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail(TEST_EMAIL);
        req.setPassword(TEST_PASSWORD);

        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.user.email").value(TEST_EMAIL))
                .andReturn()
                .getResponse()
                .getContentAsString();

        accessToken = objectMapper.readTree(response).get("data").get("token").asText();
        refreshToken = objectMapper.readTree(response).get("data").get("refreshToken").asText();
    }

    @Test
    @Order(4)
    void shouldRejectWrongPassword() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail(TEST_EMAIL);
        req.setPassword("wrongpassword");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(20003));
    }

    @Test
    @Order(5)
    void shouldRefreshToken() throws Exception {
        String body = "{\"refreshToken\":\"" + refreshToken + "\"}";

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    @Order(6)
    void shouldGetProfileWithValidToken() throws Exception {
        mockMvc.perform(get("/api/v1/user/profile")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.email").value(TEST_EMAIL));
    }

    @Test
    @Order(7)
    void shouldRejectWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/user/profile"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(8)
    void shouldRejectWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/user/profile")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isForbidden());
    }
}
