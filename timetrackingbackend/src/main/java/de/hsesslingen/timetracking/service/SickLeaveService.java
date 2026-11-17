package de.hsesslingen.timetracking.service;

import jakarta.persistence.*;
import de.hsesslingen.timetracking.repository.*;
import de.hsesslingen.timetracking.EntryTypes.*;
import de.hsesslingen.timetracking.model.*;
import java.util.Date;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.Transactional;;;

@Service
@Transactional
public class SickLeaveService {

    private final EmployeeRepository employeeRepository;
    private final TimesheetRepository timesheetRepository;
    private final TimeentryRepository timeentryRepository;
    private final SickLeaveRepository sickLeaveRepository;

    public SickLeaveService(EmployeeRepository employeeRepository,
                            TimesheetRepository timesheetRepository,
                            TimeentryRepository timeentryRepository,
                            SickLeaveRepository sickLeaveRepository) {
        this.employeeRepository = employeeRepository;
        this.timesheetRepository = timesheetRepository;
        this.timeentryRepository = timeentryRepository;
        this.sickLeaveRepository = sickLeaveRepository;
    }

    public SickLeave recordSickLeave(long employeeId, long hrId, LocalDate day,
                                     ExusedEntryType excused, String notePath) {

        Employee employee = employeeRepository.findById(employeeId)      // lowercase e
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        if (!(employeeRepository.findById(hrId).orElseThrow() instanceof HR hr)) {
            throw new IllegalArgumentException("Only HR can record sick leave");
        }

        // get or create the timesheet of the sick day's month
        Timesheet sheet = timesheetRepository
                .findByEmployeeIdAndMonthAndYear(employeeId, day.getMonthValue(), day.getYear())
                .orElseGet(() -> {
                    Timesheet t = new Timesheet(day.getMonthValue(), day.getYear(),
                            TimesheetStatus.DRAFT, null, employee);
                    employee.addTimesheet(t);
                    return timesheetRepository.save(t);
                });

        Timeentry entry = new Timeentry(day, null, null, 0, null, TimeEntryType.SICK);
        sheet.addEntry(entry); // sets entry.timesheet, throws if the sheet is already submitted/approved
        timeentryRepository.save(entry); // persist it now, SickLeave needs a saved entry

        SickLeave sickLeave = new SickLeave(day, excused, notePath, employee, hr, entry);
        return sickLeaveRepository.save(sickLeave);
    }
}
