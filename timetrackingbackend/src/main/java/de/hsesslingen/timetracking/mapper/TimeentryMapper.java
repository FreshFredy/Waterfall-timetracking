package de.hsesslingen.timetracking.mapper;

import org.springframework.stereotype.Component;

import de.hsesslingen.timetracking.dto.TimeentryRequestDTO;
import de.hsesslingen.timetracking.dto.TimeentryResponseDTO;
import de.hsesslingen.timetracking.model.Timeentry;
import de.hsesslingen.timetracking.model.Project;


@Component
public class TimeentryMapper {

    public TimeentryResponseDTO toDTO(Timeentry e) {
        if (e == null) {
            return null;
        }
        Project project = e.getProject();
        return new TimeentryResponseDTO(
            e.getId(),
            e.getEntryDate(),
            e.getStartDate(),
            e.getEndDate(),
            e.getbreakDuration(),
            e.getEntry_type(),
            e.getCreditedHours(),
            project != null ? project.getId() : null,
            project != null ? project.getName() : null
        );
    }

    // the timesheet is set afterwards by timesheet.addEntry(...)
    public Timeentry toEntity(TimeentryRequestDTO dto, Project project) {
        return new Timeentry(dto.entryDate(), dto.startTime(), dto.endTime(),
                             dto.breakMinutes(), dto.entryType(), project);
    }

    public void updateEntityFromDTO(TimeentryRequestDTO dto, Timeentry e, Project project) {
        e.update(dto.entryDate(), dto.startTime(), dto.endTime(),
                 dto.breakMinutes(), dto.entryType(), project);
    }
}
