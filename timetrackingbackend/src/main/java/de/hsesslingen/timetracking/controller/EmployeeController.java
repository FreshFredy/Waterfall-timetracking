   
package de.hsesslingen.timetracking.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.hsesslingen.timetracking.dto.EmployeeRequestDTO;
import de.hsesslingen.timetracking.dto.EmployeeResponseDTO;
import de.hsesslingen.timetracking.mapper.EmployeeMapper;
import de.hsesslingen.timetracking.model.Employee;
import de.hsesslingen.timetracking.repository.EmployeeRepository;

@RestController
@RequestMapping("/api-timetracking/Employee-management") // Schrägstrich am Ende entfernt, um doppelte Slashes (//Employees) zu vermeiden
public class EmployeeController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeMapper employeeMapper;

    @GetMapping("/Employees")
    public ResponseEntity<List<EmployeeResponseDTO>> getAllEmployees(@RequestParam(required = false) String email) {
        try {
            List<Employee> employees = (email == null) 
                    ? employeeRepository.findAll() 
                    : employeeRepository.findByemail(email);

            if (employees.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            // Entities in ResponseDTOs umwandeln
            List<EmployeeResponseDTO> responseDTOs = employees.stream()
                    .map(employeeMapper::toDTO)
                    .collect(Collectors.toList());

            return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/Employees/{id}")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeById(@PathVariable("id") long id) {
        Optional<Employee> employeeData = employeeRepository.findById(id);

        if (employeeData.isPresent()) {
            return new ResponseEntity<>(employeeMapper.toDTO(employeeData.get()), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/Employees")
    public ResponseEntity<EmployeeResponseDTO> createEmployee(@RequestBody EmployeeRequestDTO requestDTO) {
        try {
            // RequestDTO in Entity umwandeln und speichern
            Employee newEmployee = employeeMapper.toEntity(requestDTO);
            Employee savedEmployee = employeeRepository.save(newEmployee);
            
            // Gespeicherte Entity als ResponseDTO zurückgeben
            return new ResponseEntity<>(employeeMapper.toDTO(savedEmployee), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/Employees/{id}")
    public ResponseEntity<EmployeeResponseDTO> updateEmployee(@PathVariable("id") long id, @RequestBody EmployeeRequestDTO requestDTO) {
        Optional<Employee> employeeData = employeeRepository.findById(id);

        if (employeeData.isPresent()) {
            Employee existingEmployee = employeeData.get();
            // Bestehende Entity über den Mapper aktualisieren
            employeeMapper.updateEntityFromDTO(requestDTO, existingEmployee);
            Employee updatedEmployee = employeeRepository.save(existingEmployee);
            
            return new ResponseEntity<>(employeeMapper.toDTO(updatedEmployee), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/Employees/{id}")
    public ResponseEntity<HttpStatus> deleteEmployee(@PathVariable("id") long id) {
        try {
            employeeRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/Employees")
    public ResponseEntity<HttpStatus> deleteAllEmployees() {
        try {
            employeeRepository.deleteAll();
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
