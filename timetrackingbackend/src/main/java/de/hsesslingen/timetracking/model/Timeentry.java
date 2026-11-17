package de.hsesslingen.timetracking.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import de.hsesslingen.timetracking.EntryTypes.*;




@Entity
@Table(name = "Timeentry")
public class Timeentry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "entryDate")
    private LocalDate entryDate; 

    @Column(name = "startDate")
    private LocalTime startDate; 

    @Column(name = "endDate")
    private LocalTime endDate;

    @Column(name = "breakDuration")
    private Integer breakDuration;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "timesheet")
    private Timesheet timesheet;

    @Column(name = "entryType")
    private TimeEntryType entryType;

    @Column(name = "creditedHours")
    private float creditedHours;
 
    public Timeentry() {

    }

    public Timeentry(LocalDate entryDate, LocalTime startDate, LocalTime endDate, Integer breakDuration, Timesheet timesheetID, TimeEntryType entryType) {
        this.entryDate = entryDate;
        this.startDate = startDate;
        this.breakDuration = breakDuration;
        this.timesheet = timesheetID;
        this.entryType = entryType;
        calculateCreditedHours();
    }

    public void calculateCreditedHours(){
        if (this.entryType != TimeEntryType.SICK) {
        if (!this.endDate.isAfter(this.startDate) || startDate == null  || endDate == null) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        if(breakDuration == null) this.breakDuration = 0;
        long minutes = Duration.between(startDate, endDate).toMinutes() - breakDuration;
        this.creditedHours = minutes / 60f;
        }
        else if(entryType == TimeEntryType.SICK){
            this.creditedHours = 8;
        }
    }

    public long getId() {
        return id;
    }

   public LocalDate getentryDate(){
        return entryDate;
   }
    public LocalTime getstartDate(){
        return startDate;
   }
    public LocalTime getendDate(){
        return endDate;
   }
    public Integer getbreakDuration(){
        return breakDuration;
   }

    public TimeEntryType getEntryType(){
        return entryType;
   }
    public Timesheet getTimesheet(){
        return timesheet;
   }
    public void setTimesheet(Timesheet timesheetID){
        this.timesheet = timesheetID;
   }
    public float getCreditedHours(){
        return creditedHours;
   }
   


    @Override
    public String toString() {
        return "[id=" + id + "]";
    }

}