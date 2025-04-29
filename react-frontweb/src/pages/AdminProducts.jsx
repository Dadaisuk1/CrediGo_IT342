import React, { useEffect, useState } from 'react';
import apiClient from '../services/api';

const API_URL = '/admin/products'; // Adjust this if your backend uses a different route

const AdminProducts = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [editingProduct, setEditingProduct] = useState(null);
  const [form, setForm] = useState({ name: '', price: '', imageUrl: '', platformId: '' });
  const [saving, setSaving] = useState(false);
  const [deletingId, setDeletingId] = useState(null);

  // Fetch products on mount
  useEffect(() => {
    fetchProducts();
  }, []);

  const fetchProducts = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await apiClient.get(API_URL);
      if (!Array.isArray(res.data)) {
        throw new Error('Invalid response from server. Expected product list.');
      }
      setProducts(res.data);
    } catch (err) {
      if (err.response && typeof err.response.data === 'string' && err.response.data.startsWith('<!DOCTYPE')) {
        setError('Server returned HTML instead of JSON. Check if backend is running and endpoint is correct.');
      } else {
        setError(err.message);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleEdit = (product) => {
    setEditingProduct(product.id);
    setForm({
      name: product.name || '',
      price: product.price || '',
      imageUrl: product.imageUrl || '',
      platformId: product.platformId || '',
    });
  };

  const handleCancelEdit = () => {
    setEditingProduct(null);
    setForm({ name: '', price: '', imageUrl: '', platformId: '' });
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError(null);
    try {
      if (editingProduct) {
        // Update existing
        await apiClient.put(`${API_URL}/${editingProduct}`, form);
      } else {
        // Create new
        await apiClient.post(API_URL, form);
      }
      fetchProducts();
      handleCancelEdit();
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (productId) => {
    if (!window.confirm('Are you sure you want to delete this product?')) return;
    setDeletingId(productId);
    setError(null);
    try {
      await apiClient.delete(`${API_URL}/${productId}`);
      fetchProducts();
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setDeletingId(null);
    }
  };

  return (
    <div className="container mx-auto p-6">
      <h2 className="text-2xl font-bold mb-4">Admin Product Management</h2>
      {error && <div className="bg-red-100 text-red-700 p-2 rounded mb-4">{error}</div>}
      <form onSubmit={handleSave} className="mb-6 bg-gray-800 p-4 rounded-lg shadow-md">
        <h3 className="text-lg font-semibold mb-2">{editingProduct ? 'Edit Product' : 'Add New Product'}</h3>
        <div className="flex flex-wrap gap-4">
          <input
            type="text"
            name="name"
            placeholder="Product Name"
            value={form.name}
            onChange={handleInputChange}
            className="p-2 rounded border border-gray-600 bg-gray-900 text-white"
            required
          />
          <input
            type="number"
            name="price"
            placeholder="Price"
            value={form.price}
            onChange={handleInputChange}
            className="p-2 rounded border border-gray-600 bg-gray-900 text-white"
            required
            step="0.01"
          />
          <input
            type="text"
            name="imageUrl"
            placeholder="Image URL"
            value={form.imageUrl}
            onChange={handleInputChange}
            className="p-2 rounded border border-gray-600 bg-gray-900 text-white"
          />
          <input
            type="text"
            name="platformId"
            placeholder="Platform ID"
            value={form.platformId}
            onChange={handleInputChange}
            className="p-2 rounded border border-gray-600 bg-gray-900 text-white"
            required
          />
        </div>
        <div className="mt-4 flex gap-2">
          <button
            type="submit"
            className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 disabled:opacity-50"
            disabled={saving}
          >
            {saving ? 'Saving...' : editingProduct ? 'Update Product' : 'Add Product'}
          </button>
          {editingProduct && (
            <button
              type="button"
              className="bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-600"
              onClick={handleCancelEdit}
            >
              Cancel
            </button>
          )}
        </div>
      </form>
      <div>
        <h3 className="text-lg font-semibold mb-2">Product List</h3>
        {loading ? (
          <div>Loading products...</div>
        ) : products.length === 0 ? (
          <div>No products found.</div>
        ) : (
          <table className="min-w-full bg-gray-900 rounded-lg overflow-hidden">
            <thead>
              <tr>
                <th className="px-4 py-2">ID</th>
                <th className="px-4 py-2">Name</th>
                <th className="px-4 py-2">Price</th>
                <th className="px-4 py-2">Image</th>
                <th className="px-4 py-2">Platform ID</th>
                <th className="px-4 py-2">Actions</th>
              </tr>
            </thead>
            <tbody>
              {products.map((product) => (
                <tr key={product.id} className="border-t border-gray-800">
                  <td className="px-4 py-2">{product.id}</td>
                  <td className="px-4 py-2">{product.name}</td>
                  <td className="px-4 py-2">₱{product.price}</td>
                  <td className="px-4 py-2">
                    {product.imageUrl ? (
                      <img src={product.imageUrl} alt={product.name} className="w-16 h-12 object-cover rounded" />
                    ) : (
                      <span>No Image</span>
                    )}
                  </td>
                  <td className="px-4 py-2">{product.platformId}</td>
                  <td className="px-4 py-2 flex gap-2">
                    <button
                      className="bg-yellow-500 text-white px-3 py-1 rounded hover:bg-yellow-600"
                      onClick={() => handleEdit(product)}
                      disabled={saving || deletingId === product.id}
                    >
                      Edit
                    </button>
                    <button
                      className="bg-red-600 text-white px-3 py-1 rounded hover:bg-red-700"
                      onClick={() => handleDelete(product.id)}
                      disabled={saving || deletingId === product.id}
                    >
                      {deletingId === product.id ? 'Deleting...' : 'Delete'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
};

export default AdminProducts;
