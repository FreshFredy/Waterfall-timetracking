package de.hsesslingen.timetracking.model;


import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity @DiscriminatorValue("HR")
public class HR extends Employee { 
    public HR() {}

    public HR(String firstname, String lastname, String username, String email, String password, Integer targetH){
        super(firstname,lastname,username, email,password,targetH, null);
    } 
}
