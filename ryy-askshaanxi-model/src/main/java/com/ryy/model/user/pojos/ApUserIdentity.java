package com.ryy.model.user.pojos;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("ap_user_identity")
public class ApUserIdentity {
    private static final long serialVersionUID = 1L;
    /**
     * 主键
     */
    @TableId(value = "id")
    private Long id;

    @TableField("real_name")
    private String realName;

    @TableField(value = "id_card")
    private String idCard;

    /**
     * 认证时间
     */
    @TableField("identity_time")
    private Date identityTime;
}
