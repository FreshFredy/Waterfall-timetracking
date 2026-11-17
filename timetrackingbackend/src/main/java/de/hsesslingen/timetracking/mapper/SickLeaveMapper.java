package de.hsesslingen.timetracking.mapper;

import de.hsesslingen.timetracking.EntryTypes.ExusedEntryType;

import de.hsesslingen.timetracking.dto.SickLeaveDTO;
import de.hsesslingen.timetracking.dto.SickLeaveResponseDTO;

import de.hsesslingen.timetracking.model.SickLeave;


public class SickLeaveMapper {

    public SickLeaveResponseDTO toDTO(SickLeave e) {
        if (e == null) {
            return null;
        }
        return new SickLeaveResponseDTO(

            e.getID(),
            e.getDate(),
            e.getFileDocNote(),
            e.getExusedEntryType(),
            e.getEmployee(),
            e.getCreatedBy(),
            e.getTimeentry()
        );
    }
    public void updateSickLeaveFromDTO(SickLeaveDTO dto, SickLeave sc) {
        if(dto.filepath_doctors_note() != null) {
            sc.setFileDocNote(dto.filepath_doctors_note());
            sc.setExusedEntryType(dto.exusedEntryType());
        }
        else if(dto.filepath_doctors_note() == null) sc.setExusedEntryType (ExusedEntryType.NOT_EXCUSED);
        
    }

}
