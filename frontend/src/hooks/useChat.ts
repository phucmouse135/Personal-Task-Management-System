import { useState, useEffect, useCallback } from 'react';
import { ChatMessage } from '@/types';
import { chatService } from '@/services/chatService';
import { useAuthStore } from '@/store/authStore';
import toast from 'react-hot-toast';

export function useChat(projectId?: number) {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [connected, setConnected] = useState(false);
  const [loading, setLoading] = useState(true);
  const user = useAuthStore((state) => state.user);

  const connect = useCallback(() => {
    if (!user) return;

    chatService.connect(
      () => {
        setConnected(true);
        setLoading(false);
        toast.success('Connected to chat');
      },
      (error) => {
        setConnected(false);
        setLoading(false);
        toast.error(`Chat connection error: ${error.message}`);
      }
    );

    const unsubscribe = chatService.onMessage((message: ChatMessage) => {
      if (!projectId || message.projectId === projectId) {
        setMessages((prev) => [...prev, message]);
      }
    });

    return () => {
      unsubscribe();
      chatService.disconnect();
    };
  }, [user, projectId]);

  const sendMessage = (content: string) => {
    if (!user) return;

    const message: ChatMessage = {
      senderId: user.id,
      content,
      projectId,
      timestamp: new Date().toISOString(),
    };

    chatService.sendMessage(message);
  };

  useEffect(() => {
    const cleanup = connect();
    return cleanup;
  }, [connect]);

  return {
    messages,
    connected,
    loading,
    sendMessage,
    reconnect: connect,
  };
}
