import { useState, useEffect, useCallback } from 'react';
import { apiClient } from '@/lib/apiClient';
import { API_ENDPOINTS } from '@/constants';
import { Payment, ApiResponse, PageResponse } from '@/types';

interface UsePaymentsOptions {
  page?: number;
  size?: number;
  status?: string;
}

export const usePayments = (options: UsePaymentsOptions = {}) => {
  const [payments, setPayments] = useState<Payment[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [pagination, setPagination] = useState({
    currentPage: 0,
    totalPages: 0,
    totalElements: 0,
  });

  const fetchPayments = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      
      const params = {
        page: options.page || 0,
        size: options.size || 20,
        ...(options.status && options.status !== 'all' ? { status: options.status } : {}),
      };

      const response = await apiClient.get<ApiResponse<PageResponse<Payment>>>(
        API_ENDPOINTS.PAYMENTS,
        { params }
      );

      const pageData = response.data.result;
      
      if (pageData && pageData.content) {
        setPayments(pageData.content);
        setPagination({
          currentPage: pageData.number,
          totalPages: pageData.totalPages,
          totalElements: pageData.totalElements,
        });
      } else {
        setPayments([]);
      }
    } catch (err) {
      console.error('Error fetching payments:', err);
      setError(err instanceof Error ? err.message : 'Failed to load payments');
      setPayments([]);
    } finally {
      setLoading(false);
    }
  }, [options.page, options.size, options.status]);

  useEffect(() => {
    fetchPayments();
  }, [fetchPayments]);

  return {
    payments,
    loading,
    error,
    pagination,
    refetch: fetchPayments,
  };
};
