import { apiClient } from '@/lib/apiClient';
import { API_ENDPOINTS } from '@/constants';
import { Task, TaskRequest, TaskFilters, PageResponse } from '@/types';

export const taskService = {
  /**
   * Get all tasks with filters
   */
  async getTasks(filters?: TaskFilters): Promise<PageResponse<Task>> {
    const response = await apiClient.get<PageResponse<Task>>(API_ENDPOINTS.TASKS, {
      params: filters,
    });
    return response.data;
  },

  /**
   * Get my tasks (current user's tasks)
   */
  async getMyTasks(page?: number, size?: number): Promise<PageResponse<Task>> {
    const response = await apiClient.get<PageResponse<Task>>(`${API_ENDPOINTS.TASKS}/my-tasks`, {
      params: { page, size },
    });
    return response.data;
  },

  /**
   * Get task by ID
   */
  async getTaskById(id: number): Promise<Task> {
    const response = await apiClient.get<Task>(API_ENDPOINTS.TASK_BY_ID(id));
    return response.data;
  },

  /**
   * Create task
   */
  async createTask(data: TaskRequest): Promise<Task> {
    const response = await apiClient.post<Task>(API_ENDPOINTS.TASKS, data);
    return response.data;
  },

  /**
   * Update task
   */
  async updateTask(id: number, data: Partial<TaskRequest>): Promise<Task> {
    const response = await apiClient.put<Task>(API_ENDPOINTS.TASK_BY_ID(id), data);
    return response.data;
  },

  /**
   * Update task status only (for assignees)
   */
  async updateTaskStatus(id: number, status: string): Promise<Task> {
    const response = await apiClient.patch<Task>(`${API_ENDPOINTS.TASK_BY_ID(id)}/status`, { status });
    return response.data;
  },

  /**
   * Delete task
   */
  async deleteTask(id: number): Promise<void> {
    await apiClient.delete(API_ENDPOINTS.TASK_BY_ID(id));
  },

  /**
   * Restore soft-deleted task
   */
  async restoreTask(id: number): Promise<void> {
    await apiClient.patch(`${API_ENDPOINTS.TASK_BY_ID(id)}/restore`);
  },

  /**
   * Get soft-deleted tasks
   */
  async getSoftDeletedTasks(scope: 'my' | 'all' = 'my', page = 0, size = 10): Promise<PageResponse<Task>> {
    const response = await apiClient.get<PageResponse<Task>>(`${API_ENDPOINTS.TASKS}/soft-deleted`, {
      params: { scope, page, size },
    });
    return response.data;
  },
};
