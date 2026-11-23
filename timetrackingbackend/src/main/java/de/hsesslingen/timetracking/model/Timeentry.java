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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id") 
    private Project project;
 
    public Timeentry() {

    }

    public Timeentry(LocalDate entryDate, LocalTime startDate, LocalTime endDate, Integer breakDuration, TimeEntryType entryType, Project project) {
        if(this.project == null && this.entry_type == TimeEntryType.WORK) throw new IllegalArgumentException("You need to enter a Project");
        if (entryType == TimeEntryType.FLEXTIME) ;
        this.entryDate = entryDate;
        this.startDate = startDate;
        this.endDate = endDate;
        this.breakDuration = breakDuration;
        this.entry_type = entryType;
        this.project = project;
        
        calculateCreditedHours();
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
    public Project getProject(){
        return project;
    }
    public void setProject(Project project){
        this.project = project;
    }
    public void update(LocalDate entryDate, LocalTime startDate, LocalTime endDate,
                   Integer breakDuration, TimeEntryType entryType, Project project) {
    if(this.project == null && this.entry_type == TimeEntryType.WORK) throw new IllegalArgumentException("You need to enter a Project");
    this.entryDate = entryDate;
    this.startDate = startDate;
    this.endDate = endDate;
    this.breakDuration = breakDuration;
    this.entry_type = entryType;
    this.project = project;
    
    calculateCreditedHours();
    }

        public void calculateCreditedHours() {
            //Sick
            if (entry_type == TimeEntryType.SICK) {
                this.creditedHours = 8;
                return;
            }
            //Flextime
            if (entry_type == TimeEntryType.FLEXTIME) {
                this.creditedHours = 0;
                return;
            }
            //Checks
            if (startDate == null || endDate == null || !endDate.isAfter(startDate)) {
                throw new IllegalArgumentException("End time must be after start time");
            }
            int pause = breakDuration == null ? 0 : breakDuration;
            breakDuration = pause;

            long gross = Duration.between(startDate, endDate).toMinutes();
            if (gross > 10 * 60) {
                throw new IllegalArgumentException("Working time must not exceed 10 hours per day");
            }
            int requiredBreak = gross >= 9 * 60 ? 45 : gross >= 6 * 60 ? 30 : 0;
            if (pause < requiredBreak) {
                throw new IllegalArgumentException("At least " + requiredBreak + " minutes break are required");
            }
            //credit
            this.creditedHours = (gross - pause) / 60f;
    }


    

    @Override
    public String toString() {
        return "[id=" + id + "]";
    }

}