// Dedicated admin user management API helpers
import apiClient from './api';

export const getAllUsers = () => apiClient.get('/admin/users');
export const createUser = (userData) => apiClient.post('/admin/users', userData);
export const updateUser = (userId, userData) => apiClient.put(`/admin/users/${userId}`, userData);
export const deleteUser = (userId) => apiClient.delete(`/admin/users/${userId}`);

// Optionally, fetch single user details
export const getUserById = (userId) => apiClient.get(`/admin/users/${userId}`);
