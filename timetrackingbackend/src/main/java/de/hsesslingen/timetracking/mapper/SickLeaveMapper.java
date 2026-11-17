package de.hsesslingen.timetracking.mapper;

import org.springframework.stereotype.Component;

import de.hsesslingen.timetracking.EntryTypes.ExusedEntryType;

import de.hsesslingen.timetracking.dto.SickLeaveDTO;
import de.hsesslingen.timetracking.dto.SickLeaveResponseDTO;

import de.hsesslingen.timetracking.model.SickLeave;


@Component
public class SickLeaveMapper {

    public SickLeaveResponseDTO toDTO(SickLeave s) {
        return new SickLeaveResponseDTO(
            s.getID(),
            s.getDate(),
            s.getExusedEntryType(),
            s.getFileDocNote(),
            s.getEmployee().getId(),
            s.getCreatedBy().getId(),
            s.getTimeentry() != null ? s.getTimeentry().getId() : null
        );
    }

    public void updateSickLeaveFromDTO(SickLeaveDTO dto, SickLeave sc) {
        if(dto.doctorsNotePath() != null) {
            sc.setFileDocNote(dto.doctorsNotePath());
            sc.setExusedEntryType(dto.excused());
        }
        else if(dto.doctorsNotePath() == null) sc.setExusedEntryType (ExusedEntryType.NOT_EXCUSED);
        
    }

}
