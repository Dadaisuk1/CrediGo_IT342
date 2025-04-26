import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

// Create an Axios instance with default settings
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add JWT token to Authorization header
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('authToken'); // Get token from storage
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
      console.debug('Attaching token to request.');
    } else {
      console.debug('No token found, sending request without Authorization header.');
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
  (response) => {
    return response; // Pass through successful responses
  },
  (error) => {
    console.error('API Response Error:', error.response || error.message || error);
    if (error.response && error.response.status === 401) {
      console.error('Unauthorized request (401). Token might be invalid or expired.');
      // Optional: Implement automatic logout/redirect
      localStorage.removeItem('authToken');
      window.location.href = '/login';
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
  return apiClient.post('/auth/login', credentials);
};

/**
 * Sends a registration request to the backend.
 * @param {object} userData - { username: '...', email: '...', password: '...', phoneNumber: '...', dateOfBirth: '...' }
 * @returns {Promise<axios.Response>} The Axios response object.
 */
export const registerUser = (userData) => {
  return apiClient.post('/auth/register', userData);
};

// --- Wallet API Calls ---

/**
 * Fetches the current authenticated user's wallet details.
 * @returns {Promise<axios.Response>} The Axios response object.
 */
export const getWallet = () => {
  return apiClient.get('/wallet/me');
};

/**
 * Creates a Stripe Payment Intent for topping up the wallet.
 * @param {object} topUpData - { amount: 100.00 }
 * @returns {Promise<axios.Response>} The Axios response object containing the clientSecret.
 */
export const createWalletTopUpIntent = (topUpData) => {
  return apiClient.post('/wallet/create-payment-intent', topUpData);
};

// --- Product/Platform API Calls ---

/**
 * Fetches all available products, optionally filtered by platform ID.
 * @param {number} [platformId] - Optional ID of the platform (game) to filter by.
 * @returns {Promise<axios.Response>} The Axios response object.
 */
export const getAvailableProducts = (platformId) => {
  const params = platformId ? { platformId } : {};
  return apiClient.get('/products', { params }); // Pass platformId as query param if present
};

/**
 * Fetches details for a single product by its ID.
 * @param {number} productId - The ID of the product.
 * @returns {Promise<axios.Response>} The Axios response object.
 */
export const getProductById = (productId) => {
  return apiClient.get(`/products/${productId}`);
};

/**
 * Fetches all platforms (games).
 * @returns {Promise<axios.Response>} The Axios response object.
 */
export const getPlatforms = () => {
  return apiClient.get('/platforms');
};

// --- Transaction API Calls ---

/**
 * Initiates a purchase transaction.
 * @param {object} purchaseData - { productId: ..., gameAccountId: ..., quantity: ..., gameServerId: ... }
 * @returns {Promise<axios.Response>} The Axios response object.
 */
export const purchaseProduct = (purchaseData) => {
  return apiClient.post('/transactions/purchase', purchaseData);
};

/**
 * Fetches the current authenticated user's transaction history.
 * @returns {Promise<axios.Response>} The Axios response object.
 */
export const getTransactionHistory = () => {
  return apiClient.get('/transactions/history');
};

// --- Wishlist API Calls ---

/**
 * Fetches the current authenticated user's wishlist.
 * @returns {Promise<axios.Response>} The Axios response object.
 */
export const getWishlist = () => {
  return apiClient.get('/wishlist');
};

/**
 * Adds a product to the current authenticated user's wishlist.
 * @param {number} productId - The ID of the product to add.
 * @returns {Promise<axios.Response>} The Axios response object.
 */
export const addToWishlist = (productId) => {
  return apiClient.post(`/wishlist/${productId}`);
};

/**
 * Removes a product from the current authenticated user's wishlist.
 * @param {number} productId - The ID of the product to remove.
 * @returns {Promise<axios.Response>} The Axios response object.
 */
export const removeFromWishlist = (productId) => {
  return apiClient.delete(`/wishlist/${productId}`);
};

// --- Review API Calls ---

/**
 * Fetches all reviews for a specific product.
 * @param {number} productId - The ID of the product.
 * @returns {Promise<axios.Response>} The Axios response object.
 */
export const getReviewsForProduct = (productId) => {
  return apiClient.get(`/products/${productId}/reviews`);
};

/**
 * Adds a review for a specific product (requires user to be authenticated and have purchased).
 * @param {number} productId - The ID of the product being reviewed.
 * @param {object} reviewData - { rating: ..., comment: ... }
 * @returns {Promise<axios.Response>} The Axios response object.
 */
export const addReview = (productId, reviewData) => {
  return apiClient.post(`/products/${productId}/reviews`, reviewData);
};

/**
 * Deletes the authenticated user's review for a specific product.
 * @param {number} productId - The ID of the product whose review is to be deleted.
 * @returns {Promise<axios.Response>} The Axios response object.
 */
export const deleteReview = (productId) => {
  return apiClient.delete(`/products/${productId}/reviews`);
};


// Export the configured Axios instance if needed directly elsewhere,
// but prefer using the specific functions above.
export default apiClient;
