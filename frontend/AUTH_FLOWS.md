# 🔐 Hai Luồng Đăng Nhập Độc Lập

Project có **2 cách đăng nhập hoàn toàn tách biệt**:

---

## 1️⃣ Đăng Nhập Truyền Thống (Username/Password)

### Frontend Flow:
```typescript
// Component: LoginPage.tsx
// Service: authService.login()
// Endpoint: POST /auth/token

User nhập username + password
    ↓
authService.login({ username, password })
    ↓
POST /auth/token
    ↓
Backend trả về: { token, expiryTime }
    ↓
authStore.login() lưu token vào localStorage
    ↓
Gọi authService.getCurrentUser() để lấy user info
    ↓
GET /users/myInfo (với Authorization header)
    ↓
Lưu user vào localStorage
    ↓
Chuyển đến Dashboard
```

### Backend Endpoints:
- **Login**: `POST /auth/token`
  - Request: `{ username: string, password: string }`
  - Response: `{ token: string, expiryTime: Date }`
  
- **Get User Info**: `GET /users/myInfo`
  - Headers: `Authorization: Bearer {token}`
  - Response: `User object`

### Files liên quan:
- Frontend:
  - `src/pages/LoginPage.tsx` - Form đăng nhập truyền thống
  - `src/services/authService.ts` - `login()` method
  - `src/store/authStore.ts` - `login()` action
  - `src/constants/index.ts` - `LOGIN: '/auth/token'`

- Backend:
  - `AuthenticationController.java` - `@PostMapping("/token")`
  - `AuthenticationServiceImpl.java` - `authenticate()` method
  - `SecurityConfig.java` - `/auth/token` in PUBLIC_URLS

---

## 2️⃣ Đăng Nhập Google OAuth (Google Identity Services)

### Frontend Flow:
```typescript
// Component: GoogleLoginButton.tsx
// Service: authService.googleLogin()
// Endpoint: POST /auth/google

User click "Sign in with Google"
    ↓
Google Identity Services popup
    ↓
User chọn tài khoản Google
    ↓
Google trả về ID Token (credential)
    ↓
handleCredentialResponse(credential)
    ↓
authService.googleLogin({ credential })
    ↓
POST /auth/google (gửi Google ID Token)
    ↓
Backend verify token với Google API
    ↓
Backend trả về: { token, expiryTime }
    ↓
authStore.googleLogin() lưu token
    ↓
Gọi authService.getCurrentUser()
    ↓
GET /users/myInfo
    ↓
Lưu user vào localStorage
    ↓
Chuyển đến Dashboard
```

### Backend Endpoints:
- **Google Login**: `POST /auth/google`
  - Request: `{ credential: string }` (Google ID Token)
  - Response: `{ token: string, expiryTime: Date }`
  
- **Get User Info**: `GET /users/myInfo`
  - Headers: `Authorization: Bearer {token}`
  - Response: `User object`

### Files liên quan:
- Frontend:
  - `src/components/auth/GoogleLoginButton.tsx` - Google OAuth button
  - `src/pages/LoginPage.tsx` - Hiển thị GoogleLoginButton
  - `src/services/authService.ts` - `googleLogin()` method
  - `src/store/authStore.ts` - `googleLogin()` action
  - `src/constants/index.ts` - `GOOGLE_LOGIN: '/auth/google'`
  - `.env` - `VITE_GOOGLE_CLIENT_ID`

- Backend:
  - `AuthenticationController.java` - `@PostMapping("/google")`
  - `AuthenticationServiceImpl.java` - `authenticateWithGoogle()` method
  - `GoogleLoginRequest.java` - Request DTO
  - `SecurityConfig.java` - `/auth/google` in PUBLIC_URLS
  - `pom.xml` - Google API Client dependency

### Google Cloud Console Config:
**Authorized JavaScript origins:**
- `http://localhost:3000` ✅ **BẮT BUỘC**
- `http://localhost:8080` (optional)

---

## 📋 So Sánh 2 Flow

| Tiêu chí | Traditional Login | Google OAuth |
|----------|------------------|--------------|
| **Endpoint** | `POST /auth/token` | `POST /auth/google` |
| **Request** | `{username, password}` | `{credential}` (Google ID Token) |
| **Response** | `{token, expiryTime}` | `{token, expiryTime}` |
| **User info** | `GET /users/myInfo` | `GET /users/myInfo` |
| **Component** | LoginPage form | GoogleLoginButton |
| **Auth method** | BCrypt password check | Google ID Token verification |
| **User creation** | Manual registration | Auto-create from Google profile |

---

## 🔧 Cấu hình hiện tại

### Frontend (.env)
```bash
VITE_API_BASE_URL=http://localhost:8080
VITE_GOOGLE_CLIENT_ID=101718001419-32m56mclhudonq2bg6ljknblod4rtsmn.apps.googleusercontent.com
```

### Backend (application.yaml)
```yaml
outbound:
  google:
    client-id: 101718001419-32m56mclhudonq2bg6ljknblod4rtsmn.apps.googleusercontent.com
    client-secret: GOCSPX-xxx
```

### Security Config Public URLs
```java
"/auth/token",      // Traditional login
"/auth/google",     // Google login
"/users/create",    // Registration
"/users/myInfo",    // Get current user (cần JWT token)
```

---

## ✅ Checklist Troubleshooting

### Traditional Login không hoạt động:
- [ ] Backend có endpoint `POST /auth/token`?
- [ ] Frontend gọi đúng `/auth/token` (không phải `/auth/login`)?
- [ ] `/auth/token` có trong PUBLIC_URLS?
- [ ] Request body đúng format: `{username, password}`?

### Google Login không hoạt động:
- [ ] Google Cloud Console đã add `http://localhost:3000` vào Authorized JavaScript origins?
- [ ] `.env` có `VITE_GOOGLE_CLIENT_ID`?
- [ ] Backend có dependency `google-api-client`?
- [ ] Backend endpoint `POST /auth/google` hoạt động?
- [ ] `/auth/google` có trong PUBLIC_URLS?

### Cả 2 đều tự động logout:
- [ ] Token có được lưu vào localStorage?
- [ ] `/users/myInfo` endpoint hoạt động?
- [ ] JWT token có được thêm vào Authorization header?
- [ ] User info có được parse đúng từ response?

---

## 🎯 Kết luận

2 flow đăng nhập **hoàn toàn độc lập**, chỉ **chung endpoint lấy user info** (`/users/myInfo`) sau khi có JWT token.

Backend trả về **cùng format JWT token** cho cả 2 flow, nên frontend xử lý giống nhau sau khi nhận được token.
