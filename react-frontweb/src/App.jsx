import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';

// Import Layouts and Pages from their separate files
import ProtectedLayout from './layouts/ProtectedLayout';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import HomePage from './pages/HomePage';
import ProductsPage from './pages/ProductsPage';
import WalletPage from './pages/WalletPage';
import HistoryPage from './pages/HistoryPage';
import WishlistPage from './pages/WishlistPage';
import AboutPage from './pages/AboutPage';
import PaymentPage from './pages/PaymentPage';
import Page404 from './pages/Page404';

function App() {
  const { isAuthenticated, loading } = useAuth();

  if (loading) {
    return <div className="flex items-center justify-center h-screen bg-credigo-dark text-credigo-light">Loading...</div>;
  }

  return (
    <Routes>
      {/* --- Public Routes --- */}
      <Route path="/login" element={!isAuthenticated ? <LoginPage /> : <Navigate to="/" replace />} />
      <Route path="/register" element={!isAuthenticated ? <RegisterPage /> : <Navigate to="/" replace />} />
      <Route path="/about" element={<AboutPage />} />
      {/* Add other public routes like /terms here if needed */}
      <Route path="/pay" element={<PaymentPage />} />

      {/* --- Protected Routes (Render inside ProtectedLayout) --- */}
      <Route
        path="/"
        element={isAuthenticated ? <ProtectedLayout /> : <Navigate to="/login" replace />}
      >
        {/* Child routes render inside ProtectedLayout's <Outlet /> */}
        <Route index element={<HomePage />} />
        <Route path="products" element={<ProductsPage />} />
        <Route path="wallet" element={<WalletPage />} />
        <Route path="history" element={<HistoryPage />} />
        <Route path="wishlist" element={<WishlistPage />} />
        <Route path="about" element={<AboutPage />} />
        {/* Add more protected routes here, e.g., profile page */}
        {/* <Route path="profile" element={<UserProfilePage />} /> */}
      </Route>

      {/* Catch-all 404 Route */}
      <Route path="*" element={<Page404 />} />
    </Routes>
  );
}

export default App;
