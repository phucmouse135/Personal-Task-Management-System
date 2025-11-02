import { apiClient } from '@/lib/apiClient';
import { API_ENDPOINTS } from '@/constants';
import { AuthResponse, LoginRequest, RegisterRequest, GoogleLoginRequest, User, ApiResponse } from '@/types';

export const authService = {
  /**
   * Login user with username and password
   */
  async login(credentials: LoginRequest): Promise<AuthResponse> {
    const response = await apiClient.post<ApiResponse<AuthResponse>>(API_ENDPOINTS.LOGIN, credentials);
    return response.data.result;
  },

  /**
   * Login user with Google OAuth
   */
  async googleLogin(data: GoogleLoginRequest): Promise<AuthResponse> {
    const response = await apiClient.post<ApiResponse<AuthResponse>>(API_ENDPOINTS.GOOGLE_LOGIN, data);
    return response.data.result;
  },

  /**
   * Register new user
   */
  async register(data: RegisterRequest): Promise<AuthResponse> {
    const response = await apiClient.post<ApiResponse<AuthResponse>>(API_ENDPOINTS.REGISTER, data);
    return response.data.result;
  },

  /**
   * Logout user
   */
  async logout(): Promise<void> {
    await apiClient.post(API_ENDPOINTS.LOGOUT);
  },

  /**
   * Get current user info
   */
  async getCurrentUser(): Promise<User> {
    const response = await apiClient.get<ApiResponse<User>>(API_ENDPOINTS.ME);
    return response.data.result;
  },
};
