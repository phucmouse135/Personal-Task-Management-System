import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Alert } from '@/components/ui/Alert';
import { GoogleLoginButton } from '@/components/auth/GoogleLoginButton';
import toast from 'react-hot-toast';

export const RegisterPage = () => {
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    fullName: '',
    confirmPassword: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const register = useAuthStore((state) => state.register);
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (formData.password !== formData.confirmPassword) {
      setError('Mật khẩu không khớp');
      return;
    }

    setLoading(true);

    try {
      await register(formData.username, formData.email, formData.password, formData.fullName);
      toast.success('Đăng ký thành công!');
      navigate('/dashboard');
    } catch (err) {
      const errorMessage = (err as { message: string }).message || 'Đăng ký thất bại';
      setError(errorMessage);
      toast.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="bg-white p-8 rounded-lg shadow-lg">
      <h2 className="text-2xl font-bold mb-6 text-center text-gray-800">Đăng ký</h2>
      
      {error && (
        <Alert type="error">
          {error}
        </Alert>
      )}

      {/* Google Login Section */}
      <div className="mb-6 mt-4">
        <GoogleLoginButton />
      </div>

      {/* Divider */}
      <div className="relative mb-6">
        <div className="absolute inset-0 flex items-center">
          <div className="w-full border-t border-gray-300"></div>
        </div>
        <div className="relative flex justify-center text-sm">
          <span className="px-2 bg-white text-gray-500">Hoặc đăng ký bằng email</span>
        </div>
      </div>

      {/* Traditional Registration Form */}
      <form onSubmit={handleSubmit} className="space-y-4">
        <Input
          label="Họ và tên"
          value={formData.fullName}
          onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
          required
          placeholder="Nhập họ và tên"
        />

        <Input
          label="Tên đăng nhập"
          value={formData.username}
          onChange={(e) => setFormData({ ...formData, username: e.target.value })}
          required
          placeholder="Nhập tên đăng nhập"
        />

        <Input
          label="Email"
          type="email"
          value={formData.email}
          onChange={(e) => setFormData({ ...formData, email: e.target.value })}
          required
          placeholder="Nhập địa chỉ email"
        />

        <Input
          label="Mật khẩu"
          type="password"
          value={formData.password}
          onChange={(e) => setFormData({ ...formData, password: e.target.value })}
          required
          placeholder="Nhập mật khẩu"
        />

        <Input
          label="Xác nhận mật khẩu"
          type="password"
          value={formData.confirmPassword}
          onChange={(e) => setFormData({ ...formData, confirmPassword: e.target.value })}
          required
          placeholder="Nhập lại mật khẩu"
        />

        <Button type="submit" className="w-full" isLoading={loading}>
          Đăng ký
        </Button>
      </form>

      <p className="mt-4 text-center text-sm text-gray-600">
        Đã có tài khoản?{' '}
        <Link to="/login" className="text-primary-600 hover:underline font-medium">
          Đăng nhập ngay
        </Link>
      </p>
    </div>
  );
};
