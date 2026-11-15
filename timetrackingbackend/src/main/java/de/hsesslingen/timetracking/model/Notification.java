package de.hsesslingen.timetracking.model;

import jakarta.persistence.*;
import java.math.BigDecimal; 

import java.util.Date;


@Entity
@Table(name = "Notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "message")
    private String message; 

    @Column(name = "created_at")
    private Date created_at;

    @Column(name = "read")
    private boolean read;

    @OneToOne
    @JoinColumn(name = "employee_id_from", referencedColumnName = "id", unique = true)
    private Employee employee_from;

    @OneToOne
    @JoinColumn(name = "employee_id_to", referencedColumnName = "id", unique = true)
    private Employee employee_to;

    public Notification (){}
    public Notification (String message, Date created_at, Employee employee_from, Employee employee_to){
        this.message = message;
        this.created_at = created_at;
        this.employee_from = employee_from;
        this.employee_to = employee_to;
        read = false;
    }

    public long getID(){
        return id;
    }
    public String getMessage(){
        return message;
    }
    public void setMessage(String message){
        this.message = message;
    }
    public boolean getRead(){
        return read;
    }
    public void setRead(boolean read){
        this.read = read;
    }
    public Date getCreated_at(){
        return created_at;
    }
    public void setCreated_at(Date created_at){
        this.created_at = created_at;
    }
    public Employee getSender(){
        return employee_from;
    }
    public void setSender(Employee employee_from){
        this.employee_from = employee_from;
    }
    public Employee getEmpf(){
        return employee_to;
    }
    public void setEMpf(Employee employee_to){
        this.employee_to = employee_to;
    }




}
