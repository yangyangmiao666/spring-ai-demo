package com.ustc.myy.mcpclientserverdemo.controller;

import com.ustc.myy.mcpclientserverdemo.entity.User;
import com.ustc.myy.mcpclientserverdemo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("/get-all-users")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }
}
