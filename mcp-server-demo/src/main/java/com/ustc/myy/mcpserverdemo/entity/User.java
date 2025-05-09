package com.ustc.myy.mcpserverdemo.entity;

import java.util.Date;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户实体类
 *
 * @author YangyangMiao
 * @email yangyangmiao666@icloud.com
 * @date 2025/4/19 22:18
 * @version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("user")
public class User {
    @Id(keyType = KeyType.Auto)
    private Long id;
    private String username;
    private String email;
    private String password;
    private Date registrationDate;
    private Date lastLogin;
    private Date createTime;
    private Date updateTime;
    private boolean active;
}

