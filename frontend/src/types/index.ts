// User and Authentication Types
export interface User {
  id: number;
  username: string;
  email: string;
  fullName: string;
  roles: Role[];
  createdAt: string;
  updatedAt: string;
}

export interface Role {
  id: number;
  name: string;
  description?: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  fullName: string;
}

export interface GoogleLoginRequest {
  credential: string;
}

export interface AuthResponse {
  token: string;
  expiryTime: string;
}

// Backend API Response wrapper
export interface ApiResponse<T> {
  code: number;
  message?: string;
  result: T;
}

// Project Types
export interface Project {
  id: number;
  name: string;
  description: string;
  ownerId?: number;
  owner?: User;
  members?: User[];
  endDate: string;
  createdAt: string;
  updatedAt: string;
  deletedAt?: string | null;
}

export interface ProjectRequest {
  name: string;
  description: string;
  endDate: string;
}

// Task Types
export interface Task {
  id: number;
  title: string;
  description: string;
  status: TaskStatus;
  priority: TaskPriority;
  deadline: string;
  project?: Project;
  assignees?: User[];
  createdAt: string;
  overdue?: boolean;
}

export enum TaskStatus {
  TODO = 'TODO',
  IN_PROGRESS = 'IN_PROGRESS',
  DONE = 'DONE',
  CANCELLED = 'CANCELLED',
}

export enum TaskPriority {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  URGENT = 'URGENT',
}

export interface TaskRequest {
  title: string;
  description: string;
  priority: TaskPriority;
  deadline: string;
  projectId: number;
  assignees: number[];
}

export interface TaskFilters {
  status?: TaskStatus;
  priority?: TaskPriority;
  projectId?: number;
  assigneeId?: number;
  page?: number;
  size?: number;
  sort?: string;
}

// Notification Types
export interface Notification {
  id: number;
  recipientId: number;
  recipient?: User;
  message: string;
  type: NotificationType;
  status: NotificationStatus;
  createdAt: string;
  sentAt?: string;
}

export enum NotificationType {
  TASK_ASSIGNED = 'TASK_ASSIGNED',
  TASK_UPDATED = 'TASK_UPDATED',
  PROJECT_UPDATED = 'PROJECT_UPDATED',
  DEADLINE_REMINDER = 'DEADLINE_REMINDER',
  PAYMENT_RECEIVED = 'PAYMENT_RECEIVED',
}

export enum NotificationStatus {
  PENDING = 'PENDING',
  SENT = 'SENT',
  READ = 'READ',
}

// Chat Types
export interface ChatMessage {
  id?: number;
  senderId: number;
  sender?: User;
  content: string;
  projectId?: number;
  project?: Project;
  timestamp: string;
}

export interface ProjectConversation {
  projectId: number;
  projectName: string;
  memberCount: number;
  lastMessage?: string;
  lastMessageTime?: string;
}

// Payment Types
export interface Payment {
  id: number;
  user?: User;
  project?: Project;
  amount: number;
  status: PaymentStatus;
  vnpTransactionNo?: string;
  vnpBankCode?: string;
  vnpPayDate?: string;
  createdAt: string;
  updatedAt: string;
}

export enum PaymentStatus {
  PENDING = 'PENDING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
}

export interface PaymentRequest {
  projectId: number;
  amount: number;
  returnUrl?: string;
}

// Analytics Types
export interface TasksSummary {
  totalTasks: number;
  tasksByStatus: Record<TaskStatus, number>;
  tasksByPriority: Record<TaskPriority, number>;
  overdueTasks: number;
  completedThisWeek: number;
  completedThisMonth: number;
}

// API Response Types
export interface ApiResponse<T> {
  data: T;
  message?: string;
  success: boolean;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

// Spring's Page interface (from backend)
export interface SpringPage<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  numberOfElements?: number;
  empty?: boolean;
  pageable?: any;
  sort?: any;
}

export interface ApiError {
  message: string;
  status: number;
  errors?: Record<string, string[]>;
}
