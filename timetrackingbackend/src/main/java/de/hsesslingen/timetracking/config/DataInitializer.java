package de.hsesslingen.timetracking.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import de.hsesslingen.timetracking.model.Employee;
import de.hsesslingen.timetracking.model.HR;
import de.hsesslingen.timetracking.model.Supervisor;
import de.hsesslingen.timetracking.repository.EmployeeRepository;

//creates 3 testusers for testing purpose as long as database has no real users

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public void run(String... args) throws Exception {
        if (employeeRepository.count() == 0) {
            

            Employee testEmployee = new Employee(
                "Max", "Mustermann", "max.m", "employee@company.com", "123456", 8, null
            );
            employeeRepository.save(testEmployee);

            
            Supervisor testSupervisor = new Supervisor(
                "Anna", "Schmidt", "anna.s", "supervisor@company.com", "123456", 8
            );
            employeeRepository.save(testSupervisor);


            HR testHr = new HR(
                "Sarah", "Weber", "sarah.w", "hr@company.com", "123456", 8
            );
            employeeRepository.save(testHr);
            
            System.out.println(">>> Test-Benutzer (Employee, Supervisor, HR) wurden geladen! <<<");
        }
    }
}