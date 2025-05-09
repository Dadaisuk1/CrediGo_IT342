import jwtDecode from 'jwt-decode';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import credigoLogo from '../assets/images/credigo_icon.svg';
import { useAuth } from '../context/AuthContext';

const OAuth2RedirectHandler = () => {
  const navigate = useNavigate();
  const { setUser, setToken, fetchWalletBalance } = useAuth();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const processToken = async () => {
      try {
        // Get token from URL parameters
        const params = new URLSearchParams(window.location.search);
        const token = params.get('token');

        if (!token) {
          throw new Error('No token found in the URL');
        }

        // Decode token to get user information
        const decoded = jwtDecode(token);
        console.log('Decoded OAuth token:', decoded);

        // Store token in localStorage
        localStorage.setItem('authToken', token);

        // Update auth context
        setToken(token);
        setUser({
          id: decoded.id || decoded.sub,
          username: decoded.username || decoded.sub,
          email: decoded.email || decoded.sub,
          roles: decoded.roles || [],
          picture: decoded.picture || null
        });

        // Fetch wallet balance if needed
        await fetchWalletBalance();

        // Redirect to home page
        setTimeout(() => {
          navigate('/home');
        }, 1500);
      } catch (err) {
        console.error('OAuth redirect error:', err);
        setError('Authentication failed. Please try again.');
        setTimeout(() => {
          navigate('/login');
        }, 3000);
      } finally {
        setLoading(false);
      }
    };

    processToken();
  }, [navigate, setToken, setUser, fetchWalletBalance]);

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-credigo-dark text-credigo-light">
        <div className="text-center p-8 max-w-md">
          <img className="w-auto h-20 mx-auto mb-6" src={credigoLogo} alt="CrediGo Logo" />
          <h2 className="text-2xl font-bold mb-4">Processing your login...</h2>
          <div className="flex justify-center">
            <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-credigo-button"></div>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-credigo-dark text-credigo-light">
        <div className="text-center p-8 max-w-md">
          <img className="w-auto h-20 mx-auto mb-6" src={credigoLogo} alt="CrediGo Logo" />
          <div className="p-4 bg-red-500/20 border border-red-500/40 rounded-lg mb-4">
            <h2 className="text-xl font-bold text-red-400 mb-2">Authentication Error</h2>
            <p>{error}</p>
          </div>
          <p>Redirecting to login page...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="flex items-center justify-center min-h-screen bg-credigo-dark text-credigo-light">
      <div className="text-center p-8 max-w-md">
        <img className="w-auto h-20 mx-auto mb-6" src={credigoLogo} alt="CrediGo Logo" />
        <h2 className="text-2xl font-bold mb-4">Login Successful!</h2>
        <p className="mb-4">You've successfully logged in with Google.</p>
        <div className="flex justify-center">
          <div className="animate-pulse text-credigo-button">Redirecting to dashboard...</div>
        </div>
      </div>
    </div>
  );
};

export default OAuth2RedirectHandler;
