package de.hsesslingen.timetracking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import de.hsesslingen.timetracking.model.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
  
  List<Employee> findByemail(String email);
  List<Employee> findBySupervisorId(Long supervisorId);
  
  
}
