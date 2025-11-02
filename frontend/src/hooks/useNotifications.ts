import { useState, useEffect } from 'react';
import { Notification, PageResponse } from '@/types';
import { notificationService } from '@/services/notificationService';
import toast from 'react-hot-toast';

export function useNotifications(params?: {
  page?: number;
  size?: number;
  sort?: string;
}) {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [pagination, setPagination] = useState<Omit<PageResponse<Notification>, 'content'> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchNotifications = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await notificationService.getNotifications(params);
      
      // Defensive check for response structure
      if (response && response.content) {
        setNotifications(response.content);
        setPagination({
          totalElements: response.totalElements,
          totalPages: response.totalPages,
          size: response.size,
          number: response.number,
          first: response.first,
          last: response.last,
        });
      } else {
        // If response is invalid, set empty array
        setNotifications([]);
        setPagination(null);
      }
    } catch (err) {
      const errorMessage = (err as { message: string }).message || 'Failed to fetch notifications';
      setError(errorMessage);
      setNotifications([]); // Clear notifications on error
      toast.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const markAsRead = async (id: number) => {
    try {
      await notificationService.markAsRead(id);
      await fetchNotifications();
      toast.success('Notification marked as read');
    } catch (err) {
      const errorMessage = (err as { message: string }).message || 'Failed to mark notification as read';
      toast.error(errorMessage);
    }
  };

  const markAllAsRead = async () => {
    try {
      await notificationService.markAllAsRead();
      await fetchNotifications();
      toast.success('All notifications marked as read');
    } catch (err) {
      const errorMessage = (err as { message: string }).message || 'Failed to mark all notifications as read';
      toast.error(errorMessage);
    }
  };

  const unreadCount = notifications.filter(
    (n) => n.status === 'PENDING' || n.status === 'SENT'
  ).length;

  useEffect(() => {
    fetchNotifications();
  }, [JSON.stringify(params)]);

  return {
    notifications,
    pagination,
    loading,
    error,
    unreadCount,
    refetch: fetchNotifications,
    markAsRead,
    markAllAsRead,
  };
}
