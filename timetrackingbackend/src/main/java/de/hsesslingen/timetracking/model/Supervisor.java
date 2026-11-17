package de.hsesslingen.timetracking.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity @DiscriminatorValue("SUPERVISOR")
public class Supervisor extends Employee {
    @OneToMany(mappedBy = "supervisor")
    private List<Employee> team = new ArrayList<>();

    public Supervisor() {}

    public Supervisor(String firstname, String lastname, String username, String email, String password, Integer targetH){
        super(firstname,lastname,username, email,password,targetH, null);
    }
    public List<Employee> getTeam(){return team; }
}
