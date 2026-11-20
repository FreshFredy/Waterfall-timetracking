package de.hsesslingen.timetracking.repository;
import java.util.List;
import de.hsesslingen.timetracking.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository  extends JpaRepository<Project, Long>{
    Project getById(long Id);
    List<Project> findByActiveTrue();
}
