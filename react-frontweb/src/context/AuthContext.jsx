// src/context/AuthContext.jsx
import jwtDecode from 'jwt-decode';
import { createContext, useCallback, useContext, useEffect, useState } from 'react';

// Import API functions (update path as needed)
import {
  getWallet as apiGetWallet,
  loginUser as apiLogin,
  registerUser as apiRegister
} from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem('authToken'));
  const [walletBalance, setWalletBalance] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [walletError, setWalletError] = useState(false);

  // Fetch wallet balance with improved error handling
  const fetchWalletBalance = useCallback(async () => {
    if (!token) return;

    // Set a timeout for wallet balance fetch
    const walletFetchTimeout = setTimeout(() => {
      console.log("Wallet fetch is taking too long - continuing without balance");
      setWalletError(true);
      // Continue showing the app even if wallet fetch times out
      setLoading(false);
    }, 8000); // 8 seconds timeout

    try {
      const response = await apiGetWallet();
      // Clear the timeout since request completed
      clearTimeout(walletFetchTimeout);

      if (response?.data?.balance !== undefined) {
        setWalletBalance(response.data.balance);
        setWalletError(false);
      }
    } catch (err) {
      // Clear the timeout since request completed (with error)
      clearTimeout(walletFetchTimeout);

      console.error("Failed to fetch wallet balance:", err.message);
      setWalletBalance(null);
      setWalletError(true);
      // Don't block the app if wallet fetch fails - just continue
      setLoading(false);
    }
  }, [token]);

  // Validate token and fetch initial data
  useEffect(() => {
    const validateToken = async () => {
      const storedToken = localStorage.getItem('authToken');
      if (storedToken) {
        try {
          const decoded = jwtDecode(storedToken);
          const currentTime = Date.now() / 1000;

          if (decoded.exp < currentTime) {
            logout(); // Token expired
          } else {
            setToken(storedToken);
            setUser({
              id: decoded.id || decoded.sub,
              username: decoded.username || decoded.sub,
              email: decoded.email || decoded.username || '',
              roles: decoded.roles || decoded.authorities || [],
              picture: decoded.picture || null // Add picture for OAuth users
            });

            // Try to fetch wallet balance but continue even if it fails
            try {
              await fetchWalletBalance();
            } catch (e) {
              console.error("Error fetching wallet balance during initialization:", e);
              setWalletBalance(null);
              setWalletError(true);
            }
          }
        } catch (e) {
          console.error("Invalid token:", e.message);
          logout();
        }
      }
      setLoading(false);
    };

    validateToken();
  }, [fetchWalletBalance]);

  // Login function
  const login = async (credentials) => {
    setError(null);
    setLoading(true);
    try {
      const response = await apiLogin(credentials);
      const newToken = response.data.token;
      if (!newToken) throw new Error("No token received");

      const decoded = jwtDecode(newToken);
      localStorage.setItem('authToken', newToken);
      setToken(newToken);
      setUser({
        id: decoded.id || decoded.sub,
        username: decoded.username || decoded.sub,
        email: decoded.email || decoded.username || '',
        roles: decoded.roles || decoded.authorities || [],
        picture: decoded.picture || null // Add picture for OAuth users
      });

      // Try to fetch wallet balance but continue even if it fails
      try {
        await fetchWalletBalance();
      } catch (e) {
        console.error("Error fetching wallet balance during login:", e);
        setWalletBalance(null);
        setWalletError(true);
      }

      setLoading(false);
      return true;
    } catch (err) {
      const message = err.response?.data?.message || err.message || "Login failed";
      setError(message);
      setLoading(false);
      return false;
    }
  };

  // Register function
  const register = async (userData) => {
    setError(null);
    setLoading(true);
    try {
      const response = await apiRegister(userData);
      setLoading(false);
      return response;
    } catch (err) {
      // Improved error handling to extract the actual error message from the response
      console.error('Registration error:', err.response?.data || err.message);

      // Extract the error message from the response body
      // The Spring backend returns the error message directly in the response body as a string
      const errorMessage = typeof err.response?.data === 'string'
        ? err.response.data
        : (err.response?.data?.message || err.message || "Registration failed");

      setError(errorMessage);
      setLoading(false);
      return false;
    }
  };

  // Logout function
  const logout = () => {
    console.log('Logout: Clearing authentication...');

    // Clear all auth-related localStorage items
    localStorage.removeItem('authToken');
    localStorage.removeItem('token'); // In case this is also used
    localStorage.removeItem('user');  // In case this is also used

    // Clear auth state
    setUser(null);
    setToken(null);
    setWalletBalance(null);
    setError(null);
    setWalletError(false);

    // Force refresh auth state just to be safe
    console.log('Logout: Authentication cleared');

    // This helps ensure the UI updates
    window.dispatchEvent(new Event('storage'));
  };

  const value = {
    user,
    token,
    walletBalance,
    walletError,
    isAuthenticated: !!user,
    loading,
    error,
    login,
    logout,
    register,
    setError,
    fetchWalletBalance,
    setUser, // Expose setUser for OAuth2 redirect handler
    setToken // Expose setToken for OAuth2 redirect handler
  };

  return (
    <AuthContext.Provider value={value}>
      {!loading ? children : (
        <div className="flex justify-center items-center h-screen bg-gray-900 text-white">
          Loading...
        </div>
      )}
    </AuthContext.Provider>
  );
};

// Custom hook to use auth context
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
