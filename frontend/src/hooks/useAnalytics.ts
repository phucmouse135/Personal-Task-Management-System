import { useState, useEffect } from 'react';
import { TasksSummary } from '@/types';
import { analyticsService } from '@/services/analyticsService';
import toast from 'react-hot-toast';

export function useAnalytics() {
  const [summary, setSummary] = useState<TasksSummary | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchSummary = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await analyticsService.getTasksSummary();
      setSummary(data);
    } catch (err) {
      const errorMessage = (err as { message: string }).message || 'Failed to fetch analytics';
      setError(errorMessage);
      toast.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSummary();
  }, []);

  return {
    summary,
    loading,
    error,
    refetch: fetchSummary,
  };
}
