import { useEffect } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuthStore } from './store/authStore';
import { Loading } from './components/ui/Loading';
import { ProtectedRoute } from './components/auth/ProtectedRoute';

// Layouts
import { AuthLayout } from './components/layout/AuthLayout';
import { DashboardLayout } from './components/layout/DashboardLayout';

// Pages
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { DashboardPage } from './pages/DashboardPage';
import { ProjectsPage } from './pages/ProjectsPage';
import { ProjectDetailPage } from './pages/ProjectDetailPage';
import { TasksPage } from './pages/TasksPage';
import { TaskDetailPage } from './pages/TaskDetailPage';
import { UsersPage } from './pages/UsersPage';
import { NotificationsPage } from './pages/NotificationsPage';
import { ChatPage } from './pages/ChatPage';
import { PaymentsPage } from './pages/PaymentsPage';
import { PaymentCallbackPage } from './pages/PaymentCallbackPage';
import { AnalyticsPage } from './pages/AnalyticsPage';
import { SoftDeletedProjectsPage } from './pages/SoftDeletedProjectsPage';
import { SoftDeletedTasksPage } from './pages/SoftDeletedTasksPage';

function App() {
  const { isAuthenticated, isLoading, loadUser } = useAuthStore();

  useEffect(() => {
    loadUser();
  }, [loadUser]);

  if (isLoading) {
    return <Loading fullScreen />;
  }

  return (
    <Routes>
      {/* Public Routes */}
      <Route element={<AuthLayout />}>
        <Route path="/login" element={!isAuthenticated ? <LoginPage /> : <Navigate to="/dashboard" />} />
        <Route path="/register" element={!isAuthenticated ? <RegisterPage /> : <Navigate to="/dashboard" />} />
      </Route>

      {/* Protected Routes */}
      <Route element={<DashboardLayout />}>
        <Route path="/" element={<Navigate to="/dashboard" />} />
        <Route path="/dashboard" element={isAuthenticated ? <DashboardPage /> : <Navigate to="/login" />} />
        <Route path="/projects" element={isAuthenticated ? <ProjectsPage /> : <Navigate to="/login" />} />
        <Route path="/projects/soft-deleted" element={isAuthenticated ? <SoftDeletedProjectsPage /> : <Navigate to="/login" />} />
        <Route path="/projects/:id" element={isAuthenticated ? <ProjectDetailPage /> : <Navigate to="/login" />} />
        <Route path="/tasks/soft-deleted" element={isAuthenticated ? <SoftDeletedTasksPage /> : <Navigate to="/login" />} />
        <Route path="/tasks" element={isAuthenticated ? <TasksPage /> : <Navigate to="/login" />} />
        <Route path="/tasks/:id" element={isAuthenticated ? <TaskDetailPage /> : <Navigate to="/login" />} />
        <Route path="/notifications" element={isAuthenticated ? <NotificationsPage /> : <Navigate to="/login" />} />
        <Route path="/chat" element={isAuthenticated ? <ChatPage /> : <Navigate to="/login" />} />
        
        {/* Admin Only Routes */}
        <Route 
          path="/users" 
          element={
            isAuthenticated ? (
              <ProtectedRoute requireAdmin>
                <UsersPage />
              </ProtectedRoute>
            ) : (
              <Navigate to="/login" />
            )
          } 
        />
        <Route 
          path="/payments" 
          element={
            isAuthenticated ? (
              <ProtectedRoute requireAdmin>
                <PaymentsPage />
              </ProtectedRoute>
            ) : (
              <Navigate to="/login" />
            )
          } 
        />
        <Route 
          path="/payments/callback" 
          element={
            isAuthenticated ? (
              <ProtectedRoute requireAdmin>
                <PaymentCallbackPage />
              </ProtectedRoute>
            ) : (
              <Navigate to="/login" />
            )
          } 
        />
        <Route 
          path="/analytics" 
          element={
            isAuthenticated ? (
              <ProtectedRoute requireAdmin>
                <AnalyticsPage />
              </ProtectedRoute>
            ) : (
              <Navigate to="/login" />
            )
          } 
        />
      </Route>

      {/* 404 */}
      <Route path="*" element={<Navigate to="/dashboard" />} />
    </Routes>
  );
}

export default App;
