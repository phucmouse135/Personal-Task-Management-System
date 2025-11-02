import { useState, useEffect } from 'react';
import { Project, PageResponse } from '@/types';
import { projectService } from '@/services/projectService';
import { useAuthStore } from '@/store/authStore';
import toast from 'react-hot-toast';

// Helper function to check if user is admin
const isAdmin = (roles?: { name: string }[]): boolean => {
  return roles?.some(role => role.name === 'ROLE_ADMIN') || false;
};

export function useProjects(filters?: {
  page?: number;
  size?: number;
  sort?: string;
  ownerId?: number;
}) {
  const { user } = useAuthStore();
  const [projects, setProjects] = useState<Project[]>([]);
  const [pagination, setPagination] = useState<Omit<PageResponse<Project>, 'content'> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchProjects = async () => {
    try {
      setLoading(true);
      setError(null);
      
      let response: PageResponse<Project>;
      
      // If user is admin, use getProjects with all filters
      // If user is normal, use getMyProjects API
      if (isAdmin(user?.roles)) {
        response = await projectService.getProjects(filters);
      } else {
        // Normal user - use my-projects endpoint (only page/size supported)
        response = await projectService.getMyProjects({
          page: filters?.page,
          size: filters?.size,
        });
      }
      
      // Defensive check for response structure
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
      const errorMessage = (err as { message: string }).message || 'Failed to fetch projects';
      const errorStatus = (err as { status?: number }).status;
      setError(errorMessage);
      setProjects([]);
      // Don't show error toast for permission errors (403) - these are expected for filtered data
      if (errorStatus !== 403 && !errorMessage.toLowerCase().includes('permission')) {
        toast.error(errorMessage);
      }
    } finally {
      setLoading(false);
    }
  };

  const createProject = async (data: {
    name: string;
    description: string;
    ownerId: number;
    startDate: string;
    endDate: string;
    status: string;
  }) => {
    try {
      await projectService.createProject(data as never);
      toast.success('Project created successfully');
      await fetchProjects();
    } catch (err) {
      const errorMessage = (err as { message: string }).message || 'Failed to create project';
      toast.error(errorMessage);
      throw err;
    }
  };

  const updateProject = async (id: number, data: Partial<Project>) => {
    try {
      await projectService.updateProject(id, data as never);
      toast.success('Project updated successfully');
      await fetchProjects();
    } catch (err) {
      const errorMessage = (err as { message: string }).message || 'Failed to update project';
      toast.error(errorMessage);
      throw err;
    }
  };

  const deleteProject = async (id: number) => {
    try {
      await projectService.deleteProject(id);
      toast.success('Project deleted successfully');
      await fetchProjects();
    } catch (err) {
      const errorMessage = (err as { message: string }).message || 'Failed to delete project';
      toast.error(errorMessage);
      throw err;
    }
  };

  const restoreProject = async (id: number) => {
    try {
      await projectService.restoreProject(id);
      toast.success('Project restored successfully');
      await fetchProjects();
    } catch (err) {
      const errorMessage = (err as { message: string }).message || 'Failed to restore project';
      toast.error(errorMessage);
      throw err;
    }
  };

  useEffect(() => {
    fetchProjects();
  }, [JSON.stringify(filters)]);

  return {
    projects,
    pagination,
    loading,
    error,
    refetch: fetchProjects,
    createProject,
    updateProject,
    deleteProject,
    restoreProject,
  };
}
