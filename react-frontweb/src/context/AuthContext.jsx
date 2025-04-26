// src/context/AuthContext.jsx
import React, { createContext, useState, useContext, useEffect } from 'react';
import { loginUser as apiLogin, registerUser as apiRegister } from '../services/api'; // Import API functions
import { jwtDecode } from 'jwt-decode'; // Import jwt-decode library

// Create the context
const AuthContext = createContext(null);

// Create the provider component
export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null); // Holds user info { id, username, email, roles }
  const [token, setToken] = useState(localStorage.getItem('authToken')); // Holds JWT token
  const [loading, setLoading] = useState(true); // Indicate initial loading state
  const [error, setError] = useState(null); // Holds login/registration errors

  // Effect to check token validity on initial load
  useEffect(() => {
    if (token) {
      try {
        const decodedToken = jwtDecode(token);
        const currentTime = Date.now() / 1000; // Current time in seconds

        if (decodedToken.exp < currentTime) {
          // Token expired
          console.warn("Auth token expired on load.");
          localStorage.removeItem('authToken');
          setToken(null);
          setUser(null);
        } else {
          // Token is valid, potentially fetch user details if needed
          // For now, we can decode basic info if stored in token, or just mark as logged in
          // Assuming username is the 'sub' claim in the JWT from backend
          setUser({ username: decodedToken.sub }); // Basic user info from token
          console.log("User authenticated from stored token.");
        }
      } catch (e) {
        console.error("Error decoding token on load:", e);
        localStorage.removeItem('authToken');
        setToken(null);
        setUser(null);
      }
    }
    setLoading(false); // Finished initial check
  }, [token]); // Rerun if token changes (e.g., on login/logout)


  // Login function
  const login = async (credentials) => {
    setError(null); // Clear previous errors
    setLoading(true);
    try {
      const response = await apiLogin(credentials); // Call API service
      if (response.data && response.data.token) {
        const newToken = response.data.token;
        const decoded = jwtDecode(newToken);

        // Store token and user info
        localStorage.setItem('authToken', newToken);
        setToken(newToken);
        // Extract user info from response (adjust based on your LoginResponse DTO)
        setUser({
          id: response.data.userId,
          username: response.data.username,
          email: response.data.email,
          roles: response.data.roles || [] // Ensure roles is an array
        });
        console.log("Login successful:", response.data.username);
        setLoading(false);
        return true; // Indicate success
      } else {
        throw new Error(response.data?.message || "Login failed: No token received.");
      }
    } catch (err) {
      const errorMessage = err.response?.data || err.message || "Login failed.";
      console.error("Login error:", errorMessage);
      setError(errorMessage);
      setLoading(false);
      return false; // Indicate failure
    }
  };

  // Registration function (optional here, could be separate)
  const register = async (userData) => {
    setError(null);
    setLoading(true);
    try {
      const response = await apiRegister(userData);
      // Optionally log the user in automatically after registration
      // Or just show a success message and redirect to login
      console.log("Registration successful:", response.data);
      setLoading(false);
      return true; // Indicate success
    } catch (err) {
      const errorMessage = err.response?.data || err.message || "Registration failed.";
      console.error("Registration error:", errorMessage);
      setError(errorMessage);
      setLoading(false);
      return false; // Indicate failure
    }
  };


  // Logout function
  const logout = () => {
    console.log("Logging out user.");
    localStorage.removeItem('authToken'); // Remove token from storage
    setUser(null); // Clear user state
    setToken(null); // Clear token state
    // Optionally redirect to login page
    // window.location.href = '/login';
  };

  // Value provided to consuming components
  const value = {
    user,
    token,
    isAuthenticated: !!user, // Boolean flag: true if user object exists
    loading,
    error,
    login,
    logout,
    register, // Provide register function via context
    setError // Allow components to clear errors
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
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
