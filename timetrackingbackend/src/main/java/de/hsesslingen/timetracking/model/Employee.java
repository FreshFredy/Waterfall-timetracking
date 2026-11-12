package main.java.de.hsesslingen.timetracking.model;

import jakarta.persistence.*;
import java.math.BigDecimal; 

@Entity
@Table(name = "Employee")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "firstname")
    private String firstname;

    @Column(name = "lastname")
    private String lastname;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "target_hours_per_day")
    private Integer targetH;

    @Column(name = "role")
    private String role;

    @Column(name = "supervisorID")
    private long supervisorID;

    public Employee() {

    }

    public Employee(String firstname, String lastname, String email, Integer targetH, String role, long supervisorID) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.targetH = targetH;
        this.role = role;
        this.supervisorID = supervisorID;
    }

    public long getId() {
        return id;
    }

    public String getfirstname() {
        return firstname;
    }

    public void setfirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getlastname() {
        return lastname;
    }

    public void setlastname(String lastname) {
        this.lastname = lastname;
    }

    public String getemail() {
        return email;
    }

    public void setemail(String email) {
        this.email = email;
    }
  
    public Integer getTargetH() {
        return targetH;
    }

    public void setTargetH(Integer targetH) {
        this.targetH = targetH;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public long getSupervisorId() {
        return supervisorID;
    }

    public void setSupervisorId(long supervisorID) {
        this.supervisorID = supervisorID;
    }

    @Override
    public String toString() {
        return "[id=" + id + ", firstname=" + firstname + ", lastname=" + lastname + ", email=" + email + ", Target Hours=" + targetH + ", role=" + role + ", supervisorID=" + supervisorID + "]";
    }

}