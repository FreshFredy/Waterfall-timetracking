package de.hsesslingen.timetracking.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class ProjectTest {

    @Test
    void defaultConstructor_initializesActiveAsTrueAndNullName() {
        Project project = new Project();

        assertEquals(0L, project.getId());
        assertNull(project.getName());
        assertTrue(project.getActive());
    }

    @Test
    void parameterizedConstructor_setsNameAndActiveTrue() {
        Project project = new Project("Zeiterfassung HS Esslingen");

        assertEquals("Zeiterfassung HS Esslingen", project.getName());
        assertTrue(project.getActive());
    }

    @Test
    void setName_updatesProjectName() {
        Project project = new Project("Altes Projekt");

        project.setName("Neues Projekt");

        assertEquals("Neues Projekt", project.getName());
    }

    @Test
    void setActive_togglesActiveState() {
        Project project = new Project("Projekt A");
        assertTrue(project.getActive());

        project.setActive(false);
        assertFalse(project.getActive());

        project.setActive(true);
        assertTrue(project.getActive());
    }
}