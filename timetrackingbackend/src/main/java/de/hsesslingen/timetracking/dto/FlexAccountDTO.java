package de.hsesslingen.timetracking.dto;

public record FlexAccountDTO(
    double balanceHours,
    Integer softLimitMax,
    Integer softLimitMin
) {}
