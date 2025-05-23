// src/App.jsx
import { Toaster } from "@/components/ui/toaster";
import { Suspense, lazy, useEffect } from 'react';
import { Navigate, Route, Routes, useLocation } from 'react-router-dom';
import { API_BASE_URL } from './config/api.config';
import { useAuth } from './context/AuthContext';
import PaymentSuccess from "./pages/PaymentSuccess";
import { websocketService } from './services/websocket';
import { isAdmin } from './utils/auth';

// Layouts
const ProtectedLayout = lazy(() => import('./layouts/ProtectedLayout'));
const AdminLayout = lazy(() => import('./layouts/AdminLayout'));

// Public Pages
const LandingPage = lazy(() => import('./pages/LandingPage'));
const LoginPage = lazy(() => import('./pages/LoginPage'));
const RegisterPage = lazy(() => import('./pages/RegisterPage'));
const AboutPage = lazy(() => import('./pages/AboutPage'));
const PaymentPage = lazy(() => import('./pages/PaymentPage'));
const NotAuthorized = lazy(() => import('./pages/NotAuthorized'));
const Page404 = lazy(() => import('./pages/Page404'));
const OAuth2RedirectHandler = lazy(() => import('./pages/OAuth2RedirectHandler'));

// User Pages
const HomePage = lazy(() => import('./pages/HomePage'));
const ProductsPage = lazy(() => import('./pages/ProductsPage'));
const ProductDetailPage = lazy(() => import('./pages/ProductDetailPage'));
const GameCatalogPage = lazy(() => import('./pages/GameCatalogPage'));
const GameDetailPage = lazy(() => import('./pages/GameDetailPage'));
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
const AdminPayments = lazy(() => import('./pages/AdminPayments'));

// Custom loading component that matches the app's blue theme
const Loading = () => (
  <div className="fixed inset-0 flex items-center justify-center bg-credigo-dark">
    <div className="text-center">
      <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-credigo-button mb-4"></div>
      <p className="text-credigo-light text-lg font-medium">Loading...</p>
    </div>
  </div>
);

function App() {
  const { isAuthenticated, token, user } = useAuth();
  const adminOnly = isAuthenticated && isAdmin(token);
  const location = useLocation();

  // Handle WebSocket connections based on authentication state
  useEffect(() => {
    if (isAuthenticated && user?.id) {
      websocketService.connect(user.id, null);
    } else {
      websocketService.disconnect();
    }

    return () => {
      websocketService.disconnect();
    };
  }, [isAuthenticated, user?.id, location.pathname]);

  useEffect(() => {
    // Test backend connectivity when app loads
    async function testBackendConnection() {
      console.log(`Testing connection to backend server at: ${API_BASE_URL}`);

      try {
        // Try to connect to the health endpoint
        const result = await websocketService.testServerConnection();

        if (result.success) {
          console.log('✅ Backend server is reachable');
        } else {
          console.warn('⚠️ Backend server may not be reachable:', result.error);
        }
      } catch (err) {
        console.error('❌ Backend connection test failed:', err);
      }
    }

    // Run the test
    testBackendConnection();
  }, []);

  return (
    <>
      <Suspense fallback={<Loading />}>
        <Routes>
        {/* Public Routes */}
        <Route path="/" element={!isAuthenticated ? <LandingPage /> : <Navigate to="/home" replace />} />
        <Route path="/login" element={!isAuthenticated ? <LoginPage /> : <Navigate to="/home" replace />} />
        <Route path="/register" element={!isAuthenticated ? <RegisterPage /> : <Navigate to="/home" replace />} />
        <Route path="/about" element={<AboutPage />} />
        <Route path="/pay" element={<PaymentPage />} />
        <Route path="/not-authorized" element={<NotAuthorized />} />
        <Route path="/oauth2/redirect" element={<OAuth2RedirectHandler />} />

        {/* Protected User Routes - Wrapped in ProtectedLayout */}
        <Route path="/home" element={isAuthenticated ? <ProtectedLayout /> : <Navigate to="/" replace />}>
          <Route index element={<HomePage />} />
          <Route path="products" element={<ProductsPage />} />
          <Route path="products/:productId" element={<ProductDetailPage />} />
          <Route path="games" element={<GameCatalogPage />} />
          <Route path="games/:gameId" element={<GameDetailPage />} />
          <Route path="wallet" element={<WalletPage />} />
          <Route path="history" element={<HistoryPage />} />
          <Route path="wishlist" element={<WishlistPage />} />
          <Route path="about" element={<AboutPage />} />
        </Route>

        {/* Direct routes (shorter URL) - Also wrapped in ProtectedLayout */}
        <Route path="/" element={isAuthenticated ? <ProtectedLayout /> : <Navigate to="/" replace />}>
          <Route path="products" element={<ProductsPage />} />
          <Route path="products/:productId" element={<ProductDetailPage />} />
          <Route path="games" element={<GameCatalogPage />} />
          <Route path="games/:gameId" element={<GameDetailPage />} />
          <Route path="wallet" element={<WalletPage />} />
          <Route path="history" element={<HistoryPage />} />
          <Route path="wishlist" element={<WishlistPage />} />
        </Route>

        {/* Admin Routes */}
        <Route path="/admin" element={adminOnly ? <AdminLayout /> : <NotAuthorized />}>
          <Route index element={<AdminStats />} />
          <Route path="dashboard" element={<AdminDashboard />} />
          <Route path="users" element={<AdminUsers />} />
          <Route path="transactions" element={<AdminTransactions />} />
          <Route path="products" element={<AdminProducts />} />
          <Route path="payments" element={<AdminPayments />} />
          <Route path="kyc" element={<AdminKYC />} />
          <Route path="wallet" element={<AdminWallet />} />
        </Route>

        {/* Payment Routes */}
        <Route path="/payment/success" element={<PaymentSuccess />} />

        {/* Catch-all */}
        <Route path="*" element={<Page404 />} />
        </Routes>
      </Suspense>
      <Toaster />
    </>
  );
}

export default App;
