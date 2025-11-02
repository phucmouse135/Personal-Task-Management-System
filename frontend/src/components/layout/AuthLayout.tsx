import { Outlet } from 'react-router-dom';

export const AuthLayout = () => {
  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-primary-600">Task Management System</h1>
          <p className="text-gray-600 mt-2">Manage your projects and tasks efficiently</p>
        </div>
        <Outlet />
      </div>
    </div>
  );
};
