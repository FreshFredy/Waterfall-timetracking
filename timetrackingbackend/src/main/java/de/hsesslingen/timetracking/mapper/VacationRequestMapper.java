package de.hsesslingen.timetracking.mapper;


import de.hsesslingen.timetracking.dto.*;
import de.hsesslingen.timetracking.model.*;


import org.springframework.stereotype.Component;

@Component
public class VacationRequestMapper {

    public VacationRequestResponseDTO toDTO(VacationRequest r) {
        return new VacationRequestResponseDTO(
            r.getId(),
            r.getStartDate(),
            r.getEndDate(),
            r.getEffectiveDays(),
            r.getStatus(),
            r.getRejectionReason(),
            r.getEmployee().getId(),
            r.getApprover() != null ? r.getApprover().getId() : null
        );
    }

    public VacationRequest toEntity(VacationRequestCreateDTO dto, Employee employee) {
        return new VacationRequest(dto.startDate(), dto.endDate(), employee);
    }
}
