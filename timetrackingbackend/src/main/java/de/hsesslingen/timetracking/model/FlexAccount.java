package de.hsesslingen.timetracking.model;

import jakarta.persistence.*;



@Entity
@Table(name = "FlexAccount")
public class FlexAccount {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "balance_hours")
    private Integer balance_hours; 

    @Column(name = "soft_limit_min")
    private Integer soft_limit_min;

    @Column(name = "soft_limit_max")
    private Integer soft_limit_max;

    @OneToOne
    @JoinColumn(name = "employee_id", referencedColumnName = "id", unique = true)
    private Employee employee;

public FlexAccount(){

}
public FlexAccount(Integer soft_limit_min, Integer soft_limit_max, Employee employee){
    this.balance_hours = 0;
    this.soft_limit_min = soft_limit_min;
    this.soft_limit_max = soft_limit_max;
    this.employee = employee;
}
public long getID(){
    return id;
}
public void setBalance_hours(Integer balance_hours){
    this.balance_hours = balance_hours;
}
public Integer getBalance_hours(){
    return balance_hours;
}
public void setSoft_limit_min(Integer soft_limit_min){
    this.soft_limit_min = soft_limit_min;
}
public Integer getSoft_limit_min(){
    return soft_limit_min;
}
public void setSoft_limit_max(Integer soft_limit_max){
    this.soft_limit_max = soft_limit_max;
}
public Integer getSoft_limit_max(){
    return soft_limit_max;
}
public void setEmployee(Employee employee){
    this.employee = employee;
}
public boolean withinLimit(Integer change){
    if((change + balance_hours) < soft_limit_max || (balance_hours + change) > soft_limit_min) return true;
    else return false;
}
}
