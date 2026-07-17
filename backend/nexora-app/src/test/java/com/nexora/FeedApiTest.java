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
class FeedApiTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper om;

    private String token;

    @BeforeEach
    void login() throws Exception {
        if (token != null) return;
        RegisterRequest reg = new RegisterRequest();
        reg.setEmail("feedtest@nexora.ai"); reg.setPassword("test123456"); reg.setNickname("Feed");
        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsString(reg)));
        LoginRequest login = new LoginRequest();
        login.setEmail("feedtest@nexora.ai"); login.setPassword("test123456");
        String resp = mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsString(login)))
                .andReturn().getResponse().getContentAsString();
        token = om.readTree(resp).get("data").get("token").asText();
    }

    @Test
    void shouldReturnHomeFeed() throws Exception {
        mockMvc.perform(get("/api/v1/feed/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.hot").isArray());
    }

    @Test
    void shouldManageSubscriptions() throws Exception {
        // 创建
        String subReq = "{\"type\":\"TAG\",\"target\":\"AI\"}";
        String resp = mockMvc.perform(post("/api/v1/subscribe")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(subReq))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andReturn().getResponse().getContentAsString();
        int subId = om.readTree(resp).get("data").get("id").asInt();

        // 列表
        mockMvc.perform(get("/api/v1/subscribe/list")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(greaterThan(0)));

        // 删除
        mockMvc.perform(delete("/api/v1/subscribe/" + subId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
