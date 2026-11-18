package de.hsesslingen.timetracking.dto;
import java.time.LocalDateTime;

public record NotificationResponseDTO(
    long id,
    String message,
    LocalDateTime createdAt,
    boolean read,
    long recipientId
) {}
