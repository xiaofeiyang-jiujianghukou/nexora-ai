package com.nexora.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新个人信息请求
 */
@Data
public class UpdateProfileRequest {

    @Size(max = 64, message = "昵称最长 64 字符")
    private String nickname;

    @Size(max = 512, message = "头像URL过长")
    private String avatar;

    private String language;
}
