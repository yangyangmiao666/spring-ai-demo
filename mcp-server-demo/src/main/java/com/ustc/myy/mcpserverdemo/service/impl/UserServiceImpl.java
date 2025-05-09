package com.ustc.myy.mcpserverdemo.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mybatisflex.core.query.QueryWrapper;
import com.ustc.myy.mcpserverdemo.entity.User;
import static com.ustc.myy.mcpserverdemo.entity.table.UserTableDef.USER;
import com.ustc.myy.mcpserverdemo.mapper.UserMapper;
import com.ustc.myy.mcpserverdemo.service.UserService;


/**
 * 用户服务实现类
 *
 * @author YangyangMiao
 * @email yangyangmiao666@icloud.com
 * @date 2025/4/19 22:18
 * @version 1.0
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Autowired
    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public User getUserById(Long id) {
        return userMapper.selectOneById(id);
    }

    @Override
    public User getUserByUsername(String username) {
        QueryWrapper wrapper = QueryWrapper.create();
        wrapper.select().where(USER.USERNAME.eq(username));
        return userMapper.selectOneByQuery(wrapper);
    }

    @Override
    public User getUserByEmail(String email) {
        QueryWrapper wrapper = QueryWrapper.create();
        wrapper.select().where(USER.EMAIL.eq(email));
        return userMapper.selectOneByQuery(wrapper);
    }

    @Override
    public void createUser(User user) {
        userMapper.insert(user);
    }

    @Override
    public void updateUser(User user) {
        userMapper.update(user);
    }

    @Override
    public void deleteUser(Long id) {
        userMapper.deleteById(id);
    }

    @Override
    public List<User> getAllUsers() {
        return userMapper.selectAll();
    }
}
