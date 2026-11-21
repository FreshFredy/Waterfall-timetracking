package de.hsesslingen.timetracking.model;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.hsesslingen.timetracking.EntryTypes.TimesheetStatus;

class TimesheetTest {

    private Employee employee;
    private Supervisor supervisor;
    private Timesheet timesheet;

    // Hilfsklassen für isolierte Unit-Tests ohne Abhängigkeiten auf andere Entities
    static class TestEmployee extends Employee {
        private final Integer targetH;

        TestEmployee(Integer targetH) {
            this.targetH = targetH;
        }

        @Override
        public Integer getTargetH() {
            return targetH;
        }
    }

    static class TestSupervisor extends Supervisor {
    }

    static class TestTimeentry extends Timeentry {
        private final LocalDate entryDate;
        private final float creditedHours;
        private Timesheet attachedTimesheet;

        TestTimeentry(LocalDate entryDate, float creditedHours) {
            this.entryDate = entryDate;
            this.creditedHours = creditedHours;
        }

        @Override
        public LocalDate getEntryDate() {
            return entryDate;
        }

        @Override
        public float getCreditedHours() {
            return creditedHours;
        }

        @Override
        public void setTimesheet(Timesheet timesheet) {
            this.attachedTimesheet = timesheet;
        }

        public Timesheet getAttachedTimesheet() {
            return attachedTimesheet;
        }
    }

    @BeforeEach
    void setUp() {
        employee = new TestEmployee(8);
        supervisor = new TestSupervisor();
        timesheet = new Timesheet(5, 2026, TimesheetStatus.DRAFT, null, employee);
    }

    @Test
    void addEntry_addsEntryAndSetsTimesheetWhenDraft() {
        LocalDate date = LocalDate.of(2026, 5, 4); // Montag
        TestTimeentry entry = new TestTimeentry(date, 7.5f);

        timesheet.addEntry(entry);

        assertEquals(1, timesheet.getTimeEntries().size());
        assertSame(entry, timesheet.getTimeEntries().get(0));
        assertSame(timesheet, entry.getAttachedTimesheet());
    }

    @Test
    void addEntry_allowedWhenRejected() {
        timesheet.setStatus(TimesheetStatus.REJECTED);
        TestTimeentry entry = new TestTimeentry(LocalDate.of(2026, 5, 5), 8.0f);

        assertDoesNotThrow(() -> timesheet.addEntry(entry));
        assertEquals(1, timesheet.getTimeEntries().size());
    }

    @Test
    void addEntry_throwsWhenNotEditable() {
        timesheet.setStatus(TimesheetStatus.SUBMITTED);
        TestTimeentry entry = new TestTimeentry(LocalDate.of(2026, 5, 4), 8.0f);

        assertThrows(IllegalStateException.class, () -> timesheet.addEntry(entry));
        assertTrue(timesheet.getTimeEntries().isEmpty());
    }

    @Test
    void addEntry_throwsWhenDateOutsidePeriodOrNull() {
        TestTimeentry wrongMonth = new TestTimeentry(LocalDate.of(2026, 6, 1), 8.0f);
        TestTimeentry wrongYear = new TestTimeentry(LocalDate.of(2025, 5, 1), 8.0f);
        TestTimeentry nullDate = new TestTimeentry(null, 8.0f);

        assertThrows(IllegalArgumentException.class, () -> timesheet.addEntry(wrongMonth));
        assertThrows(IllegalArgumentException.class, () -> timesheet.addEntry(wrongYear));
        assertThrows(IllegalArgumentException.class, () -> timesheet.addEntry(nullDate));
    }

    @Test
    void getWorkedHours_sumsCreditedHours() {
        timesheet.addEntry(new TestTimeentry(LocalDate.of(2026, 5, 4), 6.5f));
        timesheet.addEntry(new TestTimeentry(LocalDate.of(2026, 5, 5), 8.25f));

        assertEquals(14.75f, timesheet.getWorkedHours(), 0.0001f);
    }

    @Test
    void submit_changesStatusToSubmittedAndClearsRejectionReason() {
        timesheet.setStatus(TimesheetStatus.REJECTED);
        timesheet.setRejectionReason("Fehlende Buchung");

        timesheet.submit();

        assertEquals(TimesheetStatus.SUBMITTED, timesheet.getStatus());
        assertNull(timesheet.getRejectionReason());
    }

    @Test
    void submit_throwsWhenAlreadySubmittedOrApproved() {
        timesheet.setStatus(TimesheetStatus.APPROVED);

        assertThrows(IllegalStateException.class, () -> timesheet.submit());
    }

    @Test
    void approve_setsApprovedAndApproverWhenSubmitted() {
        timesheet.setStatus(TimesheetStatus.SUBMITTED);

        timesheet.approve(supervisor);

        assertEquals(TimesheetStatus.APPROVED, timesheet.getStatus());
        assertSame(supervisor, timesheet.getApprover());
    }

    @Test
    void approve_throwsWhenNotSubmitted() {
        assertThrows(IllegalStateException.class, () -> timesheet.approve(supervisor));
    }

    @Test
    void reject_setsRejectedApproverAndReasonWhenSubmitted() {
        timesheet.setStatus(TimesheetStatus.SUBMITTED);

        timesheet.reject(supervisor, "Stunden stimmen nicht");

        assertEquals(TimesheetStatus.REJECTED, timesheet.getStatus());
        assertSame(supervisor, timesheet.getApprover());
        assertEquals("Stunden stimmen nicht", timesheet.getRejectionReason());
    }

    @Test
    void reject_throwsWhenNotSubmitted() {
        assertThrows(IllegalStateException.class, () -> timesheet.reject(supervisor, "Grund"));
    }

    @Test
    void getFlexChange_calculatesWeekdayTargetAndWeekendZeroTarget() {
        // 2026-05-04 ist Montag (Soll 8h -> zwei Einträge à 4.5h = 9h -> +1h)
        // 2026-05-05 ist Dienstag (Soll 8h -> 6h -> -2h)
        // 2026-05-09 ist Samstag (Wochenende, Soll 0h -> 4h -> +4h)
        timesheet.addEntry(new TestTimeentry(LocalDate.of(2026, 5, 4), 4.5f));
        timesheet.addEntry(new TestTimeentry(LocalDate.of(2026, 5, 4), 4.5f));
        timesheet.addEntry(new TestTimeentry(LocalDate.of(2026, 5, 5), 6.0f));
        timesheet.addEntry(new TestTimeentry(LocalDate.of(2026, 5, 9), 4.0f));

        assertEquals(3.0, timesheet.getFlexChange(), 0.0001);
    }

    @Test
    void getFlexChange_defaultsTargetTo8WhenEmployeeTargetHIsNull() {
        Timesheet flexDefaultTimesheet = new Timesheet(5, 2026, TimesheetStatus.DRAFT, null, new TestEmployee(null));
        flexDefaultTimesheet.addEntry(new TestTimeentry(LocalDate.of(2026, 5, 4), 10.0f));

        assertEquals(2.0, flexDefaultTimesheet.getFlexChange(), 0.0001);
    }
}