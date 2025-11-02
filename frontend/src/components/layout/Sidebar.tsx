import { NavLink } from 'react-router-dom';
import { 
  LayoutDashboard, 
  FolderKanban, 
  CheckSquare, 
  Users, 
  Bell, 
  MessageSquare, 
  CreditCard, 
  BarChart3,
  Trash2
} from 'lucide-react';
import { useAuthStore } from '@/store/authStore';
import { isAdmin } from '@/lib/utils';
import { cn } from '@/lib/utils';

export const Sidebar = () => {
  const user = useAuthStore((state) => state.user);
  const isAdminUser = user && isAdmin(user.roles);

  // Navigation items for all users
  const commonNavigation = [
    { name: 'Dashboard', to: '/dashboard', icon: LayoutDashboard },
    { name: 'Projects', to: '/projects', icon: FolderKanban },
    { name: 'Tasks', to: '/tasks', icon: CheckSquare },
    { name: 'Notifications', to: '/notifications', icon: Bell },
    { name: 'Chat', to: '/chat', icon: MessageSquare },
    { name: 'Deleted Projects', to: '/projects/soft-deleted', icon: Trash2 },
    { name: 'Deleted Tasks', to: '/tasks/soft-deleted', icon: Trash2 },
  ];

  // Navigation items for admin only
  const adminNavigation = [
    { name: 'Users', to: '/users', icon: Users },
    { name: 'Payments', to: '/payments', icon: CreditCard },
    { name: 'Analytics', to: '/analytics', icon: BarChart3 },
  ];

  // Combine navigation based on role
  const navigation = isAdminUser 
    ? [...commonNavigation, ...adminNavigation]
    : commonNavigation;

  return (
    <aside className="fixed left-0 top-16 h-[calc(100vh-4rem)] w-64 bg-white border-r border-gray-200 overflow-y-auto">
      <nav className="p-4 space-y-1">
        {navigation.map((item) => (
          <NavLink
            key={item.name}
            to={item.to}
            className={({ isActive }) =>
              cn(
                'flex items-center gap-3 px-4 py-3 rounded-lg transition-colors',
                isActive
                  ? 'bg-primary-50 text-primary-700 font-medium'
                  : 'text-gray-700 hover:bg-gray-100'
              )
            }
          >
            <item.icon size={20} />
            <span>{item.name}</span>
          </NavLink>
        ))}
      </nav>
    </aside>
  );
};
