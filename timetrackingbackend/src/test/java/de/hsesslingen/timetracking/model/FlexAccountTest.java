
package de.hsesslingen.timetracking.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FlexAccountTest {

    private Employee employee;
    private FlexAccount account;

    static class TestEmployee extends Employee {
    }

    @BeforeEach
    void setUp() {
        employee = new TestEmployee();
        account = new FlexAccount(-20, 40, employee);
    }

    @Test
    void constructor_initializesBalanceZeroAndLimits() {
        assertEquals(0L, account.getID());
        assertEquals(0.0, account.getBalance_hours(), 0.0001);
        assertEquals(-20, account.getSoft_limit_min());
        assertEquals(40, account.getSoft_limit_max());
    }

    @Test
    void defaultConstructor_initializesDefaultFields() {
        FlexAccount defaultAccount = new FlexAccount();

        assertEquals(0L, defaultAccount.getID());
        assertEquals(0.0, defaultAccount.getBalance_hours(), 0.0001);
        assertNull(defaultAccount.getSoft_limit_min());
        assertNull(defaultAccount.getSoft_limit_max());
    }

    @Test
    void withinLimit_checksInclusiveMinAndMaxBoundaries() {
        account.setBalance_hours(10.0);

        assertTrue(account.withinLimit(30.0));
        assertTrue(account.withinLimit(-30.0));
        assertTrue(account.withinLimit(0.0));
    }

    @Test
    void withinLimit_returnsFalseWhenExceedingLimits() {
        account.setBalance_hours(10.0);

        assertFalse(account.withinLimit(30.1));
        assertFalse(account.withinLimit(-30.1));
    }

    @Test
    void addTime_addsPositiveAndNegativeHours() {
        account.addTime(5.5);
        assertEquals(5.5, account.getBalance_hours(), 0.0001);

        account.addTime(-2.0);
        assertEquals(3.5, account.getBalance_hours(), 0.0001);
    }

    @Test
    void setBalanceHours_updatesBalance() {
        account.setBalance_hours(12.0);

        assertEquals(12.0, account.getBalance_hours(), 0.0001);
    }

    @Test
    void setters_updateLimitsAndEmployee() {
        Employee otherEmployee = new TestEmployee();

        account.setSoft_limit_min(-10);
        account.setSoft_limit_max(25);
        account.setEmployee(otherEmployee);

        assertEquals(-10, account.getSoft_limit_min());
        assertEquals(25, account.getSoft_limit_max());

        assertTrue(account.withinLimit(25.0));
        assertFalse(account.withinLimit(25.5));
    }
    @Test
    void addTime_allowsValuesOutsideSoftLimitsDirectly() {
        account.addTime(50.0); // über soft_limit_max (40)
        assertEquals(50.0, account.getBalance_hours(), 0.0001);
        assertFalse(account.withinLimit(0.0));
    }
}
