package de.hsesslingen.timetracking.model;

import jakarta.persistence.*;

enum status { DRAFT, SUBMITTED, APPROVED, REJECTED };

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

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private status status;

    @Column(name = "rejection_reason")
    private String rejection_reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee; // Korrigiert von primitivem employeeID

    public Timesheet() {}

    public Timesheet(Integer month, Integer year, status status, String rejection_reason, Employee employee) {
        this.month = month;
        this.year = year;
        this.status = status;
        this.rejection_reason = rejection_reason;
        this.employee = employee;
    }

    public long getId() {
        return id;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public status getStatus() {
        return this.status;
    }

    public void setStatus(status status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejection_reason;
    }

    public void setRejectionReason(String rejection_reason) {
        this.rejection_reason = rejection_reason;
    }
  
    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    @Override
    public String toString() {
        return "[id=" + id + ", year=" + year + ", month=" + month + ", status=" + status + "]";
    }
}