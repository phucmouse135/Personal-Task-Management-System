import { apiClient } from '@/lib/apiClient';
import { ApiResponse } from '@/types';

export interface ChatMessageDTO {
  id: number;
  content: string;
  createdAt: string;
  senderId: number;
  senderUsername: string;
  receiverId?: number;
  projectId?: number;
}

export interface Conversation {
  userId: number;
  name: string;
  email: string;
  lastMessage?: string;
  lastMessageTime?: string;
  online?: boolean;
}

export const chatService = {
  /**
   * Get list of conversations
   */
  async getConversations(): Promise<Conversation[]> {
    const response = await apiClient.get<ApiResponse<Conversation[]>>(
      '/api/chat/conversations'
    );
    return response.data.result;
  },

  /**
   * Get chat messages with a user
   */
  async getMessages(userId: number, page = 0): Promise<ChatMessageDTO[]> {
    const response = await apiClient.get<ApiResponse<ChatMessageDTO[]>>(
      `/api/chat/private/${userId}`,
      { params: { page } }
    );
    return response.data.result;
  },

  /**
   * Get project chat history
   */
  async getProjectMessages(projectId: number, page = 0, size = 20): Promise<ChatMessageDTO[]> {
    const response = await apiClient.get<ApiResponse<ChatMessageDTO[]>>(
      `/api/chat/project/${projectId}`,
      { params: { page, size } }
    );
    return response.data.result;
  },
};
