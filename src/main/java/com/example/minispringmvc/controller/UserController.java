package com.example.minispringmvc.controller;

import com.example.minispringmvc.annotation.Controller;
import com.example.minispringmvc.annotation.Qualifier;
import com.example.minispringmvc.annotation.RequestMapping;
import com.example.minispringmvc.service.UserService;

@Controller("userController")
@RequestMapping("/user")
public class UserController {

    @Qualifier("userServiceImpl")
    private UserService userService;

    @RequestMapping("/insert")
    public void insert() {
        userService.insert();
    }


}
