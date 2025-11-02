import { Bell, User, LogOut, Moon, Sun } from 'lucide-react';
import { useAuthStore } from '@/store/authStore';
import { useThemeStore } from '@/store/themeStore';
import { useNotifications } from '@/hooks/useNotifications';
import { useNavigate } from 'react-router-dom';

export const Navbar = () => {
  const user = useAuthStore((state) => state.user);
  const logout = useAuthStore((state) => state.logout);
  const { theme, toggleTheme } = useThemeStore();
  const { unreadCount } = useNotifications();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <nav className="bg-white border-b border-gray-200 px-6 py-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center">
          <h1 className="text-xl font-bold text-primary-600">Task Manager</h1>
        </div>
        
        <div className="flex items-center gap-4">
          <button
            onClick={toggleTheme}
            className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
          >
            {theme === 'light' ? <Moon size={20} /> : <Sun size={20} />}
          </button>
          
          <button
            onClick={() => navigate('/notifications')}
            className="p-2 hover:bg-gray-100 rounded-lg transition-colors relative"
          >
            <Bell size={20} />
            {unreadCount > 0 && (
              <span className="absolute top-0 right-0 bg-red-500 text-white text-xs rounded-full h-5 w-5 flex items-center justify-center">
                {unreadCount}
              </span>
            )}
          </button>
          
          <div className="flex items-center gap-2">
            <User size={20} />
            <span className="text-sm font-medium">{user?.fullName}</span>
          </div>
          
          <button
            onClick={handleLogout}
            className="p-2 hover:bg-gray-100 rounded-lg transition-colors text-red-600"
          >
            <LogOut size={20} />
          </button>
        </div>
      </div>
    </nav>
  );
};
