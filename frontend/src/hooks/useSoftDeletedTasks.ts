import { useState, useEffect, useCallback } from 'react';
import { taskService } from '@/services/taskService';
import { Task, PageResponse } from '@/types';
import { useAuthStore } from '@/store/authStore';

export const useSoftDeletedTasks = () => {
  const { user } = useAuthStore();
  const isAdmin = user?.roles?.includes('ADMIN');

  const [tasks, setTasks] = useState<Task[]>([]);
  const [pagination, setPagination] = useState({
    currentPage: 0,
    pageSize: 10,
    totalPages: 0,
    totalElements: 0,
  });
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Auto select scope based on role
  const scope = isAdmin ? 'all' : 'my';

  const fetchSoftDeletedTasks = useCallback(async (page = 0, size = 10) => {
    setIsLoading(true);
    setError(null);
    try {
      const response: PageResponse<Task> = await taskService.getSoftDeletedTasks(scope, page, size);
      setTasks(response.content);
      setPagination({
        currentPage: response.number,
        pageSize: response.size,
        totalPages: response.totalPages,
        totalElements: response.totalElements,
      });
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to fetch soft-deleted tasks';
      setError(errorMessage);
      console.error('Failed to fetch soft-deleted tasks:', err);
    } finally {
      setIsLoading(false);
    }
  }, [scope]);

  const restoreTask = async (id: number) => {
    try {
      await taskService.restoreTask(id);
      // Refresh the list
      fetchSoftDeletedTasks(pagination.currentPage, pagination.pageSize);
    } catch (err) {
      console.error('Failed to restore task:', err);
      throw err;
    }
  };

  useEffect(() => {
    fetchSoftDeletedTasks();
  }, [fetchSoftDeletedTasks]);

  return {
    tasks,
    pagination,
    isLoading,
    error,
    fetchSoftDeletedTasks,
    restoreTask,
  };
};
