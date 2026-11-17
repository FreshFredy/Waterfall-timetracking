package de.hsesslingen.timetracking.model;

import jakarta.persistence.*;


@Entity
@Table(name = "VacationBalance")
public class VacationBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "planned_days")
    private Integer planned_days; 

    @Column(name = "taken_days")
    private Integer taken_days;

    @Column(name = "annual days")
    private Integer annual_days;

    @Column(name = "remaining_days_last_year")
    private Integer remaining_days_last_year;

    @OneToOne
    @JoinColumn(name = "employee_id", referencedColumnName = "id", unique = true)
    private Employee employee;

    public VacationBalance(){}
    public VacationBalance(Integer annual_days, Employee employee){
        this.planned_days = 0;
        this.annual_days = annual_days;
        this.taken_days = 0;
        this.remaining_days_last_year = 0;
        this.employee = employee;
    }

    public long getID(){
        return id;
    }
    public void setPlannedDays(Integer planned_days){
        this.planned_days = planned_days;
    }
    public Integer getPlannedDays(){
        return planned_days;
    }
    public void setTakenDays(Integer taken_days){
        this.taken_days = taken_days;
    }
    public Integer getTakenDays(){
        return taken_days;
    }
    public void setRemainingDaysLastYear(Integer remaining_days_last_year){
        this.remaining_days_last_year = remaining_days_last_year;
    }
    public Integer getRemainingDaysLastYear(){
        return remaining_days_last_year;
    }
    public void setAnnualDays(Integer annual_days){
        this.annual_days = annual_days;
    }
    public Integer getAnnualDays(){
        return annual_days;
    }
    public void setEmployee(Employee employee){
        this.employee = employee;
    }
    public Employee getEmployee(){
        return employee;
    }
    public Integer getRemainingDays(){
        return annual_days + remaining_days_last_year - taken_days;
    }
    public void resetTakenDays(){
        this.taken_days = 0;
    }


}
