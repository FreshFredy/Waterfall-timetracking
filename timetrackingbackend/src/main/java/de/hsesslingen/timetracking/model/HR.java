package de.hsesslingen.timetracking.model;


import jakarta.persistence.*;

@Entity @DiscriminatorValue("HR")
public class HR extends Employee { }
