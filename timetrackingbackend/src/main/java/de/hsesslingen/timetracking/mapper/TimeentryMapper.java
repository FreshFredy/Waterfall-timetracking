package de.hsesslingen.timetracking.mapper;

import org.springframework.stereotype.Component;

import de.hsesslingen.timetracking.dto.TimeentryRequestDTO;
import de.hsesslingen.timetracking.dto.TimeentryResponseDTO;
import de.hsesslingen.timetracking.model.Timeentry;

@Component
public class TimeentryMapper {

    public TimeentryResponseDTO toDTO(Timeentry e) {
        if (e == null) {
            return null;
        }
        return new TimeentryResponseDTO(
            e.getId(),
            e.getEntryDate(),
            e.getStartDate(),
            e.getEndDate(),
            e.getbreakDuration(),
            e.getEntryType(),
            e.getCreditedHours()
        );
    }

    // the timesheet is set afterwards by timesheet.addEntry(...)
    public Timeentry toEntity(TimeentryRequestDTO dto) {
        return new Timeentry(dto.entryDate(), dto.startTime(), dto.endTime(),
                             dto.breakMinutes(), dto.entryType());
    }

    public void updateEntityFromDTO(TimeentryRequestDTO dto, Timeentry e) {
        e.update(dto.entryDate(), dto.startTime(), dto.endTime(),
                 dto.breakMinutes(), dto.entryType());
    }
}
