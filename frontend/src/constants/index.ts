// API Configuration
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
export const WS_BASE_URL = import.meta.env.VITE_WS_BASE_URL || 'http://localhost:8080/ws';

// Storage Keys
export const STORAGE_KEYS = {
  TOKEN: 'auth_token',
  USER: 'auth_user',
  THEME: 'theme',
} as const;

// API Endpoints
export const API_ENDPOINTS = {
  // Auth
  LOGIN: '/auth/login', // Traditional login endpoint
  REGISTER: '/users/create', // Register endpoint
  LOGOUT: '/auth/logout',
  GOOGLE_LOGIN: '/auth/google', // Google OAuth login endpoint
  ME: '/users/myInfo',
  
  // Users
  USERS: '/users',
  USER_BY_ID: (id: number) => `/users/${id}`,
  USER_BY_USERNAME: (username: string) => `/users/username/${username}`,
  
  // Projects
  PROJECTS: '/projects',
  PROJECT_BY_ID: (id: number) => `/projects/${id}`,
  CREATE_PROJECT: '/projects/create',
  RESTORE_PROJECT: (id: number) => `/projects/restore/${id}`,
  ADD_PROJECT_MEMBER: (projectId: number, userId: number) => `/projects/${projectId}/members/${userId}`,
  REMOVE_PROJECT_MEMBER: (projectId: number, userId: number) => `/projects/${projectId}/members/${userId}`,
  CHANGE_PROJECT_OWNER: (projectId: number, newOwnerId: number) => `/projects/${projectId}/owner/${newOwnerId}`,
  
  // Tasks
  TASKS: '/tasks',
  TASK_BY_ID: (id: number) => `/tasks/${id}`,
  
  // Notifications
  NOTIFICATIONS: '/notifications',
  NOTIFICATION_SEND: (id: number) => `/notifications/${id}/send`,
  
  // Payments
  PAYMENTS: '/payments',
  PAYMENT_CREATE: '/payments/create',
  PAYMENT_CALLBACK: '/payments/callback',
  
  // Analytics
  ANALYTICS_TASKS_SUMMARY: '/analytics/tasks-summary',
  
  // WebSocket
  WS_CONNECT: '/ws',
} as const;

// Route Paths
export const ROUTES = {
  HOME: '/',
  LOGIN: '/login',
  REGISTER: '/register',
  DASHBOARD: '/dashboard',
  PROJECTS: '/projects',
  PROJECT_DETAIL: '/projects/:id',
  TASKS: '/tasks',
  TASK_DETAIL: '/tasks/:id',
  USERS: '/users',
  NOTIFICATIONS: '/notifications',
  CHAT: '/chat',
  PAYMENTS: '/payments',
  ANALYTICS: '/analytics',
} as const;

// Pagination
export const DEFAULT_PAGE_SIZE = 10;
export const PAGE_SIZE_OPTIONS = [10, 20, 50, 100];

// Date Formats
export const DATE_FORMAT = 'yyyy-MM-dd';
export const DATETIME_FORMAT = 'yyyy-MM-dd HH:mm:ss';
export const DISPLAY_DATE_FORMAT = 'MMM dd, yyyy';
export const DISPLAY_DATETIME_FORMAT = 'MMM dd, yyyy HH:mm';

// Status Colors
export const TASK_STATUS_COLORS = {
  TODO: 'bg-gray-100 text-gray-800',
  IN_PROGRESS: 'bg-blue-100 text-blue-800',
  DONE: 'bg-green-100 text-green-800',
  CANCELLED: 'bg-red-100 text-red-800',
} as const;

export const TASK_PRIORITY_COLORS = {
  LOW: 'bg-gray-100 text-gray-800',
  MEDIUM: 'bg-yellow-100 text-yellow-800',
  HIGH: 'bg-orange-100 text-orange-800',
  URGENT: 'bg-red-100 text-red-800',
} as const;

export const PROJECT_STATUS_COLORS = {
  PLANNING: 'bg-purple-100 text-purple-800',
  IN_PROGRESS: 'bg-blue-100 text-blue-800',
  COMPLETED: 'bg-green-100 text-green-800',
  ON_HOLD: 'bg-yellow-100 text-yellow-800',
} as const;

export const PAYMENT_STATUS_COLORS = {
  PENDING: 'bg-yellow-100 text-yellow-800',
  SUCCESS: 'bg-green-100 text-green-800',
  FAILED: 'bg-red-100 text-red-800',
} as const;

export const NOTIFICATION_TYPE_COLORS = {
  TASK_ASSIGNED: 'bg-blue-100 text-blue-800',
  TASK_UPDATED: 'bg-purple-100 text-purple-800',
  PROJECT_UPDATED: 'bg-indigo-100 text-indigo-800',
  DEADLINE_REMINDER: 'bg-orange-100 text-orange-800',
  PAYMENT_RECEIVED: 'bg-green-100 text-green-800',
} as const;
