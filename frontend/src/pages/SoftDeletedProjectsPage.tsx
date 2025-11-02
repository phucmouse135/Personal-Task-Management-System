import { useState } from 'react';
import { useSoftDeletedProjects } from '@/hooks/useSoftDeletedProjects';
import { useAuthStore } from '@/store/authStore';
import { Trash2, RefreshCw, Calendar, User } from 'lucide-react';

// Helper function to check if user is admin
const isAdmin = (roles?: { name: string }[]): boolean => {
  return roles?.some(role => role.name === 'ROLE_ADMIN') || false;
};

export const SoftDeletedProjectsPage = () => {
  const { user } = useAuthStore();
  const [currentPage, setCurrentPage] = useState(0);

  const { projects, pagination, loading, restoreProject } = useSoftDeletedProjects({
    page: currentPage,
    size: 20,
  });

  const handleRestoreProject = async (id: number, name: string) => {
    if (window.confirm(`Are you sure you want to restore "${name}"?`)) {
      try {
        await restoreProject(id);
      } catch (error) {
        console.error('Failed to restore project:', error);
      }
    }
  };

  const formatDate = (dateString: string | null | undefined) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  if (loading && projects.length === 0) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="mb-6">
        <div className="flex items-center gap-3 mb-2">
          <Trash2 className="w-8 h-8 text-red-600" />
          <h1 className="text-3xl font-bold text-gray-900 dark:text-white">
            Deleted Projects
          </h1>
        </div>
        <p className="text-gray-600 dark:text-gray-400">
          {isAdmin(user?.roles) 
            ? 'All soft-deleted projects (Admin view)' 
            : 'Your soft-deleted projects'}
        </p>
      </div>

      {projects.length === 0 ? (
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow-md p-12 text-center">
          <Trash2 className="w-16 h-16 mx-auto text-gray-400 mb-4" />
          <h3 className="text-xl font-semibold text-gray-900 dark:text-white mb-2">
            No Deleted Projects
          </h3>
          <p className="text-gray-600 dark:text-gray-400">
            You don't have any deleted projects at the moment.
          </p>
        </div>
      ) : (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {projects.map((project) => (
              <div
                key={project.id}
                className="bg-white dark:bg-gray-800 rounded-lg shadow-md hover:shadow-lg transition-shadow p-6 border-l-4 border-red-500"
              >
                <div className="mb-4">
                  <h3 className="text-xl font-bold text-gray-900 dark:text-white mb-2">
                    {project.name}
                  </h3>
                  <p className="text-gray-600 dark:text-gray-400 text-sm line-clamp-2">
                    {project.description || 'No description'}
                  </p>
                </div>

                <div className="space-y-2 text-sm text-gray-600 dark:text-gray-400 mb-4">
                  <div className="flex items-center gap-2">
                    <User className="w-4 h-4" />
                    <span>Owner: {project.owner?.username || 'Unknown'}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <Calendar className="w-4 h-4" />
                    <span>Deleted: {formatDate(project.deletedAt)}</span>
                  </div>
                  {project.endDate && (
                    <div className="flex items-center gap-2">
                      <Calendar className="w-4 h-4" />
                      <span>End Date: {formatDate(project.endDate)}</span>
                    </div>
                  )}
                </div>

                <div className="flex gap-2">
                  <button
                    onClick={() => handleRestoreProject(project.id, project.name)}
                    className="flex-1 flex items-center justify-center gap-2 px-4 py-2 bg-green-600 hover:bg-green-700 text-white rounded-lg transition-colors"
                  >
                    <RefreshCw className="w-4 h-4" />
                    Restore
                  </button>
                </div>
              </div>
            ))}
          </div>

          {/* Pagination */}
          {pagination && pagination.totalPages > 1 && (
            <div className="mt-8 flex justify-center items-center gap-4">
              <button
                onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
                disabled={currentPage === 0}
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed"
              >
                Previous
              </button>
              <span className="text-gray-700 dark:text-gray-300">
                Page {currentPage + 1} of {pagination.totalPages}
              </span>
              <button
                onClick={() => setCurrentPage(Math.min(pagination.totalPages - 1, currentPage + 1))}
                disabled={currentPage >= pagination.totalPages - 1}
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed"
              >
                Next
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
};
