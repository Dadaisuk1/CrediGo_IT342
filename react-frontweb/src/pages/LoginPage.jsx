// src/components/LoginPage.jsx
import React, { useState } from 'react';
import credigoLogo from '../assets/images/credigo_icon.svg';
import AlertModal from '../components/AlertModal';
import { useAuth } from '../context/AuthContext';
import { Link, useNavigate } from 'react-router-dom';
import { Eye, EyeClosed } from 'lucide-react';

// Google Icon component (keep as is)
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
  const [showPassword, setShowPassword] = useState(false); // State for password visibility
  const { login, loading, error, setError } = useAuth();
  const navigate = useNavigate();

  // Modal state
  const [alertModal, setAlertModal] = useState({ open: false, title: '', message: '', type: 'info' });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
  
    const success = await login({ usernameOrEmail, password });
  
    if (success) {
      setAlertModal({ open: true, title: 'Success', message: 'Login Successful!', type: 'success' });
    
      setTimeout(() => {
        navigate('/');
        window.location.reload(); // optional: ensure auth context refresh
      }, 1000); // slightly longer delay for smoother UX
    }
  };

  const handleGoogleSignIn = () => {
    setAlertModal({ open: true, title: 'Info', message: 'Google Sign-In not implemented yet!', type: 'info' });
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
      {error && (
        <div className="text-red-500 text-sm mb-2 text-center">{typeof error === 'string' ? error : JSON.stringify(error)}</div>
      )}
      <div className="flex items-center justify-center min-h-screen px-4 py-12 font-sans bg-credigo-dark text-credigo-light">
        <div className="w-full max-w-md p-8 space-y-6 bg-credigo-input-bg rounded-2xl shadow-xl border border-gray-700">
          {/* Breadcrumbs */}
          <nav className="text-sm mb-4 text-gray-400">
            <Link to="/" className="hover:text-credigo-light">Home</Link>
            <span className="mx-2">/</span>
            <span className="cursor-default">Sign in</span>
          </nav>

        {/* Header */}
        <div className="text-center">
          <img className="w-auto h-16 mx-auto mb-6" src={credigoLogo} alt="CrediGo Logo" />
          <h2 className="text-3xl font-bold tracking-tight text-credigo-light">Sign in to CrediGo</h2>
        </div>

        {/* Form */}
        <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
          {/* Login Error */}
          {error && (
            <div className="p-3 text-sm text-red-100 bg-red-500/30 rounded-lg border border-red-500/50" role="alert">
              <span className="font-medium">Login Error:</span> {typeof error === 'string' ? error : 'Invalid credentials or server error.'}
            </div>
          )}

          {/* Username or Email Input */}
          <div className="space-y-2">
            <label htmlFor="usernameOrEmail" className="block text-sm font-medium text-credigo-light/80">Username or Email</label>
            <input
              id="usernameOrEmail" name="usernameOrEmail" type="text"
              autoComplete="username" required value={usernameOrEmail}
              onChange={(e) => setUsernameOrEmail(e.target.value)}
              className="block w-full px-4 py-3 text-credigo-light placeholder-gray-400 bg-credigo-dark border border-gray-600 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-credigo-button focus:border-transparent sm:text-sm"
              placeholder="you@example.com or username"
            />
          </div>

          {/* Password Input & Toggle Button */}
          <div className="space-y-2">
            <label htmlFor="password" className="block text-sm font-medium text-credigo-light/80">Password</label>
            {/* Relative container for positioning the button */}
            <div className="relative">
              <input
                id="password" name="password"
                type={showPassword ? 'text' : 'password'} // Toggle input type
                autoComplete="current-password" required value={password}
                onChange={(e) => setPassword(e.target.value)}
                // Add padding-right to make space for the button
                className="block w-full px-4 py-3 pr-12 text-credigo-light placeholder-gray-400 bg-credigo-dark border border-gray-600 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-credigo-button focus:border-transparent sm:text-sm"
                placeholder="Enter your password"
              />
              {/* Position the button inside the input field */}
              <button
                type="button" // Prevent form submission
                onClick={() => setShowPassword(!showPassword)} // Toggle state on click
                className="absolute inset-y-0 right-0 flex items-center px-3 text-gray-400 hover:text-credigo-light focus:outline-none"
                aria-label={showPassword ? "Hide password" : "Show password"}
              >
                {showPassword ? (
                  <EyeClosed className="w-6 mr-2 h-5" /> // EyeOff icon when shown
                ) : (
                  <Eye className="w-6 mr-2 h-5" /> // Eye icon when hidden
                )}
              </button>
            </div>
            {/* Forgot Password Link */}
            <div className="text-right text-sm mt-2">
              <a href="/forgot-password" className="font-medium text-credigo-button hover:text-opacity-80">
                Forgot password?
              </a>
            </div>
          </div>

          {/* Submit Button */}
          <div className="pt-4">
            <button
              type="submit"
              disabled={loading}
              className={`w-full flex justify-center px-4 py-3 text-sm font-bold text-credigo-dark bg-credigo-button border border-transparent rounded-lg shadow-sm hover:bg-opacity-90 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-credigo-dark focus:ring-credigo-button transition duration-150 ease-in-out ${loading ? 'opacity-60 cursor-not-allowed' : ''}`}
            >
              {loading ? 'Signing in...' : 'Sign in'}
            </button>
          </div>
        </form>

        {/* Divider */}
        <div className="relative my-6">
          <div className="absolute inset-0 flex items-center" aria-hidden="true"><div className="w-full border-t border-gray-600" /></div>
          <div className="relative flex justify-center text-sm"><span className="px-2 bg-credigo-input-bg text-gray-400">Or continue with</span></div>
        </div>

        {/* Google Sign-In Button */}
        <div>
          <button
            type="button"
            onClick={handleGoogleSignIn}
            className="w-full inline-flex justify-center items-center py-3 px-4 border border-gray-600 rounded-lg shadow-sm bg-credigo-dark text-sm font-medium text-credigo-light hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-credigo-dark focus:ring-credigo-button"
          >
            <GoogleIcon />
            <span className="ml-3">Sign in with Google</span>
          </button>
        </div>

        {/* Link to Registration */}
        <p className="mt-8 text-sm text-center text-gray-400">
          Don't have an account yet?{' '}
          <Link to="/register" className="font-medium text-credigo-button hover:text-opacity-80">
            Register now
          </Link>
        </p>
      </div>
    </div>
    </>
  );
}

export default LoginPage;
