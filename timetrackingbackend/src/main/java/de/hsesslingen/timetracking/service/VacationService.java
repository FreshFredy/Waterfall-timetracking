package de.hsesslingen.timetracking.service;


import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.hsesslingen.timetracking.EntryTypes.VacationStatus;
import de.hsesslingen.timetracking.dto.VacationRequestCreateDTO;
import de.hsesslingen.timetracking.dto.VacationRequestResponseDTO;
import de.hsesslingen.timetracking.mapper.VacationRequestMapper;
import de.hsesslingen.timetracking.model.Employee;
import de.hsesslingen.timetracking.model.Supervisor;
import de.hsesslingen.timetracking.model.VacationRequest;
import de.hsesslingen.timetracking.repository.EmployeeRepository;
import de.hsesslingen.timetracking.repository.SupervisorRepository;
import de.hsesslingen.timetracking.repository.VacationRequestRepository;


@Service
public class VacationService {

    private final VacationRequestRepository vacationRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final SupervisorRepository supervisorRepository;
    private final VacationRequestMapper vacationRequestMapper;
    private final NotificationService notificationService;

    public VacationService(VacationRequestRepository vacationRequestRepository,
                           EmployeeRepository employeeRepository,
                           SupervisorRepository supervisorRepository,
                           VacationRequestMapper vacationRequestMapper,
                            NotificationService notificationService) {
        this.vacationRequestRepository = vacationRequestRepository;
        this.employeeRepository = employeeRepository;
        this.supervisorRepository = supervisorRepository;
        this.vacationRequestMapper = vacationRequestMapper;
        this.notificationService = notificationService;
    }

    // ---------- read ----------

    @Transactional(readOnly = true)
    public VacationRequestResponseDTO getRequest(long id) {
        return vacationRequestMapper.toDTO(findRequest(id));
    }

    @Transactional(readOnly = true)
    public List<VacationRequestResponseDTO> getRequestsOfEmployee(long employeeId) {
        return vacationRequestRepository.findByEmployeeIdOrderByStartDateDesc(employeeId).stream()
                .map(vacationRequestMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<VacationRequestResponseDTO> getPendingForSupervisor(long supervisorId) {
        return vacationRequestRepository
                .findByEmployeeSupervisorIdAndStatusOrderByStartDate(supervisorId, VacationStatus.PENDING)
                .stream().map(vacationRequestMapper::toDTO).toList();
    }

    // ---------- employee ----------

    @Transactional
    public VacationRequestResponseDTO create(long employeeId, VacationRequestCreateDTO dto) {
        if (dto == null || dto.startDate() == null || dto.endDate() == null) {
            throw new IllegalArgumentException("startDate and endDate are required");
        }
        if (dto.endDate().isBefore(dto.startDate())) {
            throw new IllegalArgumentException("End date must not be before start date");
        }
        if (dto.startDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Start date must not be in the past");
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NoSuchElementException("Employee " + employeeId + " not found"));

        long overlaps = vacationRequestRepository.countOverlapping(employeeId, dto.startDate(), dto.endDate(),
                List.of(VacationStatus.PENDING, VacationStatus.APPROVED));
        if (overlaps > 0) {
            throw new IllegalStateException("Overlaps with another vacation request");
        }

        VacationRequest request = vacationRequestMapper.toEntity(dto, employee);
        int days = request.getWorkingDays();
        if (days == 0) {
            throw new IllegalArgumentException("The period contains no working days");
        }

        employee.getVacationBalance().reserveDays(days);      // throws if not enough days left
        Employee e = employee;
        notificationService.notify(e.getSupervisor(), e.getFirstname() + " " + e.getLastname() + " requested vacation from " + dto.startDate() + " to " + dto.endDate());
        return vacationRequestMapper.toDTO(vacationRequestRepository.save(request));
    }

    @Transactional
    public VacationRequestResponseDTO cancel(long requestId, long employeeId) {
        VacationRequest request = findRequest(requestId);
        if (request.getEmployee().getId() != employeeId) {
            throw new SecurityException("You can only cancel your own requests");
        }
        if (!request.getStartDate().isAfter(LocalDate.now())) {
            throw new IllegalStateException("Vacation has already started");
        }

        // Prüfen, ob der Antrag vor dem Canceln genehmigt war
        boolean wasApproved = request.getStatus() == VacationStatus.APPROVED;
        request.cancel();
        int days = request.getEffectiveDays();
        if (wasApproved) {
            request.getEmployee().getVacationBalance().releaseTakenDays(days);
        } else {
            request.getEmployee().getVacationBalance().releaseDays(days);
        }

        return vacationRequestMapper.toDTO(request);
    }

    // ---------- supervisor ----------

    @Transactional
    public VacationRequestResponseDTO approve(long requestId, long supervisorId) {
        VacationRequest request = findRequest(requestId);
        
        // 1. Antrag genehmigen
        request.approve(requireResponsibleSupervisor(request, supervisorId));
        
        // 2. Tage von planned_days in taken_days übertragen
        request.getEmployee().getVacationBalance().approveDays(request.getEffectiveDays());

        // 3. Benachrichtigung an den Mitarbeiter senden
        notificationService.notify(request.getEmployee(),
            "Your vacation from " + request.getStartDate() + " to " + request.getEndDate() + " was approved");

        return vacationRequestMapper.toDTO(request);
    }

    @Transactional
    public VacationRequestResponseDTO reject(long requestId, long supervisorId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("A rejection reason is required");
        }
        VacationRequest request = findRequest(requestId);
        request.reject(requireResponsibleSupervisor(request, supervisorId), reason);
        
        notificationService.notify(request.getEmployee(),
            "Your vacation from " + request.getStartDate() + " to " + request.getEndDate()
            + " was rejected: " + reason);
            
        // Bei Ablehnung waren die Tage in planned_days und werden wieder freigegeben
        request.getEmployee().getVacationBalance().releaseDays(request.getEffectiveDays());
        
        return vacationRequestMapper.toDTO(request);
    }

    // ---------- helpers ----------

    private VacationRequest findRequest(long id) {
        return vacationRequestRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Vacation request " + id + " not found"));
    }

    private Supervisor requireResponsibleSupervisor(VacationRequest request, long supervisorId) {
        Supervisor supervisor = supervisorRepository.findById(supervisorId)
                .orElseThrow(() -> new NoSuchElementException("Supervisor " + supervisorId + " not found"));
        Supervisor assigned = request.getEmployee().getSupervisor();
        if (assigned == null || assigned.getId() != supervisor.getId()) {
            throw new SecurityException("Only the employee's own supervisor may review this request");
        }
        return supervisor;
    }
}