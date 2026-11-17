package de.hsesslingen.timetracking.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import de.hsesslingen.timetracking.model.Supervisor;

public interface SupervisorRepository extends JpaRepository<Supervisor, Long> {}
