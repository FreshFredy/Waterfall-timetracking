package de.hsesslingen.timetracking.dto;

import java.time.LocalDate;
import de.hsesslingen.timetracking.EntryTypes.*;

public record VacationRequestResponseDTO(
    long id,
    LocalDate startDate,
    LocalDate endDate,
    int workingDays,
    VacationStatus status,
    String rejectionReason,
    long employeeId,
    Long approverId                 
) {}
