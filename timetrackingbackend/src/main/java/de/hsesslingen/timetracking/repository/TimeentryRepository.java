package de.hsesslingen.timetracking.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import de.hsesslingen.timetracking.model.Timeentry;

public interface TimeentryRepository extends JpaRepository<Timeentry, Long> {
    List<Timeentry> findByTimesheetEmployeeIdAndEntryDate(Long employeeId, LocalDate entryDate);
}

