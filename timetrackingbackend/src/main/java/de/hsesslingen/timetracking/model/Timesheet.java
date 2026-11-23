package de.hsesslingen.timetracking.model;
import de.hsesslingen.timetracking.EntryTypes.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import jakarta.persistence.*;
import java.util.Map;


@Entity
@Table(name = "Timesheet", uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "month", "year"}))
public class Timesheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "month")
    private Integer month; 

    @Column(name = "year")
    private Integer year;

    @Enumerated(EnumType.STRING)
    private TimesheetStatus status;

    @Column(name = "rejection_reason")
    private String rejection_reason;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    private Supervisor approver;   

    @OneToMany(mappedBy = "timesheet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Timeentry> timeEntries = new ArrayList<>();

    public Timesheet() {}

    public Timesheet(Integer month, Integer year, TimesheetStatus status, String rejection_reason, Employee employee) {
        this.month = month;
        this.year = year;
        this.status = status;
        this.rejection_reason = rejection_reason;
        this.employee = employee;
    }

    public void checkEditable() {
        if (status != TimesheetStatus.DRAFT && status != TimesheetStatus.REJECTED) {
            throw new IllegalStateException("Timesheet can no longer be edited (status " + status + ")");
        }
    }

    public void checkInPeriod(LocalDate date) {
        if (date == null || date.getMonthValue() != month || date.getYear() != year) {
            throw new IllegalArgumentException("Entry date must be in " + month + "/" + year);
        }
    }

    public void addEntry(Timeentry entry) {
        checkEditable();
        checkInPeriod(entry.getEntryDate());
        if (timeEntries.stream().anyMatch(e -> entry.getEntryDate().equals(e.getEntryDate()))) {
            throw new IllegalStateException("There is already an entry for " + entry.getEntryDate());
        }
        timeEntries.add(entry);
        entry.setTimesheet(this);
    }

    public float getWorkedHours() {
        float sum = 0;
        for (Timeentry e : timeEntries) {
            sum += e.getCreditedHours();
        }
        return sum;
    }

    public void submit() {
        if (status != TimesheetStatus.DRAFT && status != TimesheetStatus.REJECTED) {
            throw new IllegalStateException("Nur offene oder abgelehnte Timesheets können eingereicht werden");
        }
        status = TimesheetStatus.SUBMITTED;
        rejection_reason = null;
    }

    public void approve(Supervisor supervisor) {
        if (status != TimesheetStatus.SUBMITTED) throw new IllegalStateException("Nicht eingereicht");
        status = TimesheetStatus.APPROVED;
        approver = supervisor;
    }

    public void reject(Supervisor supervisor, String reason) {
        if (status != TimesheetStatus.SUBMITTED) throw new IllegalStateException("Nicht eingereicht");
        status = TimesheetStatus.REJECTED;
        approver = supervisor;
        rejection_reason = reason;
    }

    public long getId() {
        return id;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public TimesheetStatus getStatus() {
        return this.status;
    }

    public void setStatus(TimesheetStatus status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejection_reason;
    }

    public void setRejectionReason(String rejection_reason) {
        this.rejection_reason = rejection_reason;
    }
  
    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
    public Supervisor getApprover(){
        return approver;
    }
    public void setApprover(Supervisor approver){
        this.approver = approver;
    }
    public List<Timeentry> getTimeEntries(){
        return this.timeEntries;
    }
    public double getFlexChange() {
        int target = employee.getTargetH() == null ? 8 : employee.getTargetH();

        Map<LocalDate, Double> perDay = new HashMap<>();
        for (Timeentry e : timeEntries) {
            perDay.merge(e.getEntryDate(), (double) e.getCreditedHours(), Double::sum);
        }

        double change = 0;
        for (Map.Entry<LocalDate, Double> day : perDay.entrySet()) {
            DayOfWeek dow = day.getKey().getDayOfWeek();
            boolean weekend = dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
            change += day.getValue() - (weekend ? 0 : target);
        }
        return change;
    }

    public void replaceDayWithSickEntry(Timeentry sickEntry) {
        LocalDate day = sickEntry.getEntryDate();
        checkInPeriod(day);
        timeEntries.removeIf(e -> day.equals(e.getEntryDate()));   // orphanRemoval 
        timeEntries.add(sickEntry);
        sickEntry.setTimesheet(this);
    }

    @Override
    public String toString() {
        return "[id=" + id + ", year=" + year + ", month=" + month + ", status=" + status + "]";
    }
}