import { apiClient } from '@/lib/apiClient';
import { API_ENDPOINTS } from '@/constants';
import { Project, ProjectRequest, PageResponse, SpringPage, ApiResponse } from '@/types';

export const projectService = {
  /**
   * Get all projects
   */
  async getProjects(params?: {
    page?: number;
    size?: number;
    sort?: string;
    ownerId?: number;
  }): Promise<PageResponse<Project>> {
    // Backend returns Spring Page wrapped in ApiResponse
    const response = await apiClient.get<ApiResponse<SpringPage<Project>>>(API_ENDPOINTS.PROJECTS, { params });
    
    // Convert Spring Page to PageResponse
    const springPage = response.data.result;
    return {
      content: springPage.content,
      totalElements: springPage.totalElements,
      totalPages: springPage.totalPages,
      size: springPage.size,
      number: springPage.number,
      first: springPage.first,
      last: springPage.last,
    };
  },

  /**
   * Get my projects (current user's projects)
   */
  async getMyProjects(params?: {
    page?: number;
    size?: number;
  }): Promise<PageResponse<Project>> {
    const response = await apiClient.get<ApiResponse<SpringPage<Project>>>(`${API_ENDPOINTS.PROJECTS}/my-projects`, { params });
    
    const springPage = response.data.result;
    return {
      content: springPage.content,
      totalElements: springPage.totalElements,
      totalPages: springPage.totalPages,
      size: springPage.size,
      number: springPage.number,
      first: springPage.first,
      last: springPage.last,
    };
  },

  /**
   * Get soft-deleted projects
   * @param scope - 'my' for current user's deleted projects, 'all' for all deleted projects (admin)
   */
  async getSoftDeletedProjects(params?: {
    page?: number;
    size?: number;
    scope?: 'my' | 'all';
  }): Promise<PageResponse<Project>> {
    const response = await apiClient.get<ApiResponse<PageResponse<Project>>>(`${API_ENDPOINTS.PROJECTS}/soft-deleted`, { 
      params: {
        page: params?.page,
        size: params?.size,
        scope: params?.scope || 'my',
      }
    });
    
    return response.data.result;
  },

  /**
   * Get project by ID
   */
  async getProjectById(id: number): Promise<Project> {
    const response = await apiClient.get<ApiResponse<Project>>(API_ENDPOINTS.PROJECT_BY_ID(id));
    return response.data.result;
  },

  /**
   * Create project
   */
  async createProject(data: ProjectRequest): Promise<Project> {
    const response = await apiClient.post<ApiResponse<Project>>(API_ENDPOINTS.CREATE_PROJECT, data);
    return response.data.result;
  },

  /**
   * Update project
   */
  async updateProject(id: number, data: Partial<ProjectRequest>): Promise<Project> {
    const response = await apiClient.put<ApiResponse<Project>>(API_ENDPOINTS.PROJECT_BY_ID(id), data);
    return response.data.result;
  },

  /**
   * Delete project
   */
  async deleteProject(id: number): Promise<void> {
    await apiClient.delete(API_ENDPOINTS.PROJECT_BY_ID(id));
  },

  /**
   * Restore soft-deleted project
   */
  async restoreProject(id: number): Promise<void> {
    await apiClient.put(API_ENDPOINTS.RESTORE_PROJECT(id));
  },

  /**
   * Add member to project
   */
  async addMember(projectId: number, userId: number): Promise<Project> {
    const response = await apiClient.post<ApiResponse<Project>>(
      API_ENDPOINTS.ADD_PROJECT_MEMBER(projectId, userId)
    );
    return response.data.result;
  },

  /**
   * Remove member from project
   */
  async removeMember(projectId: number, userId: number): Promise<Project> {
    const response = await apiClient.delete<ApiResponse<Project>>(
      API_ENDPOINTS.REMOVE_PROJECT_MEMBER(projectId, userId)
    );
    return response.data.result;
  },

  /**
   * Change project owner
   */
  async changeOwner(projectId: number, newOwnerId: number): Promise<Project> {
    const response = await apiClient.put<ApiResponse<Project>>(
      API_ENDPOINTS.CHANGE_PROJECT_OWNER(projectId, newOwnerId)
    );
    return response.data.result;
  },
};
