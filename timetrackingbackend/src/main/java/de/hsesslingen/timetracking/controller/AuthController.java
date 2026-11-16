package de.hsesslingen.timetracking.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import de.hsesslingen.timetracking.model.Employee;
import de.hsesslingen.timetracking.repository.EmployeeRepository;


@RestController // <-- WICHTIG: Sagt Spring, dass das ein Controller ist
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @PostMapping("/login") // <-- WICHTIG: Muss exakt /login sein (wegen Vite-Proxy)
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        List<Employee> employees = employeeRepository.findByemail(email);

        if (employees.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Benutzer nicht gefunden."));
        }

        Employee employee = employees.get(0);

        if (!employee.getPassword().equals(password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Falsches Passwort."));
        }

        String role = "employee";
        if (employee.getClass().getSimpleName().equalsIgnoreCase("Supervisor")) {
            role = "supervisor";
        } else if (employee.getClass().getSimpleName().equalsIgnoreCase("HR")) {
            role = "hr";
        }

        return ResponseEntity.ok(Map.of(
            "success", true,
            "email", employee.getemail(),
            "name", employee.getfirstname() + " " + employee.getlastname(),
            "role", role
        ));
    }
}