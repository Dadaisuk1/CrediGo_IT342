import axios from 'axios';
import { toast } from 'react-toastify';
import { API_BASE_URL } from '../config/api.config';

// Get the base URL for the current environment
const FRONTEND_URL = import.meta.env.DEV
  ? 'http://localhost:5173'
  : 'https://credi-go-it-342.vercel.app';

// Create axios instance with base URL
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
    'X-Frontend-Url': FRONTEND_URL // Add custom header for PayMongo redirects
  },
  timeout: 30000, // Increased from 10s to 30s to prevent timeout issues
});

// Request interceptor to add JWT token to Authorization header
apiClient.interceptors.request.use(
  (config) => {
    // Check if this is a public endpoint that doesn't require authentication
    const isPublicEndpoint = config.url.includes('/api/auth/') ||
                            config.url.includes('/api/products') ||
                            config.url.includes('/api/platforms') ||
                            config.url.startsWith('/api/public/');

    const token = localStorage.getItem('authToken');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    } else if (!isPublicEndpoint) {
      console.warn('No token found for protected route:', config.url);
      // Only redirect for protected endpoints
      window.location.href = '/login';
    }
    return config;
  },
  (error) => {
    console.error('Request interceptor error:', error);
    return Promise.reject(error);
  }
);

// Response interceptor to handle global errors like 401 Unauthorized
apiClient.interceptors.response.use(
  (response) => response, // Pass through successful responses
  (error) => {
    console.error('API Response Error:', error.response || error.message || error);
    if (error.response) {
      const { status } = error.response;
      if (status === 401) {
        console.error('Unauthorized request (401). Token might be invalid or expired.');
        localStorage.removeItem('authToken');
        window.location.href = '/login';
      } else if (status === 404) {
        toast.error('Resource not found (404).');
        window.location.href = '/404';
      } else if (status >= 500) {
        toast.error('Server error. Please try again later.');
      }
    } else if (error.code === 'ECONNABORTED') {
      toast.error('Request timed out. The server is taking too long to respond.');
    } else {
      toast.error('Network error. Please check your connection.');
    }
    return Promise.reject(error);
  }
);

// --- Authentication API Calls ---

/**
 * Sends a login request to the backend.
 * @param {object} credentials - { usernameOrEmail: '...', password: '...' }
 * @returns {Promise<axios.Response>} The Axios response object.
 */
export const loginUser = (credentials) => {
  return apiClient.post('/api/auth/login', credentials);
};

/**
 * Sends a registration request to the backend.
 * @param {object} userData - { username: '...', email: '...', password: '...', phoneNumber: '...', dateOfBirth: '...' }
 * @returns {Promise<axios.Response>} The Axios response object.
 */
export const registerUser = (userData) => {
  return apiClient.post('/api/auth/register', userData);
};

// --- Wallet API Calls ---

/**
 * Fetches the current authenticated user's wallet details.
 * @returns {Promise<axios.Response>} The Axios response object.
 */
export const getWallet = () => {
  // Use a longer timeout for wallet operations which might be slower
  return apiClient.get('/api/wallet/me', {
    timeout: 45000, // 45 seconds timeout for wallet requests
  });
};

/**
 * Creates a wallet top-up intent with PayMongo.
 * @param {object} paymentData - Payment data including amount and payment type
 * @returns {Promise<axios.Response>} The Axios response object.
 */
export const createWalletTopUpIntent = (paymentData) => {
  return apiClient.post('/api/payments/wallet/topup', paymentData, {
    timeout: 45000, // 45 seconds timeout for payment requests
  });
};

/**
 * Checks the status of a payment intent and automatically credits wallet if successful.
 * @param {string} paymentIntentId - The ID of the payment intent to check.
 * @returns {Promise<axios.Response>} The Axios response object with payment status.
 */
export const checkPaymentStatus = (paymentIntentId) => {
  return apiClient.get(`/api/payments/status/${paymentIntentId}`, {
    timeout: 45000, // 45 seconds timeout for payment status checks
  });
};

// --- Product/Platform API Calls ---

/**
 * Fetches all available products, optionally filtered by platform ID.
 * @param {number} [platformId] - Optional ID of the platform (game) to filter by.
 * @returns {Promise<axios.Response>} The Axios response object.
 */
export const getAvailableProducts = (platformId) => {
  const params = platformId ? { platformId } : {};
  return apiClient.get('/api/products', { params });
};

/**
 * Fetches details for a single product by its ID.
 * @param {number} productId - The ID of the product.
 * @returns {Promise<axios.Response>} The Axios response object.
 */
export const getProductById = (productId) => {
  return apiClient.get(`/api/products/${productId}`);
};

/**
 * Fetches all platforms (games).
 * @returns {Promise<axios.Response>} The Axios response object.
 */
export const getPlatforms = () => {
  return apiClient.get('/api/platforms');
};

// --- Transaction API Calls ---

/**
 * Initiates a purchase transaction.
 * @param {object} purchaseData - { productId: ..., gameAccountId: ..., quantity: ..., gameServerId: ... }
 * @returns {Promise<axios.Response>} The Axios response object.
 */
export const purchaseProduct = (purchaseData) => {
  return apiClient.post('/api/transactions/purchase', purchaseData);
};

/**
 * Fetches the current authenticated user's transaction history.
 * @returns {Promise<axios.Response>} The Axios response object.
 */
export const getTransactionHistory = () => {
  return apiClient.get('/api/transactions/history');
};

// --- Wishlist API Calls ---

/**
 * Fetches the current authenticated user's wishlist.
 * @returns {Promise<axios.Response>} The Axios response object.
 */
export const getWishlist = () => {
  return apiClient.get('/api/wishlist');
};

/**
 * Adds a product to the current authenticated user's wishlist.
 * @param {number} productId - The ID of the product to add.
 * @returns {Promise<axios.Response>} The Axios response object.
 */
export const addToWishlist = (productId) => {
  return apiClient.post(`/api/wishlist/${productId}`);
};

/**
 * Removes a product from the current authenticated user's wishlist.
 * @param {number} productId - The ID of the product to remove.
 * @returns {Promise<axios.Response>} The Axios response object.
 */
export const removeFromWishlist = (productId) => {
  return apiClient.delete(`/api/wishlist/${productId}`);
};

// --- Review API Calls ---

/**
 * Fetches all reviews for a specific product.
 * @param {number} productId - The ID of the product.
 * @returns {Promise<axios.Response>} The Axios response object.
 */
export const getProductReviews = (productId) => {
  return apiClient.get(`/api/products/${productId}/reviews`);
};

/**
 * Submits a review for a product.
 * @param {number} productId - The ID of the product to review.
 * @param {object} reviewData - { rating: Number (1-5), comment: String }
 * @returns {Promise<axios.Response>} The Axios response object.
 */
export const submitProductReview = (productId, reviewData) => {
  return apiClient.post(`/api/products/${productId}/reviews`, reviewData);
};

/**
 * Updates an existing review for a product.
 * @param {number} productId - The ID of the product.
 * @param {object} reviewData - { rating: Number (1-5), comment: String }
 * @returns {Promise<axios.Response>} The Axios response object.
 */
export const updateProductReview = (productId, reviewData) => {
  return apiClient.put(`/api/products/${productId}/reviews`, reviewData);
};

/**
 * Deletes a user's review for a specific product.
 * @param {number} productId - The ID of the product.
 * @returns {Promise<axios.Response>} The Axios response object.
 */
export const deleteProductReview = (productId) => {
  return apiClient.delete(`/api/products/${productId}/reviews`);
};

// --- Admin Dashboard API Calls ---

/**
 * Fetches admin dashboard statistics.
 * @returns {Promise<axios.Response>} The Axios response object.
 */
export const getAdminDashboardStats = () => {
  return apiClient.get('/api/admin/stats');
};

// --- Admin KYC API Calls ---

/**
 * Fetches all KYC requests (admin only).
 * @returns {Promise<axios.Response>} The Axios response object.
 */
export const getKYCRequests = () => {
  return apiClient.get('/api/admin/kyc');
};

/**
 * Approves a KYC request.
 * @param {number} id - The KYC request ID.
 * @param {string} [comment] - Optional admin comment.
 * @returns {Promise<axios.Response>} The Axios response object.
 */
export const approveKYCRequest = (id, comment = '') => {
  return apiClient.put(`/api/admin/kyc/${id}/approve`, null, { params: { comment } });
};

/**
 * Rejects a KYC request.
 * @param {number} id - The KYC request ID.
 * @param {string} [comment] - Optional admin comment.
 * @returns {Promise<axios.Response>} The Axios response object.
 */
export const rejectKYCRequest = (id, comment = '') => {
  return apiClient.put(`/api/admin/kyc/${id}/reject`, null, { params: { comment } });
};

/**
 * Deletes a KYC request.
 * @param {number} id - The KYC request ID.
 * @returns {Promise<axios.Response>} The Axios response object.
 */
export const deleteKYCRequest = (id) => {
  return apiClient.delete(`/api/admin/kyc/${id}`);
};

/**
 * Manually confirms a payment for testing/demo purposes.
 * @param {object} paymentData - { paymentIntentId: '...', amount: 100.00, targetUsername: '...' (optional) }
 * @returns {Promise<axios.Response>} The Axios response object.
 */
export const confirmTestPayment = (paymentData) => {
  return apiClient.post('/api/payments/test-confirm-payment', paymentData);
};

/**
 * Fetches all pending payments that are awaiting confirmation.
 * @returns {Promise<axios.Response>} The Axios response object.
 */
export const getPendingPayments = () => {
  return apiClient.get('/api/payments/pending');
};

export default apiClient;
