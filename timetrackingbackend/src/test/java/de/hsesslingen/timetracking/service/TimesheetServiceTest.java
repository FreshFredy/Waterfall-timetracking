package de.hsesslingen.timetracking.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import de.hsesslingen.timetracking.EntryTypes.TimeEntryType;
import de.hsesslingen.timetracking.EntryTypes.TimesheetStatus;
import de.hsesslingen.timetracking.dto.TimeentryRequestDTO;
import de.hsesslingen.timetracking.dto.TimesheetDTO;
import de.hsesslingen.timetracking.mapper.TimeentryMapper;
import de.hsesslingen.timetracking.mapper.TimesheetMapper;
import de.hsesslingen.timetracking.model.Employee;
import de.hsesslingen.timetracking.model.Project;
import de.hsesslingen.timetracking.model.Supervisor;
import de.hsesslingen.timetracking.model.Timeentry;
import de.hsesslingen.timetracking.model.Timesheet;
import de.hsesslingen.timetracking.repository.EmployeeRepository;
import de.hsesslingen.timetracking.repository.SupervisorRepository;
import de.hsesslingen.timetracking.repository.TimeentryRepository;
import de.hsesslingen.timetracking.repository.TimesheetRepository;

@ExtendWith(MockitoExtension.class)
class TimesheetServiceTest {

    @Mock
    private TimesheetRepository timesheetRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private SupervisorRepository supervisorRepository;
    @Mock
    private TimesheetMapper timesheetMapper;
    @Mock
    private TimeentryMapper timeentryMapper;
    @Mock
    private NotificationService notificationService;
    @Mock
    private ProjectService projectService;
    @Mock
    private TimeentryRepository timeentryRepository;

    @InjectMocks
    private TimesheetService service;

    private Supervisor supervisor;
    private Employee employee;

    @BeforeEach
    void setUp() {
        supervisor = spy(new Supervisor("Anna", "Schmidt", "aschmidt", "anna@hs.de", "pw", 8));
        lenient().when(supervisor.getId()).thenReturn(10L);

        employee = spy(new Employee("Max", "Mustermann", "mmustermann", "max@hs.de", "pw", 8, supervisor));
        lenient().when(employee.getId()).thenReturn(1L);
    }

    // ---------- read ----------

    @Test
    void getTimesheet_returnsMappedDto() {
        Timesheet sheet = new Timesheet(5, 2026, TimesheetStatus.DRAFT, null, employee);
        TimesheetDTO dto = mock(TimesheetDTO.class);

        when(timesheetRepository.findById(100L)).thenReturn(Optional.of(sheet));
        when(timesheetMapper.toDTO(sheet)).thenReturn(dto);

        assertSame(dto, service.getTimesheet(100L));
    }

    @Test
    void getTimesheet_throwsWhenNotFound() {
        when(timesheetRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.getTimesheet(999L));
    }

    @Test
    void getTimesheets_filtersByMonthAndYear() {
        Timesheet may2026 = new Timesheet(5, 2026, TimesheetStatus.DRAFT, null, employee);
        Timesheet jun2026 = new Timesheet(6, 2026, TimesheetStatus.DRAFT, null, employee);
        TimesheetDTO mayDto = mock(TimesheetDTO.class);

        when(timesheetRepository.findByEmployeeIdOrderByYearDescMonthDesc(1L))
                .thenReturn(List.of(jun2026, may2026));
        when(timesheetMapper.toDTO(may2026)).thenReturn(mayDto);

        List<TimesheetDTO> result = service.getTimesheets(1L, 5, 2026);

        assertEquals(1, result.size());
        assertSame(mayDto, result.get(0));
    }

    // ---------- recordWorkTime ----------

    @Test
    void recordWorkTime_createsNewTimesheetAndSavesEntry() {
        LocalDate date = LocalDate.of(2026, 5, 6);
        TimeentryRequestDTO request = mock(TimeentryRequestDTO.class);
        when(request.entryDate()).thenReturn(date);
        when(request.entryType()).thenReturn(TimeEntryType.WORK);
        when(request.projectID()).thenReturn(50L);

        Project project = new Project("SE Projekt");
        Timeentry entry = new Timeentry(date, LocalTime.of(8, 0), LocalTime.of(16, 30), 30, TimeEntryType.WORK, project);
        TimesheetDTO expectedDto = mock(TimesheetDTO.class);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(timesheetRepository.findByEmployeeIdAndMonthAndYear(1L, 5, 2026)).thenReturn(Optional.empty());
        when(projectService.getById(50L)).thenReturn(project);
        when(timeentryMapper.toEntity(request, project)).thenReturn(entry);
        when(timeentryRepository.findByTimesheetEmployeeIdAndEntryDate(eq(1L), any())).thenReturn(List.of());
        when(timesheetRepository.save(any(Timesheet.class))).thenAnswer(inv -> inv.getArgument(0));
        when(timesheetMapper.toDTO(any(Timesheet.class))).thenReturn(expectedDto);

        TimesheetDTO result = service.recordWorkTime(1L, request);

        assertSame(expectedDto, result);
        assertEquals(1, employee.getTimesheets().size());
        assertEquals(1, employee.getTimesheets().get(0).getTimeEntries().size());
        verify(timesheetRepository).save(any(Timesheet.class));
    }

    @Test
    void recordWorkTime_rejectsSickEntryType() {
        TimeentryRequestDTO request = mock(TimeentryRequestDTO.class);
        when(request.entryDate()).thenReturn(LocalDate.of(2026, 5, 6));
        when(request.entryType()).thenReturn(TimeEntryType.SICK);

        assertThrows(IllegalArgumentException.class, () -> service.recordWorkTime(1L, request));
        verifyNoInteractions(timesheetRepository);
    }

    @Test
    void recordWorkTime_throwsWhenRestPeriodUnder11HoursFromPreviousDay() {
        LocalDate today = LocalDate.of(2026, 5, 6);
        LocalDate yesterday = today.minusDays(1);

        TimeentryRequestDTO request = mock(TimeentryRequestDTO.class);
        when(request.entryDate()).thenReturn(today);
        when(request.entryType()).thenReturn(TimeEntryType.WORK);
        when(request.projectID()).thenReturn(50L);

        Project project = new Project("SE Projekt");
        Timeentry yesterdayEntry = spy(new Timeentry(yesterday, LocalTime.of(14, 0), LocalTime.of(22, 0), 30, TimeEntryType.WORK, project));
        when(yesterdayEntry.getId()).thenReturn(1L);

        // Start um 07:00 -> nur 9 Stunden Ruhezeit nach 22:00 am Vortag
        Timeentry todayEntry = spy(new Timeentry(today, LocalTime.of(7, 0), LocalTime.of(15, 0), 30, TimeEntryType.WORK, project));
        when(todayEntry.getId()).thenReturn(2L);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(timesheetRepository.findByEmployeeIdAndMonthAndYear(1L, 5, 2026)).thenReturn(Optional.empty());
        when(projectService.getById(50L)).thenReturn(project);
        when(timeentryMapper.toEntity(request, project)).thenReturn(todayEntry);
        when(timeentryRepository.findByTimesheetEmployeeIdAndEntryDate(1L, yesterday))
                .thenReturn(List.of(yesterdayEntry));

        assertThrows(IllegalArgumentException.class, () -> service.recordWorkTime(1L, request));
        verify(timesheetRepository, never()).save(any());
    }

    // ---------- changeWorkTime ----------

    @Test
    void changeWorkTime_throwsWhenEntryIsSick() {
        Project project = new Project("Krank");
        Timeentry sickEntry = spy(new Timeentry(LocalDate.of(2026, 5, 6), LocalTime.of(8, 0), LocalTime.of(16, 0), 0, TimeEntryType.SICK, project));
        when(sickEntry.getId()).thenReturn(77L);

        Timesheet sheet = new Timesheet(5, 2026, TimesheetStatus.DRAFT, null, employee);
        sheet.addEntry(sickEntry);

        TimeentryRequestDTO request = mock(TimeentryRequestDTO.class);
        when(request.entryDate()).thenReturn(LocalDate.of(2026, 5, 6));
        when(request.entryType()).thenReturn(TimeEntryType.WORK);

        when(timesheetRepository.findById(100L)).thenReturn(Optional.of(sheet));

        assertThrows(IllegalStateException.class, () -> service.changeWorkTime(100L, 77L, request));
    }

    // ---------- submit, approve, reject ----------

    @Test
    void submit_submitsTimesheetAndNotifiesSupervisor() {
        Timesheet sheet = spy(new Timesheet(5, 2026, TimesheetStatus.DRAFT, null, employee));
        TimesheetDTO dto = mock(TimesheetDTO.class);

        when(timesheetRepository.findById(100L)).thenReturn(Optional.of(sheet));
        when(timesheetMapper.toDTO(sheet)).thenReturn(dto);

        TimesheetDTO result = service.submit(100L);

        assertSame(dto, result);
        verify(sheet).submit();
        verify(notificationService).notify(eq(supervisor), contains("Max Mustermann submitted the timesheet for 5/2026"));
    }

    @Test
    void approve_approvesTimesheetUpdatesFlexAccountAndNotifiesEmployee() {
        Timesheet sheet = spy(new Timesheet(5, 2026, TimesheetStatus.SUBMITTED, null, employee));
        doReturn(4.5).when(sheet).getFlexChange();
        TimesheetDTO dto = mock(TimesheetDTO.class);

        when(timesheetRepository.findById(100L)).thenReturn(Optional.of(sheet));
        when(supervisorRepository.findById(10L)).thenReturn(Optional.of(supervisor));
        when(timesheetMapper.toDTO(sheet)).thenReturn(dto);

        TimesheetDTO result = service.approve(100L, 10L);

        assertSame(dto, result);
        verify(sheet).approve(supervisor);
        assertEquals(4.5, employee.getFlexAccount().getBalance_hours(), 0.0001);
        verify(notificationService).notify(eq(employee), contains("5/2026 was approved"));
    }

    @Test
    void approve_throwsSecurityExceptionWhenSupervisorIsNotAssigned() {
        Supervisor otherSupervisor = spy(new Supervisor("Tom", "Beck", "tbeck", "tom@hs.de", "pw", 8));
        when(otherSupervisor.getId()).thenReturn(99L);

        Timesheet sheet = new Timesheet(5, 2026, TimesheetStatus.SUBMITTED, null, employee);

        when(timesheetRepository.findById(100L)).thenReturn(Optional.of(sheet));
        when(supervisorRepository.findById(99L)).thenReturn(Optional.of(otherSupervisor));

        assertThrows(SecurityException.class, () -> service.approve(100L, 99L));
        verifyNoInteractions(notificationService);
    }

    @Test
    void reject_throwsWhenReasonIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> service.reject(100L, 10L, "   "));
        verifyNoInteractions(timesheetRepository);
    }

    @Test
    void reject_rejectsTimesheetAndNotifiesEmployee() {
        Timesheet sheet = spy(new Timesheet(5, 2026, TimesheetStatus.SUBMITTED, null, employee));
        TimesheetDTO dto = mock(TimesheetDTO.class);

        when(timesheetRepository.findById(100L)).thenReturn(Optional.of(sheet));
        when(supervisorRepository.findById(10L)).thenReturn(Optional.of(supervisor));
        when(timesheetMapper.toDTO(sheet)).thenReturn(dto);

        TimesheetDTO result = service.reject(100L, 10L, "Pausenzeit fehlt");

        assertSame(dto, result);
        verify(sheet).reject(supervisor, "Pausenzeit fehlt");
        verify(notificationService).notify(eq(employee), contains("rejected: Pausenzeit fehlt"));
    }
}