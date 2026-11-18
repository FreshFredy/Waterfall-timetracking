package de.hsesslingen.timetracking.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.hsesslingen.timetracking.dto.SickLeaveDTO;
import de.hsesslingen.timetracking.dto.SickLeaveResponseDTO;
import de.hsesslingen.timetracking.service.SickLeaveService;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api-timetracking/SickLeave-management")
public class SickLeaveController {

    private final SickLeaveService sickLeaveService;

    public SickLeaveController(SickLeaveService sickLeaveService) {
        this.sickLeaveService = sickLeaveService;
    }

    @GetMapping("/SickLeaves/{id}")
    public ResponseEntity<SickLeaveResponseDTO> getSickLeave(@PathVariable("id") long id) {
        return ResponseEntity.ok(sickLeaveService.getSickLeave(id));
    }

    @PostMapping("/SickLeaves")
    public ResponseEntity<SickLeaveResponseDTO> recordSickLeave(
            @RequestParam long hrId,
            @RequestBody SickLeaveDTO requestDTO) {
        return new ResponseEntity<>(sickLeaveService.recordSickLeave(hrId, requestDTO), HttpStatus.CREATED);
    }
}