import { create } from 'zustand';
import { User } from '@/types';
import { STORAGE_KEYS } from '@/constants';
import { authService } from '@/services/authService';

interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (username: string, password: string) => Promise<void>;
  googleLogin: (credential: string) => Promise<void>;
  register: (username: string, email: string, password: string, fullName: string) => Promise<void>;
  logout: () => Promise<void>;
  loadUser: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  token: null,
  isAuthenticated: false,
  isLoading: true,

  login: async (username: string, password: string) => {
    const response = await authService.login({ username, password });
    localStorage.setItem(STORAGE_KEYS.TOKEN, response.token);
    
    // Fetch user info after login
    const user = await authService.getCurrentUser();
    localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(user));
    
    set({ user, token: response.token, isAuthenticated: true });
  },

  googleLogin: async (credential: string) => {
    const response = await authService.googleLogin({ credential });
    localStorage.setItem(STORAGE_KEYS.TOKEN, response.token);
    
    // Fetch user info after Google login
    const user = await authService.getCurrentUser();
    localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(user));
    
    set({ user, token: response.token, isAuthenticated: true });
  },

  register: async (username: string, email: string, password: string, fullName: string) => {
    const response = await authService.register({ username, email, password, fullName });
    localStorage.setItem(STORAGE_KEYS.TOKEN, response.token);
    
    // Fetch user info after registration
    const user = await authService.getCurrentUser();
    localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(user));
    
    set({ user, token: response.token, isAuthenticated: true });
  },

  logout: async () => {
    try {
      await authService.logout();
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      localStorage.removeItem(STORAGE_KEYS.TOKEN);
      localStorage.removeItem(STORAGE_KEYS.USER);
      set({ user: null, token: null, isAuthenticated: false });
    }
  },

  loadUser: () => {
    const token = localStorage.getItem(STORAGE_KEYS.TOKEN);
    const userStr = localStorage.getItem(STORAGE_KEYS.USER);
    
    if (token && userStr) {
      try {
        const user = JSON.parse(userStr);
        set({ user, token, isAuthenticated: true, isLoading: false });
      } catch (error) {
        console.error('Error parsing user data:', error);
        localStorage.removeItem(STORAGE_KEYS.TOKEN);
        localStorage.removeItem(STORAGE_KEYS.USER);
        set({ isLoading: false });
      }
    } else {
      set({ isLoading: false });
    }
  },
}));
