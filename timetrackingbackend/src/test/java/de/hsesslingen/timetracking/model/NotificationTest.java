package de.hsesslingen.timetracking.model;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NotificationTest {

    private Employee recipient;

    static class TestEmployee extends Employee {
    }

    @BeforeEach
    void setUp() {
        recipient = new TestEmployee();
    }

    @Test
    void defaultConstructor_initializesDefaults() {
        Notification notification = new Notification();

        assertEquals(0L, notification.getId());
        assertNull(notification.getMessage());
        assertNull(notification.getCreatedAt());
        assertFalse(notification.isRead());
        assertNull(notification.getRecipient());
    }

    @Test
    void parameterizedConstructor_setsRecipientMessageUnreadAndTimestamp() {
        LocalDateTime before = LocalDateTime.now();
        String message = "Dein Urlaubsantrag wurde genehmigt.";

        Notification notification = new Notification(recipient, message);
        LocalDateTime after = LocalDateTime.now();

        assertSame(recipient, notification.getRecipient());
        assertEquals(message, notification.getMessage());
        assertFalse(notification.isRead());
        assertNotNull(notification.getCreatedAt());
        assertFalse(notification.getCreatedAt().isBefore(before));
        assertFalse(notification.getCreatedAt().isAfter(after));
    }

    @Test
    void markRead_setsReadFlagToTrue() {
        Notification notification = new Notification(recipient, "Neuer Zeiteintrag erfasst.");
        assertFalse(notification.isRead());

        notification.markRead();

        assertTrue(notification.isRead());
    }
}