package com.dimazak.gym.controller;

import com.dimazak.gym.dto.ChangePasswordRequest;
import com.dimazak.gym.dto.LoginRequest;
import com.dimazak.gym.exception.AuthenticationException;
import com.dimazak.gym.exception.GlobalExceptionHandler;
import com.dimazak.gym.service.AuthenticationService;
import com.dimazak.gym.service.TraineeService;
import com.dimazak.gym.service.TrainerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private static final String USERNAME = "John.Doe";
    private static final String PASSWORD = "pass123456";
    private static final String WRONG_PASSWORD = "wrong";
    private static final String NEW_PASSWORD = "newPass1234";
    private static final String TRAINER_USERNAME = "Jane.Smith";
    private static final String AUTH_ERROR = "Invalid username or password";
    private static final String LOGIN_URL = "/api/users/login";
    private static final String PASSWORD_URL = "/api/users/password";

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock private AuthenticationService authenticationService;
    @Mock private TraineeService traineeService;
    @Mock private TrainerService trainerService;

    @InjectMocks
    private AuthController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void login_shouldReturn200ForValidCredentials() throws Exception {
        LoginRequest request = new LoginRequest(USERNAME, PASSWORD);

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authenticationService).authenticate(USERNAME, PASSWORD);
    }

    @Test
    void login_shouldReturn401ForInvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest(USERNAME, WRONG_PASSWORD);
        doThrow(new AuthenticationException(AUTH_ERROR))
                .when(authenticationService).authenticate(USERNAME, WRONG_PASSWORD);

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(AUTH_ERROR));
    }

    @Test
    void login_shouldReturn400WhenUsernameBlank() throws Exception {
        LoginRequest request = new LoginRequest("", PASSWORD);

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldReturn400WhenPasswordBlank() throws Exception {
        LoginRequest request = new LoginRequest(USERNAME, "");

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_shouldReturn200ForTrainee() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest(USERNAME, PASSWORD, NEW_PASSWORD);
        when(traineeService.existsByUsername(USERNAME)).thenReturn(true);

        mockMvc.perform(put(PASSWORD_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authenticationService).authenticate(USERNAME, PASSWORD);
        verify(traineeService).changePassword(USERNAME, NEW_PASSWORD);
    }

    @Test
    void changePassword_shouldReturn200ForTrainer() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest(TRAINER_USERNAME, PASSWORD, NEW_PASSWORD);
        when(traineeService.existsByUsername(TRAINER_USERNAME)).thenReturn(false);

        mockMvc.perform(put(PASSWORD_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(trainerService).changePassword(TRAINER_USERNAME, NEW_PASSWORD);
    }

    @Test
    void changePassword_shouldReturn400WhenFieldsMissing() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("", "", "");

        mockMvc.perform(put(PASSWORD_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_shouldReturn401WhenOldPasswordWrong() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest(USERNAME, WRONG_PASSWORD, NEW_PASSWORD);
        doThrow(new AuthenticationException(AUTH_ERROR))
                .when(authenticationService).authenticate(USERNAME, WRONG_PASSWORD);

        mockMvc.perform(put(PASSWORD_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}