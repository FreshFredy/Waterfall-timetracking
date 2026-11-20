package de.hsesslingen.timetracking.model;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity 
@Table(name = "Project", uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "project_name"}))
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "project_name", nullable = false, unique = true)
    private String name;

    private boolean active = true;

    public Project(){}

    public Project(String name){
        this.name = name;
        active = true;
    }
    public long getId(){
        return id;
    }
    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return this.name;
    }
    public void setActive(boolean state){
        this.active = state;
    }
    public boolean getActive(){
        return active;
    }
}   
