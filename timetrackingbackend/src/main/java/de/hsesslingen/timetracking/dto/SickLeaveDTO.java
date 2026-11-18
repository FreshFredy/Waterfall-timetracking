package de.hsesslingen.timetracking.dto;

import java.time.LocalDate;
import de.hsesslingen.timetracking.EntryTypes.ExusedEntryType;

public record SickLeaveDTO (
    Long employeeId,            // für wen die Krankmeldung ist
    LocalDate day,
    ExusedEntryType excused,
    String doctorsNotePath
){}
