package de.hsesslingen.timetracking.model;

import jakarta.persistence.*;
import java.util.Date;

enum excused  {EXCUSED, DOCOTRS_NOTE_MISSING, NOT_EXCUSED};

@Entity
@Table(name = "SickLeave")
public class SickLeave {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "date")
    private Date date; 

    @Column(name = "doctors_bote")
    private String filepath_doctors_note;

    @OneToOne
    @JoinColumn(name = "employee_id", referencedColumnName = "id", unique = true)
    private Employee employee;

    public SickLeave(){

    }
    public SickLeave(Date date, String fileString, Employee employee){
        this.date = date;
        this.filepath_doctors_note = fileString;
        this.employee = employee;
    }

    public long getID(){
        return id;
    }
    public void setDate(Date date){
        this.date = date;
    }
    public Date getDate(){
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
