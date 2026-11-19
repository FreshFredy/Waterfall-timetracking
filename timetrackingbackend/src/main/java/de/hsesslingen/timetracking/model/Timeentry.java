package de.hsesslingen.timetracking.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

import de.hsesslingen.timetracking.EntryTypes.TimeEntryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;




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

    @Enumerated(EnumType.ORDINAL)
    private TimeEntryType entry_type;

    @Column(name = "creditedHours")
    private float creditedHours;
 
    public Timeentry() {

    }

    public Timeentry(LocalDate entryDate, LocalTime startDate, LocalTime endDate, Integer breakDuration, TimeEntryType entryType) {
        this.entryDate = entryDate;
        this.startDate = startDate;
        this.endDate = endDate;
        this.breakDuration = breakDuration;
        this.entry_type = entryType;
        calculateCreditedHours();
    }

public void calculateCreditedHours() {
    if (entry_type == TimeEntryType.SICK) {
        this.creditedHours = 8;
        return;
    }
    if (entry_type == TimeEntryType.FLEXTIME) {
        this.creditedHours = 0;          // a flex day off credits nothing, the target makes it a minus
        return;
    }
    if (startDate == null || endDate == null || !endDate.isAfter(startDate)) {
        throw new IllegalArgumentException("End time must be after start time");
    }
    if (breakDuration == null) breakDuration = 0;
    long minutes = Duration.between(startDate, endDate).toMinutes() - breakDuration;
    this.creditedHours = minutes / 60f;
}

    public long getId() {
        return id;
    }

   public LocalDate getEntryDate(){
        return entryDate;
   }
    public LocalTime getStartDate(){
        return startDate;
   }
    public LocalTime getEndDate(){
        return endDate;
   }
    public Integer getbreakDuration(){
        return breakDuration;
   }

    public TimeEntryType getEntry_type(){
        return entry_type;
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
   public void update(LocalDate entryDate, LocalTime startDate, LocalTime endDate,
                   Integer breakDuration, TimeEntryType entryType) {
    this.entryDate = entryDate;
    this.startDate = startDate;
    this.endDate = endDate;
    this.breakDuration = breakDuration;
    this.entry_type = entryType;
    calculateCreditedHours();
}


    @Override
    public String toString() {
        return "[id=" + id + "]";
    }

}