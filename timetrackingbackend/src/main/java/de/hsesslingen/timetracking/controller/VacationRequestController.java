package de.hsesslingen.timetracking.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import de.hsesslingen.timetracking.dto.VacationRequestCreateDTO;
import de.hsesslingen.timetracking.dto.VacationRequestResponseDTO;
import de.hsesslingen.timetracking.dto.VacationRejectDTO;
import de.hsesslingen.timetracking.service.VacationService;

@RestController
@RequestMapping("/api-timetracking/VacationRequest-management")
public class VacationRequestController {

    private final VacationService vacationService;

    public VacationRequestController(VacationService vacationService) {
        this.vacationService = vacationService;
    }

    @GetMapping("/VacationRequests")
    public ResponseEntity<List<VacationRequestResponseDTO>> getRequestsOfEmployee(@RequestParam long employeeId) {
        return ResponseEntity.ok(vacationService.getRequestsOfEmployee(employeeId));
    }

    @GetMapping("/VacationRequests/pending")
    public ResponseEntity<List<VacationRequestResponseDTO>> getPending(@RequestParam long supervisorId) {
        return ResponseEntity.ok(vacationService.getPendingForSupervisor(supervisorId));
    }

    @GetMapping("/VacationRequests/{id}")
    public ResponseEntity<VacationRequestResponseDTO> getRequest(@PathVariable("id") long id) {
        return ResponseEntity.ok(vacationService.getRequest(id));
    }

    @PostMapping("/VacationRequests")
    public ResponseEntity<VacationRequestResponseDTO> create(
            @RequestParam long employeeId,
            @RequestBody VacationRequestCreateDTO requestDTO) {
        return new ResponseEntity<>(vacationService.create(employeeId, requestDTO), HttpStatus.CREATED);
    }

    @PostMapping("/VacationRequests/{id}/approve")
    public ResponseEntity<VacationRequestResponseDTO> approve(
            @PathVariable("id") long id, @RequestParam long supervisorId) {
        return ResponseEntity.ok(vacationService.approve(id, supervisorId));
    }

    @PostMapping("/VacationRequests/{id}/reject")
    public ResponseEntity<VacationRequestResponseDTO> reject(
            @PathVariable("id") long id, @RequestParam long supervisorId,
            @RequestBody VacationRejectDTO body) {
        return ResponseEntity.ok(vacationService.reject(id, supervisorId, body.reason()));
    }

    @PostMapping("/VacationRequests/{id}/cancel")
    public ResponseEntity<VacationRequestResponseDTO> cancel(
            @PathVariable("id") long id, @RequestParam long employeeId) {
        return ResponseEntity.ok(vacationService.cancel(id, employeeId));
    }
}
