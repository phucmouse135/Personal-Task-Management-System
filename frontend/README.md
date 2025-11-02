# Personal Task Management System - Frontend

A production-ready React + TypeScript + Tailwind CSS frontend application for managing tasks, projects, and team collaboration.

## 🚀 Features

- **Authentication**: Secure login/registration with JWT tokens
- **Projects Management**: CRUD operations for projects with role-based access
- **Tasks Management**: Advanced task management with status, priority, and deadline tracking
- **Real-time Chat**: WebSocket-based chat for project collaboration
- **Notifications**: Real-time notification system
- **Payment Integration**: VNPAY payment gateway integration
- **Analytics Dashboard**: Task statistics and insights with charts
- **Role-Based Access Control**: Admin and User roles with different permissions
- **Responsive Design**: Mobile-first responsive UI with Tailwind CSS
- **Dark Mode Support**: Toggle between light and dark themes

## 📦 Tech Stack

- **React 18** - UI library
- **TypeScript** - Type safety
- **Vite** - Build tool
- **Tailwind CSS** - Styling
- **React Router** - Routing
- **Zustand** - State management
- **Axios** - HTTP client
- **React Hook Form** - Form handling
- **Zod** - Schema validation
- **Recharts** - Data visualization
- **STOMP/SockJS** - WebSocket communication
- **Lucide React** - Icons
- **React Hot Toast** - Notifications

## 📁 Project Structure

```
frontend/
├── public/                 # Static assets
├── src/
│   ├── components/        # Reusable components
│   │   ├── layout/       # Layout components (Navbar, Sidebar, etc.)
│   │   └── ui/           # UI components (Button, Input, Modal, etc.)
│   ├── constants/        # App constants and configurations
│   ├── hooks/            # Custom React hooks
│   │   ├── useAuth.ts
│   │   ├── useProjects.ts
│   │   ├── useTasks.ts
│   │   ├── useNotifications.ts
│   │   ├── useChat.ts
│   │   ├── usePayment.ts
│   │   └── useAnalytics.ts
│   ├── lib/              # Utility libraries
│   │   ├── apiClient.ts  # Axios instance with interceptors
│   │   └── utils.ts      # Helper functions
│   ├── pages/            # Page components
│   │   ├── LoginPage.tsx
│   │   ├── RegisterPage.tsx
│   │   ├── DashboardPage.tsx
│   │   ├── ProjectsPage.tsx
│   │   ├── ProjectDetailPage.tsx
│   │   ├── TasksPage.tsx
│   │   ├── TaskDetailPage.tsx
│   │   ├── UsersPage.tsx
│   │   ├── NotificationsPage.tsx
│   │   ├── ChatPage.tsx
│   │   ├── PaymentsPage.tsx
│   │   ├── PaymentCallbackPage.tsx
│   │   └── AnalyticsPage.tsx
│   ├── services/         # API services
│   │   ├── authService.ts
│   │   ├── userService.ts
│   │   ├── projectService.ts
│   │   ├── taskService.ts
│   │   ├── notificationService.ts
│   │   ├── chatService.ts
│   │   ├── paymentService.ts
│   │   └── analyticsService.ts
│   ├── store/            # Zustand stores
│   │   ├── authStore.ts
│   │   └── themeStore.ts
│   ├── types/            # TypeScript types
│   │   └── index.ts
│   ├── App.tsx           # Main App component
│   ├── main.tsx          # Entry point
│   └── index.css         # Global styles
├── .env.example          # Environment variables example
├── .eslintrc.cjs         # ESLint configuration
├── .gitignore
├── index.html            # HTML template
├── package.json
├── postcss.config.js     # PostCSS configuration
├── tailwind.config.js    # Tailwind CSS configuration
├── tsconfig.json         # TypeScript configuration
├── tsconfig.node.json
├── vite.config.ts        # Vite configuration
└── README.md

```

## 🛠️ Installation

### Prerequisites

- Node.js 18+ and npm/yarn/pnpm
- Backend API running on `http://localhost:8080` (or configure `VITE_API_BASE_URL`)

### Steps

1. **Clone the repository**
   ```bash
   cd frontend
   ```

2. **Install dependencies**
   ```bash
   npm install
   # or
   yarn install
   # or
   pnpm install
   ```

3. **Create environment file**
   ```bash
   # Create .env file in frontend folder
   VITE_API_BASE_URL=http://localhost:8080
   VITE_WS_BASE_URL=http://localhost:8080/ws
   ```

4. **Run development server**
   ```bash
   npm run dev
   ```

5. **Open browser**
   Navigate to `http://localhost:3000`

## 🔧 Build for Production

```bash
npm run build
```

The build files will be in the `dist/` directory.

## 📡 API Integration

### Backend Endpoints

The frontend integrates with the following backend endpoints:

- **Authentication**
  - `POST /auth/login` - User login
  - `POST /auth/register` - User registration
  - `POST /auth/logout` - User logout

- **Users** (Admin only)
  - `GET /users` - Get all users (paginated)
  - `GET /users/{id}` - Get user by ID
  - `POST /users` - Create user
  - `PUT /users/{id}` - Update user
  - `DELETE /users/{id}` - Delete user

- **Projects**
  - `GET /projects` - Get all projects (with filters)
  - `GET /projects/{id}` - Get project by ID
  - `POST /projects` - Create project
  - `PUT /projects/{id}` - Update project
  - `DELETE /projects/{id}` - Delete project

- **Tasks**
  - `GET /tasks` - Get all tasks (with filters: status, priority, projectId, assigneeId)
  - `GET /tasks/{id}` - Get task by ID
  - `POST /tasks` - Create task
  - `PUT /tasks/{id}` - Update task
  - `DELETE /tasks/{id}` - Delete task

- **Notifications**
  - `GET /notifications` - Get all notifications
  - `POST /notifications/{id}/send` - Mark notification as sent/read

- **Chat (WebSocket)**
  - `WS /ws` - WebSocket connection
  - Subscribe to `/topic/messages` - Receive messages
  - Publish to `/app/chat` - Send message

- **Payments (VNPAY)**
  - `POST /payments/create` - Create payment
  - `GET /payments/callback` - Handle payment callback

- **Analytics**
  - `GET /analytics/tasks-summary` - Get tasks summary

### Authentication

All API requests (except login/register) require JWT token in the `Authorization` header:
```
Authorization: Bearer <token>
```

The token is automatically added by the Axios interceptor.

## 🎨 UI Components

### Reusable Components

- **Button** - Variants: primary, secondary, danger, ghost
- **Input** - Text input with label and error handling
- **Select** - Dropdown select with options
- **Textarea** - Multi-line text input
- **Modal** - Popup modal dialog
- **Loading** - Loading spinner
- **Alert** - Alert messages (success, error, warning, info)
- **Badge** - Status badges
- **Card** - Card container

### Layout Components

- **AuthLayout** - Layout for login/register pages
- **DashboardLayout** - Main app layout with Navbar and Sidebar
- **Navbar** - Top navigation bar with user menu
- **Sidebar** - Side navigation menu

## 🔐 Role-Based Access Control

### User Roles

- **Admin**: Can manage all users, projects, and tasks
- **User**: Can only manage their own projects and assigned tasks

### Access Control Example

```typescript
import { useAuthStore } from '@/store/authStore';
import { isAdmin } from '@/lib/utils';

const user = useAuthStore((state) => state.user);
const isAdminUser = user && isAdmin(user.roles);

// Conditionally render admin features
{isAdminUser && <UsersManagementSection />}
```

## 📊 State Management

Uses Zustand for global state:

- **authStore**: User authentication state
- **themeStore**: Theme (light/dark) state

## 🌐 WebSocket Chat

Real-time chat using STOMP over SockJS:

```typescript
import { useChat } from '@/hooks/useChat';

const { messages, sendMessage, connected } = useChat(projectId);

// Send a message
sendMessage('Hello team!');

// Messages are automatically received and added to the state
```

## 💳 Payment Integration

VNPAY payment flow:

1. User initiates payment for a task
2. Frontend calls `/payments/create`
3. Backend returns VNPAY payment URL
4. User is redirected to VNPAY
5. After payment, VNPAY redirects back to `/payments/callback`
6. Frontend handles callback and displays status

## 📈 Analytics & Charts

Uses Recharts for data visualization:

- Task status distribution (Pie/Bar chart)
- Task priority distribution
- Completed tasks over time
- Overdue tasks tracking

## 🎯 Key Features Implementation

### 1. Protected Routes

```typescript
<Route 
  path="/dashboard" 
  element={isAuthenticated ? <DashboardPage /> : <Navigate to="/login" />} 
/>
```

### 2. API Error Handling

Axios interceptor automatically:
- Adds JWT token to requests
- Handles 401 (redirect to login)
- Shows error toasts
- Provides typed error objects

### 3. Form Validation

Using React Hook Form + Zod:
```typescript
const schema = z.object({
  title: z.string().min(3),
  deadline: z.string(),
  // ...
});
```

### 4. Pagination

All list pages support pagination:
```typescript
const { tasks, pagination } = useTasks({ page: 0, size: 10 });
```

## 🧪 Development Tips

### Add a new page

1. Create page component in `src/pages/`
2. Add route in `src/App.tsx`
3. Add navigation link in `src/components/layout/Sidebar.tsx`

### Add a new API service

1. Create service file in `src/services/`
2. Use `apiClient` from `src/lib/apiClient.ts`
3. Define TypeScript interfaces in `src/types/index.ts`

### Add a new hook

1. Create hook file in `src/hooks/`
2. Use existing services
3. Handle loading and error states

## 🚨 Troubleshooting

### CORS Issues

Ensure backend allows requests from `http://localhost:3000`:
```java
@CrossOrigin(origins = "http://localhost:3000")
```

### WebSocket Connection Failed

Check:
- Backend WebSocket endpoint is running on `/ws`
- STOMP is configured correctly on backend
- JWT token is valid

### Build Errors

```bash
# Clear cache and reinstall
rm -rf node_modules
rm package-lock.json
npm install
```

## 📝 Environment Variables

Create `.env` file:

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_WS_BASE_URL=http://localhost:8080/ws
```

## 🤝 Contributing

1. Create a feature branch
2. Make your changes
3. Run linting: `npm run lint`
4. Build to check for errors: `npm run build`
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.

## 🎓 Learn More

- [React Documentation](https://react.dev/)
- [TypeScript Handbook](https://www.typescriptlang.org/docs/)
- [Tailwind CSS Docs](https://tailwindcss.com/docs)
- [Vite Guide](https://vitejs.dev/guide/)
- [Zustand Documentation](https://github.com/pmndrs/zustand)
- [React Router](https://reactrouter.com/)

## ✨ Features Checklist

- ✅ Authentication (Login/Register)
- ✅ JWT Token Management
- ✅ Role-Based Access Control
- ✅ Projects CRUD
- ✅ Tasks CRUD with Filters
- ✅ Real-time Chat (WebSocket)
- ✅ Notifications System
- ✅ Payment Integration (VNPAY)
- ✅ Analytics Dashboard
- ✅ Responsive Design
- ✅ Dark Mode Support
- ✅ Form Validation
- ✅ Error Handling
- ✅ Loading States
- ✅ Pagination
- ✅ TypeScript Support

## 📧 Support

For issues or questions, please create an issue in the repository.
