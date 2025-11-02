import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Alert } from '@/components/ui/Alert';
import { GoogleLoginButton } from '@/components/auth/GoogleLoginButton';
import toast from 'react-hot-toast';

export const LoginPage = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const login = useAuthStore((state) => state.login);
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      await login(username, password);
      toast.success('Đăng nhập thành công!');
      navigate('/dashboard');
    } catch (err) {
      const errorMessage = (err as { message: string }).message || 'Đăng nhập thất bại';
      setError(errorMessage);
      toast.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="bg-white p-8 rounded-lg shadow-lg">
      <h2 className="text-2xl font-bold mb-6 text-center text-gray-800">Đăng nhập</h2>
      
      {error && (
        <Alert type="error" className="mb-4">
          {error}
        </Alert>
      )}

      {/* Google Login Section */}
      <div className="mb-6">
        <GoogleLoginButton />
      </div>

      {/* Divider */}
      <div className="relative mb-6">
        <div className="absolute inset-0 flex items-center">
          <div className="w-full border-t border-gray-300"></div>
        </div>
        <div className="relative flex justify-center text-sm">
          <span className="px-2 bg-white text-gray-500">Hoặc đăng nhập bằng tài khoản</span>
        </div>
      </div>

      {/* Traditional Login Form */}
      <form onSubmit={handleSubmit} className="space-y-4">
        <Input
          label="Tên đăng nhập"
          type="text"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          required
          placeholder="Nhập tên đăng nhập"
        />

        <Input
          label="Mật khẩu"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
          placeholder="Nhập mật khẩu"
        />

        <Button type="submit" className="w-full" isLoading={loading}>
          Đăng nhập
        </Button>
      </form>

      <p className="mt-4 text-center text-sm text-gray-600">
        Chưa có tài khoản?{' '}
        <Link to="/register" className="text-primary-600 hover:underline font-medium">
          Đăng ký ngay
        </Link>
      </p>
    </div>
  );
};
