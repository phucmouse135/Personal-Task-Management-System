import { apiClient } from '@/lib/apiClient';
import { API_ENDPOINTS } from '@/constants';
import { TasksSummary } from '@/types';

export const analyticsService = {
  /**
   * Get tasks summary
   */
  async getTasksSummary(): Promise<TasksSummary> {
    const response = await apiClient.get<TasksSummary>(API_ENDPOINTS.ANALYTICS_TASKS_SUMMARY);
    return response.data;
  },
};
