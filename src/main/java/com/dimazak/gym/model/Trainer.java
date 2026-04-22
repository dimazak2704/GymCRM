package com.dimazak.gym.model;

import java.util.Objects;

public class Trainer {
    private Long id;
    private Long specialization;
    private Long userId;

    public Trainer() {}

    public Trainer(Long id, Long specialization, Long userId) {
        this.id = id;
        this.specialization = specialization;
        this.userId = userId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSpecialization() { return specialization; }
    public void setSpecialization(Long specialization) { this.specialization = specialization; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trainer trainer = (Trainer) o;
        return Objects.equals(id, trainer.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Trainer{id=" + id + ", specialization=" + specialization +
                ", userId=" + userId + '}';
    }
}