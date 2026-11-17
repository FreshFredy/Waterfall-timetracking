package de.hsesslingen.timetracking.dto;

import de.hsesslingen.timetracking.EntryTypes.TimesheetStatus;
import de.hsesslingen.timetracking.model.Employee;
import de.hsesslingen.timetracking.model.Supervisor;

public record TimesheetsDTO (
    Integer month,
    Integer year,
    TimesheetStatus status,
    String rejectionReason,
    Employee employee_id,
    Supervisor approver
    //List<Timeentry> timeEntries

)
{
}
