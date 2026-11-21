package de.hsesslingen.timetracking.model;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;


@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "role")  
@DiscriminatorValue("EMPLOYEE")
@Table(name = "Employee")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "firstname")  //optional otherwise just takes variableName in snake_case
    private String firstname;

    @Column(name = "lastname")
    private String lastname;

    @Column(name = "username", unique = true)
    private String username;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password", unique = false)
    private String password;

    @Column(name = "target_hours_per_day")
    private Integer targetH;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_id")
    private Supervisor supervisor;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    private List<Timesheet> timesheets = new ArrayList<>();

    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL)
    private VacationBalance vacationBalance;

    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL)
    private FlexAccount flexAccount;

    public Employee() {

    }

    public Employee(String firstname, String lastname, String username, String email, String password, Integer targetH, Supervisor supervisor) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.username = username;
        this.email = email;
        this.password = password;
        this.targetH = targetH;
        this.supervisor = supervisor;
        setVacationBalance(new VacationBalance( 30, this));
        setFlexAccount(new FlexAccount(-100, 100, this));
    }

    //Synching both sides of relation
    public void setVacationBalance(VacationBalance vacationBalance) {
        this.vacationBalance = vacationBalance;
        vacationBalance.setEmployee(this);      
    }

    public void setFlexAccount(FlexAccount flexAccount) {
        this.flexAccount = flexAccount;
        flexAccount.setEmployee(this);
    }

    public void setTimesheet(Timesheet timesheet) {
       this.timesheets.add(timesheet);
        timesheet.setEmployee(this);
    }

    public long getId() {
        return id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setfirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setlastname(String lastname) {
        this.lastname = lastname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getemail() {
        return email;
    }

    public void setemail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
  
    public Integer getTargetH() {
        return targetH;
    }

    public void setTargetH(Integer targetH) {
        this.targetH = targetH;
    }

    public Supervisor getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(Supervisor supervisor) {
        this.supervisor = supervisor;
    }

    public VacationBalance getVacationBalance() {
        return vacationBalance;
    }

    public FlexAccount getFlexAccount() {
        return flexAccount;
    }

    public List<Timesheet> getTimesheets() {
        return timesheets;
    }
    public Optional<Timesheet> getTimesheet() {
        return this.timesheets.stream().max(Comparator.comparing(Timesheet::getYear).thenComparing(Timesheet::getMonth));
    }
    public void addTimesheet(Timesheet timesheet){
        this.timesheets.add(timesheet);
        timesheet.setEmployee(this);
    }

    @Override
    public String toString() {
        return "[id=" + id + ", firstname=" + firstname + ", lastname=" + lastname + ", email=" + email + ", Target Hours=" + targetH + ", Supervisor=" + supervisor + ", VacationBalance=" + vacationBalance + ", FlexAccount=" + flexAccount + "]";
    }

}

