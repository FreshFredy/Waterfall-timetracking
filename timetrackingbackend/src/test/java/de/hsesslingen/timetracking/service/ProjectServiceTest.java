package de.hsesslingen.timetracking.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import de.hsesslingen.timetracking.model.Project;
import de.hsesslingen.timetracking.repository.ProjectRepository;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void getAll_returnsAllProjectsFromRepository() {
        Project p1 = new Project("Zeiterfassung");
        Project p2 = new Project("Urlaubsverwaltung");
        when(projectRepository.findAll()).thenReturn(List.of(p1, p2));

        List<Project> result = projectService.getAll();

        assertEquals(2, result.size());
        assertEquals(List.of(p1, p2), result);
        verify(projectRepository, times(1)).findAll();
    }

    @Test
    void getAll_returnsEmptyListWhenNoProjectsExist() {
        when(projectRepository.findAll()).thenReturn(List.of());

        List<Project> result = projectService.getAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(projectRepository, times(1)).findAll();
    }

    @Test
    void getById_returnsProjectWhenFound() {
        Project project = new Project("Zeiterfassung");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        Project result = projectService.getById(1L);

        assertSame(project, result);
        verify(projectRepository, times(1)).findById(1L);
    }

    @Test
    void getById_throwsNoSuchElementExceptionWhenNotFound() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException ex = assertThrows(
                NoSuchElementException.class,
                () -> projectService.getById(99L)
        );

        assertEquals("Project 99 not found", ex.getMessage());
        verify(projectRepository, times(1)).findById(99L);
    }
}