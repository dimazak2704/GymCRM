package com.dimazak.gym.controller;

import com.dimazak.gym.dto.*;
import com.dimazak.gym.exception.AuthenticationException;
import com.dimazak.gym.exception.EntityNotFoundException;
import com.dimazak.gym.exception.GlobalExceptionHandler;
import com.dimazak.gym.exception.ValidationException;
import com.dimazak.gym.mapper.EntityMapper;
import com.dimazak.gym.model.*;
import com.dimazak.gym.service.AuthenticationService;
import com.dimazak.gym.service.TraineeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TraineeControllerTest {

    private static final String USERNAME = "John.Doe";
    private static final String PASSWORD = "pass123456";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final LocalDate BIRTH_DATE = LocalDate.of(1990, 1, 1);
    private static final String ADDRESS = "123 Main St";
    private static final String TRAINER_USERNAME = "Jane.Smith";
    private static final String TRAINER_FIRST_NAME = "Jane";
    private static final String TRAINER_LAST_NAME = "Smith";
    private static final String SPECIALIZATION = "Cardio";
    private static final String TRAINING_NAME = "Morning Run";
    private static final LocalDate TRAINING_DATE = LocalDate.of(2024, 4, 1);
    private static final int TRAINING_DURATION = 60;
    private static final String NOT_LOGGED_ERROR = "User is not logged in. Please log in first.";
    private static final String BASE_URL = "/api/trainees";

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock private TraineeService traineeService;
    @Mock private AuthenticationService authenticationService;
    @Spy private EntityMapper mapper;

    @InjectMocks
    private TraineeController controller;

    private Trainee testTrainee;
    private Trainer testTrainer;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        User user = new User(1L, FIRST_NAME, LAST_NAME, USERNAME, PASSWORD, true);
        testTrainee = new Trainee(1L, BIRTH_DATE, ADDRESS, user);
        testTrainee.setTrainers(List.of());

        TrainingType type = new TrainingType(1L, SPECIALIZATION);
        User trainerUser = new User(2L, TRAINER_FIRST_NAME, TRAINER_LAST_NAME, TRAINER_USERNAME, "p", true);
        testTrainer = new Trainer(1L, type, trainerUser);
    }

    // ==================== Register (no auth) ====================

    @Test
    void register_shouldReturn201WithCredentials() throws Exception {
        TraineeRegistrationRequest request = new TraineeRegistrationRequest(
                FIRST_NAME, LAST_NAME, BIRTH_DATE, ADDRESS);
        when(traineeService.createTrainee(anyString(), anyString(), any(), any()))
                .thenReturn(testTrainee);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(USERNAME))
                .andExpect(jsonPath("$.password").value(PASSWORD));

        verifyNoInteractions(authenticationService);
    }

    @Test
    void register_shouldReturn400WhenFirstNameBlank() throws Exception {
        TraineeRegistrationRequest request = new TraineeRegistrationRequest(
                "", LAST_NAME, null, null);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturn400WhenLastNameBlank() throws Exception {
        TraineeRegistrationRequest request = new TraineeRegistrationRequest(
                FIRST_NAME, "", null, null);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ==================== Get profile ====================

    @Test
    void getProfile_shouldReturn200WhenLogged() throws Exception {
        testTrainee.setTrainers(List.of(testTrainer));
        when(traineeService.getProfileByUsername(USERNAME)).thenReturn(testTrainee);

        mockMvc.perform(get(BASE_URL + "/" + USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value(FIRST_NAME))
                .andExpect(jsonPath("$.lastName").value(LAST_NAME))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.trainers[0].username").value(TRAINER_USERNAME))
                .andExpect(jsonPath("$.trainers[0].specialization").value(SPECIALIZATION));

        verify(authenticationService).checkLogged(USERNAME);
    }

    @Test
    void getProfile_shouldReturn401WhenNotLogged() throws Exception {
        doThrow(new AuthenticationException(NOT_LOGGED_ERROR))
                .when(authenticationService).checkLogged(USERNAME);

        mockMvc.perform(get(BASE_URL + "/" + USERNAME))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(NOT_LOGGED_ERROR));

        verify(traineeService, never()).getProfileByUsername(any());
    }

    @Test
    void getProfile_shouldReturn404WhenNotFound() throws Exception {
        when(traineeService.getProfileByUsername("Unknown"))
                .thenThrow(new EntityNotFoundException("Trainee not found"));

        mockMvc.perform(get(BASE_URL + "/Unknown"))
                .andExpect(status().isNotFound());
    }

    // ==================== Update profile ====================

    @Test
    void updateProfile_shouldReturn200WhenLogged() throws Exception {
        UpdateTraineeRequest request = new UpdateTraineeRequest(
                FIRST_NAME, "Updated", BIRTH_DATE, "New Addr", true);
        when(traineeService.updateTrainee(anyString(), anyString(), anyString(),
                any(), any(), anyBoolean())).thenReturn(testTrainee);

        mockMvc.perform(put(BASE_URL + "/" + USERNAME)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(USERNAME));

        verify(authenticationService).checkLogged(USERNAME);
    }

    @Test
    void updateProfile_shouldReturn401WhenNotLogged() throws Exception {
        UpdateTraineeRequest request = new UpdateTraineeRequest(
                FIRST_NAME, LAST_NAME, BIRTH_DATE, ADDRESS, true);
        doThrow(new AuthenticationException(NOT_LOGGED_ERROR))
                .when(authenticationService).checkLogged(USERNAME);

        mockMvc.perform(put(BASE_URL + "/" + USERNAME)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(traineeService, never()).updateTrainee(any(), any(), any(), any(), any(), anyBoolean());
    }

    @Test
    void updateProfile_shouldReturn400WhenIsActiveNull() throws Exception {
        String json = """
                {"firstName":"John","lastName":"Doe"}
                """;

        mockMvc.perform(put(BASE_URL + "/" + USERNAME)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    // ==================== Delete profile ====================

    @Test
    void deleteProfile_shouldReturn200WhenLogged() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + USERNAME))
                .andExpect(status().isOk());

        verify(authenticationService).checkLogged(USERNAME);
        verify(traineeService).deleteByUsername(USERNAME);
    }

    @Test
    void deleteProfile_shouldReturn401WhenNotLogged() throws Exception {
        doThrow(new AuthenticationException(NOT_LOGGED_ERROR))
                .when(authenticationService).checkLogged(USERNAME);

        mockMvc.perform(delete(BASE_URL + "/" + USERNAME))
                .andExpect(status().isUnauthorized());

        verify(traineeService, never()).deleteByUsername(any());
    }

    // ==================== Unassigned trainers ====================

    @Test
    void getUnassignedTrainers_shouldReturn200WhenLogged() throws Exception {
        when(traineeService.getUnassignedTrainers(USERNAME)).thenReturn(List.of(testTrainer));

        mockMvc.perform(get(BASE_URL + "/" + USERNAME + "/unassigned-trainers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value(TRAINER_USERNAME))
                .andExpect(jsonPath("$[0].specialization").value(SPECIALIZATION));

        verify(authenticationService).checkLogged(USERNAME);
    }

    @Test
    void getUnassignedTrainers_shouldReturn401WhenNotLogged() throws Exception {
        doThrow(new AuthenticationException(NOT_LOGGED_ERROR))
                .when(authenticationService).checkLogged(USERNAME);

        mockMvc.perform(get(BASE_URL + "/" + USERNAME + "/unassigned-trainers"))
                .andExpect(status().isUnauthorized());

        verify(traineeService, never()).getUnassignedTrainers(any());
    }

    // ==================== Update trainers list ====================

    @Test
    void updateTrainersList_shouldReturn200WhenLogged() throws Exception {
        testTrainee.setTrainers(List.of(testTrainer));
        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest(
                List.of(TRAINER_USERNAME));
        when(traineeService.updateTrainersList(USERNAME, List.of(TRAINER_USERNAME)))
                .thenReturn(testTrainee);

        mockMvc.perform(put(BASE_URL + "/" + USERNAME + "/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value(TRAINER_USERNAME));

        verify(authenticationService).checkLogged(USERNAME);
    }

    @Test
    void updateTrainersList_shouldReturn401WhenNotLogged() throws Exception {
        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest(
                List.of(TRAINER_USERNAME));
        doThrow(new AuthenticationException(NOT_LOGGED_ERROR))
                .when(authenticationService).checkLogged(USERNAME);

        mockMvc.perform(put(BASE_URL + "/" + USERNAME + "/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(traineeService, never()).updateTrainersList(any(), any());
    }

    @Test
    void updateTrainersList_shouldReturn400WhenListEmpty() throws Exception {
        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest(List.of());

        mockMvc.perform(put(BASE_URL + "/" + USERNAME + "/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ==================== Trainings list ====================

    @Test
    void getTrainings_shouldReturn200WhenLoggedWithFilters() throws Exception {
        TrainingType type = new TrainingType(1L, SPECIALIZATION);
        Training training = new Training(1L, testTrainee, testTrainer,
                TRAINING_NAME, type, TRAINING_DATE, TRAINING_DURATION);
        when(traineeService.getTraineeTrainings(anyString(), any(), any(), any(), any()))
                .thenReturn(List.of(training));

        mockMvc.perform(get(BASE_URL + "/" + USERNAME + "/trainings")
                        .param("periodFrom", "2024-01-01")
                        .param("periodTo", "2024-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trainingName").value(TRAINING_NAME))
                .andExpect(jsonPath("$[0].trainingDuration").value(TRAINING_DURATION))
                .andExpect(jsonPath("$[0].trainerName").value(TRAINER_FIRST_NAME + " " + TRAINER_LAST_NAME));

        verify(authenticationService).checkLogged(USERNAME);
    }

    @Test
    void getTrainings_shouldReturn200WithNoFilters() throws Exception {
        when(traineeService.getTraineeTrainings(anyString(), any(), any(), any(), any()))
                .thenReturn(List.of());

        mockMvc.perform(get(BASE_URL + "/" + USERNAME + "/trainings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getTrainings_shouldReturn401WhenNotLogged() throws Exception {
        doThrow(new AuthenticationException(NOT_LOGGED_ERROR))
                .when(authenticationService).checkLogged(USERNAME);

        mockMvc.perform(get(BASE_URL + "/" + USERNAME + "/trainings"))
                .andExpect(status().isUnauthorized());

        verify(traineeService, never()).getTraineeTrainings(any(), any(), any(), any(), any());
    }

    // ==================== Activate / deactivate ====================

    @Test
    void activateStatus_shouldReturn200WhenLogged() throws Exception {
        ActivateDeactivateRequest request = new ActivateDeactivateRequest(false);

        mockMvc.perform(patch(BASE_URL + "/" + USERNAME + "/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authenticationService).checkLogged(USERNAME);
        verify(traineeService).setActiveStatus(USERNAME, false);
    }

    @Test
    void activateStatus_shouldReturn401WhenNotLogged() throws Exception {
        ActivateDeactivateRequest request = new ActivateDeactivateRequest(false);
        doThrow(new AuthenticationException(NOT_LOGGED_ERROR))
                .when(authenticationService).checkLogged(USERNAME);

        mockMvc.perform(patch(BASE_URL + "/" + USERNAME + "/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(traineeService, never()).setActiveStatus(any(), anyBoolean());
    }

    @Test
    void activateStatus_shouldReturn400WhenAlreadySameStatus() throws Exception {
        ActivateDeactivateRequest request = new ActivateDeactivateRequest(true);
        doThrow(new ValidationException("Trainee is already active"))
                .when(traineeService).setActiveStatus(USERNAME, true);

        mockMvc.perform(patch(BASE_URL + "/" + USERNAME + "/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Trainee is already active"));
    }

    @Test
    void activateStatus_shouldReturn400WhenIsActiveNull() throws Exception {
        String json = "{}";

        mockMvc.perform(patch(BASE_URL + "/" + USERNAME + "/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }
}