package com.dimazak.gym.model;

import java.time.LocalDate;
import java.util.Objects;

public class Trainee {
    private Long id;
    private LocalDate dateOfBirth;
    private String address;
    private Long userId;

    public Trainee() {}

    public Trainee(Long id, LocalDate dateOfBirth, String address, Long userId) {
        this.id = id;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.userId = userId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trainee trainee = (Trainee) o;
        return Objects.equals(id, trainee.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Trainee{id=" + id + ", dateOfBirth=" + dateOfBirth +
                ", address='" + address + "', userId=" + userId + '}';
    }
}