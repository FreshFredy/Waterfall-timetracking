package de.hsesslingen.timetracking.mapper;

import org.springframework.stereotype.Component;

import de.hsesslingen.timetracking.dto.EmployeeRequestDTO;
import de.hsesslingen.timetracking.dto.EmployeeResponseDTO;
import de.hsesslingen.timetracking.model.Employee;
import de.hsesslingen.timetracking.model.Supervisor;

@Component
public class EmployeeMapper {

    public EmployeeResponseDTO toDTO(Employee employee) {
        if (employee == null) {
            return null;
        }

        EmployeeResponseDTO dto = new EmployeeResponseDTO();
        dto.setId(employee.getId());
        dto.setFirstname(employee.getFirstname());
        dto.setLastname(employee.getLastname());
        dto.setEmail(employee.getemail());
        dto.setUsername(employee.getUsername());
        dto.setTargetH(employee.getTargetH());
        
        // Falls Supervisor ein Objekt ist, holen wir die ID (Beispiel: .getId())
        if (employee.getSupervisor() != null) {
            dto.setSupervisor(employee.getSupervisor().getId());
        }
        
        // Passe getDepartment() / getDepartmentId() an den echten Namen in Employee.java an
        // dto.setDepartmentID(employee.getDepartmentId()); 

        // Falls FlexAccount/VacationBalance Objekte sind, hier den Wert extrahieren:
        // dto.setFlexAccount(employee.getFlexAccount().getValue());
        // dto.setVacationBalance(employee.getVacationBalance().getValue());
        
        
        return dto;
    }

    public Employee toEntity(EmployeeRequestDTO dto, Supervisor supervisor) {
        if (dto == null) {
            return null;
        }
        return new Employee(dto.getFirstname(), dto.getLastname(), dto.getUsername(),
                            dto.getEmail(), dto.getPassword(), dto.getTargetH(), supervisor);
    }

    public void updateEntityFromDTO(EmployeeRequestDTO dto, Employee employee) {
        if (dto == null || employee == null) {
            return;
        }

        employee.setfirstname(dto.getFirstname());
        employee.setlastname(dto.getLastname());
        employee.setemail(dto.getEmail());
        employee.setUsername(dto.getUsername());
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            employee.setPassword(dto.getPassword());
        }
    }
}