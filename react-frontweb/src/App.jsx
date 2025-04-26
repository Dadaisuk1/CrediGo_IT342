// src/App.jsx
import React from 'react';
import { Routes, Route, Link, Navigate, Outlet } from 'react-router-dom'; // Import routing components
import LoginPage from './components/LoginPage'; // Assuming LoginPage.jsx exists
import RegisterPage from './components/Register'; // Assuming Register.jsx exists
import Page404 from './page/Page404'; // *** Import your 404 component ***
import { useAuth } from './context/AuthContext';

// Placeholder for pages accessible only when logged in
function ProtectedLayout() {
  const { user, logout } = useAuth();

  return (
    <div>
      <header className="bg-credigo-input-bg p-4 text-credigo-light shadow-md">
        <div className="container mx-auto flex justify-between items-center">
          <span className="font-bold">CrediGo</span>
          <nav className="space-x-4">
            {/* Add navigation links here */}
            <Link to="/" className="hover:text-credigo-accent">Home</Link>
            <Link to="/products" className="hover:text-credigo-accent">Products</Link>
            <Link to="/wallet" className="hover:text-credigo-accent">Wallet</Link>
            <Link to="/history" className="hover:text-credigo-accent">History</Link>
            <Link to="/wishlist" className="hover:text-credigo-accent">Wishlist</Link>
            {/* Display username and logout */}
            <span>Welcome, {user?.username}!</span>
            <button
              onClick={logout}
              className="px-3 py-1 bg-credigo-button text-credigo-dark rounded hover:bg-opacity-90 text-sm"
            >
              Logout
            </button>
          </nav>
        </div>
      </header>
      <main className="container mx-auto p-4">
        {/* Nested routes will render here */}
        <Outlet />
      </main>
    </div>
  );
}

// Placeholder for your main content page after login
function HomePage() {
  const { user } = useAuth();
  return (
    <div className="p-4 bg-white rounded-lg shadow">
      <h1 className="text-2xl font-semibold text-gray-800">Dashboard</h1>
      <p className="mt-2 text-gray-600">Welcome back, {user?.username}!</p>
      <p className="mt-1 text-gray-600">Your User ID: {user?.id}</p>
      <p className="mt-1 text-gray-600">Your Roles: {user?.roles?.join(', ')}</p>
      {/* Add more dashboard content here */}
    </div>
  );
}

// Placeholder for other pages
const ProductsPage = () => <div className="p-4 bg-white rounded-lg shadow"><h1 className="text-xl">Products Page (TODO)</h1></div>;
const WalletPage = () => <div className="p-4 bg-white rounded-lg shadow"><h1 className="text-xl">Wallet Page (TODO)</h1></div>;
const HistoryPage = () => <div className="p-4 bg-white rounded-lg shadow"><h1 className="text-xl">Transaction History Page (TODO)</h1></div>;
const WishlistPage = () => <div className="p-4 bg-white rounded-lg shadow"><h1 className="text-xl">Wishlist Page (TODO)</h1></div>;

function App() {
  const { isAuthenticated, loading } = useAuth();

  // Optional: Show a loading indicator while checking auth state
  if (loading) {
    return <div className="flex items-center justify-center h-screen bg-credigo-dark text-credigo-light">Loading...</div>;
  }

  return (
    <Routes>
      {/* Public Routes */}
      <Route path="/login" element={!isAuthenticated ? <LoginPage /> : <Navigate to="/" />} />
      <Route path="/register" element={!isAuthenticated ? <RegisterPage /> : <Navigate to="/" />} />

      {/* Protected Routes */}
      <Route
        path="/"
        element={isAuthenticated ? <ProtectedLayout /> : <Navigate to="/login" />}
      >
        {/* Default route after login */}
        <Route index element={<HomePage />} />
        {/* Other protected routes nested within ProtectedLayout */}
        <Route path="products" element={<ProductsPage />} />
        <Route path="wallet" element={<WalletPage />} />
        <Route path="history" element={<HistoryPage />} />
        <Route path="wishlist" element={<WishlistPage />} />
        {/* Add more protected routes here */}
      </Route>

      {/* *** Catch-all 404 Route *** */}
      {/* This route will only match if none of the above routes did */}
      <Route path="*" element={<Page404 />} />
    </Routes>
  );
}

export default App;
