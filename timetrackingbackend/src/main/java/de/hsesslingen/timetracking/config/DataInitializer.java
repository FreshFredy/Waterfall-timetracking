package de.hsesslingen.timetracking.config;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import de.hsesslingen.timetracking.EntryTypes.ExusedEntryType;
import de.hsesslingen.timetracking.EntryTypes.TimeEntryType;
import de.hsesslingen.timetracking.EntryTypes.TimesheetStatus;
import de.hsesslingen.timetracking.model.Employee;
import de.hsesslingen.timetracking.model.HR;
import de.hsesslingen.timetracking.model.SickLeave;
import de.hsesslingen.timetracking.model.Supervisor;
import de.hsesslingen.timetracking.model.Timeentry;
import de.hsesslingen.timetracking.model.Timesheet;
import de.hsesslingen.timetracking.repository.EmployeeRepository;
import de.hsesslingen.timetracking.repository.SickLeaveRepository;
import de.hsesslingen.timetracking.repository.TimesheetRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private TimesheetRepository timesheetRepository;

    @Autowired
    private SickLeaveRepository sickLeaveRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (employeeRepository.count() > 0) {
            return;
        }

        Supervisor testSupervisor = new Supervisor(
            "Sabine", "Weber", "sabine.w", "supervisor@company.com", "123456", 8
        );
        employeeRepository.save(testSupervisor);

        Employee testEmployee = new Employee(
            "Markus", "Müller", "markus.m", "employee@company.com", "123456", 8, testSupervisor
        );
        employeeRepository.save(testEmployee);

        HR testHr = new HR(
            "Thomas", "Koch", "thomas.k", "hr@company.com", "123456", 8, testSupervisor
        );
        employeeRepository.save(testHr);

        Random random = new Random();
        LocalDate today = LocalDate.now();
        Set<Integer> usedWorkCounts = new HashSet<>();

        generateRandomCurrentMonthData(
            testSupervisor, testHr, today, random, usedWorkCounts, "notes/au_anna.pdf", false
        );
        generateRandomCurrentMonthData(
            testEmployee, testHr, today, random, usedWorkCounts, null, true
        );
        generateRandomCurrentMonthData(
            testHr, testHr, today, random, usedWorkCounts, "notes/au_sarah.pdf", false
        );

        System.out.println(">>> Test-Benutzer, Timesheets (aktueller Monat), Timeentries und Krankheitstage wurden geladen! <<<");
    }

    private void generateRandomCurrentMonthData(
            Employee employee,
            HR hr,
            LocalDate referenceDate,
            Random random,
            Set<Integer> usedWorkCounts,
            String doctorsNotePath,
            boolean requireTenHourDayInStreak) {

        int year = referenceDate.getYear();
        int month = referenceDate.getMonthValue();

        Timesheet sheet = new Timesheet(month, year, TimesheetStatus.DRAFT, null, employee);
        employee.addTimesheet(sheet);

        List<LocalDate> weekdays = getWeekdaysOfMonth(year, month);
        if (weekdays.isEmpty()) {
            timesheetRepository.save(sheet);
            return;
        }

        // Alle möglichen zusammenhängenden Mo-Fr-Wochen (5 aufeinanderfolgende Werktage) ermitteln
        List<List<LocalDate>> fullWorkWeeks = getFullWorkWeeks(weekdays);

        List<LocalDate> shuffledWeekdays = new ArrayList<>(weekdays);
        Collections.shuffle(shuffledWeekdays, random);

        // Zufällige Arbeitswoche wählen (falls vorhanden)
        List<LocalDate> streakWeek = new ArrayList<>();
        if (!fullWorkWeeks.isEmpty()) {
            streakWeek = new ArrayList<>(fullWorkWeeks.get(random.nextInt(fullWorkWeeks.size())));
        }

        // Krankheitstag so wählen, dass er nicht in der gewählten Arbeitswoche liegt (falls möglich)
        LocalDate sickDay = null;
        for (LocalDate candidate : shuffledWeekdays) {
            if (!streakWeek.contains(candidate)) {
                sickDay = candidate;
                break;
            }
        }
        if (sickDay == null) {
            sickDay = shuffledWeekdays.get(0);
        }

        final LocalDate finalSickDay = sickDay;

        // Verbleibende Werktage ohne Krankheitstag
        List<LocalDate> remainingDays = new ArrayList<>(weekdays);
        remainingDays.remove(finalSickDay);

        int baseHalfDays = remainingDays.size() / 2;
        int workDaysCount = pickUniqueWorkDaysCount(remainingDays.size(), baseHalfDays, random, usedWorkCounts);
        usedWorkCounts.add(workDaysCount);

        // Zuerst die Tage der zusammenhängenden Arbeitswoche fest einplanen
        Set<LocalDate> selectedWorkDays = new HashSet<>();
        for (LocalDate day : streakWeek) {
            if (!day.equals(finalSickDay) && selectedWorkDays.size() < workDaysCount) {
                selectedWorkDays.add(day);
            }
        }

        // Restliche Arbeitstage zufällig aus den übrigen Werktagen auffüllen
        List<LocalDate> poolForExtraDays = new ArrayList<>(remainingDays);
        poolForExtraDays.removeAll(selectedWorkDays);
        Collections.shuffle(poolForExtraDays, random);

        for (LocalDate day : poolForExtraDays) {
            if (selectedWorkDays.size() >= workDaysCount) {
                break;
            }
            selectedWorkDays.add(day);
        }

        // Bei Bedarf an einem Tag der Arbeitswoche mindestens 10 Stunden setzen
        LocalDate tenHourDay = null;
        if (requireTenHourDayInStreak) {
            List<LocalDate> streakWorkDays = new ArrayList<>();
            for (LocalDate day : streakWeek) {
                if (selectedWorkDays.contains(day)) {
                    streakWorkDays.add(day);
                }
            }
            if (!streakWorkDays.isEmpty()) {
                tenHourDay = streakWorkDays.get(random.nextInt(streakWorkDays.size()));
            } else if (!selectedWorkDays.isEmpty()) {
                tenHourDay = selectedWorkDays.iterator().next();
            }
        }

        // Einträge chronologisch zusammenbauen
        Map<LocalDate, TimeEntryType> dayTypes = new HashMap<>();
        for (LocalDate d : selectedWorkDays) {
            dayTypes.put(d, TimeEntryType.WORK);
        }
        dayTypes.put(finalSickDay, TimeEntryType.SICK);

        List<LocalDate> sortedDays = new ArrayList<>(dayTypes.keySet());
        sortedDays.sort(Comparator.naturalOrder());

        Timeentry sickTimeEntry = null;

        for (LocalDate date : sortedDays) {
            TimeEntryType type = dayTypes.get(date);
            if (type == TimeEntryType.SICK) {
                sickTimeEntry = new Timeentry(date, null, null, 0, TimeEntryType.SICK);
                sheet.addEntry(sickTimeEntry);
            } else if (date.equals(tenHourDay)) {
                // 10.0 Stunden: 08:00 - 18:30 abzgl. 30 Min Pause = 600 Min = 10.0h
                Timeentry longWorkEntry = new Timeentry(
                    date,
                    LocalTime.of(8, 0),
                    LocalTime.of(18, 30),
                    30,
                    TimeEntryType.WORK
                );
                sheet.addEntry(longWorkEntry);
            } else {
                // Regulärer 8.0-Stunden-Tag: 08:00 - 16:30 abzgl. 30 Min Pause = 480 Min = 8.0h
                Timeentry workEntry = new Timeentry(
                    date,
                    LocalTime.of(8, 0),
                    LocalTime.of(16, 30),
                    30,
                    TimeEntryType.WORK
                );
                sheet.addEntry(workEntry);
            }
        }

        // Zuerst Timesheet + Timeentries speichern, damit sickTimeEntry eine ID hat
        timesheetRepository.save(sheet);

        // Danach SickLeave mit Referenz auf den gespeicherten Timeentry anlegen
        if (sickTimeEntry != null) {
            ExusedEntryType excusedType = (doctorsNotePath != null)
                ? ExusedEntryType.EXCUSED
                : ExusedEntryType.DOCOTRS_NOTE_MISSING;

            SickLeave sickLeave = new SickLeave(
                finalSickDay,
                doctorsNotePath,
                employee,
                hr,
                sickTimeEntry,
                excusedType
            );
            sickLeaveRepository.save(sickLeave);
        }
    }

    private List<List<LocalDate>> getFullWorkWeeks(List<LocalDate> weekdays) {
        List<List<LocalDate>> weeks = new ArrayList<>();
        for (int i = 0; i + 4 < weekdays.size(); i++) {
            LocalDate d0 = weekdays.get(i);
            LocalDate d1 = weekdays.get(i + 1);
            LocalDate d2 = weekdays.get(i + 2);
            LocalDate d3 = weekdays.get(i + 3);
            LocalDate d4 = weekdays.get(i + 4);

            if (d0.getDayOfWeek() == DayOfWeek.MONDAY
                    && d1.equals(d0.plusDays(1))
                    && d2.equals(d1.plusDays(1))
                    && d3.equals(d2.plusDays(1))
                    && d4.equals(d3.plusDays(1))
                    && d4.getDayOfWeek() == DayOfWeek.FRIDAY) {
                weeks.add(List.of(d0, d1, d2, d3, d4));
            }
        }
        return weeks;
    }

    private int pickUniqueWorkDaysCount(int maxAllowed, int baseHalfDays, Random random, Set<Integer> usedWorkCounts) {
        int minTarget = Math.max(0, Math.min(maxAllowed, baseHalfDays - 4));
        int maxTarget = Math.max(0, Math.min(maxAllowed, baseHalfDays - 1));

        List<Integer> candidates = new ArrayList<>();
        for (int i = minTarget; i <= maxTarget; i++) {
            if (!usedWorkCounts.contains(i)) {
                candidates.add(i);
            }
        }

        if (!candidates.isEmpty()) {
            return candidates.get(random.nextInt(candidates.size()));
        }

        int minus = 1 + random.nextInt(4);
        return Math.max(0, Math.min(maxAllowed, baseHalfDays - minus));
    }

    private List<LocalDate> getWeekdaysOfMonth(int year, int month) {
        List<LocalDate> weekdays = new ArrayList<>();
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            DayOfWeek dow = cursor.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                weekdays.add(cursor);
            }
            cursor = cursor.plusDays(1);
        }
        return weekdays;
    }
}