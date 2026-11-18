package de.hsesslingen.timetracking.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import de.hsesslingen.timetracking.EntryTypes.TimeEntryType;

public record TimeentryResponseDTO(
    long id,
    LocalDate entryDate,
    LocalTime startTime,
    LocalTime endTime,
    Integer breakMinutes,
    TimeEntryType entryType,
    float creditedHours
) {}
