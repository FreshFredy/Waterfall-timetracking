package de.hsesslingen.timetracking.model;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.hsesslingen.timetracking.EntryTypes.ExusedEntryType;

class SickLeaveTest {

    private Employee employee;
    private HR hr;
    private Timeentry timeentry;
    private LocalDate date;

    static class TestEmployee extends Employee {
    }

    static class TestHR extends HR {
    }

    static class TestTimeentry extends Timeentry {
    }

    @BeforeEach
    void setUp() {
        employee = new TestEmployee();
        hr = new TestHR();
        timeentry = new TestTimeentry();
        date = LocalDate.of(2026, 5, 6);
    }

    @Test
    void constructor_setsDoctorsNoteMissingWhenFileIsNullAndNotExcused() {
        SickLeave sickLeave = new SickLeave(
                date,
                null,
                employee,
                hr,
                timeentry,
                ExusedEntryType.NOT_EXCUSED
        );

        assertEquals(date, sickLeave.getDate());
        assertNull(sickLeave.getFileDocNote());
        assertSame(employee, sickLeave.getEmployee());
        assertSame(hr, sickLeave.getCreatedBy());
        assertSame(timeentry, sickLeave.getTimeentry());
        assertEquals(ExusedEntryType.DOCOTRS_NOTE_MISSING, sickLeave.getExusedEntryType());
    }

    @Test
    void constructor_setsExcusedWhenFileIsPresent() {
        String notePath = "/uploads/attest-2026-05-06.pdf";
        SickLeave sickLeave = new SickLeave(
                date,
                notePath,
                employee,
                hr,
                timeentry,
                ExusedEntryType.NOT_EXCUSED
        );

        assertEquals(notePath, sickLeave.getFileDocNote());
        assertEquals(ExusedEntryType.EXCUSED, sickLeave.getExusedEntryType());
    }

    @Test
    void constructor_leavesStatusNullWhenFileIsNullAndPassedStatusIsExcused() {
        SickLeave sickLeave = new SickLeave(
                date,
                null,
                employee,
                hr,
                timeentry,
                ExusedEntryType.EXCUSED
        );

        assertNull(sickLeave.getExusedEntryType());
    }

    @Test
    void setFileDocNote_updatesPathAndMarksAsExcused() {
        SickLeave sickLeave = new SickLeave(date, null, employee, hr, timeentry, null);
        assertEquals(ExusedEntryType.DOCOTRS_NOTE_MISSING, sickLeave.getExusedEntryType());

        String path = "/notes/arztbrief.pdf";
        sickLeave.setFileDocNote(path);

        assertEquals(path, sickLeave.getFileDocNote());
        assertEquals(ExusedEntryType.EXCUSED, sickLeave.getExusedEntryType());
    }

    @Test
    void setExusedEntryNotExcuse_setsStatusToNotExcused() {
        SickLeave sickLeave = new SickLeave(date, "/notes/attest.pdf", employee, hr, timeentry, null);
        assertEquals(ExusedEntryType.EXCUSED, sickLeave.getExusedEntryType());

        sickLeave.setExusedEntryNotExcuse();

        assertEquals(ExusedEntryType.NOT_EXCUSED, sickLeave.getExusedEntryType());
    }

    @Test
    void settersAndGetters_updateDateEmployeeAndStatus() {
        SickLeave sickLeave = new SickLeave();
        LocalDate updatedDate = LocalDate.of(2026, 5, 7);
        Employee updatedEmployee = new TestEmployee();

        sickLeave.setDate(updatedDate);
        sickLeave.setEmployee(updatedEmployee);
        sickLeave.setExusedEntryType(ExusedEntryType.EXCUSED);

        assertEquals(updatedDate, sickLeave.getDate());
        assertSame(updatedEmployee, sickLeave.getEmployee());
        assertEquals(ExusedEntryType.EXCUSED, sickLeave.getExusedEntryType());
    }
}