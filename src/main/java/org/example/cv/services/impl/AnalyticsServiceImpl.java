package org.example.cv.services.impl;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.example.cv.constants.TaskStatus;
import org.example.cv.models.entities.ProjectEntity;
import org.example.cv.models.entities.TaskEntity;
import org.example.cv.models.responses.*;
import org.example.cv.repositories.ProjectRepository;
import org.example.cv.repositories.TaskRepository;
import org.example.cv.services.AnalyticsService;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsServiceImpl implements AnalyticsService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    @Override
    public AnalyticsSummaryResponse getTasksSummary() {
        log.info("Getting tasks summary");

        List<TaskEntity> allTasks = taskRepository.findAll();
        List<ProjectEntity> allProjects = projectRepository.findAll();

        long totalTasks = allTasks.size();
        long completedTasks = allTasks.stream()
                .filter(t -> TaskStatus.DONE.equals(t.getStatus()))
                .count();
        long inProgressTasks = allTasks.stream()
                .filter(t -> TaskStatus.IN_PROGRESS.equals(t.getStatus()))
                .count();
        long todoTasks = allTasks.stream()
                .filter(t -> TaskStatus.TODO.equals(t.getStatus()))
                .count();
        long cancelledTasks = allTasks.stream()
                .filter(t -> TaskStatus.CANCELLED.equals(t.getStatus()))
                .count();

        // Overdue tasks: deadline passed and not DONE
        Instant now = Instant.now();
        long overdueTasks = allTasks.stream()
                .filter(t -> t.getDeadline() != null)
                .filter(t -> t.getDeadline().isBefore(now))
                .filter(t -> !TaskStatus.DONE.equals(t.getStatus()))
                .count();

        // Completed this month
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        Instant startOfMonthInstant = startOfMonth.atStartOfDay().toInstant(ZoneOffset.UTC);
        long completedThisMonth = allTasks.stream()
                .filter(t -> TaskStatus.DONE.equals(t.getStatus()))
                .filter(t -> t.getUpdatedAt() != null)
                .filter(t -> t.getUpdatedAt().isAfter(startOfMonthInstant))
                .count();
        long totalProjects = allProjects.size();
        // All projects are considered active since there's no endDate field
        long activeProjects = totalProjects;

        double completionRate = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0.0;

        return AnalyticsSummaryResponse.builder()
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .inProgressTasks(inProgressTasks)
                .todoTasks(todoTasks)
                .cancelledTasks(cancelledTasks)
                .overdueTasks(overdueTasks)
                .completedThisMonth(completedThisMonth)
                .totalProjects(totalProjects)
                .activeProjects(activeProjects)
                .completionRate(Math.round(completionRate * 100.0) / 100.0)
                .build();
    }

    @Override
    public List<TaskStatusCountResponse> getTasksByStatus() {
        log.info("Getting tasks by status");

        List<TaskEntity> allTasks = taskRepository.findAll();

        Map<TaskStatus, Long> statusCounts =
                allTasks.stream().collect(Collectors.groupingBy(TaskEntity::getStatus, Collectors.counting()));

        return statusCounts.entrySet().stream()
                .map(entry -> TaskStatusCountResponse.builder()
                        .status(entry.getKey().name())
                        .count(entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskPriorityCountResponse> getTasksByPriority() {
        log.info("Getting tasks by priority");

        List<TaskEntity> allTasks = taskRepository.findAll();

        Map<String, Long> priorityCounts = allTasks.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getPriority() != null ? t.getPriority().name() : "MEDIUM", Collectors.counting()));

        return priorityCounts.entrySet().stream()
                .map(entry -> TaskPriorityCountResponse.builder()
                        .priority(entry.getKey())
                        .count(entry.getValue())
                        .build())
                .sorted(Comparator.comparing(TaskPriorityCountResponse::getCount)
                        .reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskProjectCountResponse> getTasksByProject() {
        log.info("Getting tasks by project");

        List<TaskEntity> allTasks = taskRepository.findAll();

        Map<Long, List<TaskEntity>> projectGroups = allTasks.stream()
                .filter(t -> t.getProject() != null)
                .collect(Collectors.groupingBy(t -> t.getProject().getId()));

        return projectGroups.entrySet().stream()
                .map(entry -> {
                    TaskEntity firstTask = entry.getValue().get(0);
                    return TaskProjectCountResponse.builder()
                            .projectId(entry.getKey())
                            .projectName(firstTask.getProject().getName())
                            .count((long) entry.getValue().size())
                            .build();
                })
                .sorted(Comparator.comparing(TaskProjectCountResponse::getCount).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskTrendResponse> getTasksTrend(int days) {
        log.info("Getting tasks trend for {} days", days);

        List<TaskEntity> allTasks = taskRepository.findAll();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        List<TaskTrendResponse> trend = new ArrayList<>();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.format(formatter);

            long created = allTasks.stream()
                    .filter(t -> t.getCreatedAt() != null)
                    .filter(t -> {
                        LocalDate createdDate = LocalDate.ofInstant(t.getCreatedAt(), ZoneOffset.UTC);
                        return createdDate.equals(date);
                    })
                    .count();

            long completed = allTasks.stream()
                    .filter(t -> TaskStatus.DONE.equals(t.getStatus()))
                    .filter(t -> t.getUpdatedAt() != null)
                    .filter(t -> {
                        LocalDate updatedDate = LocalDate.ofInstant(t.getUpdatedAt(), ZoneOffset.UTC);
                        return updatedDate.equals(date);
                    })
                    .count();
            trend.add(TaskTrendResponse.builder()
                    .date(dateStr)
                    .created(created)
                    .completed(completed)
                    .build());
        }

        return trend;
    }

    @Override
    public Object getProjectsSummary() {
        log.info("Getting projects summary");

        List<ProjectEntity> allProjects = projectRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        Map<String, Object> summary = new HashMap<>();
        summary.put("total", allProjects.size());
        //        summary.put(
        //                "active",
        //                allProjects.stream()
        //                        .filter(p -> p.getEndDate() == null || p.getEndDate().isAfter(now))
        //                        .count());
        //        summary.put(
        //                "completed",
        //                allProjects.stream()
        //                        .filter(p -> p.getEndDate() != null && p.getEndDate().isBefore(now))
        //                        .count());

        return summary;
    }
}
