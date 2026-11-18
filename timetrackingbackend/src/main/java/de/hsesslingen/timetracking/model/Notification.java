package de.hsesslingen.timetracking.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "Notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "message", nullable = false, length = 500)
    private String message;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)     // Employee 1 – 0..* Notification
    @JoinColumn(name = "recipient_id")
    private Employee recipient;

    public Notification() {}

    public Notification(Employee recipient, String message) {
        this.recipient = recipient;
        this.message = message;
        this.createdAt = LocalDateTime.now();
        this.read = false;
    }

    public void markRead() { this.read = true; }

    public long getId() { return id; }
    public String getMessage() { return message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isRead() { return read; }
    public Employee getRecipient() { return recipient; }
}