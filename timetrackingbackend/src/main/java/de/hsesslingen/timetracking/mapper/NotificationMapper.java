package de.hsesslingen.timetracking.mapper;
import org.springframework.stereotype.Component;
import de.hsesslingen.timetracking.dto.*;
import de.hsesslingen.timetracking.model.Notification;

@Component
public class NotificationMapper {

    public NotificationResponseDTO toDTO(Notification n) {
        return new NotificationResponseDTO(
            n.getId(), n.getMessage(), n.getCreatedAt(), n.isRead(), n.getRecipient().getId());
    }
}
