package de.hsesslingen.timetracking.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.hsesslingen.timetracking.dto.NotificationResponseDTO;
import de.hsesslingen.timetracking.mapper.NotificationMapper;
import de.hsesslingen.timetracking.model.Employee;
import de.hsesslingen.timetracking.model.Notification;
import de.hsesslingen.timetracking.repository.NotificationRepository;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    public NotificationService(NotificationRepository notificationRepository,
                               NotificationMapper notificationMapper) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
    }

    // called by the other services; joins their transaction
    @Transactional
    public void notify(Employee recipient, String message) {
        if (recipient == null) {
            return;                    // e.g. an employee without supervisor
        }
        notificationRepository.save(new Notification(recipient, message));
        log.info("Notification for {}: {}", recipient.getUsername(), message);
        // later: send the e-mail here (see "E-Mail" below)
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getNotifications(long employeeId, boolean unreadOnly) {
        List<Notification> list = unreadOnly
                ? notificationRepository.findByRecipientIdAndReadFalseOrderByCreatedAtDesc(employeeId)
                : notificationRepository.findByRecipientIdOrderByCreatedAtDesc(employeeId);
        return list.stream().map(notificationMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public long countUnread(long employeeId) {
        return notificationRepository.countByRecipientIdAndReadFalse(employeeId);
    }

    @Transactional
    public NotificationResponseDTO markAsRead(long notificationId, long employeeId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NoSuchElementException("Notification " + notificationId + " not found"));
        if (n.getRecipient().getId() != employeeId) {
            throw new SecurityException("You can only read your own notifications");
        }
        n.markRead();
        return notificationMapper.toDTO(n);
    }

    @Transactional
    public void markAllAsRead(long employeeId) {
        notificationRepository.findByRecipientIdAndReadFalseOrderByCreatedAtDesc(employeeId)
                .forEach(Notification::markRead);
    }
}
