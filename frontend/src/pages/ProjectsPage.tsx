import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useProjects } from '@/hooks/useProjects';
import { useAuthStore } from '@/store/authStore';
import { Project, ProjectRequest } from '@/types';
import toast from 'react-hot-toast';
import { Trash2 } from 'lucide-react';

// Helper function to convert date string to ISO instant format
const dateToInstant = (dateString: string): string => {
  if (!dateString) return '';
  // Create date at start of day in local timezone, then convert to ISO string
  const date = new Date(dateString + 'T00:00:00');
  return date.toISOString();
};

// Helper function to convert ISO instant to date string for input
const instantToDate = (instant: string): string => {
  if (!instant) return '';
  return instant.split('T')[0];
};

export const ProjectsPage = () => {
  const { user } = useAuthStore();
  const navigate = useNavigate();
  const [searchTerm, setSearchTerm] = useState('');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [selectedProject, setSelectedProject] = useState<Project | null>(null);
  const [currentPage, setCurrentPage] = useState(0);

  // Backend will automatically filter projects for normal users via /my-projects endpoint
  const { projects, pagination, loading, error, createProject, updateProject, deleteProject, restoreProject } = useProjects({
    page: currentPage,
    size: 12,
  });

  // Client-side search filter (only for search term)
  const filteredProjects = projects.filter((project) => {
    const matchesSearch = project.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         project.description?.toLowerCase().includes(searchTerm.toLowerCase());
    return matchesSearch;
  });

  const handleCreateProject = async (data: Omit<ProjectRequest, 'ownerId'>) => {
    try {
      await createProject({
        ...data,
        ownerId: user?.id || 1,
      });
      setShowCreateModal(false);
      toast.success('Project created successfully');
    } catch (error) {
      toast.error('Failed to create project');
    }
  };

  const handleUpdateProject = async (id: number, data: Partial<ProjectRequest>) => {
    try {
      await updateProject(id, data);
      setSelectedProject(null);
      toast.success('Project updated successfully');
    } catch (error) {
      toast.error('Failed to update project');
    }
  };

  const handleDeleteProject = async (id: number) => {
    if (window.confirm('Are you sure you want to delete this project?')) {
      try {
        await deleteProject(id);
        toast.success('Project deleted successfully');
      } catch (error) {
        toast.error('Failed to delete project');
      }
    }
  };

  const handleRestoreProject = async (id: number) => {
    if (window.confirm('Are you sure you want to restore this project?')) {
      try {
        await restoreProject(id);
        toast.success('Project restored successfully');
      } catch (error) {
        toast.error('Failed to restore project');
      }
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Header */}
        <div className="mb-8 flex items-start justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-900 dark:text-white">Projects</h1>
            <p className="mt-2 text-sm text-gray-600 dark:text-gray-400">
              Manage and organize your projects
            </p>
          </div>
          <Link
            to="/projects/soft-deleted"
            className="flex items-center gap-2 px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 dark:bg-gray-800 dark:text-gray-300 dark:border-gray-600 dark:hover:bg-gray-700"
          >
            <Trash2 className="w-4 h-4" />
            Deleted Projects
          </Link>
        </div>

        {/* Toolbar */}
        <div className="mb-6 flex flex-col sm:flex-row gap-4 items-start sm:items-center justify-between">
          {/* Search */}
          <div className="relative flex-1 max-w-md">
            <svg className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
            <input
              type="text"
              placeholder="Search projects..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-10 pr-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 dark:bg-gray-800 dark:text-white"
            />
          </div>

          {/* Actions */}
          <div className="flex gap-3">
            <button
              onClick={() => setShowCreateModal(true)}
              className="inline-flex items-center px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors"
            >
              <svg className="h-5 w-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
              </svg>
              New Project
            </button>
          </div>
        </div>

        {/* Loading State */}
        {loading && (
          <div className="text-center py-12">
            <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
            <p className="mt-4 text-gray-600 dark:text-gray-400">Loading projects...</p>
          </div>
        )}

        {/* Error State */}
        {error && (
          <div className="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg p-4 mb-6">
            <p className="text-red-800 dark:text-red-400">{error}</p>
          </div>
        )}

        {/* Projects Grid */}
        {!loading && !error && (
          <>
            {filteredProjects.length === 0 ? (
              <div className="text-center py-12 bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700">
                <p className="text-gray-500 dark:text-gray-400">No projects found</p>
                <button
                  onClick={() => setShowCreateModal(true)}
                  className="mt-4 text-blue-600 hover:text-blue-700 dark:text-blue-400"
                >
                  Create your first project
                </button>
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {filteredProjects.map((project) => {
                  // User can only edit/delete projects they own
                  const canEdit = project.owner?.id === user?.id;
                  
                  return (
                  <ProjectCard
                    key={project.id}
                    project={project}
                    onEdit={() => setSelectedProject(project)}
                    onDelete={() => handleDeleteProject(project.id)}
                    onRestore={() => handleRestoreProject(project.id)}
                    onClick={() => navigate(`/projects/${project.id}`)}
                    canEdit={canEdit}
                  />
                  );
                })}
              </div>
            )}

            {/* Pagination */}
            {pagination && pagination.totalPages > 1 && (
              <div className="mt-8 flex justify-center gap-2">
                <button
                  onClick={() => setCurrentPage(currentPage - 1)}
                  disabled={currentPage === 0}
                  className="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50 dark:hover:bg-gray-800 dark:text-white"
                >
                  Previous
                </button>
                <span className="px-4 py-2 text-gray-600 dark:text-gray-400">
                  Page {currentPage + 1} of {pagination.totalPages}
                </span>
                <button
                  onClick={() => setCurrentPage(currentPage + 1)}
                  disabled={pagination.last}
                  className="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50 dark:hover:bg-gray-800 dark:text-white"
                >
                  Next
                </button>
              </div>
            )}
          </>
        )}
      </div>

      {/* Create/Edit Modal */}
      {(showCreateModal || selectedProject) && (
        <ProjectModal
          project={selectedProject}
          onClose={() => {
            setShowCreateModal(false);
            setSelectedProject(null);
          }}
          onSubmit={(data) => {
            if (selectedProject) {
              handleUpdateProject(selectedProject.id, data);
            } else {
              handleCreateProject(data);
            }
          }}
        />
      )}
    </div>
  );
};

// Project Card Component
const ProjectCard = ({ project, onEdit, onDelete, onRestore, onClick, canEdit }: {
  project: Project;
  onEdit: () => void;
  onDelete: () => void;
  onRestore: () => void;
  onClick: () => void;
  canEdit: boolean;
}) => {
  const isDeleted = !!project.deletedAt;
  
  return (
    <div 
      onClick={onClick}
      className={`bg-white dark:bg-gray-800 rounded-lg border p-6 hover:shadow-lg transition-shadow cursor-pointer ${
        isDeleted ? 'border-red-300 dark:border-red-700 opacity-75' : 'border-gray-200 dark:border-gray-700'
      }`}
    >
      <div className="flex justify-between items-start mb-4">
        <div className="flex-1">
          <div className="flex items-center gap-2 mb-1">
            <h3 className="text-lg font-semibold text-gray-900 dark:text-white">{project.name}</h3>
            {isDeleted && (
              <span className="px-2 py-1 text-xs font-medium bg-red-100 dark:bg-red-900/30 text-red-600 dark:text-red-400 rounded">
                Deleted
              </span>
            )}
          </div>
        </div>
        <div className="flex gap-2">
          {isDeleted ? (
            <button
              onClick={(e) => {
                e.stopPropagation();
                onRestore();
              }}
              className="text-green-600 hover:text-green-700 dark:text-green-400 text-sm font-medium"
            >
              Restore
            </button>
          ) : (
            <>
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  onEdit();
                }}
                disabled={!canEdit}
                className={`text-sm ${canEdit ? 'text-blue-600 hover:text-blue-700 dark:text-blue-400' : 'text-gray-400 cursor-not-allowed'}`}
                title={!canEdit ? 'Only owner can edit' : ''}
              >
                Edit
              </button>
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  onDelete();
                }}
                disabled={!canEdit}
                className={`text-sm ${canEdit ? 'text-red-600 hover:text-red-700 dark:text-red-400' : 'text-gray-400 cursor-not-allowed'}`}
                title={!canEdit ? 'Only owner can delete' : ''}
              >
                Delete
              </button>
            </>
          )}
        </div>
      </div>
      
      <p className="text-gray-600 dark:text-gray-400 text-sm mb-4 line-clamp-2">
        {project.description || 'No description'}
      </p>

      <div className="space-y-2">
        <div className="flex items-center justify-between text-sm">
          <span className="text-gray-500 dark:text-gray-400">
            Owner: {project.owner?.username || project.owner?.email || 'Unknown'}
          </span>
        </div>
        
        {project.endDate && (
          <div className="flex items-center justify-between text-sm">
            <span className="text-gray-500 dark:text-gray-400">
              End: {new Date(project.endDate).toLocaleDateString()}
            </span>
          </div>
        )}

        {isDeleted && (
          <div className="text-xs text-red-500 dark:text-red-400 mt-2">
            Deleted: {new Date(project.deletedAt!).toLocaleString()}
          </div>
        )}
      </div>
    </div>
  );
};

// Project Modal Component
const ProjectModal = ({ project, onClose, onSubmit }: {
  project: Project | null;
  onClose: () => void;
  onSubmit: (data: ProjectRequest) => void;
}) => {
  const [formData, setFormData] = useState({
    name: project?.name || '',
    description: project?.description || '',
    endDate: project?.endDate ? instantToDate(project.endDate) : '',
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSubmit({
      name: formData.name,
      description: formData.description,
      endDate: formData.endDate ? dateToInstant(formData.endDate) : '',
    });
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white dark:bg-gray-800 rounded-lg p-6 max-w-md w-full mx-4">
        <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-4">
          {project ? 'Edit Project' : 'Create Project'}
        </h2>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              Name
            </label>
            <input
              type="text"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              Description
            </label>
            <textarea
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
              rows={3}
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              End Date
            </label>
            <input
              type="date"
              value={formData.endDate}
              onChange={(e) => setFormData({ ...formData, endDate: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
            />
          </div>

          <div className="flex gap-3 pt-4">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 px-4 py-2 border border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="flex-1 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg"
            >
              {project ? 'Update' : 'Create'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
