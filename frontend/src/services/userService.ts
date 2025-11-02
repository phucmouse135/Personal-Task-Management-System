import { apiClient } from '@/lib/apiClient';
import { API_ENDPOINTS } from '@/constants';
import { User, PageResponse, ApiResponse, SpringPage } from '@/types';

export const userService = {
  /**
   * Get all users (Admin only)
   */
  async getUsers(params?: {
    page?: number;
    size?: number;
    sort?: string;
  }): Promise<PageResponse<User>> {
    const response = await apiClient.get<PageResponse<User>>(API_ENDPOINTS.USERS, { params });
    return response.data;
  },

  /**
   * Get user by ID
   */
  async getUserById(id: number): Promise<User> {
    const response = await apiClient.get<User>(API_ENDPOINTS.USER_BY_ID(id));
    return response.data;
  },

  /**
   * Create user (Admin only)
   */
  async createUser(data: Partial<User>): Promise<User> {
    const response = await apiClient.post<User>(API_ENDPOINTS.USERS, data);
    return response.data;
  },

  /**
   * Update user
   */
  async updateUser(id: number, data: Partial<User>): Promise<User> {
    const response = await apiClient.put<User>(API_ENDPOINTS.USER_BY_ID(id), data);
    return response.data;
  },

  /**
   * Delete user (Admin only)
   */
  async deleteUser(id: number): Promise<void> {
    await apiClient.delete(API_ENDPOINTS.USER_BY_ID(id));
  },

  /**
   * Get user by username
   */
  async getUserByUsername(username: string): Promise<User> {
    const response = await apiClient.get<ApiResponse<User>>(API_ENDPOINTS.USER_BY_USERNAME(username));
    return response.data.result;
  },

  /**
   * Search users by username (partial match)
   */
  async searchUsers(searchTerm: string): Promise<User[]> {
    const response = await apiClient.get<ApiResponse<SpringPage<User>>>(API_ENDPOINTS.USERS, {
      params: {
        search: searchTerm,
        size: 10, // Limit to 10 results
      },
    });
    return response.data.result.content || [];
  },
};
