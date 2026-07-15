package com.nexora.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubscribeRequest {

    @NotBlank
    private String type;   // TAG / ENTITY / CATEGORY / COMPANY

    @NotBlank
    private String target; // e.g. "AI", "OpenAI", "technology"
}
