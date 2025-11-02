import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { WS_BASE_URL, STORAGE_KEYS, API_BASE_URL } from '@/constants';
import { ChatMessage } from '@/types';
import axios from 'axios';

export class ChatService {
  private client: Client | null = null;
  private messageHandlers: ((message: ChatMessage) => void)[] = [];

  /**
   * Connect to WebSocket
   */
  connect(onConnect?: () => void, onError?: (error: Error) => void): void {
    const token = localStorage.getItem(STORAGE_KEYS.TOKEN);

    this.client = new Client({
      webSocketFactory: () => new SockJS(WS_BASE_URL),
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      debug: (str) => {
        console.log('STOMP: ' + str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        console.log('WebSocket connected');
        this.subscribe();
        onConnect?.();
      },
      onStompError: (frame) => {
        console.error('STOMP error: ' + frame.headers['message']);
        console.error('Additional details: ' + frame.body);
        onError?.(new Error(frame.headers['message']));
      },
    });

    this.client.activate();
  }

  /**
   * Subscribe to message topic
   */
  private subscribe(): void {
    if (!this.client) return;

    this.client.subscribe('/topic/messages', (message) => {
      const chatMessage: ChatMessage = JSON.parse(message.body);
      this.messageHandlers.forEach((handler) => handler(chatMessage));
    });
  }

  /**
   * Send message
   */
  sendMessage(message: ChatMessage): void {
    if (!this.client || !this.client.connected) {
      console.error('WebSocket is not connected');
      return;
    }

    this.client.publish({
      destination: '/app/chat',
      body: JSON.stringify(message),
    });
  }

  /**
   * Add message handler
   */
  onMessage(handler: (message: ChatMessage) => void): () => void {
    this.messageHandlers.push(handler);
    return () => {
      this.messageHandlers = this.messageHandlers.filter((h) => h !== handler);
    };
  }

  /**
   * Disconnect
   */
  disconnect(): void {
    if (this.client) {
      this.client.deactivate();
      this.client = null;
    }
  }

  /**
   * Check connection status
   */
  isConnected(): boolean {
    return this.client?.connected || false;
  }
}

export const chatService = new ChatService();

/**
 * Get chat members (project members available for private chat)
 */
export const getChatMembers = async (search?: string) => {
  const token = localStorage.getItem(STORAGE_KEYS.TOKEN);
  const response = await axios.get(`${API_BASE_URL}/api/chat/members`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
    params: search ? { search } : {},
  });
  return response.data;
};

/**
 * Get project conversations (group chats)
 */
export const getProjectConversations = async () => {
  const token = localStorage.getItem(STORAGE_KEYS.TOKEN);
  const response = await axios.get(`${API_BASE_URL}/api/chat/projects`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
  return response.data;
};
