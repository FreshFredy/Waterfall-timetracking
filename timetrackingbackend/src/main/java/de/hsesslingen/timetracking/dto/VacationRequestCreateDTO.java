package de.hsesslingen.timetracking.dto;
import java.time.LocalDate;

public record VacationRequestCreateDTO(
    LocalDate startDate,
    LocalDate endDate
) {}
