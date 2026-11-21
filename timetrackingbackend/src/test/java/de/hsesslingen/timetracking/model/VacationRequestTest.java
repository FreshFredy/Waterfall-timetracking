package de.hsesslingen.timetracking.model;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.hsesslingen.timetracking.EntryTypes.VacationStatus;

class VacationRequestTest {

    private Employee employee;
    private Supervisor supervisor;
    private VacationRequest request;

    static class TestEmployee extends Employee {
    }

    static class TestSupervisor extends Supervisor {
    }

    @BeforeEach
    void setUp() {
        employee = new TestEmployee();
        supervisor = new TestSupervisor();
        request = new VacationRequest(
                LocalDate.of(2026, 5, 4), // Montag
                LocalDate.of(2026, 5, 8), // Freitag
                employee
        );
    }

    @Test
    void constructor_setsInitialFieldsAndPendingStatus() {
        assertEquals(LocalDate.of(2026, 5, 4), request.getStartDate());
        assertEquals(LocalDate.of(2026, 5, 8), request.getEndDate());
        assertSame(employee, request.getEmployee());
        assertEquals(VacationStatus.PENDING, request.getStatus());
        assertNull(request.getApprover());
        assertNull(request.getRejectionReason());
    }

    @Test
    void getWorkingDays_countsWeekdaysInclusiveAndExcludesWeekend() {
        // 2026-05-04 (Mo) bis 2026-05-10 (So): Mo-Fr = 5, Sa-So = 0 -> 5
        VacationRequest fullWeekPlusWeekend = new VacationRequest(
                LocalDate.of(2026, 5, 4),
                LocalDate.of(2026, 5, 10),
                employee
        );

        assertEquals(5, request.getWorkingDays());
        assertEquals(5, fullWeekPlusWeekend.getWorkingDays());
    }

    @Test
    void getWorkingDays_returnsZeroForWeekendOnlyOrReversedRange() {
        VacationRequest weekendOnly = new VacationRequest(
                LocalDate.of(2026, 5, 9), // Samstag
                LocalDate.of(2026, 5, 10), // Sonntag
                employee
        );
        VacationRequest reversedRange = new VacationRequest(
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 4),
                employee
        );

        assertEquals(0, weekendOnly.getWorkingDays());
        assertEquals(0, reversedRange.getWorkingDays());
    }

    @Test
    void approve_setsApprovedStatusAndApproverWhenPending() {
        request.approve(supervisor);

        assertEquals(VacationStatus.APPROVED, request.getStatus());
        assertSame(supervisor, request.getApprover());
    }

    @Test
    void approve_throwsWhenNotPending() {
        request.approve(supervisor);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> request.approve(supervisor)
        );
        assertTrue(ex.getMessage().contains("APPROVED"));
    }

    @Test
    void reject_setsRejectedStatusApproverAndReasonWhenPending() {
        request.reject(supervisor, "Zu viele Ausfälle im Team");

        assertEquals(VacationStatus.REJECTED, request.getStatus());
        assertSame(supervisor, request.getApprover());
        assertEquals("Zu viele Ausfälle im Team", request.getRejectionReason());
    }

    @Test
    void reject_throwsWhenNotPending() {
        request.reject(supervisor, "Erster Grund");

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> request.reject(supervisor, "Zweiter Grund")
        );
        assertTrue(ex.getMessage().contains("REJECTED"));
    }

    @Test
    void cancel_allowedWhenPendingOrApproved() {
        request.cancel();
        assertEquals(VacationStatus.CANCELLED, request.getStatus());

        VacationRequest approvedRequest = new VacationRequest(
                LocalDate.of(2026, 5, 4),
                LocalDate.of(2026, 5, 8),
                employee
        );
        approvedRequest.approve(supervisor);
        approvedRequest.cancel();

        assertEquals(VacationStatus.CANCELLED, approvedRequest.getStatus());
    }

    @Test
    void cancel_throwsWhenAlreadyCancelledOrRejected() {
        request.cancel();
        assertThrows(IllegalStateException.class, request::cancel);

        VacationRequest rejectedRequest = new VacationRequest(
                LocalDate.of(2026, 5, 4),
                LocalDate.of(2026, 5, 8),
                employee
        );
        rejectedRequest.reject(supervisor, "Abgelehnt");
        assertThrows(IllegalStateException.class, rejectedRequest::cancel);
    }
}