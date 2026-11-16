package de.hsesslingen.timetracking.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

import de.hsesslingen.timetracking.model.Employee;
import de.hsesslingen.timetracking.repository.EmployeeRepository;

@RestController
@RequestMapping("/api-timetracking/Employee-management/")
public class EmployeeController {

	@Autowired
	EmployeeRepository EmployeeRepository;

	@GetMapping("/Employees")
	public ResponseEntity<List<Employee>> getAllEmployees(@RequestParam(required = false) String email) {
		
		try {
			List<Employee> Employees = new ArrayList<Employee>();

			if (email == null)
				EmployeeRepository.findAll().forEach(Employees::add);
			else
				EmployeeRepository.findByemail(email).forEach(Employees::add);

			if (Employees.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}

			return new ResponseEntity<>(Employees, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/Employees/{id}")
	public ResponseEntity<Employee> getEmployeeById(@PathVariable("id") long id) {
		Optional<Employee> EmployeeData = EmployeeRepository.findById(id);

		if (EmployeeData.isPresent()) {
			return new ResponseEntity<>(EmployeeData.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@PostMapping("/Employees")
	public ResponseEntity<Employee> createEmployee(@RequestBody Employee Employee) {
		try {
			Employee _Employee = EmployeeRepository
					.save(new Employee(Employee.getfirstname(), Employee.getlastname(), Employee.getemail(), Employee.getTargetH(), Employee.getRole(), Employee.getDepartmentID()));
			return new ResponseEntity<>(_Employee, HttpStatus.CREATED);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/Employees/{id}")
	public ResponseEntity<Employee> updateEmployee(@PathVariable("id") long id, @RequestBody Employee Employee) {
		Optional<Employee> EmployeeData = EmployeeRepository.findById(id);

		if (EmployeeData.isPresent()) {
			Employee _Employee = EmployeeData.get();
			_Employee.setfirstname(Employee.getfirstname());
			_Employee.setlastname(Employee.getlastname());
			_Employee.setemail(Employee.getemail());
			_Employee.setUsername(Employee.getUsername());
			_Employee.setPassword(Employee.getPassword());
			_Employee.setTargetH(Employee.getTargetH());
			_Employee.setSupervisor(Employee.getSupervisor());
			_Employee.setFlexAccount(Employee.getFlexAccount());
			_Employee.setVacationBalance(Employee.getVacationBalance());
			_Employee.setFlexAccount(Employee.getVacationBalance());
			return new ResponseEntity<>(EmployeeRepository.save(_Employee), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@DeleteMapping("/Employees/{id}")
	public ResponseEntity<HttpStatus> deleteEmployee(@PathVariable("id") long id) {
		try {
			EmployeeRepository.deleteById(id);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@DeleteMapping("/Employees")
	public ResponseEntity<HttpStatus> deleteAllEmployees() {
		try {
			EmployeeRepository.deleteAll();
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

}
