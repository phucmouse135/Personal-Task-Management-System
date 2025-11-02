import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useTasks } from '@/hooks/useTasks';
import { useProjects } from '@/hooks/useProjects';
import { projectService } from '@/services/projectService';
import { Task, TaskRequest, TaskStatus, TaskPriority, Project } from '@/types';
import { useAuthStore } from '@/store/authStore';
import { Trash2 } from 'lucide-react';
import toast from 'react-hot-toast';

// Helper function to convert date string to ISO instant format
const dateToInstant = (dateString: string): string => {
  if (!dateString) return '';
  const date = new Date(dateString + 'T00:00:00');
  return date.toISOString();
};

// Helper function to convert ISO instant to date string for input
const instantToDate = (instant: string): string => {
  if (!instant) return '';
  return instant.split('T')[0];
};

// Helper function to check if user is admin
const isAdmin = (roles?: { name: string }[]): boolean => {
  return roles?.some(role => role.name === 'ROLE_ADMIN') || false;
};

export const TasksPage = () => {
  const { user } = useAuthStore();
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<TaskStatus | 'all'>('all');
  const [priorityFilter, setPriorityFilter] = useState<TaskPriority | 'all'>('all');
  const [projectFilter, setProjectFilter] = useState<number | null>(null);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [selectedTask, setSelectedTask] = useState<Task | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [selectedProjectForTask, setSelectedProjectForTask] = useState<Project | null>(null);
  const [selectedAssignees, setSelectedAssignees] = useState<number[]>([]);

  // Backend will automatically filter tasks for normal users via /my-tasks endpoint
  const { tasks, loading, error, createTask, updateTask, updateTaskStatus, deleteTask } = useTasks({
    page: currentPage,
    size: 20,
    status: statusFilter !== 'all' ? statusFilter : undefined,
    priority: priorityFilter !== 'all' ? priorityFilter : undefined,
    projectId: projectFilter || undefined,
  });

  const { projects } = useProjects({ size: 100 });

  // Load project details when project is selected in form
  useEffect(() => {
    const loadProjectDetails = async (projectId: number) => {
      try {
        const project = await projectService.getProjectById(projectId);
        setSelectedProjectForTask(project);
      } catch (error) {
        console.error('Failed to load project details:', error);
      }
    };

    // Will be triggered when user selects a project in the form
    if (selectedProjectForTask?.id) {
      loadProjectDetails(selectedProjectForTask.id);
    }
  }, [selectedProjectForTask?.id]);

  // Load project details when editing a task
  useEffect(() => {
    const loadTaskProject = async () => {
      if (selectedTask?.project?.id) {
        try {
          const project = await projectService.getProjectById(selectedTask.project.id);
          setSelectedProjectForTask(project);
          // Pre-select current assignees if any
          // Note: Backend returns assignee (singular), need to check structure
          setSelectedAssignees([]);
        } catch (error) {
          console.error('Failed to load task project:', error);
        }
      }
    };

    if (selectedTask) {
      loadTaskProject();
    } else {
      setSelectedProjectForTask(null);
      setSelectedAssignees([]);
    }
  }, [selectedTask]);

  const filteredTasks = tasks.filter((task) =>
    task.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
    task.description?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const handleCreateTask = async (data: TaskRequest) => {
    try {
      await createTask(data);
      setShowCreateModal(false);
      setSelectedProjectForTask(null);
      setSelectedAssignees([]);
      toast.success('Task created successfully');
    } catch (error) {
      toast.error('Failed to create task');
    }
  };

  const handleUpdateTask = async (id: number, data: Partial<TaskRequest>) => {
    try {
      await updateTask(id, data);
      setSelectedTask(null);
      setSelectedProjectForTask(null);
      setSelectedAssignees([]);
      toast.success('Task updated successfully');
    } catch (error) {
      toast.error('Failed to update task');
    }
  };

  const handleUpdateTaskStatus = async (id: number, status: string) => {
    try {
      await updateTaskStatus(id, status);
      setSelectedTask(null);
      setSelectedProjectForTask(null);
      setSelectedAssignees([]);
      toast.success('Task status updated successfully');
    } catch (error) {
      toast.error('Failed to update task status');
    }
  };

  const handleDeleteTask = async (id: number) => {
    if (window.confirm('Are you sure you want to delete this task?')) {
      try {
        await deleteTask(id);
        toast.success('Task deleted successfully');
      } catch (error) {
        toast.error('Failed to delete task');
      }
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'TODO': return 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300';
      case 'IN_PROGRESS': return 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-300';
      case 'DONE': return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-300';
      case 'CANCELLED': return 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-300';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'LOW': return 'text-green-600 dark:text-green-400';
      case 'MEDIUM': return 'text-yellow-600 dark:text-yellow-400';
      case 'HIGH': return 'text-orange-600 dark:text-orange-400';
      case 'URGENT': return 'text-red-600 dark:text-red-400';
      default: return 'text-gray-600';
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 dark:text-white">Tasks</h1>
          <p className="mt-2 text-sm text-gray-600 dark:text-gray-400">Manage and track your tasks</p>
        </div>

        <div className="mb-6 space-y-4">
          <div className="flex flex-col lg:flex-row gap-4">
            <div className="relative flex-1">
              <input
                type="text"
                placeholder="Search tasks..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 dark:bg-gray-800 dark:text-white"
              />
            </div>
            <div className="flex gap-2">
              <Link
                to="/tasks/soft-deleted"
                className="inline-flex items-center justify-center px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg"
              >
                <Trash2 className="h-4 w-4 mr-2" />
                Deleted Tasks
              </Link>
              <button
                onClick={() => setShowCreateModal(true)}
                className="inline-flex items-center justify-center px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg"
              >
                + New Task
              </button>
            </div>
          </div>

          <div className="flex flex-wrap gap-3">
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value as TaskStatus | 'all')}
              className="px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg dark:bg-gray-800 dark:text-white"
            >
              <option value="all">All Status</option>
              <option value="TODO">To Do</option>
              <option value="IN_PROGRESS">In Progress</option>
              <option value="DONE">Done</option>
              <option value="CANCELLED">Cancelled</option>
            </select>

            <select
              value={priorityFilter}
              onChange={(e) => setPriorityFilter(e.target.value as TaskPriority | 'all')}
              className="px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg dark:bg-gray-800 dark:text-white"
            >
              <option value="all">All Priority</option>
              <option value="LOW">Low</option>
              <option value="MEDIUM">Medium</option>
              <option value="HIGH">High</option>
              <option value="URGENT">Urgent</option>
            </select>

            <select
              value={projectFilter || ''}
              onChange={(e) => setProjectFilter(e.target.value ? Number(e.target.value) : null)}
              className="px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg dark:bg-gray-800 dark:text-white"
            >
              <option value="">All Projects</option>
              {projects.map((project) => (
                <option key={project.id} value={project.id}>{project.name}</option>
              ))}
            </select>
          </div>
        </div>

        {loading && <div className="text-center py-12">Loading...</div>}
        {error && <div className="bg-red-100 dark:bg-red-900 text-red-800 dark:text-red-200 p-4 rounded">{error}</div>}

        {!loading && !error && filteredTasks.length === 0 && (
          <div className="text-center py-12 bg-white dark:bg-gray-800 rounded-lg">
            <p className="text-gray-500 dark:text-gray-400">No tasks found</p>
          </div>
        )}

        {!loading && !error && filteredTasks.length > 0 && (
          <div className="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 overflow-hidden">
            <table className="min-w-full">
              <thead className="bg-gray-50 dark:bg-gray-900">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase">Task</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase">Status</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase">Priority</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase">Project</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase">Deadline</th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 dark:text-gray-400 uppercase">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200 dark:divide-gray-700">
                {filteredTasks.map((task) => {
                  // Check if current user is the creator (owner of project)
                  const isCreator = task.project?.owner?.id === user?.id;
                  const isAssigned = task.assignees?.some(a => a.id === user?.id);
                  
                  return (
                  <tr key={task.id} className="hover:bg-gray-50 dark:hover:bg-gray-700">
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-2">
                        <div>
                          <div className="text-sm font-medium text-gray-900 dark:text-white">{task.title}</div>
                          {task.description && (
                            <div className="text-sm text-gray-500 dark:text-gray-400 line-clamp-1">{task.description}</div>
                          )}
                        </div>
                        {isCreator && (
                          <span className="px-2 py-0.5 text-xs font-medium bg-purple-100 text-purple-700 dark:bg-purple-900 dark:text-purple-300 rounded">
                            Created by me
                          </span>
                        )}
                        {isAssigned && !isCreator && (
                          <span className="px-2 py-0.5 text-xs font-medium bg-blue-100 text-blue-700 dark:bg-blue-900 dark:text-blue-300 rounded">
                            Assigned
                          </span>
                        )}
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <span className={`px-2 py-1 text-xs rounded-full ${getStatusColor(task.status)}`}>
                        {task.status.replace('_', ' ')}
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      <span className={`text-sm font-medium ${getPriorityColor(task.priority)}`}>{task.priority}</span>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-500 dark:text-gray-400">{task.project?.name || 'N/A'}</td>
                    <td className="px-6 py-4 text-sm text-gray-500 dark:text-gray-400">{new Date(task.deadline).toLocaleDateString()}</td>
                    <td className="px-6 py-4 text-right">
                      <button onClick={() => setSelectedTask(task)} className="text-blue-600 hover:text-blue-900 dark:text-blue-400 mr-3">Edit</button>
                      <button onClick={() => handleDeleteTask(task.id)} className="text-red-600 hover:text-red-900 dark:text-red-400">Delete</button>
                    </td>
                  </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}

        {/* Task Modal */}
        {(showCreateModal || selectedTask) && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
            <div className="bg-white dark:bg-gray-800 rounded-lg max-w-2xl w-full p-6 max-h-[90vh] overflow-y-auto">
              <h2 className="text-xl font-bold text-gray-900 dark:text-white mb-4">
                {selectedTask ? 'Edit Task' : 'Create New Task'}
              </h2>
              <form
                onSubmit={(e) => {
                  e.preventDefault();
                  const formData = new FormData(e.currentTarget);
                  const deadlineValue = formData.get('deadline') as string;
                  const assigneesValue = formData.get('assignees') as string;
                  
                  if (selectedTask) {
                    // Check if user can do full update or just status update
                    const isCreator = selectedTask.project?.owner?.id === user?.id;
                    const isAdminUser = isAdmin(user?.roles);
                    const statusValue = formData.get('status') as string;
                    
                    // If user is just an assignee (not creator, not admin), only allow status update
                    if (!isCreator && !isAdminUser) {
                      handleUpdateTaskStatus(selectedTask.id, statusValue);
                    } else {
                      // Full update for admin and creator
                      const data = {
                        title: formData.get('title') as string,
                        description: formData.get('description') as string,
                        status: statusValue,
                        priority: formData.get('priority'),
                        projectId: Number(formData.get('projectId')),
                        deadline: deadlineValue ? dateToInstant(deadlineValue) : '',
                        assignees: assigneesValue ? assigneesValue.split(',').map(id => Number(id.trim())).filter(id => !isNaN(id)) : [],
                      };
                      handleUpdateTask(selectedTask.id, data as Partial<TaskRequest>);
                    }
                  } else {
                    // Create new task
                    const data = {
                      title: formData.get('title') as string,
                      description: formData.get('description') as string,
                      priority: formData.get('priority'),
                      projectId: Number(formData.get('projectId')),
                      deadline: deadlineValue ? dateToInstant(deadlineValue) : '',
                      assignees: assigneesValue ? assigneesValue.split(',').map(id => Number(id.trim())).filter(id => !isNaN(id)) : [],
                    };
                    handleCreateTask(data as TaskRequest);
                  }
                }}
                className="space-y-4"
              >
                {/* Check permissions for editing */}
                {(() => {
                  const isCreator = selectedTask?.project?.owner?.id === user?.id;
                  const isAdminUser = isAdmin(user?.roles);
                  const canFullEdit = !selectedTask || isCreator || isAdminUser;
                  
                  return (
                    <>
                      {/* Info message for status-only edit */}
                      {selectedTask && !canFullEdit && (
                        <div className="bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg p-3">
                          <p className="text-sm text-blue-800 dark:text-blue-300">
                            ℹ️ You can only update the status of this task. Contact the project owner to make other changes.
                          </p>
                        </div>
                      )}
                      
                      <div>
                        <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                          Title *
                        </label>
                        <input
                          type="text"
                          name="title"
                          required
                          disabled={!canFullEdit}
                          defaultValue={selectedTask?.title}
                          className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white disabled:bg-gray-100 disabled:cursor-not-allowed"
                    placeholder="Enter task title"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    Description
                  </label>
                  <textarea
                    name="description"
                    rows={4}
                    disabled={!canFullEdit}
                    defaultValue={selectedTask?.description}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white disabled:bg-gray-100 disabled:cursor-not-allowed"
                    placeholder="Describe the task..."
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    Priority *
                  </label>
                  <select
                    name="priority"
                    required
                    disabled={!canFullEdit}
                    defaultValue={selectedTask?.priority || 'MEDIUM'}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white disabled:bg-gray-100 disabled:cursor-not-allowed"
                  >
                    <option value="LOW">Low</option>
                    <option value="MEDIUM">Medium</option>
                    <option value="HIGH">High</option>
                    <option value="URGENT">Urgent</option>
                  </select>
                </div>

                {selectedTask && (
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                      Status *
                    </label>
                    <select
                      name="status"
                      required
                      defaultValue={selectedTask?.status || 'TODO'}
                      className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                    >
                      <option value="TODO">To Do</option>
                      <option value="IN_PROGRESS">In Progress</option>
                      <option value="DONE">Done</option>
                      <option value="CANCELLED">Cancelled</option>
                    </select>
                  </div>
                )}

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                      Project *
                    </label>
                    <select
                      name="projectId"
                      required
                      disabled={!canFullEdit}
                      defaultValue={selectedTask?.project?.id}
                      onChange={(e) => {
                        const projectId = Number(e.target.value);
                        const project = projects.find(p => p.id === projectId);
                        if (project) {
                          setSelectedProjectForTask(project);
                          setSelectedAssignees([]);
                        }
                      }}
                      className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white disabled:bg-gray-100 disabled:cursor-not-allowed"
                    >
                      <option value="">Select Project</option>
                      {projects.map((project) => (
                        <option key={project.id} value={project.id}>
                          {project.name}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                      Deadline *
                    </label>
                    <input
                      type="date"
                      name="deadline"
                      required
                      disabled={!canFullEdit}
                      defaultValue={selectedTask?.deadline ? instantToDate(selectedTask.deadline) : ''}
                      className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white disabled:bg-gray-100 disabled:cursor-not-allowed"
                    />
                  </div>
                </div>

                {/* Assignees Section - Show when project is selected */}
                {selectedProjectForTask && selectedProjectForTask.members && selectedProjectForTask.members.length > 0 && (
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                      Assignees (Select team members)
                    </label>
                    <div className="border border-gray-300 dark:border-gray-600 rounded-lg p-3 max-h-48 overflow-y-auto space-y-2">
                      {selectedProjectForTask.members.map((member) => (
                        <label
                          key={member.id}
                          className="flex items-center gap-3 p-2 hover:bg-gray-50 dark:hover:bg-gray-700 rounded cursor-pointer"
                        >
                          <input
                            type="checkbox"
                            disabled={!canFullEdit}
                            checked={selectedAssignees.includes(member.id)}
                            onChange={(e) => {
                              if (e.target.checked) {
                                setSelectedAssignees([...selectedAssignees, member.id]);
                              } else {
                                setSelectedAssignees(selectedAssignees.filter(id => id !== member.id));
                              }
                            }}
                            className="w-4 h-4 text-blue-600 rounded focus:ring-blue-500 disabled:cursor-not-allowed"
                          />
                          <div className="flex items-center gap-2">
                            <div className="w-8 h-8 bg-blue-100 dark:bg-blue-900 rounded-full flex items-center justify-center">
                              <span className="text-blue-600 dark:text-blue-300 text-sm font-semibold">
                                {member.username?.charAt(0).toUpperCase()}
                              </span>
                            </div>
                            <div>
                              <p className="text-sm font-medium text-gray-900 dark:text-white">
                                {member.username}
                              </p>
                              <p className="text-xs text-gray-500 dark:text-gray-400">
                                {member.email}
                              </p>
                            </div>
                          </div>
                        </label>
                      ))}
                    </div>
                    <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                      {selectedAssignees.length} member(s) selected
                    </p>
                    <input type="hidden" name="assignees" value={selectedAssignees.join(',')} />
                  </div>
                )}

                <div className="flex gap-3">
                  <button
                    type="button"
                    onClick={() => {
                      setShowCreateModal(false);
                      setSelectedTask(null);
                    }}
                    className="flex-1 px-4 py-2 border border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    className="flex-1 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg"
                  >
                    {selectedTask ? 'Update Task' : 'Create Task'}
                  </button>
                </div>
                    </>
                  );
                })()}
              </form>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
