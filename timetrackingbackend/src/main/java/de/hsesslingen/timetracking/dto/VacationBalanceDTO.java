package de.hsesslingen.timetracking.dto;

public record VacationBalanceDTO(
    Integer annualDays,
    Integer plannedDays,
    Integer takenDays,
    Integer remainingDaysLastYear,
    Integer remainingDays         
) {}
