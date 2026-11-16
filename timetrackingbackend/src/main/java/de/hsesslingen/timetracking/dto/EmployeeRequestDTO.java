package de.hsesslingen.timetracking.dto;

public class EmployeeRequestDTO {
    private String firstname;
    private String lastname;
    private String email;
    private String username;
    private String password;
    private double targetH;
    private Long supervisor;
    private Long departmentID;
    private double flexAccount;
    private double vacationBalance;

    // Getter und Setter
    public String getFirstname() { return firstname; }
    public void setFirstname(String firstname) { this.firstname = firstname; }
    public String getLastname() { return lastname; }
    public void setLastname(String lastname) { this.lastname = lastname; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public double getTargetH() { return targetH; }
    public void setTargetH(double targetH) { this.targetH = targetH; }
    public Long getSupervisor() { return supervisor; }
    public void setSupervisor(Long supervisor) { this.supervisor = supervisor; }
    public Long getDepartmentID() { return departmentID; }
    public void setDepartmentID(Long departmentID) { this.departmentID = departmentID; }
    public double getFlexAccount() { return flexAccount; }
    public void setFlexAccount(double flexAccount) { this.flexAccount = flexAccount; }
    public double getVacationBalance() { return vacationBalance; }
    public void setVacationBalance(double vacationBalance) { this.vacationBalance = vacationBalance; }
}