import { useState, useEffect, useRef } from 'react';
import { MessageSquare, Users, Search as SearchIcon } from 'lucide-react';
import { chatService, Conversation, ChatMessageDTO } from '@/services/chatApiService';
import { useAuthStore } from '@/store/authStore';
import { MemberSearch } from '@/components/chat/MemberSearch';
import { ProjectConversationsList } from '@/components/chat/ProjectConversationsList';
import { User, ProjectConversation } from '@/types';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { WS_BASE_URL, STORAGE_KEYS } from '@/constants';

type TabType = 'private' | 'groups' | 'search';

export const ChatPage = () => {
  const [activeTab, setActiveTab] = useState<TabType>('private');
  const [selectedUser, setSelectedUser] = useState<number | null>(null);
  const [selectedProjectId, setSelectedProjectId] = useState<number | null>(null);
  const [message, setMessage] = useState('');
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [messages, setMessages] = useState<ChatMessageDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const { user } = useAuthStore();

  // Load conversations on mount
  useEffect(() => {
    const loadConversations = async () => {
      try {
        const data = await chatService.getConversations();
        setConversations(data);
      } catch (error) {
        console.error('Failed to load conversations:', error);
        // Set empty array on error to prevent blocking
        setConversations([]);
      } finally {
        setLoading(false);
      }
    };

    loadConversations();
  }, []);

  // Load messages when user is selected
  useEffect(() => {
    if (selectedUser) {
      const loadMessages = async () => {
        try {
          const data = await chatService.getMessages(selectedUser);
          setMessages(data);
        } catch (error) {
          console.error('Failed to load messages:', error);
        }
      };

      loadMessages();
    } else {
      setMessages([]);
    }
  }, [selectedUser]);

  const handleSendMessage = (e: React.FormEvent) => {
    e.preventDefault();
    if (!message.trim()) return;
    // TODO: Implement send message with WebSocket
    console.log('Send message:', message);
    setMessage('');
  };

  const handleSelectMember = (member: User) => {
    setSelectedUser(member.id);
    setSelectedProjectId(null);
    setActiveTab('private');
  };

  const handleSelectConversation = (conversation: ProjectConversation) => {
    setSelectedProjectId(conversation.projectId);
    setSelectedUser(null);
    setActiveTab('groups');
  };

  const getTimeAgo = (dateString?: string) => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    
    if (diffMins < 1) return 'just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    const diffHours = Math.floor(diffMins / 60);
    if (diffHours < 24) return `${diffHours}h ago`;
    return date.toLocaleDateString();
  };

  if (loading) {
    return (
      <div className="h-screen bg-gray-50 dark:bg-gray-900 flex items-center justify-center">
        <div className="text-gray-600 dark:text-gray-400">Loading conversations...</div>
      </div>
    );
  }

  return (
    <div className="h-screen bg-gray-50 dark:bg-gray-900 flex">
      {/* Sidebar - Users List with Tabs */}
      <div className="w-80 bg-white dark:bg-gray-800 border-r border-gray-200 dark:border-gray-700 flex flex-col">
        {/* Header with Tabs */}
        <div className="p-4 border-b border-gray-200 dark:border-gray-700">
          <h1 className="text-xl font-bold text-gray-900 dark:text-white mb-4">Messages</h1>
          
          {/* Tab Buttons */}
          <div className="flex gap-1 bg-gray-100 dark:bg-gray-700 rounded-lg p-1">
            <button
              onClick={() => setActiveTab('private')}
              className={`flex-1 px-3 py-2 rounded-md text-sm font-medium transition-colors flex items-center justify-center gap-2 ${
                activeTab === 'private'
                  ? 'bg-white dark:bg-gray-600 text-blue-600 dark:text-blue-400 shadow'
                  : 'text-gray-600 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white'
              }`}
            >
              <MessageSquare className="w-4 h-4" />
              <span>Riêng tư</span>
            </button>
            <button
              onClick={() => setActiveTab('groups')}
              className={`flex-1 px-3 py-2 rounded-md text-sm font-medium transition-colors flex items-center justify-center gap-2 ${
                activeTab === 'groups'
                  ? 'bg-white dark:bg-gray-600 text-blue-600 dark:text-blue-400 shadow'
                  : 'text-gray-600 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white'
              }`}
            >
              <Users className="w-4 h-4" />
              <span>Nhóm</span>
            </button>
            <button
              onClick={() => setActiveTab('search')}
              className={`flex-1 px-3 py-2 rounded-md text-sm font-medium transition-colors flex items-center justify-center gap-2 ${
                activeTab === 'search'
                  ? 'bg-white dark:bg-gray-600 text-blue-600 dark:text-blue-400 shadow'
                  : 'text-gray-600 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white'
              }`}
            >
              <SearchIcon className="w-4 h-4" />
              <span>Tìm kiếm</span>
            </button>
          </div>
        </div>

        {/* Tab Content */}
        <div className="flex-1 overflow-hidden">
          {activeTab === 'private' && (
            <div className="h-full overflow-y-auto">
              {conversations.map((conv) => (
                <div
                  key={conv.userId}
                  onClick={() => setSelectedUser(conv.userId)}
                  className={`p-4 border-b border-gray-200 dark:border-gray-700 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-700 ${
                    selectedUser === conv.userId ? 'bg-blue-50 dark:bg-blue-900/20' : ''
                  }`}
                >
                  <div className="flex items-center gap-3">
                    <div className="relative">
                      <div className="text-3xl">👤</div>
                      {conv.online && (
                        <div className="absolute bottom-0 right-0 w-3 h-3 bg-green-500 rounded-full border-2 border-white dark:border-gray-800"></div>
                      )}
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex justify-between items-baseline">
                        <h3 className="text-sm font-medium text-gray-900 dark:text-white truncate">{conv.name}</h3>
                        <span className="text-xs text-gray-500 dark:text-gray-400">{getTimeAgo(conv.lastMessageTime)}</span>
                      </div>
                      <p className="text-sm text-gray-600 dark:text-gray-400 truncate">{conv.lastMessage || 'No messages yet'}</p>
                    </div>
                  </div>
                </div>
              ))}
              {conversations.length === 0 && (
                <div className="p-8 text-center text-gray-500 dark:text-gray-400">
                  No conversations yet
                </div>
              )}
            </div>
          )}

          {activeTab === 'groups' && (
            <ProjectConversationsList
              onSelectConversation={handleSelectConversation}
              selectedProjectId={selectedProjectId || undefined}
            />
          )}

          {activeTab === 'search' && (
            <MemberSearch onSelectMember={handleSelectMember} />
          )}
        </div>
      </div>

      {/* Main Chat Area */}
      <div className="flex-1 flex flex-col">
        {selectedUser ? (
          <>
            {/* Chat Header */}
            <div className="h-16 bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 flex items-center px-6">
              <div className="flex items-center gap-3">
                <div className="text-2xl">👤</div>
                <div>
                  <h2 className="text-sm font-medium text-gray-900 dark:text-white">
                    {conversations.find((c) => c.userId === selectedUser)?.name}
                  </h2>
                  <p className="text-xs text-gray-500 dark:text-gray-400">
                    {conversations.find((c) => c.userId === selectedUser)?.online ? 'Online' : 'Offline'}
                  </p>
                </div>
              </div>
            </div>

            {/* Messages */}
            <div className="flex-1 overflow-y-auto p-6 space-y-4 bg-gray-50 dark:bg-gray-900">
              {messages.map((msg) => {
                const isMe = msg.senderId === user?.id;
                const messageTime = new Date(msg.createdAt).toLocaleTimeString('en-US', { 
                  hour: '2-digit', 
                  minute: '2-digit' 
                });
                
                return (
                  <div
                    key={msg.id}
                    className={`flex ${isMe ? 'justify-end' : 'justify-start'}`}
                  >
                    <div className={`max-w-xs lg:max-w-md ${isMe ? 'order-2' : 'order-1'}`}>
                      <div className={`px-4 py-2 rounded-lg ${
                        isMe
                          ? 'bg-blue-600 text-white'
                          : 'bg-white dark:bg-gray-800 text-gray-900 dark:text-white border border-gray-200 dark:border-gray-700'
                      }`}>
                        <p className="text-sm">{msg.content}</p>
                      </div>
                      <p className={`text-xs text-gray-500 dark:text-gray-400 mt-1 ${isMe ? 'text-right' : 'text-left'}`}>
                        {messageTime}
                      </p>
                    </div>
                  </div>
                );
              })}
            </div>

            {/* Message Input */}
            <div className="bg-white dark:bg-gray-800 border-t border-gray-200 dark:border-gray-700 p-4">
              <form onSubmit={handleSendMessage} className="flex gap-2">
                <input
                  type="text"
                  value={message}
                  onChange={(e) => setMessage(e.target.value)}
                  placeholder="Type a message..."
                  className="flex-1 px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                />
                <button
                  type="submit"
                  disabled={!message.trim()}
                  className="px-6 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  Send
                </button>
              </form>
            </div>
          </>
        ) : (
          <div className="flex-1 flex items-center justify-center bg-gray-50 dark:bg-gray-900">
            <div className="text-center">
              <div className="text-6xl mb-4">💬</div>
              <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-2">Select a conversation</h3>
              <p className="text-sm text-gray-600 dark:text-gray-400">
                Choose a conversation from the sidebar to start messaging
              </p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
