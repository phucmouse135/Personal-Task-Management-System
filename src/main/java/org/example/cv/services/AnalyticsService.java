package org.example.cv.services;

import java.util.List;

import org.example.cv.models.responses.*;
import org.springframework.stereotype.Service;

@Service
public interface AnalyticsService {

    /**
     * Lấy tổng quan thống kê task
     */
    AnalyticsSummaryResponse getTasksSummary();

    /**
     * Lấy số lượng task theo status
     */
    List<TaskStatusCountResponse> getTasksByStatus();

    /**
     * Lấy số lượng task theo priority
     */
    List<TaskPriorityCountResponse> getTasksByPriority();

    /**
     * Lấy số lượng task theo project
     */
    List<TaskProjectCountResponse> getTasksByProject();

    /**
     * Lấy xu hướng task theo ngày
     */
    List<TaskTrendResponse> getTasksTrend(int days);

    /**
     * Lấy thống kê project
     */
    Object getProjectsSummary();
}
