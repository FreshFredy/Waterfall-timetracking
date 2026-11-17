package de.hsesslingen.timetracking.dto;

import java.time.LocalDate;
import de.hsesslingen.timetracking.EntryTypes.ExusedEntryType;
import de.hsesslingen.timetracking.model.Employee;
import de.hsesslingen.timetracking.model.HR;
import de.hsesslingen.timetracking.model.Timeentry;

public record SickLeaveDTO (
    LocalDate date,
    String filepath_doctors_note,
    ExusedEntryType exusedEntryType,
    Employee employee,
    HR createdBy,
    Timeentry timeEntry
){}
