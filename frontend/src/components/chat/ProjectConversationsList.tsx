import React, { useState, useEffect } from 'react';
import { Users, MessageCircle } from 'lucide-react';
import { getProjectConversations } from '@/services/chatService';
import { ProjectConversation } from '@/types';
import { formatDistanceToNow } from 'date-fns';

interface ProjectConversationsListProps {
  onSelectConversation: (conversation: ProjectConversation) => void;
  selectedProjectId?: number;
}

export const ProjectConversationsList: React.FC<ProjectConversationsListProps> = ({
  onSelectConversation,
  selectedProjectId,
}) => {
  const [conversations, setConversations] = useState<ProjectConversation[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchConversations();
  }, []);

  const fetchConversations = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await getProjectConversations();
      setConversations(data);
    } catch (err) {
      setError('Không thể tải danh sách nhóm chat');
      console.error('Error fetching conversations:', err);
    } finally {
      setIsLoading(false);
    }
  };

  const formatTime = (timestamp?: string) => {
    if (!timestamp) return '';
    try {
      return formatDistanceToNow(new Date(timestamp), {
        addSuffix: true,
      });
    } catch {
      return '';
    }
  };

  return (
    <div className="flex flex-col h-full">
      {/* Header */}
      <div className="p-4 border-b">
        <h3 className="font-semibold text-lg flex items-center gap-2">
          <Users className="w-5 h-5" />
          Nhóm Chat Dự Án
        </h3>
      </div>

      {/* Conversations List */}
      <div className="flex-1 overflow-y-auto">
        {isLoading && (
          <div className="flex items-center justify-center py-8">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
          </div>
        )}

        {error && (
          <div className="text-center py-8 text-red-500">
            {error}
          </div>
        )}

        {!isLoading && !error && conversations.length === 0 && (
          <div className="text-center py-8 text-gray-500">
            <MessageCircle className="w-12 h-12 mx-auto mb-2 text-gray-400" />
            <p>Chưa có nhóm chat nào</p>
          </div>
        )}

        {!isLoading && !error && conversations.length > 0 && (
          <div className="divide-y">
            {conversations.map((conversation) => (
              <button
                key={conversation.projectId}
                onClick={() => onSelectConversation(conversation)}
                className={`w-full p-4 hover:bg-gray-50 transition-colors text-left ${
                  selectedProjectId === conversation.projectId ? 'bg-blue-50 border-l-4 border-blue-500' : ''
                }`}
              >
                <div className="flex items-start gap-3">
                  {/* Project Icon */}
                  <div className="w-12 h-12 rounded-lg bg-gradient-to-br from-blue-500 to-purple-500 flex items-center justify-center text-white font-bold text-lg flex-shrink-0">
                    {conversation.projectName.charAt(0).toUpperCase()}
                  </div>

                  {/* Content */}
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between mb-1">
                      <h4 className="font-semibold text-gray-900 truncate">
                        {conversation.projectName}
                      </h4>
                      {conversation.lastMessageTime && (
                        <span className="text-xs text-gray-500 ml-2 flex-shrink-0">
                          {formatTime(conversation.lastMessageTime)}
                        </span>
                      )}
                    </div>

                    <div className="flex items-center gap-2 text-sm text-gray-600 mb-1">
                      <Users className="w-4 h-4" />
                      <span>{conversation.memberCount} thành viên</span>
                    </div>

                    {conversation.lastMessage && (
                      <p className="text-sm text-gray-500 truncate">
                        {conversation.lastMessage}
                      </p>
                    )}

                    {!conversation.lastMessage && (
                      <p className="text-sm text-gray-400 italic">
                        Chưa có tin nhắn nào
                      </p>
                    )}
                  </div>
                </div>
              </button>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
