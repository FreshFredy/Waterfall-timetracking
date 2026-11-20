package de.hsesslingen.timetracking.controller;

import java.util.List;


import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import de.hsesslingen.timetracking.dto.ProjectResponseDTO;
import de.hsesslingen.timetracking.service.ProjectService;
import de.hsesslingen.timetracking.mapper.ProjectMapper;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api-timetracking/Project-Management")
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectMapper projectMapper;

    public ProjectController(ProjectService projectService, ProjectMapper projectMapper) {
        this.projectService = projectService;
        this.projectMapper = projectMapper;
    }

    @GetMapping
    public List<ProjectResponseDTO> getAll() {
        return projectService.getAll().stream().map(projectMapper::toResponse).toList();
    }
}