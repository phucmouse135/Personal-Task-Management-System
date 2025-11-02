import { useState } from 'react';
import { paymentService } from '@/services/paymentService';
import toast from 'react-hot-toast';

export function usePayment() {
  const [loading, setLoading] = useState(false);

  const createPayment = async (taskId: number, amount: number) => {
    try {
      setLoading(true);
      const response = await paymentService.createPayment({
        taskId,
        amount,
        returnUrl: `${window.location.origin}/payments/callback`,
      });
      
      // Redirect to payment URL
      if (response.paymentUrl) {
        window.location.href = response.paymentUrl;
      }
    } catch (err) {
      const errorMessage = (err as { message: string }).message || 'Failed to create payment';
      toast.error(errorMessage);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const handleCallback = async (params: Record<string, string>) => {
    try {
      setLoading(true);
      const payment = await paymentService.handleCallback(params);
      
      if (payment.status === 'SUCCESS') {
        toast.success('Payment successful!');
      } else {
        toast.error('Payment failed!');
      }
      
      return payment;
    } catch (err) {
      const errorMessage = (err as { message: string }).message || 'Failed to process payment callback';
      toast.error(errorMessage);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  return {
    loading,
    createPayment,
    handleCallback,
  };
}
