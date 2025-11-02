package org.example.cv.controllers;

import org.example.cv.models.responses.AnalyticsSummaryResponse;
import org.example.cv.services.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics Controller", description = "API cho phân tích và thống kê")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(summary = "Lấy tổng quan thống kê task")
    @GetMapping("/tasks-summary")
    public ResponseEntity<AnalyticsSummaryResponse> getTasksSummary() {
        AnalyticsSummaryResponse summary = analyticsService.getTasksSummary();
        return ResponseEntity.ok(summary);
    }

    @Operation(summary = "Lấy thống kê task theo status")
    @GetMapping("/tasks-by-status")
    public ResponseEntity<?> getTasksByStatus() {
        return ResponseEntity.ok(analyticsService.getTasksByStatus());
    }

    @Operation(summary = "Lấy thống kê task theo priority")
    @GetMapping("/tasks-by-priority")
    public ResponseEntity<?> getTasksByPriority() {
        return ResponseEntity.ok(analyticsService.getTasksByPriority());
    }

    @Operation(summary = "Lấy thống kê task theo project")
    @GetMapping("/tasks-by-project")
    public ResponseEntity<?> getTasksByProject() {
        return ResponseEntity.ok(analyticsService.getTasksByProject());
    }

    @Operation(summary = "Lấy xu hướng task theo ngày")
    @GetMapping("/tasks-trend")
    public ResponseEntity<?> getTasksTrend(@RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(analyticsService.getTasksTrend(days));
    }

    @Operation(summary = "Lấy thống kê project")
    @GetMapping("/projects-summary")
    public ResponseEntity<?> getProjectsSummary() {
        return ResponseEntity.ok(analyticsService.getProjectsSummary());
    }
}
