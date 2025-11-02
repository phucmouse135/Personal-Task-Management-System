# 🔧 Cấu hình Google OAuth cho Frontend

## Lỗi hiện tại:
```
403 Error: The given origin is not allowed for the given client ID
```

## Nguyên nhân:
Frontend React đang chạy ở `http://localhost:3000` nhưng Google Cloud Console chưa được cấu hình cho origin này.

## Giải pháp:

### Bước 1: Truy cập Google Cloud Console
1. Mở https://console.cloud.google.com/
2. Chọn project: **Personal-Task-Manager**
3. Vào **APIs & Services** → **Credentials**

### Bước 2: Cấu hình OAuth Client ID
1. Click vào OAuth 2.0 Client ID của bạn (ID: `101718001419-32m56mclhudonq2bg6ljknblod4rtsmn.apps.googleusercontent.com`)
2. Trong phần **Authorized JavaScript origins**, thêm:
   ```
   http://localhost:3000
   ```
3. Click **Save**

### Bước 3: Cấu hình cuối cùng

Sau khi lưu, cấu hình của bạn sẽ như này:

**Authorized JavaScript origins:**
- `http://localhost:8080` (cho backend - nếu cần)
- `http://localhost:3000` ✅ **BẮT BUỘC cho React frontend**

**Authorized redirect URIs:**
- `http://localhost:8080/login/oauth2/code/google` (cho backend flow - không dùng nữa)

### Bước 4: Test
1. Đợi vài giây để Google cập nhật
2. Refresh trang login (`http://localhost:3000/login`)
3. Click nút "Sign in with Google"
4. Hoàn tất!

## Flow hoạt động:

```
Frontend (localhost:3000)
    ↓
Google Identity Services
    ↓
Nhận Google ID Token
    ↓
POST /auth/google → Backend (localhost:8080)
    ↓
Backend verify token + return JWT
    ↓
Frontend lưu JWT + fetch user info
    ↓
Redirect to Dashboard
```

## Lưu ý:
- **KHÔNG cần** Authorized redirect URIs cho flow này
- Chỉ cần **Authorized JavaScript origins** = `http://localhost:3000`
- Backend endpoint: `POST /auth/google` nhận Google ID token
