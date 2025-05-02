// src/App.jsx
import React from 'react';
import { Routes, Route } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import { Suspense, lazy } from 'react';
import { Navigate } from 'react-router-dom';

// Layouts
const ProtectedLayout = lazy(() => import('./layouts/ProtectedLayout'));
const AdminLayout = lazy(() => import('./layouts/AdminLayout'));

// Public Pages
const LoginPage = lazy(() => import('./pages/LoginPage'));
const RegisterPage = lazy(() => import('./pages/RegisterPage'));
const AboutPage = lazy(() => import('./pages/AboutPage'));
const PaymentPage = lazy(() => import('./pages/PaymentPage'));
const NotAuthorized = lazy(() => import('./pages/NotAuthorized'));
const Page404 = lazy(() => import('./pages/Page404'));

// User Pages
const HomePage = lazy(() => import('./pages/HomePage'));
const ProductsPage = lazy(() => import('./pages/ProductsPage'));
const WalletPage = lazy(() => import('./pages/WalletPage'));
const HistoryPage = lazy(() => import('./pages/HistoryPage'));
const WishlistPage = lazy(() => import('./pages/WishlistPage'));

// Admin Pages
const AdminDashboard = lazy(() => import('./pages/AdminDashboard'));
const AdminUsers = lazy(() => import('./pages/AdminUsers'));
const AdminTransactions = lazy(() => import('./pages/AdminTransactions'));
const AdminKYC = lazy(() => import('./pages/AdminKYC'));
const AdminWallet = lazy(() => import('./pages/AdminWallet'));
const AdminStats = lazy(() => import('./pages/AdminStats'));
const AdminProducts = lazy(() => import('./pages/AdminProducts'));

function App() {
  const { isAuthenticated, token } = useAuth();
  const adminOnly = isAuthenticated && isAdmin(token);

  return (
    <Suspense fallback={<div className="text-center mt-20 text-lg text-gray-600">Loading page...</div>}>
      <Routes>
      {/* Public Routes */}
      <Route path="/login" element={!isAuthenticated ? <LoginPage /> : <Navigate to="/" replace />} />
      <Route path="/register" element={!isAuthenticated ? <RegisterPage /> : <Navigate to="/" replace />} />
      <Route path="/about" element={<AboutPage />} />
      <Route path="/pay" element={<PaymentPage />} />
      <Route path="/not-authorized" element={<NotAuthorized />} />

      {/* Protected User Routes */}
      <Route path="/" element={<ProtectedLayout />}>
        <Route index element={<HomePage />} />
        <Route path="products" element={<ProductsPage />} />
        <Route path="wallet" element={<WalletPage />} />
        <Route path="history" element={<HistoryPage />} />
        <Route path="wishlist" element={<WishlistPage />} />
        <Route path="about" element={<AboutPage />} />
      </Route>

      {/* Admin Routes */}
      <Route path="/admin" element={adminOnly ? <AdminLayout /> : <NotAuthorized />}>
        <Route index element={<AdminStats />} />
        <Route path="dashboard" element={<AdminDashboard />} />
        <Route path="users" element={<AdminUsers />} />
        <Route path="transactions" element={<AdminTransactions />} />
        <Route path="products" element={<AdminProducts />} />
        <Route path="kyc" element={<AdminKYC />} />
        <Route path="wallet" element={<AdminWallet />} />
      </Route>

      {/* Catch-all */}
      <Route path="*" element={<Page404 />} />
      </Routes>
    </Suspense>
  );
}

export default App;