package org.example.cv.models.responses;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AnalyticsSummaryResponse {
    Long totalTasks;
    Long completedTasks;
    Long inProgressTasks;
    Long todoTasks;
    Long cancelledTasks;
    Long overdueTasks;
    Long completedThisMonth;
    Long totalProjects;
    Long activeProjects;
    Double completionRate;
}
