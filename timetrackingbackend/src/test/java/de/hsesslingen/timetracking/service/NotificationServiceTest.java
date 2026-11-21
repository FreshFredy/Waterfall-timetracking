package de.hsesslingen.timetracking.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import de.hsesslingen.timetracking.dto.NotificationResponseDTO;
import de.hsesslingen.timetracking.mapper.NotificationMapper;
import de.hsesslingen.timetracking.model.Employee;
import de.hsesslingen.timetracking.model.Notification;
import de.hsesslingen.timetracking.repository.NotificationRepository;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationService service;

    @Test
    void notify_skipsWhenRecipientIsNull() {
        service.notify(null, "Nachricht");

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void notify_savesNotificationWhenRecipientPresent() {
        Employee recipient = mock(Employee.class);
        when(recipient.getUsername()).thenReturn("mmustermann");

        service.notify(recipient, "Antrag genehmigt");

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void getNotifications_usesUnreadQueryWhenUnreadOnlyIsTrue() {
        Notification n = new Notification(mock(Employee.class), "Ungelesen");
        NotificationResponseDTO dto = mock(NotificationResponseDTO.class);

        when(notificationRepository.findByRecipientIdAndReadFalseOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(n));
        when(notificationMapper.toDTO(n)).thenReturn(dto);

        List<NotificationResponseDTO> result = service.getNotifications(1L, true);

        assertEquals(1, result.size());
        assertSame(dto, result.get(0));
        verify(notificationRepository, never()).findByRecipientIdOrderByCreatedAtDesc(anyLong());
    }

    @Test
    void markAsRead_throwsSecurityExceptionForOtherEmployee() {
        Employee owner = mock(Employee.class);
        when(owner.getId()).thenReturn(1L);

        Notification n = new Notification(owner, "Privat");
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(n));

        assertThrows(SecurityException.class, () -> service.markAsRead(10L, 2L));
        assertFalse(n.isRead());
    }

    @Test
    void markAsRead_throwsWhenNotFound() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.markAsRead(99L, 1L));
    }
    @Test
    void countUnread_delegatesToRepository() {
        when(notificationRepository.countByRecipientIdAndReadFalse(1L)).thenReturn(3L);

        long count = service.countUnread(1L);

        assertEquals(3L, count);
        verify(notificationRepository, times(1)).countByRecipientIdAndReadFalse(1L);
    }

    @Test
    void markAllAsRead_marksAllUnreadNotificationsAsRead() {
        Employee owner = mock(Employee.class);
        Notification n1 = new Notification(owner, "Nachricht 1");
        Notification n2 = new Notification(owner, "Nachricht 2");

        when(notificationRepository.findByRecipientIdAndReadFalseOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(n1, n2));

        service.markAllAsRead(1L);

        assertTrue(n1.isRead());
        assertTrue(n2.isRead());
        verify(notificationRepository, times(1)).findByRecipientIdAndReadFalseOrderByCreatedAtDesc(1L);
    }

    @Test
    void markAsRead_marksOwnNotificationAndMapsToDto() {
        Employee owner = mock(Employee.class);
        when(owner.getId()).thenReturn(1L);

        Notification n = new Notification(owner, "Eigene Nachricht");
        NotificationResponseDTO dto = mock(NotificationResponseDTO.class);

        when(notificationRepository.findById(10L)).thenReturn(Optional.of(n));
        when(notificationMapper.toDTO(n)).thenReturn(dto);

        NotificationResponseDTO result = service.markAsRead(10L, 1L);

        assertTrue(n.isRead());
        assertSame(dto, result);
    }

    @Test
    void getNotifications_usesAllQueryWhenUnreadOnlyIsFalse() {
        Notification n = new Notification(mock(Employee.class), "Alle");
        NotificationResponseDTO dto = mock(NotificationResponseDTO.class);

        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(n));
        when(notificationMapper.toDTO(n)).thenReturn(dto);

        List<NotificationResponseDTO> result = service.getNotifications(1L, false);

        assertEquals(1, result.size());
        assertSame(dto, result.get(0));
        verify(notificationRepository, never()).findByRecipientIdAndReadFalseOrderByCreatedAtDesc(anyLong());
    }
}