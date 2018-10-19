package com.example.minispringmvc.service.impl;

import com.example.minispringmvc.annotation.Qualifier;
import com.example.minispringmvc.annotation.Service;
import com.example.minispringmvc.dao.UserDao;
import com.example.minispringmvc.service.UserService;

@Service("userServiceImpl")
public class UserServiceImpl implements UserService {

    @Qualifier("userDaoImpl")
    private UserDao userDao;

    @Override
    public void insert() {
        System.out.println("run user service start....");
        userDao.insert();
        System.out.println("run user service end....");
    }
}
