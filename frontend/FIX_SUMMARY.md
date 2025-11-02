# 🔧 Sửa Lỗi Đăng Nhập

## ❌ Lỗi đã sửa:

### 1. **Traditional Login** - Endpoint sai
**Trước:**
```typescript
LOGIN: '/auth/login'  // ❌ Backend không có endpoint này
```

**Sau:**
```typescript
LOGIN: '/auth/token'  // ✅ Đúng endpoint của backend
```

### 2. **Register** - Endpoint sai
**Trước:**
```typescript
REGISTER: '/auth/register'  // ❌ Backend không có endpoint này
```

**Sau:**
```typescript
REGISTER: '/users/create'  // ✅ Đúng endpoint của backend
```

### 3. **Google Login** - Origin chưa được phép
**Vấn đề:** 
```
403 Error: The given origin is not allowed for the given client ID
```

**Giải pháp:**
Thêm `http://localhost:3000` vào **Google Cloud Console**:
1. Vào https://console.cloud.google.com/apis/credentials
2. Click OAuth Client ID: `101718001419-32m56mclhudonq2bg6ljknblod4rtsmn`
3. **Authorized JavaScript origins** → Add URI → `http://localhost:3000`
4. Save

---

## ✅ Checklist sau khi sửa:

### Traditional Login:
- [x] Endpoint đúng: `POST /auth/token`
- [x] Request format: `{ username: string, password: string }`
- [x] Response: `{ token: string, expiryTime: Date }`
- [x] Fetch user info sau login: `GET /users/myInfo`

### Google Login:
- [ ] **BẠN CẦN LÀM**: Add `http://localhost:3000` vào Google Cloud Console
- [x] Endpoint đúng: `POST /auth/google`
- [x] Request format: `{ credential: string }`
- [x] Response: `{ token: string, expiryTime: Date }`
- [x] Fetch user info sau login: `GET /users/myInfo`

### Register:
- [x] Endpoint đúng: `POST /users/create`
- [x] Request format: `{ username, email, password, fullName }`
- [x] Response: `{ token: string, expiryTime: Date }`
- [x] Fetch user info sau register: `GET /users/myInfo`

---

## 🚀 Test ngay:

### 1. Traditional Login:
```bash
# Mở browser
http://localhost:3000/login

# Nhập username/password
# Click "Đăng nhập"
# ✅ Phải chuyển đến Dashboard
```

### 2. Google Login:
```bash
# Mở browser
http://localhost:3000/login

# Click "Sign in with Google"
# ⚠️ Nếu vẫn lỗi 403 → Chưa add origin vào Google Console
# ✅ Nếu popup Google mở → Đã đúng!
```

### 3. Register:
```bash
# Mở browser
http://localhost:3000/register

# Điền form đăng ký
# Click "Đăng ký"
# ✅ Phải chuyển đến Dashboard
```

---

## 📝 Files đã sửa:

1. `frontend/src/constants/index.ts`:
   - `LOGIN: '/auth/token'`
   - `REGISTER: '/users/create'`
   - `GOOGLE_LOGIN: '/auth/google'` (không đổi)

2. `frontend/src/types/index.ts`:
   - `AuthResponse` → `{ token: string, expiryTime: string }`

3. `frontend/src/store/authStore.ts`:
   - Fetch user info sau mỗi login/register/googleLogin

4. `frontend/src/services/authService.ts`:
   - Thêm `getCurrentUser()` method

---

## ⚠️ Điều quan trọng nhất:

**Google Login SẼ KHÔNG hoạt động** cho đến khi bạn thêm `http://localhost:3000` vào Google Cloud Console!

**Traditional Login ĐÃ SỬA XONG**, test ngay được!
