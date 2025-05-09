package com.ustc.myy.springaitoolsdemo.service;

import java.util.List;

import com.ustc.myy.springaitoolsdemo.entity.User;

/**
 * 用户服务接口
 *
 * @author YangyangMiao
 * @email yangyangmiao666@icloud.com
 * @date 2025/4/19 22:18
 * @version 1.0
 */
public interface UserService {

    User getUserById(Long id);

    User getUserByUsername(String username);

    User getUserByEmail(String email);

    void createUser(User user);

    void updateUser(User user);

    void deleteUser(Long id);

    List<User> getAllUsers();
}
