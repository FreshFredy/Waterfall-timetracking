package de.hsesslingen.timetracking.model;

import jakarta.persistence.*;
import java.math.BigDecimal; 

import java.util.Date;
import de.hsesslingen.timetracking.model.Employee;


enum entryType {SICK, WORK, FLEXTIME}

@Entity
@Table(name = "Timeentry")
public class Timeentry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "entryDate")
    private Date entryDate; 

    @Column(name = "startDate")
    private Date startDate; 

    @Column(name = "endDate")
    private Date endDate;

    @Column(name = "breakDuration")
    private Integer breakDuration;

    @Column(name = "timesheetID")
    private long timesheetID;

    @Column(name = "entryType")
    private entryType entryType;

    @Column(name = "project")
    private String project;

    @Column(name = "creditedHours")
    private float creditedHours;
 
    public Timeentry() {

    }

    public Timeentry(Date entryDate, Date startDate, Date endDate, Integer breakDuration, long timesheetID, String project, float creditedHours) {
        this.entryDate = entryDate;
        this.startDate = startDate;
        this.breakDuration = breakDuration;
        this.timesheetID = timesheetID;
        this.project = project;
        this.creditedHours = creditedHours;
    }

    public long getId() {
        return id;
    }

   public Date getentryDate(){
        return entryDate;
   }
    public Date getstartDate(){
        return startDate;
   }
    public Date getendDate(){
        return endDate;
   }
    public Integer getbreakDuration(){
        return breakDuration;
   }
    public String getProject(){
        return project;
   }
    public entryType getEntryType(){
        return entryType;
   }
    public long gettimesheetID(){
        return timesheetID;
   }
    public float getcreditedHours(){
        return creditedHours;
   }


    @Override
    public String toString() {
        return "[id=" + id + "]";
    }

}