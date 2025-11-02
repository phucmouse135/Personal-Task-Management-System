import { useState, useCallback } from 'react';
import { chatService as chatApiService, ChatMessageDTO } from '@/services/chatApiService';
import toast from 'react-hot-toast';

export function usePrivateChat() {
  const [messages, setMessages] = useState<ChatMessageDTO[]>([]);
  const [loading, setLoading] = useState(false);

  // Load message history from REST API
  const loadMessages = useCallback(async (otherUserId: number) => {
    setLoading(true);
    try {
      const history = await chatApiService.getMessages(otherUserId);
      setMessages(history);
    } catch (error) {
      console.error('Failed to load chat history:', error);
      toast.error('Failed to load chat history');
      setMessages([]);
    } finally {
      setLoading(false);
    }
  }, []);

  const clearMessages = useCallback(() => {
    setMessages([]);
  }, []);

  return {
    messages,
    loading,
    loadMessages,
    clearMessages,
  };
}
