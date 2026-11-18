package de.hsesslingen.timetracking.repository;

import java.util.List;
import java.time.LocalDate;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import de.hsesslingen.timetracking.model.VacationRequest;
import de.hsesslingen.timetracking.EntryTypes.*;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VacationRequestRepository extends JpaRepository<VacationRequest, Long> {

    List<VacationRequest> findByEmployeeIdOrderByStartDateDesc(Long employeeId);

    // open requests of all employees of one supervisor
    List<VacationRequest> findByEmployeeSupervisorIdAndStatusOrderByStartDate(Long supervisorId, VacationStatus status);

    @Query("""
        select count(v) from VacationRequest v
        where v.employee.id = :employeeId
          and v.status in :statuses
          and v.startDate <= :endDate
          and v.endDate >= :startDate
        """)
    long countOverlapping(@Param("employeeId") Long employeeId,
                          @Param("startDate") LocalDate startDate,
                          @Param("endDate") LocalDate endDate,
                          @Param("statuses") Collection<VacationStatus> statuses);
    }
