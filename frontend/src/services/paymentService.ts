import { apiClient } from '@/lib/apiClient';
import { API_ENDPOINTS } from '@/constants';
import { Payment, PaymentRequest } from '@/types';

export const paymentService = {
  /**
   * Create payment
   */
  async createPayment(data: PaymentRequest): Promise<{ paymentUrl: string }> {
    const response = await apiClient.post<{ paymentUrl: string }>(
      API_ENDPOINTS.PAYMENT_CREATE,
      data
    );
    return response.data;
  },

  /**
   * Handle payment callback
   */
  async handleCallback(params: Record<string, string>): Promise<Payment> {
    const response = await apiClient.get<Payment>(API_ENDPOINTS.PAYMENT_CALLBACK, { params });
    return response.data;
  },
};
