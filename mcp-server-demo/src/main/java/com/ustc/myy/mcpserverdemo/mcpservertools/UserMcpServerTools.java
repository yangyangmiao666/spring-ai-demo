package com.ustc.myy.mcpserverdemo.mcpservertools;

import com.ustc.myy.mcpserverdemo.entity.User;
import com.ustc.myy.mcpserverdemo.service.UserService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户服务mcp-server-tool
 *
 * @author YangyangMiao
 * @version 1.0
 * @email yangyangmiao666@outlook.com
 * @date 2025/4/19 22:20
 */
@Component
public class UserMcpServerTools {

    private final UserService userService;

    @Autowired
    public UserMcpServerTools(UserService userService) {
        this.userService = userService;
    }

    @Tool(name = "getUserById", description = "根据id获取用户信息")
    public User getUserById(Long id) {
        return userService.getUserById(id);
    }

    @Tool(name = "getUserByUsername", description = "根据用户名获取用户信息")
    public User getUserByUsername(String username) {
        return userService.getUserByUsername(username);
    }

    @Tool(name = "getUserByEmail", description = "根据邮箱获取用户信息")
    public User getUserByEmail(String email) {
        return userService.getUserByEmail(email);
    }

    @Tool(name = "createUser", description = "创建用户")
    public void createUser(User user) {
        userService.createUser(user);
    }

    @Tool(name = "updateUser", description = "更新用户信息")
    public void updateUser(User user) {
        userService.updateUser(user);
    }

    @Tool(name = "deleteUser", description = "删除用户")
    public void deleteUser(Long id) {
        userService.deleteUser(id);
    }

    @Tool(name = "getAllUsers", description = "获取所有用户信息")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }
}
