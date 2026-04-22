package com.dimazak.gym.model;

import java.util.Objects;

public class TrainingType {
    private Long id;
    private String trainingTypeName;

    public TrainingType() {}

    public TrainingType(Long id, String trainingTypeName) {
        this.id = id;
        this.trainingTypeName = trainingTypeName;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTrainingTypeName() { return trainingTypeName; }
    public void setTrainingTypeName(String trainingTypeName) {
        this.trainingTypeName = trainingTypeName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrainingType that = (TrainingType) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "TrainingType{id=" + id + ", name='" + trainingTypeName + "'}";
    }
}