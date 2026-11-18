package de.hsesslingen.timetracking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import de.hsesslingen.timetracking.model.SickLeave;

public interface SickLeaveRepository extends JpaRepository<SickLeave, Long>{
    List<SickLeave> findByEmployeeId(long employeeId);
}
