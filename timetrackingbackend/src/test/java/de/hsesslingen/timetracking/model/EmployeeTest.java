
package de.hsesslingen.timetracking.model;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.hsesslingen.timetracking.EntryTypes.TimesheetStatus;

class EmployeeTest {

    private Supervisor supervisor;
    private Employee employee;

    static class TestSupervisor extends Supervisor {
    }

    static class TestTimesheet extends Timesheet {
        private Employee attachedEmployee;

        TestTimesheet(Integer month, Integer year) {
            super(month, year, TimesheetStatus.DRAFT, null, null);
        }

        @Override
        public void setEmployee(Employee employee) {
            super.setEmployee(employee);
            this.attachedEmployee = employee;
        }

        public Employee getAttachedEmployee() {
            return attachedEmployee;
        }
    }

    @BeforeEach
    void setUp() {
        supervisor = new TestSupervisor();

        employee = new Employee(
                "Lukas",
                "Weber",
                "lweber",
                "lukas.weber@hs-esslingen.de",
                "geheim123",
                8,
                supervisor
        );
    }

    @Test
    void defaultConstructor_initializesEmptyTimesheetsAndNullFields() {
        Employee defaultEmployee = new Employee();

        assertEquals(0L, defaultEmployee.getId());
        assertNull(defaultEmployee.getFirstname());
        assertNull(defaultEmployee.getLastname());
        assertNull(defaultEmployee.getUsername());
        assertNull(defaultEmployee.getemail());
        assertNull(defaultEmployee.getPassword());
        assertNull(defaultEmployee.getTargetH());
        assertNull(defaultEmployee.getSupervisor());
        assertNull(defaultEmployee.getVacationBalance());
        assertNull(defaultEmployee.getFlexAccount());

        assertNotNull(defaultEmployee.getTimesheets());
        assertTrue(defaultEmployee.getTimesheets().isEmpty());
        assertTrue(defaultEmployee.getTimesheet().isEmpty());
    }

    @Test
    void parameterizedConstructor_setsFieldsAndCreatesVacationAndFlexAccount() {
        assertEquals("Lukas", employee.getFirstname());
        assertEquals("Weber", employee.getLastname());
        assertEquals("lweber", employee.getUsername());
        assertEquals("lukas.weber@hs-esslingen.de", employee.getemail());
        assertEquals("geheim123", employee.getPassword());
        assertEquals(8, employee.getTargetH());
        assertSame(supervisor, employee.getSupervisor());

        assertNotNull(employee.getVacationBalance());
        assertEquals(30, employee.getVacationBalance().getAnnualDays());
        assertSame(employee, employee.getVacationBalance().getEmployee());

        assertNotNull(employee.getFlexAccount());
        assertEquals(-100, employee.getFlexAccount().getSoft_limit_min());
        assertEquals(100, employee.getFlexAccount().getSoft_limit_max());
    }

    @Test
    void settersAndGetters_updatePersonalAndWorkAttributes() {
        Supervisor newSupervisor = new TestSupervisor();

        employee.setfirstname("Lena");
        employee.setlastname("Hoffmann");
        employee.setUsername("lhoffmann");
        employee.setemail("lena.hoffmann@hs-esslingen.de");
        employee.setPassword("neu123");
        employee.setTargetH(6);
        employee.setSupervisor(newSupervisor);

        assertEquals("Lena", employee.getFirstname());
        assertEquals("Hoffmann", employee.getLastname());
        assertEquals("lhoffmann", employee.getUsername());
        assertEquals("lena.hoffmann@hs-esslingen.de", employee.getemail());
        assertEquals("neu123", employee.getPassword());
        assertEquals(6, employee.getTargetH());
        assertSame(newSupervisor, employee.getSupervisor());
    }

    @Test
    void setVacationBalanceAndSetFlexAccount_syncRelations() {
        VacationBalance customBalance = new VacationBalance(28, null);
        FlexAccount customFlex = new FlexAccount(-15, 30, null);

        employee.setVacationBalance(customBalance);
        employee.setFlexAccount(customFlex);

        assertSame(customBalance, employee.getVacationBalance());
        assertSame(employee, customBalance.getEmployee());

        assertSame(customFlex, employee.getFlexAccount());
    }

    @Test
    void addTimesheetAndSetTimesheet_addEntryAndAttachEmployee() {
        TestTimesheet ts1 = new TestTimesheet(4, 2026);
        TestTimesheet ts2 = new TestTimesheet(5, 2026);

        employee.addTimesheet(ts1);
        employee.setTimesheet(ts2);

        assertEquals(2, employee.getTimesheets().size());
        assertTrue(employee.getTimesheets().contains(ts1));
        assertTrue(employee.getTimesheets().contains(ts2));

        assertSame(employee, ts1.getAttachedEmployee());
        assertSame(employee, ts2.getAttachedEmployee());
    }

    @Test
    void getTimesheet_returnsLatestByYearThenMonth() {
        assertTrue(employee.getTimesheet().isEmpty());

        Timesheet may2026 = new TestTimesheet(5, 2026);
        Timesheet dec2025 = new TestTimesheet(12, 2025);
        Timesheet oct2026 = new TestTimesheet(10, 2026);

        employee.addTimesheet(may2026);
        employee.addTimesheet(dec2025);
        employee.addTimesheet(oct2026);

        Optional<Timesheet> latest = employee.getTimesheet();

        assertTrue(latest.isPresent());
        assertSame(oct2026, latest.get());
    }

    @Test
    void getTimesheet_returnsSingleTimesheet() {
        Timesheet ts = new TestTimesheet(4, 2026);

        employee.addTimesheet(ts);

        assertTrue(employee.getTimesheet().isPresent());
        assertSame(ts, employee.getTimesheet().get());
    }

    @Test
    void getTimesheet_returnsLatestMonthWithinSameYear() {
        Timesheet april = new TestTimesheet(4, 2026);
        Timesheet november = new TestTimesheet(11, 2026);

        employee.addTimesheet(april);
        employee.addTimesheet(november);

        assertSame(november, employee.getTimesheet().get());
    }
}
