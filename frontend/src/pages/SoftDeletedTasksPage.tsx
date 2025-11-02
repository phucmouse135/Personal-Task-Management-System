import { useState } from 'react';
import { useSoftDeletedTasks } from '@/hooks/useSoftDeletedTasks';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { Trash2, RefreshCw, Calendar, User, Tag } from 'lucide-react';

export const SoftDeletedTasksPage = () => {
  const { tasks, pagination, isLoading, restoreTask, fetchSoftDeletedTasks } = useSoftDeletedTasks();
  const [restoringId, setRestoringId] = useState<number | null>(null);

  const handleRestore = async (id: number) => {
    if (window.confirm('Are you sure you want to restore this task?')) {
      setRestoringId(id);
      try {
        await restoreTask(id);
      } finally {
        setRestoringId(null);
      }
    }
  };

  const handlePageChange = (newPage: number) => {
    fetchSoftDeletedTasks(newPage, pagination.pageSize);
  };

  const getStatusColor = (status: string) => {
    const colors: Record<string, string> = {
      TODO: 'bg-gray-500',
      IN_PROGRESS: 'bg-blue-500',
      DONE: 'bg-green-500',
      CANCELLED: 'bg-red-500',
    };
    return colors[status] || 'bg-gray-500';
  };

  const getPriorityColor = (priority: string) => {
    const colors: Record<string, string> = {
      LOW: 'bg-green-600',
      MEDIUM: 'bg-yellow-600',
      HIGH: 'bg-orange-600',
      CRITICAL: 'bg-red-600',
    };
    return colors[priority] || 'bg-gray-600';
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="container mx-auto py-8 px-4">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 dark:text-white mb-2">
          Deleted Tasks
        </h1>
        <p className="text-gray-600 dark:text-gray-400">
          View and restore soft-deleted tasks
        </p>
      </div>

      {tasks.length === 0 ? (
        <Card>
          <div className="flex flex-col items-center justify-center py-12">
            <Trash2 className="h-16 w-16 text-gray-400 mb-4" />
            <h3 className="text-xl font-semibold text-gray-900 dark:text-white mb-2">
              No deleted tasks
            </h3>
            <p className="text-gray-600 dark:text-gray-400">
              There are no soft-deleted tasks to display.
            </p>
          </div>
        </Card>
      ) : (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {tasks.map((task) => (
              <Card key={task.id} className="border-l-4 border-l-red-500 hover:shadow-lg transition-shadow">
                <div className="p-6">
                  <h3 className="text-lg font-semibold mb-2">{task.title}</h3>
                  <div className="flex gap-2 mb-4">
                    <Badge className={getStatusColor(task.status)}>
                      {task.status.replace('_', ' ')}
                    </Badge>
                    <Badge className={getPriorityColor(task.priority)}>
                      {task.priority}
                    </Badge>
                  </div>

                  <div className="space-y-3">
                    {task.description && (
                      <p className="text-sm text-gray-600 dark:text-gray-400 line-clamp-2">
                        {task.description}
                      </p>
                    )}

                    <div className="flex items-center text-sm text-gray-600 dark:text-gray-400">
                      <User className="h-4 w-4 mr-2" />
                      <span>Project: {task.project?.name || 'N/A'}</span>
                    </div>

                    {task.assignees && task.assignees.length > 0 && (
                      <div className="flex items-center text-sm text-gray-600 dark:text-gray-400">
                        <Tag className="h-4 w-4 mr-2" />
                        <span>
                          Assignees: {task.assignees.map(a => a.username).join(', ')}
                        </span>
                      </div>
                    )}

                    {task.deadline && (
                      <div className="flex items-center text-sm text-gray-600 dark:text-gray-400">
                        <Calendar className="h-4 w-4 mr-2" />
                        <span>Deadline: {new Date(task.deadline).toLocaleDateString()}</span>
                      </div>
                    )}

                    <div className="flex items-center text-sm text-red-600 dark:text-red-400">
                      <Trash2 className="h-4 w-4 mr-2" />
                      <span>Deleted recently</span>
                    </div>

                    <Button
                      onClick={() => handleRestore(task.id)}
                      disabled={restoringId === task.id}
                      className="w-full mt-4"
                    >
                      <RefreshCw className={`h-4 w-4 mr-2 ${restoringId === task.id ? 'animate-spin' : ''}`} />
                      {restoringId === task.id ? 'Restoring...' : 'Restore Task'}
                    </Button>
                  </div>
                </div>
              </Card>
            ))}
          </div>

          {pagination.totalPages > 1 && (
            <div className="flex justify-center items-center gap-2 mt-8">
              <Button
                onClick={() => handlePageChange(pagination.currentPage - 1)}
                disabled={pagination.currentPage === 0}
              >
                Previous
              </Button>
              <span className="text-sm text-gray-600 dark:text-gray-400">
                Page {pagination.currentPage + 1} of {pagination.totalPages}
              </span>
              <Button
                onClick={() => handlePageChange(pagination.currentPage + 1)}
                disabled={pagination.currentPage >= pagination.totalPages - 1}
              >
                Next
              </Button>
            </div>
          )}
        </>
      )}
    </div>
  );
};
