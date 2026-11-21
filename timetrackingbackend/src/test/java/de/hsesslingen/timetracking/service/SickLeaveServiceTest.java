package de.hsesslingen.timetracking.service;

import java.time.LocalDate;
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
import org.mockito.MockedConstruction;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import de.hsesslingen.timetracking.EntryTypes.ExusedEntryType;
import de.hsesslingen.timetracking.EntryTypes.TimeEntryType;
import de.hsesslingen.timetracking.EntryTypes.TimesheetStatus;
import de.hsesslingen.timetracking.dto.SickLeaveDTO;
import de.hsesslingen.timetracking.dto.SickLeaveResponseDTO;
import de.hsesslingen.timetracking.mapper.SickLeaveMapper;
import de.hsesslingen.timetracking.model.Employee;
import de.hsesslingen.timetracking.model.HR;
import de.hsesslingen.timetracking.model.SickLeave;
import de.hsesslingen.timetracking.model.Timeentry;
import de.hsesslingen.timetracking.model.Timesheet;
import de.hsesslingen.timetracking.repository.EmployeeRepository;
import de.hsesslingen.timetracking.repository.SickLeaveRepository;
import de.hsesslingen.timetracking.repository.TimeentryRepository;
import de.hsesslingen.timetracking.repository.TimesheetRepository;

@ExtendWith(MockitoExtension.class)
class SickLeaveServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private TimesheetRepository timesheetRepository;
    @Mock
    private TimeentryRepository timeentryRepository;
    @Mock
    private SickLeaveRepository sickLeaveRepository;
    @Mock
    private SickLeaveMapper sickLeaveMapper;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private SickLeaveService service;

    private Employee employee;
    private HR hrUser;

    @BeforeEach
    void setUp() {
        employee = spy(new Employee("Max", "Mustermann", "mmustermann", "max@hs.de", "pw", 8, null));
        lenient().when(employee.getId()).thenReturn(1L);

        hrUser = spy(new HR("Clara", "Personal", "cpersonal", "clara@hs.de", "pw", 8));
        lenient().when(hrUser.getId()).thenReturn(50L);
    }

    // ---------- read ----------

    @Test
    void getSickLeave_returnsMappedDtoWhenFound() {
        SickLeave sickLeave = mock(SickLeave.class);
        SickLeaveResponseDTO dto = mock(SickLeaveResponseDTO.class);

        when(sickLeaveRepository.findById(10L)).thenReturn(Optional.of(sickLeave));
        when(sickLeaveMapper.toDTO(sickLeave)).thenReturn(dto);

        assertSame(dto, service.getSickLeave(10L));
    }

    @Test
    void getSickLeave_throwsWhenNotFound() {
        when(sickLeaveRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException ex = assertThrows(
                NoSuchElementException.class,
                () -> service.getSickLeave(99L)
        );
        assertEquals("Sick leave 99 not found", ex.getMessage());
    }

    @Test
    void getSickLeavesByEmployee_returnsMappedList() {
        SickLeave sl1 = mock(SickLeave.class);
        SickLeave sl2 = mock(SickLeave.class);
        SickLeaveResponseDTO dto1 = mock(SickLeaveResponseDTO.class);
        SickLeaveResponseDTO dto2 = mock(SickLeaveResponseDTO.class);

        when(sickLeaveRepository.findByEmployeeId(1L)).thenReturn(List.of(sl1, sl2));
        when(sickLeaveMapper.toDTO(sl1)).thenReturn(dto1);
        when(sickLeaveMapper.toDTO(sl2)).thenReturn(dto2);

        List<SickLeaveResponseDTO> result = service.getSickLeavesByEmployee(1L);

        assertEquals(List.of(dto1, dto2), result);
    }

    // ---------- recordSickLeave ----------

    @Test
    void recordSickLeave_throwsWhenRequiredFieldsMissing() {
        SickLeaveDTO dto = mock(SickLeaveDTO.class);
        when(dto.employeeId()).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> service.recordSickLeave(50L, dto));
        verifyNoInteractions(employeeRepository, timesheetRepository, sickLeaveRepository);
    }

    @Test
    void recordSickLeave_throwsWhenEmployeeNotFound() {
        SickLeaveDTO dto = mock(SickLeaveDTO.class);
        when(dto.employeeId()).thenReturn(999L);
        when(dto.day()).thenReturn(LocalDate.of(2026, 5, 11));
        when(dto.excused()).thenReturn(ExusedEntryType.EXCUSED);

        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.recordSickLeave(50L, dto));
    }

    @Test
    void recordSickLeave_throwsSecurityExceptionWhenCallerIsNotHr() {
        Employee nonHrUser = new Employee("Tom", "Test", "ttest", "tom@hs.de", "pw", 8, null);

        SickLeaveDTO dto = mock(SickLeaveDTO.class);
        when(dto.employeeId()).thenReturn(1L);
        when(dto.day()).thenReturn(LocalDate.of(2026, 5, 11));
        when(dto.excused()).thenReturn(ExusedEntryType.EXCUSED);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(nonHrUser));

        assertThrows(SecurityException.class, () -> service.recordSickLeave(2L, dto));
        verifyNoInteractions(timesheetRepository, sickLeaveRepository);
    }
@Test
    void recordSickLeave_createsNewTimesheetAndNotifiesWithDoctorNoteHint() {
        LocalDate day = LocalDate.of(2026, 5, 11);
        SickLeaveDTO dto = mock(SickLeaveDTO.class);
        when(dto.employeeId()).thenReturn(1L);
        when(dto.day()).thenReturn(day);
        when(dto.excused()).thenReturn(ExusedEntryType.DOCOTRS_NOTE_MISSING);
        when(dto.doctorsNotePath()).thenReturn(null);

        SickLeaveResponseDTO responseDto = mock(SickLeaveResponseDTO.class);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.findById(50L)).thenReturn(Optional.of(hrUser));
        when(timesheetRepository.findByEmployeeIdAndMonthAndYear(1L, 5, 2026)).thenReturn(Optional.empty());
        when(timesheetRepository.save(any(Timesheet.class))).thenAnswer(inv -> inv.getArgument(0));
        when(timeentryRepository.save(any(Timeentry.class))).thenAnswer(inv -> inv.getArgument(0));
        when(sickLeaveRepository.save(any(SickLeave.class))).thenAnswer(inv -> inv.getArgument(0));
        when(sickLeaveMapper.toDTO(any(SickLeave.class))).thenReturn(responseDto);

        try (MockedConstruction<Timeentry> mockedEntry = mockConstruction(Timeentry.class,
                (mock, context) -> {
                    when(mock.getEntryDate()).thenReturn(day);
                    when(mock.getEntry_type()).thenReturn(TimeEntryType.SICK);
                })) {

            SickLeaveResponseDTO result = service.recordSickLeave(50L, dto);

            assertSame(responseDto, result);
            assertEquals(1, mockedEntry.constructed().size());
            assertEquals(1, employee.getTimesheets().size());
            Timesheet createdSheet = employee.getTimesheets().get(0);
            assertEquals(1, createdSheet.getTimeEntries().size());
            assertEquals(TimeEntryType.SICK, createdSheet.getTimeEntries().get(0).getEntry_type());

            verify(notificationService).notify(
                    eq(employee),
                    contains("Please hand in your doctor's note.")
            );
            verify(timeentryRepository).save(any(Timeentry.class));
            verify(sickLeaveRepository).save(any(SickLeave.class));
        }
    }

    @Test
    void recordSickLeave_usesExistingTimesheetWithoutCreatingNewOne() {
        LocalDate day = LocalDate.of(2026, 5, 12);
        Timesheet existingSheet = new Timesheet(5, 2026, TimesheetStatus.DRAFT, null, employee);

        SickLeaveDTO dto = mock(SickLeaveDTO.class);
        when(dto.employeeId()).thenReturn(1L);
        when(dto.day()).thenReturn(day);
        when(dto.excused()).thenReturn(ExusedEntryType.EXCUSED);
        when(dto.doctorsNotePath()).thenReturn("/notes/attest.pdf");

        SickLeaveResponseDTO responseDto = mock(SickLeaveResponseDTO.class);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.findById(50L)).thenReturn(Optional.of(hrUser));
        when(timesheetRepository.findByEmployeeIdAndMonthAndYear(1L, 5, 2026))
                .thenReturn(Optional.of(existingSheet));
        when(timeentryRepository.save(any(Timeentry.class))).thenAnswer(inv -> inv.getArgument(0));
        when(sickLeaveRepository.save(any(SickLeave.class))).thenAnswer(inv -> inv.getArgument(0));
        when(sickLeaveMapper.toDTO(any(SickLeave.class))).thenReturn(responseDto);

        try (MockedConstruction<Timeentry> mockedEntry = mockConstruction(Timeentry.class,
                (mock, context) -> when(mock.getEntryDate()).thenReturn(day))) {

            SickLeaveResponseDTO result = service.recordSickLeave(50L, dto);

            assertSame(responseDto, result);
            assertEquals(1, mockedEntry.constructed().size());
            assertEquals(1, existingSheet.getTimeEntries().size());
            verify(timesheetRepository, never()).save(any());
            verify(notificationService, never()).notify(any(), any());
        }
    }
}
    