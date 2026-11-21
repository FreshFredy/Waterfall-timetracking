package de.hsesslingen.timetracking.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
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
import static org.mockito.ArgumentMatchers.anyList;
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

import de.hsesslingen.timetracking.EntryTypes.VacationStatus;
import de.hsesslingen.timetracking.dto.VacationRequestCreateDTO;
import de.hsesslingen.timetracking.dto.VacationRequestResponseDTO;
import de.hsesslingen.timetracking.mapper.VacationRequestMapper;
import de.hsesslingen.timetracking.model.Employee;
import de.hsesslingen.timetracking.model.Supervisor;
import de.hsesslingen.timetracking.model.VacationBalance;
import de.hsesslingen.timetracking.model.VacationRequest;
import de.hsesslingen.timetracking.repository.EmployeeRepository;
import de.hsesslingen.timetracking.repository.SupervisorRepository;
import de.hsesslingen.timetracking.repository.VacationRequestRepository;

@ExtendWith(MockitoExtension.class)
class VacationServiceTest {

    @Mock
    private VacationRequestRepository vacationRequestRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private SupervisorRepository supervisorRepository;
    @Mock
    private VacationRequestMapper vacationRequestMapper;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private VacationService service;

    private Supervisor supervisor;
    private Employee employee;
    private VacationBalance vacationBalance;

    @BeforeEach
    void setUp() {
        supervisor = spy(new Supervisor("Anna", "Schmidt", "aschmidt", "anna@hs.de", "pw", 8));
        lenient().when(supervisor.getId()).thenReturn(10L);

        employee = spy(new Employee("Max", "Mustermann", "mmustermann", "max@hs.de", "pw", 8, supervisor));
        lenient().when(employee.getId()).thenReturn(1L);

        vacationBalance = spy(new VacationBalance(30, employee));
        lenient().when(employee.getVacationBalance()).thenReturn(vacationBalance);
    }

    // ---------- read ----------

    @Test
    void getRequest_returnsMappedDtoWhenFound() {
        VacationRequest request = new VacationRequest(
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(12),
                employee
        );
        VacationRequestResponseDTO dto = mock(VacationRequestResponseDTO.class);

        when(vacationRequestRepository.findById(5L)).thenReturn(Optional.of(request));
        when(vacationRequestMapper.toDTO(request)).thenReturn(dto);

        assertSame(dto, service.getRequest(5L));
    }

    @Test
    void getRequest_throwsWhenNotFound() {
        when(vacationRequestRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.getRequest(404L));
    }

    @Test
    void getRequestsOfEmployee_mapsRequestsToDtos() {
        VacationRequest r1 = new VacationRequest(
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(11),
                employee
        );
        VacationRequestResponseDTO dto1 = mock(VacationRequestResponseDTO.class);

        when(vacationRequestRepository.findByEmployeeIdOrderByStartDateDesc(1L)).thenReturn(List.of(r1));
        when(vacationRequestMapper.toDTO(r1)).thenReturn(dto1);

        List<VacationRequestResponseDTO> result = service.getRequestsOfEmployee(1L);

        assertEquals(List.of(dto1), result);
    }

    @Test
    void getPendingForSupervisor_queriesPendingRequestsAndMapsDtos() {
        VacationRequest pending = new VacationRequest(
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(12),
                employee
        );
        VacationRequestResponseDTO dto = mock(VacationRequestResponseDTO.class);

        when(vacationRequestRepository
                .findByEmployeeSupervisorIdAndStatusOrderByStartDate(10L, VacationStatus.PENDING))
                .thenReturn(List.of(pending));
        when(vacationRequestMapper.toDTO(pending)).thenReturn(dto);

        List<VacationRequestResponseDTO> result = service.getPendingForSupervisor(10L);

        assertEquals(List.of(dto), result);
    }

    // ---------- create ----------

    @Test
    void create_reservesDaysNotifiesSupervisorAndSavesRequest() {
        LocalDate start = LocalDate.now().plusDays(14).with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
        LocalDate end = start.plusDays(2); // Mo-Mi = 3 Werktage

        VacationRequestCreateDTO dto = mock(VacationRequestCreateDTO.class);
        when(dto.startDate()).thenReturn(start);
        when(dto.endDate()).thenReturn(end);

        VacationRequest entity = spy(new VacationRequest(start, end, employee));
        doReturn(3).when(entity).getWorkingDays();
        VacationRequestResponseDTO responseDto = mock(VacationRequestResponseDTO.class);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(vacationRequestRepository.countOverlapping(
                eq(1L), eq(start), eq(end), eq(List.of(VacationStatus.PENDING, VacationStatus.APPROVED))))
                .thenReturn(0L);
        when(vacationRequestMapper.toEntity(dto, employee)).thenReturn(entity);
        when(vacationRequestRepository.save(entity)).thenReturn(entity);
        when(vacationRequestMapper.toDTO(entity)).thenReturn(responseDto);

        VacationRequestResponseDTO result = service.create(1L, dto);

        assertSame(responseDto, result);
        verify(vacationBalance).reserveDays(3);
        verify(notificationService).notify(
                eq(supervisor),
                contains("Max Mustermann requested vacation from " + start + " to " + end)
        );
        verify(vacationRequestRepository).save(entity);
    }

    @Test
    void create_throwsWhenStartDateIsInThePast() {
        VacationRequestCreateDTO dto = mock(VacationRequestCreateDTO.class);
        when(dto.startDate()).thenReturn(LocalDate.now().minusDays(1));
        when(dto.endDate()).thenReturn(LocalDate.now().plusDays(2));

        assertThrows(IllegalArgumentException.class, () -> service.create(1L, dto));
        verifyNoInteractions(vacationRequestRepository, notificationService);
    }

    @Test
    void create_throwsWhenEndDateBeforeStartDate() {
        LocalDate start = LocalDate.now().plusDays(5);
        VacationRequestCreateDTO dto = mock(VacationRequestCreateDTO.class);
        when(dto.startDate()).thenReturn(start);
        when(dto.endDate()).thenReturn(start.minusDays(1));

        assertThrows(IllegalArgumentException.class, () -> service.create(1L, dto));
    }

    @Test
    void create_throwsWhenOverlappingRequestExists() {
        LocalDate start = LocalDate.now().plusDays(10);
        LocalDate end = LocalDate.now().plusDays(12);

        VacationRequestCreateDTO dto = mock(VacationRequestCreateDTO.class);
        when(dto.startDate()).thenReturn(start);
        when(dto.endDate()).thenReturn(end);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(vacationRequestRepository.countOverlapping(
                eq(1L), eq(start), eq(end), anyList()))
                .thenReturn(1L);

        assertThrows(IllegalStateException.class, () -> service.create(1L, dto));
        verify(vacationRequestRepository, never()).save(any());
    }

    @Test
    void create_throwsWhenWorkingDaysZero() {
        LocalDate saturday = LocalDate.now().plusDays(14).with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
        LocalDate sunday = saturday.plusDays(1);

        VacationRequestCreateDTO dto = mock(VacationRequestCreateDTO.class);
        when(dto.startDate()).thenReturn(saturday);
        when(dto.endDate()).thenReturn(sunday);

        VacationRequest entity = spy(new VacationRequest(saturday, sunday, employee));
        doReturn(0).when(entity).getWorkingDays();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(vacationRequestRepository.countOverlapping(eq(1L), eq(saturday), eq(sunday), anyList()))
                .thenReturn(0L);
        when(vacationRequestMapper.toEntity(dto, employee)).thenReturn(entity);

        assertThrows(IllegalArgumentException.class, () -> service.create(1L, dto));
        verify(vacationRequestRepository, never()).save(any());
    }

    // ---------- cancel ----------

    @Test
    void cancel_pendingRequestReleasesDaysWithoutSupervisorNotification() {
        LocalDate start = LocalDate.now().plusDays(10);
        LocalDate end = LocalDate.now().plusDays(12);
        VacationRequest request = spy(new VacationRequest(start, end, employee));
        doReturn(VacationStatus.PENDING).when(request).getStatus();
        doReturn(3).when(request).getWorkingDays();

        VacationRequestResponseDTO dto = mock(VacationRequestResponseDTO.class);
        when(vacationRequestRepository.findById(20L)).thenReturn(Optional.of(request));
        when(vacationRequestMapper.toDTO(request)).thenReturn(dto);

        VacationRequestResponseDTO result = service.cancel(20L, 1L);

        assertSame(dto, result);
        verify(request).cancel();
        verify(vacationBalance).releaseDays(3);
        verifyNoInteractions(notificationService);
    }

    @Test
    void cancel_approvedRequestCancelsApprovedDaysAndNotifiesSupervisor() {
        LocalDate start = LocalDate.now().plusDays(10);
        LocalDate end = LocalDate.now().plusDays(12);
        VacationRequest request = spy(new VacationRequest(start, end, employee));
        doReturn(VacationStatus.APPROVED).when(request).getStatus();
        doReturn(3).when(request).getWorkingDays();

        VacationRequestResponseDTO dto = mock(VacationRequestResponseDTO.class);
        when(vacationRequestRepository.findById(20L)).thenReturn(Optional.of(request));
        when(vacationRequestMapper.toDTO(request)).thenReturn(dto);

        VacationRequestResponseDTO result = service.cancel(20L, 1L);

        assertSame(dto, result);
        verify(request).cancel();
        verify(vacationBalance).cancelApprovedDays(3);
        verify(notificationService).notify(
                eq(supervisor),
                contains("cancelled the approved vacation")
        );
    }

    @Test
    void cancel_throwsSecurityExceptionForOtherEmployee() {
        LocalDate start = LocalDate.now().plusDays(10);
        VacationRequest request = spy(new VacationRequest(start, start.plusDays(1), employee));
        when(vacationRequestRepository.findById(20L)).thenReturn(Optional.of(request));

        assertThrows(SecurityException.class, () -> service.cancel(20L, 999L));
        verify(request, never()).cancel();
    }

    @Test
    void cancel_throwsWhenVacationAlreadyStartedTodayOrEarlier() {
        VacationRequest request = new VacationRequest(LocalDate.now(), LocalDate.now().plusDays(2), employee);
        when(vacationRequestRepository.findById(20L)).thenReturn(Optional.of(request));

        assertThrows(IllegalStateException.class, () -> service.cancel(20L, 1L));
    }

    // ---------- approve & reject ----------

    @Test
    void approve_approvesRequestMovesDaysToTakenAndNotifiesEmployee() {
        LocalDate start = LocalDate.now().plusDays(10);
        LocalDate end = LocalDate.now().plusDays(12);
        VacationRequest request = spy(new VacationRequest(start, end, employee));
        doReturn(3).when(request).getWorkingDays();

        VacationRequestResponseDTO dto = mock(VacationRequestResponseDTO.class);
        when(vacationRequestRepository.findById(30L)).thenReturn(Optional.of(request));
        when(supervisorRepository.findById(10L)).thenReturn(Optional.of(supervisor));
        when(vacationRequestMapper.toDTO(request)).thenReturn(dto);

        VacationRequestResponseDTO result = service.approve(30L, 10L);

        assertSame(dto, result);
        verify(request).approve(supervisor);
        verify(vacationBalance).approveDays(3);
        verify(notificationService).notify(
                eq(employee),
                contains("was approved")
        );
    }

    @Test
    void reject_throwsWhenReasonIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> service.reject(30L, 10L, "  "));
        verifyNoInteractions(vacationRequestRepository);
    }

    @Test
    void reject_rejectsRequestReleasesDaysAndNotifiesEmployee() {
        LocalDate start = LocalDate.now().plusDays(10);
        LocalDate end = LocalDate.now().plusDays(12);
        VacationRequest request = spy(new VacationRequest(start, end, employee));
        doReturn(3).when(request).getWorkingDays();

        VacationRequestResponseDTO dto = mock(VacationRequestResponseDTO.class);
        when(vacationRequestRepository.findById(30L)).thenReturn(Optional.of(request));
        when(supervisorRepository.findById(10L)).thenReturn(Optional.of(supervisor));
        when(vacationRequestMapper.toDTO(request)).thenReturn(dto);

        VacationRequestResponseDTO result = service.reject(30L, 10L, "Projekt-Deadline");

        assertSame(dto, result);
        verify(request).reject(supervisor, "Projekt-Deadline");
        verify(vacationBalance).releaseDays(3);
        verify(notificationService).notify(
                eq(employee),
                contains("was rejected: Projekt-Deadline")
        );
    }
}