package de.hsesslingen.timetracking.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Date;


enum excused  {EXCUSED, DOCOTRS_NOTE_MISSING, NOT_EXCUSED};

@Entity
@Table(name = "SickLeave")
public class SickLeave {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "date")
    private LocalDate date; 

    @Column(name = "doctors_note")
    private String filepath_doctors_note;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_id")
    private HR createdBy;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_entry_id", unique = true)
    private Timeentry timeEntry;

    public SickLeave(){

    }
    public SickLeave(LocalDate date, String fileString, Employee employee){
        this.date = date;
        this.filepath_doctors_note = fileString;
        this.employee = employee;
    }

    public long getID(){
        return id;
    }
    public void setDate(LocalDate date){
        this.date = date;
    }
    public LocalDate getDate(){
        return date;
    }
    public void setFileDocNote(String filePath){
        this.filepath_doctors_note = filePath;
    }
    public String getFileDocNote(){
        return filepath_doctors_note;
    }
    public void setEmployee(Employee employee){
        this.employee = employee;
    }
    public Employee getEmployee(){
        return employee;
    }

}
