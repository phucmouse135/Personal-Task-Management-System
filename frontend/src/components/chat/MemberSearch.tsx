import React, { useState, useEffect, useCallback } from 'react';
import { Search, User } from 'lucide-react';
import { getChatMembers } from '@/services/chatService';
import { User as UserType } from '@/types';

interface MemberSearchProps {
  onSelectMember: (member: UserType) => void;
}

export const MemberSearch: React.FC<MemberSearchProps> = ({ onSelectMember }) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [members, setMembers] = useState<UserType[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Debounced search
  const fetchMembers = useCallback(async (search: string) => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await getChatMembers(search || undefined);
      setMembers(data);
    } catch (err) {
      setError('Không thể tải danh sách thành viên');
      console.error('Error fetching members:', err);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    const timer = setTimeout(() => {
      fetchMembers(searchTerm);
    }, 300);

    return () => clearTimeout(timer);
  }, [searchTerm, fetchMembers]);

  return (
    <div className="flex flex-col h-full">
      {/* Search Input */}
      <div className="p-4 border-b">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
          <input
            type="text"
            placeholder="Tìm kiếm theo username, email hoặc tên..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-10 pr-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
      </div>

      {/* Members List */}
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

        {!isLoading && !error && members.length === 0 && (
          <div className="text-center py-8 text-gray-500">
            Không tìm thấy thành viên nào
          </div>
        )}

        {!isLoading && !error && members.length > 0 && (
          <div className="divide-y">
            {members.map((member) => (
              <button
                key={member.id}
                onClick={() => onSelectMember(member)}
                className="w-full p-4 hover:bg-gray-50 transition-colors flex items-center gap-3 text-left"
              >
                <div className="w-10 h-10 rounded-full bg-blue-500 flex items-center justify-center text-white font-semibold">
                  {member.username.charAt(0).toUpperCase()}
                </div>
                <div className="flex-1 min-w-0">
                  <div className="font-semibold text-gray-900 truncate">
                    {member.username}
                  </div>
                  <div className="text-sm text-gray-500 truncate">
                    {member.email}
                  </div>
                  {member.fullName && (
                    <div className="text-sm text-gray-600 truncate">
                      {member.fullName}
                    </div>
                  )}
                </div>
                <User className="w-5 h-5 text-gray-400" />
              </button>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
