package de.hsesslingen.timetracking.dto;

import java.util.List;
import de.hsesslingen.timetracking.EntryTypes.TimesheetStatus;

public record TimesheetDTO(
    long id,
    Integer month,
    Integer year,
    TimesheetStatus status,
    String rejectionReason,
    long employeeId,
    Long approverId,                       
    float workedHours,                    
    List<TimeentryResponseDTO> timeEntries,
    double balanceHours
) {}
