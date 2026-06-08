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

    @Transactional
    public void login(String username, String password) {
        log.info("Login attempt for user: {}", username);
        User user = userDao.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("Invalid username or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthenticationException("Invalid username or password");
        }

        user.setLogged(true);
        userDao.save(user);
        log.info("User '{}' logged in successfully", username);
    }

    @Transactional
    public void logout(String username) {
        log.info("Logout for user: {}", username);
        User user = userDao.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("User not found"));
        user.setLogged(false);
        userDao.save(user);
        log.info("User '{}' logged out", username);
    }

    @Transactional(readOnly = true)
    public void checkLogged(String username) {
        log.debug("Checking logged-in status for user: {}", username);
        User user = userDao.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Auth check failed: user '{}' not found", username);
                    return new AuthenticationException("User not found");
                });

        if (!user.isLogged()) {
            log.warn("User '{}' is not logged in", username);
            throw new AuthenticationException("User is not logged in. Please log in first.");
        }
    }
}