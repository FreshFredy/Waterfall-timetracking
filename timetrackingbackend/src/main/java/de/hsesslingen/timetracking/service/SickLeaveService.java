package de.hsesslingen.timetracking.service;


import de.hsesslingen.timetracking.repository.*;
import de.hsesslingen.timetracking.EntryTypes.*;
import de.hsesslingen.timetracking.model.*;
import de.hsesslingen.timetracking.dto.SickLeaveDTO;
import de.hsesslingen.timetracking.dto.SickLeaveResponseDTO;
import de.hsesslingen.timetracking.mapper.SickLeaveMapper;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.transaction.annotation.Transactional;;;

@Service
@Transactional
public class SickLeaveService {

    private final EmployeeRepository employeeRepository;
    private final TimesheetRepository timesheetRepository;
    private final TimeentryRepository timeentryRepository;
    private final SickLeaveRepository sickLeaveRepository;
    private final SickLeaveMapper sickLeaveMapper;
    private final NotificationService notificationService;

    public SickLeaveService(EmployeeRepository employeeRepository,
                            TimesheetRepository timesheetRepository,
                            TimeentryRepository timeentryRepository,
                            SickLeaveRepository sickLeaveRepository,
                            SickLeaveMapper sickLeaveMapper,
                            NotificationService notificationService) {
        this.employeeRepository = employeeRepository;
        this.timesheetRepository = timesheetRepository;
        this.timeentryRepository = timeentryRepository;
        this.sickLeaveRepository = sickLeaveRepository;
        this.sickLeaveMapper = sickLeaveMapper;
        this.notificationService = notificationService;
    }
@Transactional(readOnly = true)
public SickLeaveResponseDTO getSickLeave(long id) {
    return sickLeaveRepository.findById(id)
            .map(sickLeaveMapper::toDTO)
            .orElseThrow(() -> new NoSuchElementException("Sick leave " + id + " not found"));
}

@Transactional(readOnly = true)
public List<SickLeaveResponseDTO> getSickLeavesByEmployee(long employeeId) {
    return sickLeaveRepository.findByEmployeeId(employeeId).stream()
            .map(sickLeaveMapper::toDTO)
            .toList();
}

public SickLeaveResponseDTO recordSickLeave(long hrId, SickLeaveDTO dto) {
    if (dto.employeeId() == null || dto.day() == null || dto.excused() == null) {
        throw new IllegalArgumentException("employeeId, day and excused are required");
    }

    Employee employee = employeeRepository.findById(dto.employeeId())
            .orElseThrow(() -> new NoSuchElementException("Employee " + dto.employeeId() + " not found"));

    if (!(employeeRepository.findById(hrId)
            .orElseThrow(() -> new NoSuchElementException("User " + hrId + " not found")) instanceof HR hr)) {
        throw new SecurityException("Only HR can record sick leave");
    }

    // get or create the timesheet of the sick day's month
    Timesheet sheet = timesheetRepository
            .findByEmployeeIdAndMonthAndYear(employee.getId(), dto.day().getMonthValue(), dto.day().getYear())
            .orElseGet(() -> {
                Timesheet t = new Timesheet(dto.day().getMonthValue(), dto.day().getYear(),
                        TimesheetStatus.DRAFT, null, employee);
                employee.addTimesheet(t);
                String hint = dto.excused() == ExusedEntryType.DOCOTRS_NOTE_MISSING ? " Please hand in your doctor's note." : "";
                    notificationService.notify(employee, "HR recorded a sick day for you on " + dto.day() + "." + hint);
                return timesheetRepository.save(t);
            });

    Timeentry entry = new Timeentry(dto.day(), null, null, 0, TimeEntryType.SICK);
    sheet.addEntry(entry);
    timeentryRepository.save(entry);

    SickLeave sickLeave = new SickLeave(dto.day(), dto.doctorsNotePath(), employee, hr, entry, dto.excused());
    return sickLeaveMapper.toDTO(sickLeaveRepository.save(sickLeave));
}
}
