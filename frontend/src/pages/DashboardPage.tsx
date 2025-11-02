import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';
import { useTasks } from '@/hooks/useTasks';
import { useProjects } from '@/hooks/useProjects';
import { TaskStatus, TaskPriority } from '@/types';
import { 
  CheckSquare, 
  FolderKanban, 
  AlertCircle, 
  CheckCircle,
  Users
} from 'lucide-react';

export const DashboardPage = () => {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const [taskFilter, setTaskFilter] = useState<'all' | 'assigned' | 'overdue'>('all');
  
  // Backend will automatically filter tasks/projects for normal users via /my-tasks and /my-projects endpoints
  const { tasks, loading: tasksLoading } = useTasks({ 
    page: 0, 
    size: 20,
  });
  
  const { projects, loading: projectsLoading } = useProjects({ 
    page: 0, 
    size: 10,
  });

  // Calculate statistics (no need to filter by user - backend already does this for normal users)
  const todoTasks = tasks.filter(t => t.status === TaskStatus.TODO);
  const inProgressTasks = tasks.filter(t => t.status === TaskStatus.IN_PROGRESS);
  const completedTasks = tasks.filter(t => t.status === TaskStatus.DONE);
  const overdueTasks = tasks.filter(t => t.overdue);

  // Filter tasks for display
  const filteredTasks = taskFilter === 'overdue'
    ? overdueTasks
    : tasks;

  const getStatusColor = (status: TaskStatus) => {
    switch (status) {
      case TaskStatus.TODO:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300';
      case TaskStatus.IN_PROGRESS:
        return 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-300';
      case TaskStatus.DONE:
        return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-300';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const getPriorityColor = (priority: TaskPriority) => {
    switch (priority) {
      case TaskPriority.LOW:
        return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-300';
      case TaskPriority.MEDIUM:
        return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-300';
      case TaskPriority.HIGH:
      case TaskPriority.URGENT:
        return 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-300';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  if (tasksLoading || projectsLoading) {
    return (
      <div className="flex items-center justify-center h-96">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
      {/* Welcome Header */}
      <div>
        <h1 className="text-3xl font-bold text-gray-900 dark:text-white">
          Welcome back, {user?.username}! 👋
        </h1>
        <p className="text-gray-600 dark:text-gray-400 mt-1">
          Here's an overview of your tasks and projects
        </p>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {/* My Tasks */}
        <div className="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-600 dark:text-gray-400">My Tasks</p>
              <p className="text-2xl font-bold text-gray-900 dark:text-white">{tasks.length}</p>
              <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                {todoTasks.length} to do · {inProgressTasks.length} in progress
              </p>
            </div>
            <CheckSquare className="h-8 w-8 text-blue-500" />
          </div>
        </div>

        {/* My Projects */}
        <div className="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-600 dark:text-gray-400">My Projects</p>
              <p className="text-2xl font-bold text-gray-900 dark:text-white">{projects.length}</p>
              <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                Active projects
              </p>
            </div>
            <FolderKanban className="h-8 w-8 text-purple-500" />
          </div>
        </div>

        {/* Overdue Tasks */}
        <div className="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Overdue</p>
              <p className="text-2xl font-bold text-red-600 dark:text-red-400">{overdueTasks.length}</p>
              <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                Need attention
              </p>
            </div>
            <AlertCircle className="h-8 w-8 text-red-500" />
          </div>
        </div>

        {/* Completed */}
        <div className="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Completed</p>
              <p className="text-2xl font-bold text-green-600 dark:text-green-400">{completedTasks.length}</p>
              <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                Tasks done
              </p>
            </div>
            <CheckCircle className="h-8 w-8 text-green-500" />
          </div>
        </div>
      </div>

      {/* Quick Actions & Priority Tasks */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Priority Tasks */}
        <div className="lg:col-span-2">
          <div className="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700">
            <div className="p-6 border-b border-gray-200 dark:border-gray-700">
              <div className="flex items-center justify-between">
                <h2 className="text-lg font-semibold text-gray-900 dark:text-white">
                  My Tasks
                </h2>
                <div className="flex gap-2">
                  <button
                    onClick={() => setTaskFilter('all')}
                    className={`px-3 py-1 text-sm rounded-lg ${
                      taskFilter === 'all'
                        ? 'bg-blue-100 text-blue-700 dark:bg-blue-900 dark:text-blue-300'
                        : 'text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700'
                    }`}
                  >
                    All
                  </button>
                  <button
                    onClick={() => setTaskFilter('overdue')}
                    className={`px-3 py-1 text-sm rounded-lg ${
                      taskFilter === 'overdue'
                        ? 'bg-red-100 text-red-700 dark:bg-red-900 dark:text-red-300'
                        : 'text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700'
                    }`}
                  >
                    Overdue
                  </button>
                </div>
              </div>
            </div>

            <div className="p-6">
              {filteredTasks.length > 0 ? (
                <div className="space-y-3">
                  {filteredTasks.slice(0, 8).map((task) => {
                    // Check if current user is the creator (owner of project)
                    const isCreator = task.project?.owner?.id === user?.id;
                    const isAssigned = task.assignees?.some(a => a.id === user?.id);
                    
                    return (
                    <div
                      key={task.id}
                      onClick={() => navigate(`/tasks/${task.id}`)}
                      className="flex items-center justify-between p-4 bg-gray-50 dark:bg-gray-700 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-600 cursor-pointer transition-colors"
                    >
                      <div className="flex-1">
                        <div className="flex items-center gap-2">
                          <h3 className="font-medium text-gray-900 dark:text-white">
                            {task.title}
                          </h3>
                          {isCreator && (
                            <span className="px-2 py-0.5 text-xs bg-purple-100 dark:bg-purple-900 text-purple-700 dark:text-purple-300 rounded">
                              Created
                            </span>
                          )}
                          {isAssigned && !isCreator && (
                            <span className="px-2 py-0.5 text-xs bg-blue-100 dark:bg-blue-900 text-blue-700 dark:text-blue-300 rounded">
                              Assigned
                            </span>
                          )}
                          {task.overdue && (
                            <span className="px-2 py-0.5 text-xs bg-red-100 dark:bg-red-900 text-red-800 dark:text-red-200 rounded">
                              Overdue
                            </span>
                          )}
                        </div>
                        <div className="flex items-center gap-3 mt-1 text-sm text-gray-600 dark:text-gray-400">
                          <span>{task.project?.name || 'No project'}</span>
                          <span>•</span>
                          <span>{new Date(task.deadline).toLocaleDateString()}</span>
                          {task.assignees && task.assignees.length > 0 && (
                            <>
                              <span>•</span>
                              <span>{task.assignees.length} assignee(s)</span>
                            </>
                          )}
                        </div>
                      </div>
                      <div className="flex items-center gap-2">
                        <span className={`px-2 py-1 rounded-full text-xs ${getPriorityColor(task.priority)}`}>
                          {task.priority}
                        </span>
                        <span className={`px-2 py-1 rounded-full text-xs ${getStatusColor(task.status)}`}>
                          {task.status}
                        </span>
                      </div>
                    </div>
                    );
                  })}
                </div>
              ) : (
                <div className="text-center py-12">
                  <CheckSquare className="h-12 w-12 text-gray-400 mx-auto mb-4" />
                  <p className="text-gray-600 dark:text-gray-400">No tasks found</p>
                  <p className="text-sm text-gray-500 dark:text-gray-500 mt-1">
                    Try changing the filter or create a new task
                  </p>
                </div>
              )}

              {filteredTasks.length > 8 && (
                <button
                  onClick={() => navigate('/tasks')}
                  className="w-full mt-4 px-4 py-2 text-sm text-blue-600 dark:text-blue-400 hover:bg-blue-50 dark:hover:bg-blue-900/20 rounded-lg transition-colors"
                >
                  View all tasks ({filteredTasks.length})
                </button>
              )}
            </div>
          </div>
        </div>

        {/* Quick Actions & Recent Projects */}
        <div className="space-y-6">
          {/* Quick Actions */}
          <div className="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 p-6">
            <h2 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
              Quick Actions
            </h2>
            <div className="space-y-3">
              <button
                onClick={() => navigate('/tasks')}
                className="w-full px-4 py-3 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-sm font-medium transition-colors flex items-center justify-center gap-2"
              >
                <CheckSquare className="h-4 w-4" />
                View My Tasks
              </button>
              <button
                onClick={() => navigate('/projects')}
                className="w-full px-4 py-3 bg-purple-600 hover:bg-purple-700 text-white rounded-lg text-sm font-medium transition-colors flex items-center justify-center gap-2"
              >
                <FolderKanban className="h-4 w-4" />
                Browse Projects
              </button>
              <button
                onClick={() => navigate('/chat')}
                className="w-full px-4 py-3 bg-green-600 hover:bg-green-700 text-white rounded-lg text-sm font-medium transition-colors flex items-center justify-center gap-2"
              >
                <Users className="h-4 w-4" />
                Team Chat
              </button>
            </div>
          </div>

          {/* Recent Projects */}
          <div className="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 p-6">
            <h2 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
              My Projects
            </h2>
            {projects.length > 0 ? (
              <div className="space-y-3">
                {projects.slice(0, 5).map((project) => (
                  <div
                    key={project.id}
                    onClick={() => navigate(`/projects/${project.id}`)}
                    className="p-3 bg-gray-50 dark:bg-gray-700 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-600 cursor-pointer transition-colors"
                  >
                    <h3 className="font-medium text-gray-900 dark:text-white text-sm">
                      {project.name}
                    </h3>
                    <p className="text-xs text-gray-600 dark:text-gray-400 mt-1 line-clamp-1">
                      {project.description || 'No description'}
                    </p>
                    <div className="flex items-center gap-2 mt-2 text-xs text-gray-500 dark:text-gray-400">
                      <Users className="h-3 w-3" />
                      <span>{project.members?.length || 0} members</span>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="text-center py-8">
                <FolderKanban className="h-8 w-8 text-gray-400 mx-auto mb-2" />
                <p className="text-sm text-gray-600 dark:text-gray-400">No projects yet</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
