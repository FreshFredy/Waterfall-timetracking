package de.hsesslingen.timetracking.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity @DiscriminatorValue("SUPERVISOR")
public class Supervisor extends Employee {
    @OneToMany(mappedBy = "supervisor")
    private List<Employee> team = new ArrayList<>();
}
