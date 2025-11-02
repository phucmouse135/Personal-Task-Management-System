import { useState, useEffect } from 'react';
import { Task, TaskFilters, TaskRequest, PageResponse } from '@/types';
import { taskService } from '@/services/taskService';
import { useAuthStore } from '@/store/authStore';
import toast from 'react-hot-toast';

// Helper function to check if user is admin
const isAdmin = (roles?: { name: string }[]): boolean => {
  return roles?.some(role => role.name === 'ROLE_ADMIN') || false;
};

export function useTasks(filters?: TaskFilters) {
  const { user } = useAuthStore();
  const [tasks, setTasks] = useState<Task[]>([]);
  const [pagination, setPagination] = useState<Omit<PageResponse<Task>, 'content'> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchTasks = async () => {
    try {
      setLoading(true);
      setError(null);
      
      let response: PageResponse<Task>;
      
      // If user is admin, use getAllTasks with filters
      // If user is normal, use getMyTasks API
      if (isAdmin(user?.roles)) {
        response = await taskService.getTasks(filters);
      } else {
        // Normal user - use my-tasks endpoint (ignores filters except page/size)
        response = await taskService.getMyTasks(filters?.page, filters?.size);
      }
      
      // Defensive check for response structure
      if (response && response.content) {
        setTasks(response.content);
        setPagination({
          totalElements: response.totalElements,
          totalPages: response.totalPages,
          size: response.size,
          number: response.number,
          first: response.first,
          last: response.last,
        });
      } else {
        setTasks([]);
        setPagination(null);
      }
    } catch (err) {
      const errorMessage = (err as { message: string }).message || 'Failed to fetch tasks';
      const errorStatus = (err as { status?: number }).status;
      setError(errorMessage);
      setTasks([]);
      // Don't show error toast for permission errors (403) - these are expected for filtered data
      if (errorStatus !== 403 && !errorMessage.toLowerCase().includes('permission')) {
        toast.error(errorMessage);
      }
    } finally {
      setLoading(false);
    }
  };

  const createTask = async (data: TaskRequest) => {
    try {
      await taskService.createTask(data);
      toast.success('Task created successfully');
      await fetchTasks();
    } catch (err) {
      const errorMessage = (err as { message: string }).message || 'Failed to create task';
      toast.error(errorMessage);
      throw err;
    }
  };

  const updateTask = async (id: number, data: Partial<Task>) => {
    try {
      await taskService.updateTask(id, data as never);
      toast.success('Task updated successfully');
      await fetchTasks();
    } catch (err) {
      const errorMessage = (err as { message: string }).message || 'Failed to update task';
      toast.error(errorMessage);
      throw err;
    }
  };

  const updateTaskStatus = async (id: number, status: string) => {
    try {
      await taskService.updateTaskStatus(id, status);
      toast.success('Task status updated successfully');
      await fetchTasks();
    } catch (err) {
      const errorMessage = (err as { message: string }).message || 'Failed to update task status';
      toast.error(errorMessage);
      throw err;
    }
  };

  const deleteTask = async (id: number) => {
    try {
      await taskService.deleteTask(id);
      toast.success('Task deleted successfully');
      await fetchTasks();
    } catch (err) {
      const errorMessage = (err as { message: string }).message || 'Failed to delete task';
      toast.error(errorMessage);
      throw err;
    }
  };

  useEffect(() => {
    fetchTasks();
  }, [JSON.stringify(filters)]);

  return {
    tasks,
    pagination,
    loading,
    error,
    refetch: fetchTasks,
    createTask,
    updateTask,
    updateTaskStatus,
    deleteTask,
  };
}
