# 🚀 Quick Installation Guide

## Prerequisites
- Node.js 18+ installed
- Backend API running on port 8080

## Installation Steps

### 1. Navigate to frontend folder
```bash
cd frontend
```

### 2. Install dependencies
```bash
npm install
```

This will install all required packages:
- React, TypeScript, Vite
- Tailwind CSS
- Axios, Zustand
- React Router
- WebSocket libraries
- UI libraries (Recharts, Lucide icons, etc.)

### 3. Create environment file
```bash
# Create .env file
copy .env.example .env
```

Or manually create `.env`:
```env
VITE_API_BASE_URL=http://localhost:8080
VITE_WS_BASE_URL=http://localhost:8080/ws
```

### 4. Start development server
```bash
npm run dev
```

The application will start at `http://localhost:3000`

### 5. Default credentials (if backend has seeded data)
```
Admin:
  Username: admin
  Password: admin123

User:
  Username: user
  Password: user123
```

## Build for Production

```bash
npm run build
```

Output will be in `dist/` folder.

## Verify Installation

After starting the dev server, you should see:
1. Login page at `http://localhost:3000/login`
2. No console errors
3. Able to login (if backend is running)
4. Dashboard loads with navigation

## Common Issues

### Port 3000 already in use
```bash
# Edit vite.config.ts and change port
server: {
  port: 3001, // Change this
}
```

### Cannot connect to backend
- Verify backend is running on `http://localhost:8080`
- Check CORS settings on backend allow `http://localhost:3000`
- Check `.env` file has correct `VITE_API_BASE_URL`

### TypeScript errors during install
These are expected and will resolve after dependencies are installed. If they persist:
```bash
rm -rf node_modules package-lock.json
npm install
```

## Project Structure Overview

```
frontend/
├── src/
│   ├── components/     # Reusable UI components
│   ├── pages/          # Page components
│   ├── services/       # API services
│   ├── hooks/          # Custom hooks
│   ├── store/          # Zustand state
│   ├── types/          # TypeScript types
│   ├── lib/            # Utilities
│   └── constants/      # App constants
├── package.json
├── vite.config.ts
├── tailwind.config.js
└── tsconfig.json
```

## Next Steps

1. ✅ Backend is running
2. ✅ Frontend is installed and running
3. 📝 Read `README.md` for full documentation
4. 📝 Check `PROJECT_STRUCTURE.md` for detailed guide
5. 🎨 Start customizing pages in `src/pages/`
6. 🔧 Add more features using existing patterns

## Available Scripts

```bash
npm run dev      # Start development server
npm run build    # Build for production
npm run preview  # Preview production build
npm run lint     # Run ESLint
```

## Tech Stack

- **React 18** - UI Library
- **TypeScript** - Type Safety
- **Vite** - Build Tool (Fast HMR)
- **Tailwind CSS** - Styling
- **Zustand** - State Management
- **React Router** - Routing
- **Axios** - HTTP Client
- **STOMP/SockJS** - WebSocket
- **Recharts** - Charts
- **React Hot Toast** - Notifications

## Features Implemented

✅ Authentication (Login/Register)
✅ JWT Token Management
✅ Protected Routes
✅ Role-Based Access Control
✅ Projects CRUD
✅ Tasks CRUD with Filters
✅ Real-time Chat (WebSocket)
✅ Notifications
✅ Payments (VNPAY)
✅ Analytics Dashboard
✅ Responsive Design
✅ Dark Mode Support

## Support

If you encounter issues:
1. Check backend is running: `http://localhost:8080`
2. Verify `.env` configuration
3. Clear browser cache
4. Check browser console for errors
5. Review `README.md` and `PROJECT_STRUCTURE.md`

---

**Happy Coding! 🎉**
