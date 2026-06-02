package com.dimazak.gym.service;

import com.dimazak.gym.dao.UserDao;
import com.dimazak.gym.exception.AuthenticationException;
import com.dimazak.gym.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(UserDao userDao, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public void authenticate(String username, String password) {
        log.debug("Authenticating user: {}", username);

        User user = userDao.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Authentication failed: user '{}' not found", username);
                    return new AuthenticationException("Invalid username or password");
                });

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Authentication failed: wrong password for user '{}'", username);
            throw new AuthenticationException("Invalid username or password");
        }

        log.debug("User '{}' authenticated successfully", username);
    }
}