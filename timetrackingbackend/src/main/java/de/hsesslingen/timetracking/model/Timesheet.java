package main.java.de.hsesslingen.timetracking.model;

import jakarta.persistence.*;
import java.math.BigDecimal; 

enum status{DRAFT, SUBMITTED, APPROVED, REJECTED};

@Entity
@Table(name = "Timesheet")
public class Timesheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "month")
    private Integer month; 

    @Column(name = "year")
    private Integer year;

    @Column(name = "status")
    private status status;

    @Column(name = "rejection_reason")
    private String rejection_reason;

    @Column(name = "employeeID")
    private long employeeID;


    public Timesheet() {

    }

    public Timesheet(Integer month, Integer year, status status, String rejection_reason, long employeeID) {
        this.month = month;
        this.year = year;
        this.status = status;
        this.rejection_reason = rejection_reason;
        this.employeeID = employeeID;
    }

    public long getId() {
        return id;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public status getStatus() {
        return this.status;
    }

    public void setStatus(status status) {
        this.status = status;
    }
  
    public long getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(long employeeID) {
        this.employeeID = employeeID;
    }


    @Override
    public String toString() {
        return "[id=" + id + ", year=" + year + ", month=" + month + ", status=" + status + ", employeeID=" + employeeID + "]";
    }

}