package de.hsesslingen.timetracking.model;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.hsesslingen.timetracking.EntryTypes.TimeEntryType;

class TimeentryTest {

    private Project project;
    private LocalDate date;

    static class TestProject extends Project {
    }

    @BeforeEach
    void setUp() {
        project = new TestProject();
        date = LocalDate.of(2026, 5, 4);
    }

    @Test
    void constructor_calculatesCreditedHoursWithoutBreakUnderSixHours() {
        Timeentry entry = new Timeentry(
                date,
                LocalTime.of(8, 0),
                LocalTime.of(13, 30),
                null,
                TimeEntryType.WORK,
                project
        );

        assertEquals(5.5f, entry.getCreditedHours(), 0.0001f);
        assertEquals(0, entry.getbreakDuration());
        assertEquals(date, entry.getEntryDate());
        assertSame(project, entry.getProject());
    }

    @Test
    void constructor_requires30MinBreakForSixToUnderNineHoursGross() {
        Timeentry entry = new Timeentry(
                date,
                LocalTime.of(8, 0),
                LocalTime.of(14, 0), // 360 Min. brutto -> mind. 30 Min. Pause
                30,
                TimeEntryType.WORK,
                project
        );

        assertEquals(5.5f, entry.getCreditedHours(), 0.0001f);
    }

    @Test
    void constructor_throwsWhenBreakLessThan30MinForSixHoursGross() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new Timeentry(
                        date,
                        LocalTime.of(8, 0),
                        LocalTime.of(14, 0),
                        29,
                        TimeEntryType.WORK,
                        project
                )
        );
        assertEquals("At least 30 minutes break are required", ex.getMessage());
    }

    @Test
    void constructor_requires45MinBreakForNineOrMoreHoursGross() {
        Timeentry entry = new Timeentry(
                date,
                LocalTime.of(8, 0),
                LocalTime.of(17, 0), // 540 Min. brutto -> mind. 45 Min. Pause
                45,
                TimeEntryType.WORK,
                project
        );

        assertEquals(8.25f, entry.getCreditedHours(), 0.0001f);
    }

    @Test
    void constructor_throwsWhenBreakLessThan45MinForNineHoursGross() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new Timeentry(
                        date,
                        LocalTime.of(8, 0),
                        LocalTime.of(17, 0),
                        30,
                        TimeEntryType.WORK,
                        project
                )
        );
        assertEquals("At least 45 minutes break are required", ex.getMessage());
    }

    @Test
    void constructor_allowsUpTo10HoursGrossWith45MinBreak() {
        Timeentry entry = new Timeentry(
                date,
                LocalTime.of(7, 0),
                LocalTime.of(17, 0), // 600 Min. brutto = 10h max
                60,
                TimeEntryType.WORK,
                project
        );

        assertEquals(9.0f, entry.getCreditedHours(), 0.0001f);
    }

    @Test
    void constructor_throwsWhenGrossExceeds10Hours() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new Timeentry(
                        date,
                        LocalTime.of(7, 0),
                        LocalTime.of(17, 1), // 601 Min. brutto > 10h
                        60,
                        TimeEntryType.WORK,
                        project
                )
        );
        assertEquals("Working time must not exceed 10 hours per day", ex.getMessage());
    }

    @Test
    void constructor_throwsWhenEndTimeNotAfterStartTimeOrNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Timeentry(date, LocalTime.of(10, 0), LocalTime.of(10, 0), 0, TimeEntryType.WORK, project)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new Timeentry(date, LocalTime.of(12, 0), LocalTime.of(11, 0), 0, TimeEntryType.WORK, project)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new Timeentry(date, null, LocalTime.of(11, 0), 0, TimeEntryType.WORK, project)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new Timeentry(date, LocalTime.of(8, 0), null, 0, TimeEntryType.WORK, project)
        );
    }

    @Test
    void constructor_throwsWhenProjectIsNullForWorkEntry() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new Timeentry(date, LocalTime.of(8, 0), LocalTime.of(12, 0), 0, TimeEntryType.WORK, null)
        );
        assertEquals("You need to enter a Project", ex.getMessage());
    }

    @Test
    void sickEntry_sets8CreditedHoursWithoutTimeValidation() {
        Timeentry sickEntry = new Timeentry(date, null, null, null, TimeEntryType.SICK, project);

        assertEquals(8.0f, sickEntry.getCreditedHours(), 0.0001f);
        assertEquals(TimeEntryType.SICK, sickEntry.getEntry_type());
    }

    @Test
    void flextimeEntry_sets0CreditedHoursWithoutTimeValidation() {
        Timeentry flexEntry = new Timeentry(date, null, null, null, TimeEntryType.FLEXTIME, project);

        assertEquals(0.0f, flexEntry.getCreditedHours(), 0.0001f);
        assertEquals(TimeEntryType.FLEXTIME, flexEntry.getEntry_type());
    }

    @Test
    void update_updatesFieldsAndRecalculatesCreditedHours() {
        Timeentry entry = new Timeentry(
                date,
                LocalTime.of(8, 0),
                LocalTime.of(12, 0),
                0,
                TimeEntryType.WORK,
                project
        );
        LocalDate newDate = LocalDate.of(2026, 5, 5);
        Project newProject = new TestProject();

        entry.update(newDate, LocalTime.of(8, 0), LocalTime.of(15, 0), 30, TimeEntryType.WORK, newProject);

        assertEquals(newDate, entry.getEntryDate());
        assertEquals(LocalTime.of(8, 0), entry.getStartDate());
        assertEquals(LocalTime.of(15, 0), entry.getEndDate());
        assertEquals(30, entry.getbreakDuration());
        assertSame(newProject, entry.getProject());
        assertEquals(6.5f, entry.getCreditedHours(), 0.0001f);
    }

    @Test
    void update_throwsWhenProjectIsNull() {
        Timeentry entry = new Timeentry(
                date,
                LocalTime.of(8, 0),
                LocalTime.of(12, 0),
                0,
                TimeEntryType.WORK,
                project
        );

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> entry.update(date, LocalTime.of(8, 0), LocalTime.of(12, 0), 0, TimeEntryType.WORK, null)
        );
        assertEquals("You need to enter a Project", ex.getMessage());
    }
}