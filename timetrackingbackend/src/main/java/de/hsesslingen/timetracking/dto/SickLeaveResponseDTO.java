package de.hsesslingen.timetracking.dto;

import java.time.LocalDate;
import de.hsesslingen.timetracking.EntryTypes.ExusedEntryType;


public record SickLeaveResponseDTO (

    long id,
    LocalDate day,
    ExusedEntryType excused,
    String doctorsNotePath,
    long employeeId,
    long createdById,
    Long timeEntryId           

){}
