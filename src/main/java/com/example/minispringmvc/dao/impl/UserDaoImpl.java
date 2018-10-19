package com.example.minispringmvc.dao.impl;

import com.example.minispringmvc.annotation.Repository;
import com.example.minispringmvc.dao.UserDao;

@Repository("userDaoImpl")
public class UserDaoImpl implements UserDao {

    @Override
    public void insert() {
        System.out.println("run user dao");
    }
}
