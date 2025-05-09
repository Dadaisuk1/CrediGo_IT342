// src/components/LoginPage.jsx
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardFooter, CardHeader } from "@/components/ui/card";
import { FormLabel } from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { Eye, EyeOff } from 'lucide-react';
import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import credigoLogo from '../assets/images/credigo_icon.svg';
import AlertModal from '../components/AlertModal';
import { API_BASE_URL } from '../config/api.config';
import { useAuth } from '../context/AuthContext';

// Google Icon component
const GoogleIcon = () => (
  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 48 48" width="24px" height="24px">
    <path fill="#FFC107" d="M43.611,20.083H42V20H24v8h11.303c-1.649,4.657-6.08,8-11.303,8c-6.627,0-12-5.373-12-12c0-6.627,5.373-12,12-12c3.059,0,5.842,1.154,7.961,3.039l5.657-5.657C34.046,6.053,29.268,4,24,4C12.955,4,4,12.955,4,24c0,11.045,8.955,20,20,20c11.045,0,20-8.955,20-20C44,22.659,43.862,21.35,43.611,20.083z" />
    <path fill="#FF3D00" d="M6.306,14.691l6.571,4.819C14.655,15.108,18.961,12,24,12c3.059,0,5.842,1.154,7.961,3.039l5.657-5.657C34.046,6.053,29.268,4,24,4C16.318,4,9.656,8.337,6.306,14.691z" />
    <path fill="#4CAF50" d="M24,44c5.166,0,9.86-1.977,13.409-5.192l-6.19-5.238C29.211,35.091,26.715,36,24,36c-5.202,0-9.619-3.317-11.283-7.946l-6.522,5.025C9.505,39.556,16.227,44,24,44z" />
    <path fill="#1976D2" d="M43.611,20.083H42V20H24v8h11.303c-0.792,2.237-2.231,4.166-4.087,5.574l6.19,5.238C39.999,36.801,44,31.134,44,24C44,22.659,43.862,21.35,43.611,20.083z" />
  </svg>
);

function LoginPage() {
  const [usernameOrEmail, setUsernameOrEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [rememberMe, setRememberMe] = useState(false);
  const { login, loading, error, setError } = useAuth();
  const navigate = useNavigate();

  const [alertModal, setAlertModal] = useState({ open: false, title: '', message: '', type: 'info' });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    try {
      const success = await login({ usernameOrEmail, password, rememberMe });

      if (success) {
        setAlertModal({ open: true, title: 'Success', message: 'Login Successful!', type: 'success' });
        setTimeout(() => navigate('/'), 1000);
      } else {
        setAlertModal({
          open: true,
          title: 'Login Failed',
          message: 'Invalid credentials or server error. Please try again.',
          type: 'error',
        });
      }
    } catch (err) {
      console.error('Login error:', err);
      setAlertModal({
        open: true,
        title: 'Login Error',
        message: 'An unexpected error occurred. Please try again.',
        type: 'error',
      });
    }
  };

  const handleGoogleSignIn = () => {
    window.location.href = `${API_BASE_URL}/api/auth/oauth2/authorize/google`;
  };

  return (
    <>
      <AlertModal
        open={alertModal.open}
        title={alertModal.title}
        message={alertModal.message}
        type={alertModal.type}
        onClose={() => setAlertModal({ ...alertModal, open: false })}
      />

      <div className="flex items-center justify-center min-h-screen px-4 py-12 font-sans bg-credigo-dark text-credigo-light">
        <Card className="w-full max-w-md bg-credigo-input-bg border-gray-700">
          <CardHeader>
            <nav className="text-sm mb-4 text-gray-400">
              <Link to="/" className="hover:text-credigo-light">Home</Link>
              <span className="mx-2">/</span>
              <span className="cursor-default">Sign in</span>
            </nav>
            <div className="text-center">
              <img className="w-auto h-16 mx-auto mb-6" src={credigoLogo} alt="CrediGo Logo" />
              <h2 className="text-3xl font-bold tracking-tight text-credigo-light">Sign in to CrediGo</h2>
            </div>
          </CardHeader>

          <CardContent>
            <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
              {error && (
                <div className="p-3 text-sm text-red-100 bg-red-500/30 rounded-lg border border-red-500/50" role="alert">
                  <span className="font-medium">Login Error:</span> {typeof error === 'string' ? error : 'Invalid credentials or server error.'}
                </div>
              )}

              <div className="space-y-2">
                <FormLabel htmlFor="usernameOrEmail" className="text-credigo-light/80">
                  Username or Email
                </FormLabel>
                <Input
                  id="usernameOrEmail"
                  name="usernameOrEmail"
                  type="text"
                  autoComplete="username"
                  required
                  value={usernameOrEmail}
                  onChange={(e) => setUsernameOrEmail(e.target.value)}
                  className="bg-credigo-dark border-gray-600 text-credigo-light placeholder-gray-400 focus:ring-credigo-button"
                  placeholder="you@example.com or username"
                />
              </div>

              <div className="space-y-2">
                <FormLabel htmlFor="password" className="text-credigo-light/80">Password</FormLabel>
                <div className="relative">
                  <Input
                    id="password"
                    name="password"
                    type={showPassword ? 'text' : 'password'}
                    autoComplete="current-password"
                    required
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    className="pr-12 bg-credigo-dark border-gray-600 text-credigo-light placeholder-gray-400 focus:ring-credigo-button"
                    placeholder="Enter your password"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute inset-y-0 right-0 flex items-center px-3 text-gray-400 hover:text-credigo-light focus:outline-none"
                    aria-label={showPassword ? "Hide password" : "Show password"}
                  >
                    {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                  </button>
                </div>
              </div>

              <div className="flex items-center justify-between">
                <div className="flex items-center">
                  <input
                    id="remember-me"
                    name="remember-me"
                    type="checkbox"
                    checked={rememberMe}
                    onChange={(e) => setRememberMe(e.target.checked)}
                    className="h-4 w-4 text-credigo-button focus:ring-credigo-button/50 border-gray-600 rounded"
                  />
                  <label htmlFor="remember-me" className="ml-2 block text-sm text-gray-300">
                    Remember me
                  </label>
                </div>
                <div className="text-sm">
                  <a href="/forgot-password" className="font-medium text-credigo-button hover:text-opacity-80">
                    Forgot password?
                  </a>
                </div>
              </div>

              <div className="pt-4">
                <Button type="submit" disabled={loading} className="w-full bg-credigo-button text-credigo-dark hover:bg-opacity-90">
                  {loading ? 'Signing in...' : 'Sign in'}
                </Button>
              </div>
            </form>

            <div className="relative my-6">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-gray-600" />
              </div>
              <div className="relative flex justify-center text-sm">
                <span className="px-2 bg-credigo-input-bg text-gray-400">Or continue with</span>
              </div>
            </div>

            <Button
              type="button"
              onClick={handleGoogleSignIn}
              variant="outline"
              className="w-full border-gray-600 bg-credigo-dark text-credigo-light hover:bg-gray-700"
            >
              <GoogleIcon />
              <span className="ml-3">Sign in with Google</span>
            </Button>
          </CardContent>

          <CardFooter>
            <p className="w-full text-sm text-center text-gray-400">
              Don't have an account yet?{' '}
              <Link to="/register" className="font-medium text-credigo-button hover:text-opacity-80">
                Register now
              </Link>
            </p>
          </CardFooter>
        </Card>
      </div>
    </>
  );
}

export default LoginPage;
