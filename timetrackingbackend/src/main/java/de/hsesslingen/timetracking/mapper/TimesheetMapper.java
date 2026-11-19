package de.hsesslingen.timetracking.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import de.hsesslingen.timetracking.dto.TimeentryResponseDTO;
import de.hsesslingen.timetracking.dto.TimesheetDTO;
import de.hsesslingen.timetracking.model.Timesheet;

@Component
public class TimesheetMapper {

    private final TimeentryMapper timeentryMapper;

    public TimesheetMapper(TimeentryMapper timeentryMapper) {
        this.timeentryMapper = timeentryMapper;
    }

    public TimesheetDTO toDTO(Timesheet t) {
        if (t == null) {
            return null;
        }
        List<TimeentryResponseDTO> entries = t.getTimeEntries().stream()
                .map(timeentryMapper::toDTO)
                .toList();

        return new TimesheetDTO(
            t.getId(),
            t.getMonth(),
            t.getYear(),
            t.getStatus(),
            t.getRejectionReason(),
            t.getEmployee().getId(),
            t.getApprover() != null ? t.getApprover().getId() : null,
            t.getWorkedHours(),
            entries,
            t.getFlexChange()

        );
    }
}
