package main.java.de.hsesslingen.timetracking.model;

import jakarta.persistence.*;
import java.math.BigDecimal; 

@Entity
@Table(name = "Department")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name")
    private String name;

    @Column(name = "supervisor")
    private long supervisorID;


    public Department() {

    }

    public Department(String name, long supervisorID) {
        this.name = name;
        this.supervisorID = supervisorID;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSupervisorID() {
        return supervisorID;
    }

    public void setSupervisorID(long supervisorID) {
        this.supervisorID = supervisorID;
    }


    @Override
    public String toString() {
        return "[id=" + id + ", name=" + name + ", Supervisor ID=" + supervisorID + "]";
    }

}