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

    @Enumerated(EnumType.STRING)
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

    public void calculateCreditedHours(){
     if (entry_type == TimeEntryType.SICK) {
        this.creditedHours = 8;
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