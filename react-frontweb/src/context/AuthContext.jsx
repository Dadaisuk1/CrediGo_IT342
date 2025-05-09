// src/context/AuthContext.jsx
import jwtDecode from 'jwt-decode';
import React, { createContext, useCallback, useContext, useEffect, useState } from 'react';

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

  // Fetch wallet balance
  const fetchWalletBalance = useCallback(async () => {
    if (!token) return;
    try {
      const response = await apiGetWallet();
      if (response?.data?.balance !== undefined) {
        setWalletBalance(response.data.balance);
      }
    } catch (err) {
      console.error("Failed to fetch wallet balance:", err.message);
      setWalletBalance(null);
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
            await fetchWalletBalance();
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

      await fetchWalletBalance();
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
      const message = err.response?.data?.message || err.message || "Registration failed";
      setError(message);
      setLoading(false);
      return false;
    }
  };

  // Logout function
  const logout = () => {
    localStorage.removeItem('authToken');
    setUser(null);
    setToken(null);
    setWalletBalance(null);
    setError(null);
  };

  const value = {
    user,
    token,
    walletBalance,
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
