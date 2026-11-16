package de.hsesslingen.timetracking.mapper;

import de.hsesslingen.timetracking.dto.VacationBalanceDTO;
import de.hsesslingen.timetracking.model.VacationBalance;
import org.springframework.stereotype.Component;

@Component
public class VacationBalanceMapper {

    public VacationBalanceDTO toDto(VacationBalance vb) {
        if (vb == null) {
            return null;
        }
        return new VacationBalanceDTO(
            vb.getAnnualDays(),
            vb.getPlannedDays(),
            vb.getTakenDays(),
            vb.getRemainingDaysLastYear(),
            vb.getRemainingDays()
        );
    }
    public void updateEntityFromDTO(VacationBalanceDTO dto, VacationBalance vb) {
        if (dto.annualDays() != null) {
            vb.setAnnualDays(dto.annualDays());
        }
        vb.setRemainingDaysLastYear(dto.remainingDaysLastYear());
        vb.setPlannedDays(dto.plannedDays());
        vb.setTakenDays(dto.takenDays());
    }
}
