package com.dimazak.gym.service;

import com.dimazak.gym.dao.UserDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    @Transactional(readOnly = true)
    public long countActiveUsers() {
        return userDao.countActiveUsers();
    }
}
