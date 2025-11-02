import { useState, useEffect } from 'react';
import { Project, PageResponse } from '@/types';
import { projectService } from '@/services/projectService';
import { useAuthStore } from '@/store/authStore';
import toast from 'react-hot-toast';

// Helper function to check if user is admin
const isAdmin = (roles?: { name: string }[]): boolean => {
  return roles?.some(role => role.name === 'ROLE_ADMIN') || false;
};

export function useSoftDeletedProjects(filters?: {
  page?: number;
  size?: number;
}) {
  const { user } = useAuthStore();
  const [projects, setProjects] = useState<Project[]>([]);
  const [pagination, setPagination] = useState<Omit<PageResponse<Project>, 'content'> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchSoftDeletedProjects = async () => {
    try {
      setLoading(true);
      setError(null);
      
      // Determine scope based on user role
      const scope = isAdmin(user?.roles) ? 'all' : 'my';
      
      const response = await projectService.getSoftDeletedProjects({
        page: filters?.page,
        size: filters?.size,
        scope,
      });
      
      if (response && response.content) {
        setProjects(response.content);
        setPagination({
          totalElements: response.totalElements,
          totalPages: response.totalPages,
          size: response.size,
          number: response.number,
          first: response.first,
          last: response.last,
        });
      } else {
        setProjects([]);
        setPagination(null);
      }
    } catch (err) {
      const errorMessage = (err as { message: string }).message || 'Failed to fetch soft deleted projects';
      setError(errorMessage);
      setProjects([]);
      toast.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const restoreProject = async (id: number) => {
    try {
      await projectService.restoreProject(id);
      toast.success('Project restored successfully');
      await fetchSoftDeletedProjects();
    } catch (err) {
      const errorMessage = (err as { message: string }).message || 'Failed to restore project';
      toast.error(errorMessage);
      throw err;
    }
  };

  useEffect(() => {
    fetchSoftDeletedProjects();
  }, [JSON.stringify(filters)]);

  return {
    projects,
    pagination,
    loading,
    error,
    refetch: fetchSoftDeletedProjects,
    restoreProject,
  };
}
