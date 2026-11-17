package de.hsesslingen.timetracking.model;

import jakarta.persistence.*;
import de.hsesslingen.timetracking.EntryTypes.*;

import java.time.LocalDate;





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

    @Column(name = "exused_status")
    private ExusedEntryType exusedEntryType;

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
    public SickLeave(LocalDate date, String fileString, Employee employee, HR createdBy, Timeentry timeEntry){
        this.date = date;
        this.filepath_doctors_note = fileString;
        this.employee = employee;
        if(fileString == null)this.exusedEntryType = ExusedEntryType.DOCOTRS_NOTE_MISSING;
        else if(fileString != null)this.exusedEntryType = ExusedEntryType.EXCUSED;
        this.createdBy = createdBy;
        this.timeEntry = timeEntry;
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
        this.exusedEntryType = ExusedEntryType.EXCUSED;
    }
    public void setExusedEntryNotExcuse(){
        this.exusedEntryType = ExusedEntryType.NOT_EXCUSED;
    }
    public ExusedEntryType getExusedEntryType(){
        return this.exusedEntryType ;
    }
    public void setExusedEntryType(ExusedEntryType eet){
        this.exusedEntryType = eet;
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
    public HR getCreatedBy(){
        return createdBy;
    }
    public Timeentry getTimeentry(){
        return timeEntry;
    }

}
