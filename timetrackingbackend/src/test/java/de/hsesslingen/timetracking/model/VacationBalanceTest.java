package de.hsesslingen.timetracking.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VacationBalanceTest {

    private Employee employee;
    private VacationBalance balance;

    static class TestEmployee extends Employee {
    }

    @BeforeEach
    void setUp() {
        employee = new TestEmployee();
        balance = new VacationBalance(30, employee);
    }

    @Test
    void constructor_initializesDefaultsAndFields() {
        assertEquals(30, balance.getAnnualDays());
        assertEquals(0, balance.getPlannedDays());
        assertEquals(0, balance.getTakenDays());
        assertEquals(0, balance.getRemainingDaysLastYear());
        assertSame(employee, balance.getEmployee());
        assertEquals(30, balance.getRemainingDays());
    }

    @Test
    void getRemainingDays_includesCarryoverAndSubstractsTakenDays() {
        balance.setRemainingDaysLastYear(5);
        balance.setTakenDays(12);
        balance.setPlannedDays(4); // wirkt nicht auf getRemainingDays(), aber auf reserveDays()

        assertEquals(23, balance.getRemainingDays()); // 30 + 5 - 12
    }

    @Test
    void reserveDays_increasesPlannedDaysWhenAvailable() {
        balance.setRemainingDaysLastYear(5);
        balance.setTakenDays(10);
        balance.setPlannedDays(5); // verfügbar: (30 + 5 - 10) - 5 = 20

        balance.reserveDays(10);

        assertEquals(15, balance.getPlannedDays());
        assertEquals(25, balance.getRemainingDays());
    }

    @Test
    void reserveDays_allowsExactRemainingAvailableDays() {
        balance.setPlannedDays(10); // verfügbar: 30 - 10 = 20

        assertDoesNotThrow(() -> balance.reserveDays(20));
        assertEquals(30, balance.getPlannedDays());
    }

    @Test
    void reserveDays_throwsWhenExceedingAvailableDaysIncludingPlanned() {
        balance.setRemainingDaysLastYear(2);
        balance.setTakenDays(6);
        balance.setPlannedDays(10); // verfügbar: (30 + 2 - 6) - 10 = 16

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> balance.reserveDays(17)
        );
        assertEquals("Not enough vacation days left", ex.getMessage());
        assertEquals(10, balance.getPlannedDays());
    }

    @Test
    void releaseDays_reducesPlannedDaysAndClampsAtZero() {
        balance.setPlannedDays(6);
        balance.releaseDays(4);
        assertEquals(2, balance.getPlannedDays());

        balance.releaseDays(10);
        assertEquals(0, balance.getPlannedDays());
    }

    @Test
    void approveDays_movesDaysFromPlannedToTakenAndClampsPlannedAtZero() {
        balance.setPlannedDays(8);
        balance.setTakenDays(3);

        balance.approveDays(5);

        assertEquals(3, balance.getPlannedDays());
        assertEquals(8, balance.getTakenDays());
        assertEquals(22, balance.getRemainingDays()); // 30 + 0 - 8
    }

    @Test
    void approveDays_clampsPlannedAtZeroWhenMoreApprovedThanPlanned() {
        balance.setPlannedDays(2);
        balance.setTakenDays(1);

        balance.approveDays(5);

        assertEquals(0, balance.getPlannedDays());
        assertEquals(6, balance.getTakenDays());
    }

    @Test
    void cancelApprovedDays_reducesTakenDaysAndClampsAtZero() {
        balance.setTakenDays(7);
        balance.cancelApprovedDays(3);
        assertEquals(4, balance.getTakenDays());

        balance.cancelApprovedDays(10);
        assertEquals(0, balance.getTakenDays());
    }

    @Test
    void resetTakenDays_setsTakenDaysToZero() {
        balance.setTakenDays(15);

        balance.resetTakenDays();

        assertEquals(0, balance.getTakenDays());
        assertEquals(30, balance.getRemainingDays());
    }
    @Test
    void fullVacationLifecycle_reserveApproveCancelAndYearRollover() {
        // Ausgangslage: 30 Jahrestage, 4 Resttage aus dem Vorjahr -> 34 Resttage insgesamt
        balance.setRemainingDaysLastYear(4);
        assertEquals(34, balance.getRemainingDays());

        // 1. Reservieren: 10 Tage geplant -> noch 24 frei reservierbar (34 - 10)
        balance.reserveDays(10);
        assertEquals(10, balance.getPlannedDays());
        assertEquals(0, balance.getTakenDays());
        assertEquals(34, balance.getRemainingDays());

        // 2. Genehmigen: 6 geplante Tage werden genommen -> 4 geplant, 6 genommen, 28 Resttage
        balance.approveDays(6);
        assertEquals(4, balance.getPlannedDays());
        assertEquals(6, balance.getTakenDays());
        assertEquals(28, balance.getRemainingDays());

        // 3. Stornieren: 2 genommene Tage werden storniert -> 4 genommen, 30 Resttage
        balance.cancelApprovedDays(2);
        assertEquals(4, balance.getPlannedDays());
        assertEquals(4, balance.getTakenDays());
        assertEquals(30, balance.getRemainingDays());

        // 4. Jahreswechsel simulieren: verbleibende Resttage als Resturlaub übernehmen und takenDays zurücksetzen
        int carryoverNextYear = balance.getRemainingDays(); // 30
        balance.setRemainingDaysLastYear(carryoverNextYear);
        balance.resetTakenDays();

        assertEquals(0, balance.getTakenDays());
        assertEquals(30, balance.getRemainingDaysLastYear());
        assertEquals(4, balance.getPlannedDays());
        assertEquals(60, balance.getRemainingDays()); // 30 annual + 30 carryover - 0 taken
    }
}