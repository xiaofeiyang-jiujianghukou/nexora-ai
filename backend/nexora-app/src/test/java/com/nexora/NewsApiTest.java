package com.nexora;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexora.user.dto.LoginRequest;
import com.nexora.user.dto.RegisterRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class NewsApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String getToken() throws Exception {
        // 每次登录拿新 token
        LoginRequest login = new LoginRequest();
        login.setEmail("newstest@nexora.ai");
        login.setPassword("test123456");
        String resp = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andReturn().getResponse().getContentAsString();
        // 如果登录失败(用户不存在)，先注册
        if (objectMapper.readTree(resp).get("code").asInt() != 0) {
            RegisterRequest reg = new RegisterRequest();
            reg.setEmail("newstest@nexora.ai");
            reg.setPassword("test123456");
            reg.setNickname("新闻测试");
            mockMvc.perform(post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reg)));
            // 再次登录
            resp = mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(login)))
                    .andReturn().getResponse().getContentAsString();
        }
        return objectMapper.readTree(resp).get("data").get("token").asText();
    }

    @Test
    @Order(1)
    void shouldReturnNewsList() throws Exception {
        mockMvc.perform(get("/api/v1/news/list")
                        .param("page", "1").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list").isArray());
    }

    @Test
    @Order(2)
    void shouldReturnCategories() throws Exception {
        mockMvc.perform(get("/api/v1/news/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(greaterThan(0)));
    }

    @Test
    @Order(3)
    void shouldReturn404ForNonexistentNews() throws Exception {
        mockMvc.perform(get("/api/v1/news/99999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(30001));
    }

    @Test
    @Order(4)
    void shouldAddAndListFavorites() throws Exception {
        String token = getToken();
        mockMvc.perform(post("/api/v1/news/1/favorite")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(30001));

        mockMvc.perform(get("/api/v1/favorites")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @Order(5)
    void shouldRejectFavoriteWithoutToken() throws Exception {
        mockMvc.perform(post("/api/v1/news/1/favorite"))
                .andExpect(status().isBadRequest());
    }
}
