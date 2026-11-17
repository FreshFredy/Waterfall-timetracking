package de.hsesslingen.timetracking.dto;

public record FlexAccountDTO(
    Integer balanceHours,
    Integer softLimitMax,
    Integer softLimitMin
) {}
