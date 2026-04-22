package com.dimazak.gym.model;

import java.time.LocalDate;
import java.util.Objects;

public class Training {
    private Long id;
    private Long traineeId;
    private Long trainerId;
    private String trainingName;
    private Long trainingTypeId;
    private LocalDate trainingDate;
    private int trainingDurationMinutes;

    public Training() {}

    public Training(Long id, Long traineeId, Long trainerId, String trainingName,
                    Long trainingTypeId, LocalDate trainingDate, int trainingDurationMinutes) {
        this.id = id;
        this.traineeId = traineeId;
        this.trainerId = trainerId;
        this.trainingName = trainingName;
        this.trainingTypeId = trainingTypeId;
        this.trainingDate = trainingDate;
        this.trainingDurationMinutes = trainingDurationMinutes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTraineeId() { return traineeId; }
    public void setTraineeId(Long traineeId) { this.traineeId = traineeId; }

    public Long getTrainerId() { return trainerId; }
    public void setTrainerId(Long trainerId) { this.trainerId = trainerId; }

    public String getTrainingName() { return trainingName; }
    public void setTrainingName(String trainingName) { this.trainingName = trainingName; }

    public Long getTrainingTypeId() { return trainingTypeId; }
    public void setTrainingTypeId(Long trainingTypeId) { this.trainingTypeId = trainingTypeId; }

    public LocalDate getTrainingDate() { return trainingDate; }
    public void setTrainingDate(LocalDate trainingDate) { this.trainingDate = trainingDate; }

    public int getTrainingDurationMinutes() { return trainingDurationMinutes; }
    public void setTrainingDurationMinutes(int trainingDurationMinutes) {
        this.trainingDurationMinutes = trainingDurationMinutes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Training training = (Training) o;
        return Objects.equals(id, training.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Training{id=" + id + ", traineeId=" + traineeId + ", trainerId=" + trainerId +
                ", trainingName='" + trainingName + "', trainingDate=" + trainingDate +
                ", duration=" + trainingDurationMinutes + "min}";
    }
}