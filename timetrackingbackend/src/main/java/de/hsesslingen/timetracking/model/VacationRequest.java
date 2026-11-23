package de.hsesslingen.timetracking.model;

import java.time.DayOfWeek;
import java.time.LocalDate;

import de.hsesslingen.timetracking.EntryTypes.VacationStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "VacationRequest")
public class VacationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private VacationStatus status = VacationStatus.PENDING;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "credited_back_days")
    private Integer creditedBackDays;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    private Supervisor approver;             // set when reviewed


    public VacationRequest() {}

    public VacationRequest(LocalDate startDate, LocalDate endDate, Employee employee) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.employee = employee;
        this.status = VacationStatus.PENDING;
    }

    // Monday to Friday between start and end, both included (public holidays not considered)
    public int getWorkingDays() {
        int days = 0;
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            DayOfWeek dow = d.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                days++;
            }
        }
        return days;
    }

    public void approve(Supervisor supervisor) {
        if (status != VacationStatus.PENDING) {
            throw new IllegalStateException("Only pending requests can be approved (status " + status + ")");
        }
        status = VacationStatus.APPROVED;
        approver = supervisor;
    }

    public void reject(Supervisor supervisor, String reason) {
        if (status != VacationStatus.PENDING) {
            throw new IllegalStateException("Only pending requests can be rejected (status " + status + ")");
        }
        status = VacationStatus.REJECTED;
        approver = supervisor;
        rejectionReason = reason;
    }

    public void cancel() {
        if (status != VacationStatus.PENDING && status != VacationStatus.APPROVED) {
            throw new IllegalStateException("Request is already " + status);
        }
        status = VacationStatus.CANCELLED;
    }
    public int getCreditedBackDays() { 
        return creditedBackDays == null ? 0 : creditedBackDays; 
    }

    // Arbeitstage, die noch gegen das Urlaubskonto zählen
    public int getEffectiveDays() { 
        return Math.max(0, getWorkingDays() - getCreditedBackDays()); 
    }

    public void creditBackDay() { 
        creditedBackDays = getCreditedBackDays() + 1; 
    }


    public long getId() { 
        return id; 
    }
    public LocalDate getStartDate() { 
        return startDate; 
    }
    public LocalDate getEndDate() { 
        return endDate; 
    }
    public VacationStatus getStatus() { 
        return status; 
    }
    public String getRejectionReason() {
        return rejectionReason; 
    }
    public Employee getEmployee() { 
        return employee; 
    }
    public Supervisor getApprover() { 
        return approver; 
    }
}