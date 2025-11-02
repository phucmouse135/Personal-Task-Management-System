import { apiClient } from '@/lib/apiClient';
import { Notification, PageResponse, ApiResponse } from '@/types';
import { useAuthStore } from '@/store/authStore';

/**
 * Get current user ID from auth store
 */
const getCurrentUserId = (): number => {
  const user = useAuthStore.getState().user;
  if (!user || !user.id) {
    throw new Error('User not authenticated');
  }
  return user.id;
};

export const notificationService = {
  /**
   * Get all notifications for current user
   */
  async getNotifications(params?: {
    page?: number;
    size?: number;
    sort?: string;
  }): Promise<PageResponse<Notification>> {
    const userId = getCurrentUserId();
    
    const response = await apiClient.get<ApiResponse<PageResponse<Notification>>>(
      `/notifications/user/${userId}`,
      { params }
    );
    return response.data.result;
  },

  /**
   * Mark notification as read
   */
  async markAsRead(id: number): Promise<void> {
    const userId = getCurrentUserId();
    
    await apiClient.post<ApiResponse<void>>(
      `/notifications/${id}/read/user/${userId}`
    );
  },

  /**
   * Mark all notifications as read for current user
   */
  async markAllAsRead(): Promise<void> {
    const userId = getCurrentUserId();
    
    await apiClient.post<void>(
      `/notifications/read-all/user/${userId}`
    );
  },
};
