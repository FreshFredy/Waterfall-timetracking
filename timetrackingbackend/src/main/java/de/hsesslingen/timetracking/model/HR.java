package de.hsesslingen.timetracking.model;


import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity @DiscriminatorValue("HR")
public class HR extends Employee {  
}
