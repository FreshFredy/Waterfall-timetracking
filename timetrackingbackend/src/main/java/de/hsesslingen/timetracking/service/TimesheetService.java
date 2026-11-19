package de.hsesslingen.timetracking.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.hsesslingen.timetracking.EntryTypes.TimeEntryType;
import de.hsesslingen.timetracking.EntryTypes.TimesheetStatus;
import de.hsesslingen.timetracking.dto.TimeentryRequestDTO;
import de.hsesslingen.timetracking.dto.TimesheetDTO;

import de.hsesslingen.timetracking.mapper.TimeentryMapper;
import de.hsesslingen.timetracking.mapper.TimesheetMapper;

import de.hsesslingen.timetracking.model.Employee;
import de.hsesslingen.timetracking.model.Supervisor;
import de.hsesslingen.timetracking.model.Timeentry;
import de.hsesslingen.timetracking.model.Timesheet;

import de.hsesslingen.timetracking.repository.EmployeeRepository;
import de.hsesslingen.timetracking.repository.SupervisorRepository;
import de.hsesslingen.timetracking.repository.TimesheetRepository;

@Service
public class TimesheetService {

    private final TimesheetRepository timesheetRepository;
    private final EmployeeRepository employeeRepository;
    private final SupervisorRepository supervisorRepository;
    private final TimesheetMapper timesheetMapper;
    private final TimeentryMapper timeentryMapper;
    private final NotificationService notificationService;

    public TimesheetService(TimesheetRepository timesheetRepository,
                            EmployeeRepository employeeRepository,
                            SupervisorRepository supervisorRepository,
                            TimesheetMapper timesheetMapper,
                            TimeentryMapper timeentryMapper,
                        NotificationService notificationMapper) {
        this.timesheetRepository = timesheetRepository;
        this.employeeRepository = employeeRepository;
        this.supervisorRepository = supervisorRepository;
        this.timesheetMapper = timesheetMapper;
        this.timeentryMapper = timeentryMapper;
        this.notificationService = notificationMapper;
    }

    // ---------- read ----------

    @Transactional(readOnly = true)
    public TimesheetDTO getTimesheet(long id) {
        return timesheetMapper.toDTO(findTimesheet(id));
    }

    @Transactional(readOnly = true)
    public List<TimesheetDTO> getTimesheets(long employeeId, Integer month, Integer year) {
        return timesheetRepository.findByEmployeeIdOrderByYearDescMonthDesc(employeeId).stream()
                .filter(t -> month == null || t.getMonth().equals(month))
                .filter(t -> year == null || t.getYear().equals(year))
                .map(timesheetMapper::toDTO)
                .toList();
    }

    // ---------- employee: record and edit time ----------

    @Transactional
    public TimesheetDTO recordWorkTime(long employeeId, TimeentryRequestDTO dto) {
        validate(dto);
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NoSuchElementException("Employee " + employeeId + " not found"));

        int month = dto.entryDate().getMonthValue();
        int year = dto.entryDate().getYear();

        // get the timesheet of the entry's month, or create it
        Timesheet sheet = timesheetRepository
                .findByEmployeeIdAndMonthAndYear(employeeId, month, year)
                .orElseGet(() -> {
                    Timesheet t = new Timesheet(month, year, TimesheetStatus.DRAFT, null, employee);
                    employee.addTimesheet(t);
                    return t;
                });

        sheet.addEntry(timeentryMapper.toEntity(dto));   // checks status and month, sets entry.timesheet
        return timesheetMapper.toDTO(timesheetRepository.save(sheet));
    }

    @Transactional
    public TimesheetDTO changeWorkTime(long timesheetId, long entryId, TimeentryRequestDTO dto) {
        Timesheet sheet = findTimesheet(timesheetId);
        sheet.checkEditable();
        validate(dto);
        sheet.checkInPeriod(dto.entryDate());

        Timeentry entry = sheet.getTimeEntries().stream()
                .filter(e -> e.getId() == entryId)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(
                        "Entry " + entryId + " not found in timesheet " + timesheetId));
        if (entry.getEntry_type() == TimeEntryType.SICK) {
            throw new IllegalStateException("Sick entries are managed by HR");
        }

        timeentryMapper.updateEntityFromDTO(dto, entry);   // recalculates creditedHours
        return timesheetMapper.toDTO(sheet);               // no save() needed: the entity is managed
    }

    // ---------- employee: submit ----------

    @Transactional
    public TimesheetDTO submit(long timesheetId) {
        Timesheet sheet = findTimesheet(timesheetId);
        sheet.submit();
        Employee employee = sheet.getEmployee();
        notificationService.notify(employee.getSupervisor(),
            employee.getFirstname() + " " + employee.getLastname()
            + " submitted the timesheet for " + sheet.getMonth() + "/" + sheet.getYear());
        return timesheetMapper.toDTO(sheet);
    }

    // ---------- supervisor: review ----------

    @Transactional
    public TimesheetDTO approve(long timesheetId, long supervisorId) {
        Timesheet sheet = findTimesheet(timesheetId);
        sheet.approve(requireResponsibleSupervisor(sheet, supervisorId));
        sheet.getEmployee().getFlexAccount().addTime(sheet.getFlexChange());
        notificationService.notify(sheet.getEmployee(),
            "Your timesheet for " + sheet.getMonth() + "/" + sheet.getYear() + " was approved");
        return timesheetMapper.toDTO(sheet);
    }

    @Transactional
    public TimesheetDTO reject(long timesheetId, long supervisorId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("A rejection reason is required");
        }
        Timesheet sheet = findTimesheet(timesheetId);
        sheet.reject(requireResponsibleSupervisor(sheet, supervisorId), reason);
    
        notificationService.notify(sheet.getEmployee(),
        "Your timesheet for " + sheet.getMonth() + "/" + sheet.getYear() + " was rejected: " + reason);
        return timesheetMapper.toDTO(sheet);
    }
    @Transactional(readOnly = true)
    public List<TimesheetDTO> getTimesheetsForSupervisor(long supervisorId, TimesheetStatus status) {
        if (!supervisorRepository.existsById(supervisorId)) {
            throw new NoSuchElementException("Supervisor " + supervisorId + " not found");
        }
        List<Timesheet> sheets = status == null
            ? timesheetRepository.findByEmployeeSupervisorIdOrderByYearDescMonthDesc(supervisorId)
            : timesheetRepository.findByEmployeeSupervisorIdAndStatusOrderByYearDescMonthDesc(supervisorId, status);
        return sheets.stream().map(timesheetMapper::toDTO).toList();
    }

    // ---------- helpers ----------

    private Timesheet findTimesheet(long id) {
        return timesheetRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Timesheet " + id + " not found"));
    }

    private Supervisor requireResponsibleSupervisor(Timesheet sheet, long supervisorId) {
        Supervisor supervisor = supervisorRepository.findById(supervisorId)
                .orElseThrow(() -> new NoSuchElementException("Supervisor " + supervisorId + " not found"));
        Supervisor assigned = sheet.getEmployee().getSupervisor();
        if (assigned == null || assigned.getId() != supervisor.getId()) {
            throw new SecurityException("Only the employee's own supervisor may review this timesheet");
        }
        return supervisor;
    }

    private void validate(TimeentryRequestDTO dto) {
        if (dto == null || dto.entryDate() == null || dto.entryType() == null) {
            throw new IllegalArgumentException("entryDate and entryType are required");
        }
        if (dto.entryType() == TimeEntryType.SICK) {
            throw new IllegalArgumentException("Sick days are recorded by HR, not as work time");
        }
    }

}
