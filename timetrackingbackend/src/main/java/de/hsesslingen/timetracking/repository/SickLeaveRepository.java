package de.hsesslingen.timetracking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import de.hsesslingen.timetracking.model.SickLeave;

public interface SickLeaveRepository extends JpaRepository<SickLeave, Long>{
    
}
