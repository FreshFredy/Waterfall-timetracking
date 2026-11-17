package de.hsesslingen.timetracking.dto;

public class EmployeeResponseDTO {
    private Long id;
    private String firstname;
    private String lastname;
    private String email;
    private String username;
    private Integer targetH;
    private Long supervisor;
    private double flexAccount;
    private double vacationBalance;

    // Konstruktoren, Getter und Setter
    public EmployeeResponseDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFirstname() { return firstname; }
    public void setFirstname(String firstname) { this.firstname = firstname; }
    public String getLastname() { return lastname; }
    public void setLastname(String lastname) { this.lastname = lastname; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Integer getTargetH() { return targetH; }
    public void setTargetH(Integer targetH) { this.targetH = targetH; }
    public Long getSupervisor() { return supervisor; }
    public void setSupervisor(Long supervisor) { this.supervisor = supervisor; }
    public double getFlexAccount() { return flexAccount; }
    public void setFlexAccount(double flexAccount) { this.flexAccount = flexAccount; }
    public double getVacationBalance() { return vacationBalance; }
    public void setVacationBalance(double vacationBalance) { this.vacationBalance = vacationBalance; }
}