// src/context/AuthContext.jsx
import React, { createContext, useState, useContext, useEffect, useCallback } from 'react';
// Import API functions (assuming getWallet is exported from api.js)
import { loginUser as apiLogin, registerUser as apiRegister, getWallet as apiGetWallet } from '../services/api';
import { jwtDecode } from 'jwt-decode';

// Create the context
const AuthContext = createContext(null);

// Create the provider component
export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null); // Holds user info { id, username, email, roles }
  const [token, setToken] = useState(localStorage.getItem('authToken')); // Holds JWT token
  const [walletBalance, setWalletBalance] = useState(null); // *** State for wallet balance ***
  const [loading, setLoading] = useState(true); // Indicate initial loading state
  const [error, setError] = useState(null); // Holds login/registration/fetch errors

  // Function to fetch wallet balance (memoized with useCallback)
  const fetchWalletBalance = useCallback(async () => {
    if (!token) return; // Don't fetch if no token
    console.log("Attempting to fetch wallet balance...");
    try {
      const response = await apiGetWallet(); // Call API
      if (response.data && response.data.balance !== undefined) {
        setWalletBalance(response.data.balance);
        console.log("Wallet balance fetched:", response.data.balance);
      } else {
        throw new Error("Invalid wallet data received");
      }
    } catch (err) {
      console.error("Failed to fetch wallet balance:", err);
      // Don't necessarily clear auth state here, maybe just show error
      setError("Could not load wallet balance.");
      setWalletBalance(null); // Reset balance on error
    }
  }, [token]); // Dependency: re-fetch if token changes (login/logout)


  // Effect to check token validity and fetch user/wallet data on initial load
  useEffect(() => {
    const validateTokenAndFetchData = async () => {
      setLoading(true);
      const storedToken = localStorage.getItem('authToken');
      if (storedToken) {
        try {
          const decodedToken = jwtDecode(storedToken);
          const currentTime = Date.now() / 1000;

          if (decodedToken.exp < currentTime) {
            console.warn("Auth token expired on load.");
            logout(); // Use logout function to clear state
          } else {
            console.log("User token valid on load.");
            setToken(storedToken); // Ensure token state is set
            // Assume token contains enough info for basic user state
            // Or fetch full user details here if needed
            setUser({ username: decodedToken.sub }); // Basic user info
            await fetchWalletBalance(); // Fetch wallet balance
          }
        } catch (e) {
          console.error("Error processing token on load:", e);
          logout(); // Clear state if token is invalid
        }
      }
      setLoading(false);
    };

    validateTokenAndFetchData();
  }, [fetchWalletBalance]); // Rerun this effect only once on mount essentially, but include fetchWalletBalance


  // Login function
  const login = async (credentials) => {
    setError(null);
    setLoading(true);
    try {
      const response = await apiLogin(credentials);
      if (response.data && response.data.token) {
        const newToken = response.data.token;
        const decoded = jwtDecode(newToken);

        localStorage.setItem('authToken', newToken);
        setToken(newToken);
        setUser({
          id: response.data.userId,
          username: response.data.username,
          email: response.data.email,
          roles: response.data.roles || []
        });
        console.log("Login successful:", response.data.username);
        await fetchWalletBalance(); // Fetch wallet balance immediately after login
        setLoading(false);
        return true;
      } else {
        throw new Error(response.data?.message || "Login failed: No token received.");
      }
    } catch (err) {
      const errorMessage = err.response?.data || err.message || "Login failed.";
      console.error("Login error:", errorMessage);
      setError(errorMessage);
      setLoading(false);
      return false;
    }
  };

  // Registration function
  const register = async (userData) => {
    setError(null);
    setLoading(true);
    try {
      const response = await apiRegister(userData);
      console.log("Registration successful:", response.data);
      setLoading(false);
      return true;
    } catch (err) {
      const errorMessage = err.response?.data || err.message || "Registration failed.";
      console.error("Registration error:", errorMessage);
      setError(errorMessage);
      setLoading(false);
      return false;
    }
  };


  // Logout function
  const logout = () => {
    console.log("Logging out user.");
    localStorage.removeItem('authToken');
    setUser(null);
    setToken(null);
    setWalletBalance(null); // Clear wallet balance on logout
    setError(null); // Clear errors on logout
  };

  // Value provided to consuming components
  const value = {
    user,
    token,
    walletBalance, // Expose wallet balance
    isAuthenticated: !!user,
    loading,
    error,
    login,
    logout,
    register,
    setError,
    fetchWalletBalance // Expose function to manually refresh if needed
  };

  return (
    <AuthContext.Provider value={value}>
      {!loading && children} {/* Optionally render children only after initial load check */}
      {/* Or just: {children} */}
    </AuthContext.Provider>
  );
};

// Custom hook to use the AuthContext
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
