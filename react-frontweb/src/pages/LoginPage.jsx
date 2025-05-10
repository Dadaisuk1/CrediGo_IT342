// src/components/LoginPage.jsx
import { motion } from 'framer-motion';
import { ArrowLeft, Eye, EyeOff } from 'lucide-react';
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

// Discord Icon component
const DiscordIcon = () => (
  <svg className="h-5 w-5" fill="currentColor" viewBox="0 0 24 24" aria-hidden="true">
    <path d="M20.317 4.37a19.791 19.791 0 0 0-4.885-1.515.074.074 0 0 0-.079.037c-.21.375-.444.864-.608 1.25a18.27 18.27 0 0 0-5.487 0 12.64 12.64 0 0 0-.617-1.25.077.077 0 0 0-.079-.037A19.736 19.736 0 0 0 3.677 4.37a.07.07 0 0 0-.032.027C.533 9.046-.32 13.58.099 18.057a.082.082 0 0 0 .031.057 19.9 19.9 0 0 0 5.993 3.03.078.078 0 0 0 .084-.028c.462-.63.874-1.295 1.226-1.994a.076.076 0 0 0-.041-.106 13.107 13.107 0 0 1-1.872-.892.077.077 0 0 1-.008-.128 10.2 10.2 0 0 0 .372-.292.074.074 0 0 1 .077-.01c3.928 1.793 8.18 1.793 12.062 0a.074.074 0 0 1 .078.01c.12.098.246.198.373.292a.077.077 0 0 1-.006.127 12.299 12.299 0 0 1-1.873.892.077.077 0 0 0-.041.107c.36.698.772 1.362 1.225 1.993a.076.076 0 0 0 .084.028 19.839 19.839 0 0 0 6.002-3.03.077.077 0 0 0 .032-.054c.5-5.177-.838-9.674-3.549-13.66a.061.061 0 0 0-.031-.03zM8.02 15.33c-1.183 0-2.157-1.085-2.157-2.419 0-1.333.956-2.419 2.157-2.419 1.21 0 2.176 1.096 2.157 2.42 0 1.333-.956 2.418-2.157 2.418zm7.975 0c-1.183 0-2.157-1.085-2.157-2.419 0-1.333.955-2.419 2.157-2.419 1.21 0 2.176 1.096 2.157 2.42 0 1.333-.946 2.418-2.157 2.418z"/>
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
    window.location.href = `${API_BASE_URL}/api/auth/oauth2/authorize/google?redirect_uri=${window.location.origin}/oauth2/redirect`;
  };

  const handleDiscordSignIn = () => {
    setAlertModal({
      open: true,
      title: 'Not Implemented',
      message: 'Discord Sign-In is not implemented yet!',
      type: 'info'
    });
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

      <div className="min-h-screen flex items-center justify-center px-4 py-12 bg-credigo-dark text-credigo-light relative overflow-hidden">
        {/* Abstract shapes background */}
        <div className="absolute inset-0 overflow-hidden -z-10">
          <div className="absolute -top-20 -right-20 w-64 h-64 bg-purple-600 rounded-full filter blur-3xl opacity-20"></div>
          <div className="absolute top-40 -left-20 w-72 h-72 bg-blue-500 rounded-full filter blur-3xl opacity-20"></div>
          <div className="absolute bottom-20 right-1/3 w-80 h-80 bg-cyan-400 rounded-full filter blur-3xl opacity-10"></div>
        </div>

        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
          className="w-full max-w-md"
        >
          <div className="absolute top-4 left-4 md:top-8 md:left-8">
            <Link
              to="/"
              className="flex items-center text-credigo-light hover:text-credigo-accent transition-colors"
            >
              <ArrowLeft className="w-5 h-5 mr-2" />
              <span>Back to Home</span>
            </Link>
          </div>

          <div className="w-full overflow-hidden backdrop-blur-sm bg-credigo-input-bg/95 border border-gray-700/50 shadow-xl rounded-xl">
            <div className="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-credigo-accent via-purple-500 to-blue-500"></div>

            {/* Header */}
            <div className="text-center p-6 pb-0">
              <div className="relative mx-auto w-20 h-20 mb-2">
                <div className="absolute inset-0 rounded-full bg-gradient-to-r from-credigo-accent to-purple-500 animate-pulse blur-md opacity-50"></div>
                <div className="relative flex items-center justify-center w-full h-full rounded-full bg-credigo-dark/80 border border-white/10">
                  <img className="w-12 h-12" src={credigoLogo} alt="CrediGo Logo" />
                </div>
              </div>
              <h2 className="text-3xl font-bold">Welcome Back</h2>
              <p className="text-gray-400">Sign in to continue your gaming journey</p>
            </div>

            {/* Form */}
            <div className="p-6">
              <form className="space-y-5" onSubmit={handleSubmit}>
                {error && (
                  <div className="p-3 text-sm text-red-100 bg-red-500/30 rounded-lg border border-red-500/50" role="alert">
                    <span className="font-medium">Login Error:</span> {typeof error === 'string' ? error : 'Invalid credentials or server error.'}
                  </div>
                )}

                <div className="space-y-2">
                  <label htmlFor="usernameOrEmail" className="block text-sm font-medium text-credigo-light/80">
                    Username or Email <span className="text-red-400">*</span>
                  </label>
                  <input
                    id="usernameOrEmail"
                    name="usernameOrEmail"
                    type="text"
                    autoComplete="username"
                    required
                    value={usernameOrEmail}
                    onChange={(e) => setUsernameOrEmail(e.target.value)}
                    className="block w-full px-4 py-3 text-credigo-light placeholder-gray-400 bg-credigo-dark border border-gray-700 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-credigo-accent focus:border-transparent sm:text-sm"
                    placeholder="you@example.com or username"
                  />
                </div>

                <div className="space-y-2">
                  <label htmlFor="password" className="block text-sm font-medium text-credigo-light/80">
                    Password <span className="text-red-400">*</span>
                  </label>
                  <div className="relative">
                    <input
                      id="password"
                      name="password"
                      type={showPassword ? 'text' : 'password'}
                      autoComplete="current-password"
                      required
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      className="block w-full px-4 py-3 pr-12 text-credigo-light placeholder-gray-400 bg-credigo-dark border border-gray-700 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-credigo-accent focus:border-transparent sm:text-sm"
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
                      className="h-4 w-4 rounded border-gray-600 bg-credigo-dark text-credigo-accent focus:ring-credigo-accent/50"
                    />
                    <label htmlFor="remember-me" className="ml-2 block text-sm text-gray-300">
                      Remember me
                    </label>
                  </div>
                  <div className="text-sm">
                    <a href="/forgot-password" className="font-medium text-credigo-accent hover:text-opacity-80 transition-colors">
                      Forgot password?
                    </a>
                  </div>
                </div>

                <div className="pt-4">
                  <button
                    type="submit"
                    disabled={loading}
                    className="w-full flex justify-center px-4 py-3 text-sm font-bold text-credigo-dark bg-gradient-to-r from-credigo-accent to-purple-500 border border-transparent rounded-lg shadow-sm hover:shadow-lg hover:shadow-purple-500/20 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-credigo-input-bg focus:ring-credigo-accent transition-all"
                  >
                    {loading ? 'SIGNING IN...' : 'SIGN IN'}
                  </button>
                </div>
              </form>

              <div className="relative my-6">
                <div className="absolute inset-0 flex items-center" aria-hidden="true">
                  <div className="w-full border-t border-gray-700" />
                </div>
                <div className="relative flex justify-center text-sm">
                  <span className="px-2 bg-credigo-input-bg text-gray-400">Or continue with</span>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <button
                  type="button"
                  onClick={handleGoogleSignIn}
                  className="inline-flex justify-center items-center py-2.5 px-4 border border-gray-700 rounded-lg shadow-sm bg-credigo-dark/50 text-sm font-medium text-credigo-light hover:bg-gray-800 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-credigo-dark focus:ring-credigo-accent transition-colors"
                >
                  <GoogleIcon />
                  <span className="ml-2">Google</span>
                </button>

                <button
                  type="button"
                  onClick={handleDiscordSignIn}
                  className="inline-flex justify-center items-center py-2.5 px-4 border border-gray-700 rounded-lg shadow-sm bg-credigo-dark/50 text-sm font-medium text-credigo-light hover:bg-gray-800 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-credigo-dark focus:ring-credigo-accent transition-colors"
                >
                  <DiscordIcon />
                  <span className="ml-2">Discord</span>
                </button>
              </div>

              <p className="mt-8 text-center text-gray-400">
                Don't have an account?{' '}
                <Link to="/register" className="font-medium text-credigo-accent hover:text-opacity-80 transition-colors">
                  Sign up now
                </Link>
              </p>
            </div>
          </div>
        </motion.div>
      </div>
    </>
  );
}

export default LoginPage;
