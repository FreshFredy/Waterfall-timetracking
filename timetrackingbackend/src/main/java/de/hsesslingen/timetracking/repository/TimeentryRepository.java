package de.hsesslingen.timetracking.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import de.hsesslingen.timetracking.model.Timeentry;
public interface TimeentryRepository extends JpaRepository<Timeentry, Long> {}

