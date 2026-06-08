package com.dimazak.gym.controller;

import com.dimazak.gym.dto.AddTrainingRequest;
import com.dimazak.gym.exception.AuthenticationException;
import com.dimazak.gym.exception.EntityNotFoundException;
import com.dimazak.gym.exception.GlobalExceptionHandler;
import com.dimazak.gym.service.AuthenticationService;
import com.dimazak.gym.service.TrainingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TrainingControllerTest {

    private static final String TRAINEE_USERNAME = "John.Doe";
    private static final String TRAINER_USERNAME = "Jane.Smith";
    private static final String TRAINING_NAME = "Morning Cardio";
    private static final LocalDate TRAINING_DATE = LocalDate.of(2024, 4, 1);
    private static final int TRAINING_DURATION = 60;
    private static final int INVALID_DURATION = -1;
    private static final String NOT_LOGGED_ERROR = "User is not logged in. Please log in first.";
    private static final String BASE_URL = "/api/trainings";

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock private TrainingService trainingService;
    @Mock private AuthenticationService authenticationService;

    @InjectMocks
    private TrainingController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void addTraining_shouldReturn200WhenTraineeLogged() throws Exception {
        AddTrainingRequest request = new AddTrainingRequest(
                TRAINEE_USERNAME, TRAINER_USERNAME, TRAINING_NAME, TRAINING_DATE, TRAINING_DURATION);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authenticationService).checkLogged(TRAINEE_USERNAME);
        verify(trainingService).addTraining(TRAINEE_USERNAME, TRAINER_USERNAME,
                TRAINING_NAME, TRAINING_DATE, TRAINING_DURATION);
    }

    @Test
    void addTraining_shouldReturn401WhenTraineeNotLogged() throws Exception {
        AddTrainingRequest request = new AddTrainingRequest(
                TRAINEE_USERNAME, TRAINER_USERNAME, TRAINING_NAME, TRAINING_DATE, TRAINING_DURATION);
        doThrow(new AuthenticationException(NOT_LOGGED_ERROR))
                .when(authenticationService).checkLogged(TRAINEE_USERNAME);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(NOT_LOGGED_ERROR));

        verify(trainingService, never()).addTraining(any(), any(), any(), any(), anyInt());
    }

    @Test
    void addTraining_shouldReturn400WhenTraineeUsernameBlank() throws Exception {
        AddTrainingRequest request = new AddTrainingRequest(
                "", TRAINER_USERNAME, TRAINING_NAME, TRAINING_DATE, TRAINING_DURATION);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authenticationService);
        verifyNoInteractions(trainingService);
    }

    @Test
    void addTraining_shouldReturn400WhenTrainerUsernameBlank() throws Exception {
        AddTrainingRequest request = new AddTrainingRequest(
                TRAINEE_USERNAME, "", TRAINING_NAME, TRAINING_DATE, TRAINING_DURATION);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addTraining_shouldReturn400WhenNameBlank() throws Exception {
        AddTrainingRequest request = new AddTrainingRequest(
                TRAINEE_USERNAME, TRAINER_USERNAME, "", TRAINING_DATE, TRAINING_DURATION);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addTraining_shouldReturn400WhenDurationNegative() throws Exception {
        AddTrainingRequest request = new AddTrainingRequest(
                TRAINEE_USERNAME, TRAINER_USERNAME, TRAINING_NAME, TRAINING_DATE, INVALID_DURATION);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addTraining_shouldReturn400WhenDateNull() throws Exception {
        AddTrainingRequest request = new AddTrainingRequest(
                TRAINEE_USERNAME, TRAINER_USERNAME, TRAINING_NAME, null, TRAINING_DURATION);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addTraining_shouldReturn404WhenTraineeNotFound() throws Exception {
        AddTrainingRequest request = new AddTrainingRequest(
                "Unknown", TRAINER_USERNAME, TRAINING_NAME, TRAINING_DATE, TRAINING_DURATION);
        doThrow(new EntityNotFoundException("Trainee not found: Unknown"))
                .when(trainingService).addTraining(anyString(), anyString(), anyString(), any(), anyInt());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}