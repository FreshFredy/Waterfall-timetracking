package de.hsesslingen.timetracking.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import de.hsesslingen.timetracking.model.Timesheet;

public interface TimesheetRepository extends JpaRepository<Timesheet, Long> {
    Optional<Timesheet> findByEmployeeIdAndMonthAndYear(Long employeeId, int month, int year);
}
