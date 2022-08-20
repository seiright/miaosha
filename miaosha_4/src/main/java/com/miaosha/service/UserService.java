package com.miaosha.service;

import com.miaosha.dao.UserDao;
import com.miaosha.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author zhaolifeng
 * @version 1.0
 * @description: TODO
 * @date 2022/8/12 17:09
 */

@Service
public class UserService implements UserDao{
    UserDao userDao;

    @Autowired
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }


    @Override
    public User getById(int id) {
        return userDao.getById(id);
    }

    @Override
    public int insert(User user) {
        return 0;
    }

    @Transactional
    public boolean tx(){
        User u1 = new User(2,"2222");
        userDao.insert(u1);

        User u2 = new User(1,"11111");
        userDao.insert(u2);

        return true;
    }
}
