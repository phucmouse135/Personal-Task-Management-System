import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { projectService } from '@/services/projectService';
import { userService } from '@/services/userService';
import { useTasks } from '@/hooks/useTasks';
import { useAuthStore } from '@/store/authStore';
import { Project, User } from '@/types';
import toast from 'react-hot-toast';

export const ProjectDetailPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const [project, setProject] = useState<Project | null>(null);
  const [loading, setLoading] = useState(true);
  const [showAddMemberModal, setShowAddMemberModal] = useState(false);
  const [showChangeOwnerModal, setShowChangeOwnerModal] = useState(false);

  // Check if current user is owner
  const isOwner = project?.owner?.id === user?.id;

  // Load tasks for this project
  const { tasks, loading: tasksLoading } = useTasks({
    projectId: Number(id),
    page: 0,
    size: 100,
  });

  // Load project details
  useEffect(() => {
    const loadProject = async () => {
      if (!id) return;
      try {
        setLoading(true);
        const data = await projectService.getProjectById(Number(id));
        setProject(data);
      } catch (error) {
        console.error('Failed to load project:', error);
        toast.error('Failed to load project details');
        navigate('/projects');
      } finally {
        setLoading(false);
      }
    };

    loadProject();
  }, [id, navigate]);

  const handleAddMember = async (userId: number) => {
    if (!project) return;
    try {
      const updated = await projectService.addMember(project.id, userId);
      setProject(updated);
      setShowAddMemberModal(false);
      toast.success('Member added successfully');
    } catch (error) {
      toast.error('Failed to add member');
    }
  };

  const handleRemoveMember = async (userId: number) => {
    if (!project) return;
    if (!window.confirm('Are you sure you want to remove this member?')) return;
    
    try {
      const updated = await projectService.removeMember(project.id, userId);
      setProject(updated);
      toast.success('Member removed successfully');
    } catch (error) {
      toast.error('Failed to remove member');
    }
  };

  const handleChangeOwner = async (newOwnerId: number) => {
    if (!project) return;
    if (!window.confirm('Are you sure you want to change the project owner?')) return;

    try {
      const updated = await projectService.changeOwner(project.id, newOwnerId);
      setProject(updated);
      setShowChangeOwnerModal(false);
      toast.success('Owner changed successfully');
    } catch (error) {
      toast.error('Failed to change owner');
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-gray-600 dark:text-gray-400">Loading project...</div>
      </div>
    );
  }

  if (!project) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-gray-600 dark:text-gray-400">Project not found</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Header */}
        <div className="mb-6">
          <button
            onClick={() => navigate('/projects')}
            className="text-blue-600 hover:text-blue-700 dark:text-blue-400 mb-4 flex items-center gap-2"
          >
            ← Back to Projects
          </button>
          
          <div className="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 p-6">
            <div className="flex justify-between items-start">
              <div className="flex-1">
                <h1 className="text-3xl font-bold text-gray-900 dark:text-white mb-2">
                  {project.name}
                </h1>
                <p className="text-gray-600 dark:text-gray-400 mb-4">
                  {project.description || 'No description'}
                </p>
                
                <div className="grid grid-cols-2 md:grid-cols-3 gap-4 text-sm">
                  <div>
                    <span className="text-gray-500 dark:text-gray-400">Owner:</span>
                    <p className="font-medium text-gray-900 dark:text-white">
                      {project.owner?.username || 'Unknown'}
                    </p>
                  </div>
                  <div>
                    <span className="text-gray-500 dark:text-gray-400">Members:</span>
                    <p className="font-medium text-gray-900 dark:text-white">
                      {project.members?.length || 0}
                    </p>
                  </div>
                  <div>
                    <span className="text-gray-500 dark:text-gray-400">End Date:</span>
                    <p className="font-medium text-gray-900 dark:text-white">
                      {project.endDate ? new Date(project.endDate).toLocaleDateString() : 'N/A'}
                    </p>
                  </div>
                </div>
              </div>

              {project.deletedAt && (
                <span className="px-3 py-1 bg-red-100 dark:bg-red-900/30 text-red-800 dark:text-red-300 rounded-full text-sm">
                  Deleted
                </span>
              )}
            </div>
          </div>
        </div>

        {/* Members Section */}
        <div className="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 p-6 mb-6">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-xl font-semibold text-gray-900 dark:text-white">
              Team Members ({project.members?.length || 0})
            </h2>
            <div className="flex gap-3">
              <button
                onClick={() => setShowChangeOwnerModal(true)}
                disabled={!isOwner}
                className={`px-4 py-2 text-sm rounded-lg ${
                  isOwner 
                    ? 'bg-purple-600 hover:bg-purple-700 text-white' 
                    : 'bg-gray-300 dark:bg-gray-600 text-gray-500 dark:text-gray-400 cursor-not-allowed'
                }`}
                title={!isOwner ? 'Only owner can change owner' : ''}
              >
                Change Owner
              </button>
              <button
                onClick={() => setShowAddMemberModal(true)}
                disabled={!isOwner}
                className={`px-4 py-2 text-sm rounded-lg ${
                  isOwner 
                    ? 'bg-blue-600 hover:bg-blue-700 text-white' 
                    : 'bg-gray-300 dark:bg-gray-600 text-gray-500 dark:text-gray-400 cursor-not-allowed'
                }`}
                title={!isOwner ? 'Only owner can add members' : ''}
              >
                + Add Member
              </button>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {project.members && project.members.length > 0 ? (
              project.members.map((member) => (
                <div
                  key={member.id}
                  className="flex items-center justify-between p-4 border border-gray-200 dark:border-gray-700 rounded-lg"
                >
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 bg-blue-100 dark:bg-blue-900 rounded-full flex items-center justify-center">
                      <span className="text-blue-600 dark:text-blue-300 font-semibold">
                        {member.username?.charAt(0).toUpperCase()}
                      </span>
                    </div>
                    <div>
                      <p className="font-medium text-gray-900 dark:text-white">
                        {member.username}
                      </p>
                      <p className="text-sm text-gray-500 dark:text-gray-400">
                        {member.email}
                      </p>
                    </div>
                  </div>
                  
                  {member.id !== project.owner?.id && (
                    <button
                      onClick={() => handleRemoveMember(member.id)}
                      disabled={!isOwner}
                      className={`text-sm ${
                        isOwner 
                          ? 'text-red-600 hover:text-red-700 dark:text-red-400' 
                          : 'text-gray-400 cursor-not-allowed'
                      }`}
                      title={!isOwner ? 'Only owner can remove members' : ''}
                    >
                      Remove
                    </button>
                  )}
                </div>
              ))
            ) : (
              <p className="text-gray-500 dark:text-gray-400 col-span-3">
                No members yet
              </p>
            )}
          </div>
        </div>

        {/* Tasks Section */}
        <div className="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 p-6">
          <h2 className="text-xl font-semibold text-gray-900 dark:text-white mb-4">
            Project Tasks ({tasks.length})
          </h2>

          {tasksLoading ? (
            <p className="text-gray-500 dark:text-gray-400">Loading tasks...</p>
          ) : tasks.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-gray-200 dark:border-gray-700">
                    <th className="px-4 py-3 text-left text-sm font-medium text-gray-500 dark:text-gray-400">
                      Title
                    </th>
                    <th className="px-4 py-3 text-left text-sm font-medium text-gray-500 dark:text-gray-400">
                      Status
                    </th>
                    <th className="px-4 py-3 text-left text-sm font-medium text-gray-500 dark:text-gray-400">
                      Priority
                    </th>
                    <th className="px-4 py-3 text-left text-sm font-medium text-gray-500 dark:text-gray-400">
                      Deadline
                    </th>
                    <th className="px-4 py-3 text-left text-sm font-medium text-gray-500 dark:text-gray-400">
                      Assignees
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {tasks.map((task) => (
                    <tr
                      key={task.id}
                      className="border-b border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-700 cursor-pointer"
                      onClick={() => navigate(`/tasks/${task.id}`)}
                    >
                      <td className="px-4 py-3 text-sm text-gray-900 dark:text-white">
                        {task.title}
                      </td>
                      <td className="px-4 py-3 text-sm">
                        <span className={`px-2 py-1 rounded-full text-xs ${getStatusColor(task.status)}`}>
                          {task.status}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-sm">
                        <span className={`px-2 py-1 rounded-full text-xs ${getPriorityColor(task.priority)}`}>
                          {task.priority}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-sm text-gray-600 dark:text-gray-400">
                        {new Date(task.deadline).toLocaleDateString()}
                      </td>
                      <td className="px-4 py-3 text-sm text-gray-600 dark:text-gray-400">
                        {task.assignees && task.assignees.length > 0 
                          ? task.assignees.map(a => a.username).join(', ')
                          : 'Unassigned'
                        }
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <p className="text-gray-500 dark:text-gray-400">No tasks yet</p>
          )}
        </div>

        {/* Add Member Modal */}
        {showAddMemberModal && (
          <AddMemberModal
            onClose={() => setShowAddMemberModal(false)}
            onSubmit={handleAddMember}
          />
        )}

        {/* Change Owner Modal */}
        {showChangeOwnerModal && (
          <ChangeOwnerModal
            currentOwner={project.owner}
            members={project.members || []}
            onClose={() => setShowChangeOwnerModal(false)}
            onSubmit={handleChangeOwner}
          />
        )}
      </div>
    </div>
  );
};

// Helper functions
const getStatusColor = (status: string) => {
  switch (status) {
    case 'TODO':
      return 'bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-gray-300';
    case 'IN_PROGRESS':
      return 'bg-blue-100 dark:bg-blue-900 text-blue-800 dark:text-blue-300';
    case 'DONE':
      return 'bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-300';
    default:
      return 'bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-gray-300';
  }
};

const getPriorityColor = (priority: string) => {
  switch (priority) {
    case 'LOW':
      return 'bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-300';
    case 'MEDIUM':
      return 'bg-yellow-100 dark:bg-yellow-900 text-yellow-800 dark:text-yellow-300';
    case 'HIGH':
      return 'bg-red-100 dark:bg-red-900 text-red-800 dark:text-red-300';
    default:
      return 'bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-gray-300';
  }
};

// Add Member Modal Component
const AddMemberModal = ({
  onClose,
  onSubmit,
}: {
  onClose: () => void;
  onSubmit: (userId: number) => void;
}) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [searchResults, setSearchResults] = useState<User[]>([]);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [isSearching, setIsSearching] = useState(false);

  // Search users when searchTerm changes
  useEffect(() => {
    const searchUsers = async () => {
      if (!searchTerm || searchTerm.length < 2) {
        setSearchResults([]);
        return;
      }

      console.log('Searching for users with term:', searchTerm);
      setIsSearching(true);
      try {
        const users = await userService.searchUsers(searchTerm);
        console.log('Search results:', users);
        setSearchResults(users);
      } catch (error) {
        console.error('Failed to search users:', error);
        toast.error('Failed to search users');
        setSearchResults([]);
      } finally {
        setIsSearching(false);
      }
    };

    const timeoutId = setTimeout(searchUsers, 300); // Debounce
    return () => clearTimeout(timeoutId);
  }, [searchTerm]);

  const handleSelectUser = (user: User) => {
    setSelectedUser(user);
    setSearchTerm(user.username);
    setSearchResults([]);
  };

  const handleSubmit = () => {
    if (selectedUser) {
      onSubmit(selectedUser.id);
      setSearchTerm('');
      setSelectedUser(null);
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white dark:bg-gray-800 rounded-lg p-6 max-w-md w-full mx-4">
        <h2 className="text-xl font-bold text-gray-900 dark:text-white mb-4">
          Add Team Member
        </h2>

        <div className="mb-4 relative">
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            Search User
          </label>
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => {
              setSearchTerm(e.target.value);
              setSelectedUser(null);
            }}
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
            placeholder="Type username to search..."
            autoFocus
          />
          
          {/* Search results dropdown */}
          {searchResults.length > 0 && (
            <div className="absolute z-10 w-full mt-1 bg-white dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-lg shadow-lg max-h-60 overflow-y-auto">
              {searchResults.map((user) => (
                <div
                  key={user.id}
                  onClick={() => handleSelectUser(user)}
                  className="px-4 py-3 hover:bg-gray-100 dark:hover:bg-gray-600 cursor-pointer border-b border-gray-200 dark:border-gray-600 last:border-b-0"
                >
                  <div className="font-medium text-gray-900 dark:text-white">
                    {user.username}
                  </div>
                  <div className="text-sm text-gray-500 dark:text-gray-400">
                    {user.email}
                  </div>
                </div>
              ))}
            </div>
          )}

          {/* Loading indicator */}
          {isSearching && (
            <div className="absolute right-3 top-9 text-gray-400">
              <div className="animate-spin h-4 w-4 border-2 border-blue-500 border-t-transparent rounded-full"></div>
            </div>
          )}

          {/* Selected user display */}
          {selectedUser && (
            <div className="mt-2 p-3 bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg">
              <div className="flex items-center justify-between">
                <div>
                  <div className="font-medium text-blue-900 dark:text-blue-300">
                    {selectedUser.username}
                  </div>
                  <div className="text-sm text-blue-600 dark:text-blue-400">
                    {selectedUser.email}
                  </div>
                </div>
                <button
                  onClick={() => {
                    setSelectedUser(null);
                    setSearchTerm('');
                  }}
                  className="text-blue-600 dark:text-blue-400 hover:text-blue-700"
                >
                  ✕
                </button>
              </div>
            </div>
          )}

          <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
            Type at least 2 characters to search for users
          </p>
        </div>

        <div className="flex gap-3">
          <button
            onClick={onClose}
            className="flex-1 px-4 py-2 border border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700"
          >
            Cancel
          </button>
          <button
            onClick={handleSubmit}
            disabled={!selectedUser}
            className="flex-1 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Add Member
          </button>
        </div>
      </div>
    </div>
  );
};

// Change Owner Modal Component
const ChangeOwnerModal = ({
  currentOwner,
  members,
  onClose,
  onSubmit,
}: {
  currentOwner?: User;
  members: User[];
  onClose: () => void;
  onSubmit: (newOwnerId: number) => void;
}) => {
  const [selectedMemberId, setSelectedMemberId] = useState('');

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white dark:bg-gray-800 rounded-lg p-6 max-w-md w-full mx-4">
        <h2 className="text-xl font-bold text-gray-900 dark:text-white mb-4">
          Change Project Owner
        </h2>

        <div className="mb-4">
          <p className="text-sm text-gray-600 dark:text-gray-400 mb-3">
            Current owner: <strong>{currentOwner?.username}</strong>
          </p>

          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            Select New Owner
          </label>
          <select
            value={selectedMemberId}
            onChange={(e) => setSelectedMemberId(e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
          >
            <option value="">Select a member</option>
            {members
              .filter((m) => m.id !== currentOwner?.id)
              .map((member) => (
                <option key={member.id} value={member.id}>
                  {member.username} ({member.email})
                </option>
              ))}
          </select>
        </div>

        <div className="flex gap-3">
          <button
            onClick={onClose}
            className="flex-1 px-4 py-2 border border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700"
          >
            Cancel
          </button>
          <button
            onClick={() => {
              if (selectedMemberId) {
                onSubmit(Number(selectedMemberId));
              }
            }}
            disabled={!selectedMemberId}
            className="flex-1 px-4 py-2 bg-purple-600 hover:bg-purple-700 text-white rounded-lg disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Change Owner
          </button>
        </div>
      </div>
    </div>
  );
};
