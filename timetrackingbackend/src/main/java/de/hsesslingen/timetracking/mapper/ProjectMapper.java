package de.hsesslingen.timetracking.mapper;
import org.springframework.stereotype.Component;

import de.hsesslingen.timetracking.dto.ProjectResponseDTO;
import de.hsesslingen.timetracking.model.Project;

@Component
public class ProjectMapper {
    public ProjectResponseDTO toResponse(Project project) {
        return new ProjectResponseDTO(project.getId(), project.getName());
    }
}
