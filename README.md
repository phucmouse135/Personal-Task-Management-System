# Personal Task Management System

## Tổng quan dự án
**Personal Task Management System**  xây dựng bằng **Java Spring Boot**, hỗ trợ quản lý **user, project, task, notification real-time, chat WebSocket**, và **tích hợp thanh toán VNPAY**.

---

## 🎯 Mục tiêu
- Quản lý dự án và task cho cá nhân hoặc nhóm.  
- Hỗ trợ **real-time notifications** và **chat** giữa các thành viên.  
- Tích hợp cổng thanh toán **VNPAY** cho task hoặc dịch vụ.  
- Kiến trúc mở rộng, chuẩn hóa **REST API + JWT Security**.  
- Hỗ trợ **filtering, pagination, sorting, audit log, metrics và monitoring**.

---

## ✨ Tính năng chính

### 🔐 User & Authentication
- Đăng ký, đăng nhập, JWT authentication.  
- Role-based & attribute-based access control (Admin/User).  

### 📂 Project Management
- CRUD Projects.  
- Owner/admin có quyền chỉnh sửa/xóa.  
- Pagination, filtering, sorting.  

### ✅ Task Management
- CRUD Tasks, assign user, priority & status.  
- Overdue detection, recurring tasks.  
- Filtering, pagination, sorting, search keyword.  

### 🔔 Notification & Event-driven
- Notification khi task update, overdue hoặc reminder.  
- Hỗ trợ **email, SMS hoặc push**.  
- Asynchronous processing bằng **Spring Events/Kafka**.  

### 💬 Real-time Chat (WebSocket)
- Chat private hoặc nhóm theo dự án.  
- STOMP endpoint `/ws` với topic `/topic/messages`.  

### 💳 Payment Integration (VNPAY) - UNCOMPLETED
- Tạo order thanh toán cho task hoặc dịch vụ.  
- Callback xử lý kết quả thanh toán từ VNPAY.  
- Trạng thái: `PENDING`, `SUCCESS`, `FAILED`.  

### 📊 Analytics
- Thống kê task theo status và priority.  

### 🚀 Advanced Features
- **Caching Redis**, full-text search **Elasticsearch**.  
- Metrics & monitoring (**Micrometer + Prometheus + Grafana**).  
- Audit logs và distributed tracing (**Sleuth + Zipkin**).  
- Rate limiting (**Bucket4j**).  

---

## 🏗️ Kiến trúc tổng quan
- **Backend**: Java Spring Boot (REST API + WebSocket + Security)  
- **Database**: PostgreSQL  
- **Cache**: Redis  
- **Search**: Elasticsearch (optional)  
- **Event-driven**: Spring Events / Kafka  
- **Containerization**: Docker + Docker Compose, CI/CD pipeline  
- **Observability**: Logging, Metrics, Tracing  

---

## 🗄️ Mô hình database
Các bảng chính:
- **users, roles, user_roles, projects, tasks, notifications, chat_messages, payments**

Quan hệ:
- **N-N**: users ↔ roles  
- **1-N**: users ↔ projects  
- **1-N**: projects ↔ tasks  
- **1-N**: tasks ↔ notifications, tasks ↔ payments  
- **1-N**: users ↔ chat_messages (sender/receiver)  

*(DBML hoặc ERD có thể được đính kèm để visualize schema)*  

---

## ⚙️ Cài đặt cơ bản

### Clone repository
```bash
git clone <repo-url>
cd personal-task-management
````

### Cấu hình

* Cấu hình **PostgreSQL, Redis** trong `application-dev.yml`.

### Build & chạy với Docker Compose

```bash
docker-compose up -d
```

### Build & chạy Spring Boot

```bash
./mvnw clean install
./mvnw spring-boot:run
```

### Truy cập Swagger UI

```
http://localhost:8080/swagger-ui.html
```

---

## 🔌 API & WebSocket

### REST API endpoints

* `/auth`, `/users`, `/projects`, `/tasks`, `/notifications`, `/payments`, `/analytics`

### WebSocket

* **Endpoint**: `/ws`
* **Topic**: `/topic/messages`
* **DTO**:

  ```json
  {
    "senderId": 1,
    "receiverId": 2,
    "projectId": 3,
    "content": "Hello",
    "timestamp": "2025-09-24T12:00:00"
  }
  ```

---

## 💳 Payment (VNPAY)

* **Tạo payment order**: `POST /payments/create`
* **Callback xử lý**: `GET /payments/callback`
* **Trạng thái**: `PENDING`, `SUCCESS`, `FAILED`

---

## 🛠️ Công nghệ

* **Backend**: Java 17, Spring Boot, Spring Data JPA, Spring Security, WebSocket
* **Database**: PostgreSQL
* **Cache**: Redis
* **Search**: Elasticsearch
* **Messaging**: Kafka / Spring Events
* **Containerization & DevOps**: Docker, Docker Compose, CI/CD Pipeline
* **Observability**: SLF4J + Logback, Micrometer, Prometheus, Grafana, Sleuth + Zipkin

---

## 🧪 Testing

* **Unit tests**: Service & Utility
* **Integration tests**: Controller + Repository
* **End-to-end tests**: Postman / RestAssured
* **Security tests**: JWT, RBAC, ABAC

