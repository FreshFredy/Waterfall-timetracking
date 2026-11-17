package de.hsesslingen.timetracking.mapper;

import de.hsesslingen.timetracking.dto.FlexAccountDTO;
import de.hsesslingen.timetracking.model.FlexAccount;

import org.springframework.stereotype.Component;

@Component
public class FlexAccountMapper {

    public FlexAccountDTO toDto(FlexAccount fc) {
        if (fc == null) {
            return null;
        }
        return new FlexAccountDTO(
            fc.getBalance_hours(),
            fc.getSoft_limit_max(),
            fc.getSoft_limit_min()
        );
    }
    public void updateFlexFromDTO(FlexAccountDTO dto, FlexAccount fc) {
        if (dto.balanceHours() != null) {
            fc.setBalance_hours(dto.balanceHours());
        }
        fc.setSoft_limit_max(dto.softLimitMax());
        fc.setSoft_limit_min(dto.softLimitMin());
    }
}
