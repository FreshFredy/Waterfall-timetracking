package de.hsesslingen.timetracking.model;

import jakarta.persistence.*;

import java.util.Date;


@Entity
@Table(name = "VacationRequest")
public class VacationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "start_date")
    private Date start_date; 

    @Column(name = "end_Date")
    private Date end_date;

    @Column(name = "rejection_reason")
    private String rejection_reason;

    @OneToOne
    @JoinColumn(name = "employee_id", referencedColumnName = "id", unique = true)
    private Employee employee;

public VacationRequest() {}
public VacationRequest(Date start_date, Date end_date, Employee employee) {
    this.start_date = start_date;
    this.end_date = end_date;
    this.rejection_reason = "";
    this.employee = employee;
}
    public long getID(){
        return id;
    }
    public void setStartDate(Date start_date){
        this.start_date = start_date;
    }
    public Date getStartDate(){
        return start_date;
    }
    public void setEndDate(Date end_date){
        this.end_date = end_date;
    }
    public Date getEndDate(){
        return end_date;
    }
    public void setRejectionReason(String rejection_reason){
        this.rejection_reason = rejection_reason;
    }
    public String getRejectionReason(){
        return rejection_reason;
    }
    public void setEmployee(Employee employee){
        this.employee = employee;
    }
    public Employee getEmployee(){
        return employee;
    }
}