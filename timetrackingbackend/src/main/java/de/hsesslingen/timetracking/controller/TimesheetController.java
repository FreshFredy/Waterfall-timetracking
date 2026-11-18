package de.hsesslingen.timetracking.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import de.hsesslingen.timetracking.dto.TimeentryRequestDTO;
import de.hsesslingen.timetracking.dto.TimesheetRejectDTO;
import de.hsesslingen.timetracking.dto.TimesheetDTO;
import de.hsesslingen.timetracking.service.TimesheetService;

@RestController
@RequestMapping("/api-timetracking/Timesheet-management")
public class TimesheetController {

    private final TimesheetService timesheetService;

    public TimesheetController(TimesheetService timesheetService) {
        this.timesheetService = timesheetService;
    }

    @GetMapping("/Timesheets")
    public ResponseEntity<List<TimesheetDTO>> getTimesheets(
            @RequestParam long employeeId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(timesheetService.getTimesheets(employeeId, month, year));
    }

    @GetMapping("/Timesheets/{id}")
    public ResponseEntity<TimesheetDTO> getTimesheet(@PathVariable("id") long id) {
        return ResponseEntity.ok(timesheetService.getTimesheet(id));
    }

    @PostMapping("/Timesheets/entries")
    public ResponseEntity<TimesheetDTO> recordWorkTime(
            @RequestParam long employeeId,
            @RequestBody TimeentryRequestDTO requestDTO) {
        return new ResponseEntity<>(timesheetService.recordWorkTime(employeeId, requestDTO), HttpStatus.CREATED);
    }

    @PutMapping("/Timesheets/{timesheetId}/entries/{entryId}")
    public ResponseEntity<TimesheetDTO> changeWorkTime(
            @PathVariable("timesheetId") long timesheetId,
            @PathVariable("entryId") long entryId,
            @RequestBody TimeentryRequestDTO requestDTO) {
        return ResponseEntity.ok(timesheetService.changeWorkTime(timesheetId, entryId, requestDTO));
    }

    @PostMapping("/Timesheets/{id}/submit")
    public ResponseEntity<TimesheetDTO> submit(@PathVariable("id") long id) {
        return ResponseEntity.ok(timesheetService.submit(id));
    }

    @PostMapping("/Timesheets/{id}/approve")
    public ResponseEntity<TimesheetDTO> approve(
            @PathVariable("id") long id,
            @RequestParam long supervisorId) {
        return ResponseEntity.ok(timesheetService.approve(id, supervisorId));
    }

    @PostMapping("/Timesheets/{id}/reject")
    public ResponseEntity<TimesheetDTO> reject(
            @PathVariable("id") long id,
            @RequestParam long supervisorId,
            @RequestBody TimesheetRejectDTO body) {
        return ResponseEntity.ok(timesheetService.reject(id, supervisorId, body.reason()));
    }
}