package com.nexora.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_subscription")
public class UserSubscriptionDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** TAG / ENTITY / CATEGORY / COMPANY */
    private String type;

    private String target;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}
