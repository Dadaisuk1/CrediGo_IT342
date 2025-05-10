import { useEffect, useState } from 'react';
import { FaEdit, FaPencilAlt, FaPlus, FaSearch, FaTimesCircle, FaTrash } from 'react-icons/fa';
import apiClient from '../services/api';

const API_URL = '/api/products/admin'; // Updated to match Spring Boot controller endpoint

const AdminProducts = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [editingProduct, setEditingProduct] = useState(null);
  const [form, setForm] = useState({ name: '', price: '', description: '', imageUrl: '', platformId: '', itemCode: '' });
  const [saving, setSaving] = useState(false);
  const [deletingId, setDeletingId] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [platforms, setPlatforms] = useState([]);

  // Fetch products and platforms on mount
  useEffect(() => {
    fetchProducts();
    fetchPlatforms();
  }, []);

  const fetchPlatforms = async () => {
    try {
      const res = await apiClient.get('/api/platforms');
      setPlatforms(Array.isArray(res.data) ? res.data : []);
    } catch (err) {
      console.error('Failed to fetch platforms:', err);
    }
  };

  const fetchProducts = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await apiClient.get('/api/products'); // Fetch from public endpoint for all products
      if (!Array.isArray(res.data)) {
        throw new Error('Invalid response from server. Expected product list.');
      }
      setProducts(res.data);
    } catch (err) {
      if (err.response && typeof err.response.data === 'string' && err.response.data.startsWith('<!DOCTYPE')) {
        setError('Server returned HTML instead of JSON. Check if backend is running and endpoint is correct.');
      } else {
        setError(err.message || 'Failed to fetch products');
      }
      console.error('Error fetching products:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    let processedValue = value;

    // Convert price to proper number format
    if (name === 'price') {
      // Remove any non-digit and non-period characters
      processedValue = value.replace(/[^\d.]/g, '');
    }

    setForm((prev) => ({ ...prev, [name]: processedValue }));
  };

  const handleEdit = (product) => {
    setEditingProduct(product.id);
    setForm({
      name: product.name || '',
      description: product.description || '',
      price: product.price || '',
      imageUrl: product.imageUrl || '',
      platformId: product.platform?.id || '',
      itemCode: product.itemCode || '',
      isAvailable: product.isAvailable || true
    });
  };

  const handleCancelEdit = () => {
    setEditingProduct(null);
    setForm({ name: '', description: '', price: '', imageUrl: '', platformId: '', itemCode: '', isAvailable: true });
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError(null);

    // Form validation
    if (!form.name || !form.price || !form.platformId) {
      setError('Name, price, and platform are required!');
      setSaving(false);
      return;
    }

    try {
      const productData = {
        ...form,
        price: parseFloat(form.price),
        platformId: parseInt(form.platformId)
      };

      if (editingProduct) {
        // Update existing product
        await apiClient.put(`${API_URL}/${editingProduct}`, productData);
      } else {
        // Create new product
        await apiClient.post(API_URL, productData);
      }
      fetchProducts();
      handleCancelEdit();
    } catch (err) {
      setError(err.response?.data || err.message || 'Error saving product');
      console.error('Error saving product:', err);
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
      setError(err.response?.data || err.message || 'Error deleting product');
      console.error('Error deleting product:', err);
    } finally {
      setDeletingId(null);
    }
  };

  // Filter products based on search term
  const filteredProducts = products.filter(
    product =>
      product.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      product.description?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      product.platform?.name.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="bg-white rounded-xl shadow-md border border-gray-100 p-6">
        <div className="flex flex-col md:flex-row md:items-center md:justify-between">
          <div>
            <h1 className="text-2xl font-bold text-[#232946]">Product Management</h1>
            <p className="text-gray-500 mt-1">Manage game top-up items and digital goods</p>
          </div>
        </div>
      </div>

      {/* Product Form */}
      <div className="bg-white rounded-xl shadow-md border border-gray-100 p-6">
        <form onSubmit={handleSave} className="space-y-4">
          <h2 className="text-lg font-semibold text-[#232946] mb-4">
            {editingProduct ? 'Edit Product' : 'Add New Product'}
          </h2>

          {error && (
            <div className="bg-red-50 text-red-700 p-3 rounded-lg border border-red-200 mb-4">
              {error}
            </div>
          )}

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {/* Name */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Product Name *</label>
              <input
                type="text"
                name="name"
                placeholder="e.g., 100 Diamonds"
                value={form.name}
                onChange={handleInputChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#eebbc3] focus:border-[#eebbc3] outline-none transition"
                required
              />
            </div>

            {/* Price */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Price (₱) *</label>
              <input
                type="text"
                name="price"
                placeholder="e.g., 99.99"
                value={form.price}
                onChange={handleInputChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#eebbc3] focus:border-[#eebbc3] outline-none transition"
                required
              />
            </div>

            {/* Platform */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Platform/Game *</label>
              <select
                name="platformId"
                value={form.platformId}
                onChange={handleInputChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#eebbc3] focus:border-[#eebbc3] outline-none transition"
                required
              >
                <option value="">Select Platform</option>
                {platforms.map(platform => (
                  <option key={platform.id} value={platform.id}>{platform.name}</option>
                ))}
              </select>
            </div>

            {/* Image URL */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Image URL</label>
              <input
                type="text"
                name="imageUrl"
                placeholder="https://example.com/image.jpg"
                value={form.imageUrl}
                onChange={handleInputChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#eebbc3] focus:border-[#eebbc3] outline-none transition"
              />
            </div>

            {/* Item Code */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Item Code</label>
              <input
                type="text"
                name="itemCode"
                placeholder="Optional product code"
                value={form.itemCode}
                onChange={handleInputChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#eebbc3] focus:border-[#eebbc3] outline-none transition"
              />
            </div>

            {/* Description */}
            <div className="md:col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
              <textarea
                name="description"
                placeholder="Product description"
                value={form.description}
                onChange={handleInputChange}
                rows="3"
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#eebbc3] focus:border-[#eebbc3] outline-none transition"
              />
            </div>
          </div>

          <div className="flex gap-2 pt-2">
            <button
              type="submit"
              className="px-4 py-2 bg-[#232946] text-white rounded-lg hover:bg-[#1a2035] transition-colors flex items-center"
              disabled={saving}
            >
              {saving ? (
                <>
                  <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  Saving...
                </>
              ) : editingProduct ? (
                <>
                  <FaPencilAlt className="mr-1" /> Update Product
                </>
              ) : (
                <>
                  <FaPlus className="mr-1" /> Add Product
                </>
              )}
            </button>
            {editingProduct && (
              <button
                type="button"
                className="px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-colors flex items-center"
                onClick={handleCancelEdit}
              >
                <FaTimesCircle className="mr-1" /> Cancel
              </button>
            )}
          </div>
        </form>
      </div>

      {/* Search & Products List */}
      <div className="bg-white rounded-xl shadow-md border border-gray-100 overflow-hidden">
        <div className="p-6 border-b border-gray-100">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-semibold text-[#232946]">Products</h2>
            <div className="relative w-64">
              <input
                type="text"
                placeholder="Search products..."
                className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#eebbc3] focus:border-[#eebbc3] outline-none transition"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
              <FaSearch className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
            </div>
          </div>
        </div>

        {loading ? (
          <div className="flex justify-center items-center h-64">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-[#eebbc3]"></div>
          </div>
        ) : filteredProducts.length === 0 ? (
          <div className="p-10 text-center text-gray-500">
            {searchTerm ? 'No products match your search' : 'No products available. Create your first product above.'}
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Product</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Platform</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Price</th>
                  <th className="px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {filteredProducts.map((product) => (
                  <tr key={product.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center">
                        {product.imageUrl ? (
                          <img src={product.imageUrl} alt={product.name} className="w-12 h-12 object-cover rounded-md mr-3" />
                        ) : (
                          <div className="w-12 h-12 bg-gray-200 rounded-md mr-3 flex items-center justify-center text-gray-500">
                            No img
                          </div>
                        )}
                        <div>
                          <div className="font-medium text-gray-900">{product.name}</div>
                          {product.itemCode && <div className="text-xs text-gray-500">Code: {product.itemCode}</div>}
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                        {product.platform?.name || 'Unknown'}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-gray-900 font-medium">
                      ₱{parseFloat(product.price).toFixed(2)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                      <button
                        onClick={() => handleEdit(product)}
                        className="text-[#eebbc3] hover:text-[#d39ba4] mr-3"
                        disabled={saving || deletingId === product.id}
                      >
                        <FaEdit size={18} />
                      </button>
                      <button
                        onClick={() => handleDelete(product.id)}
                        className="text-red-500 hover:text-red-700"
                        disabled={saving || deletingId === product.id}
                      >
                        {deletingId === product.id ? (
                          <svg className="animate-spin h-4 w-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                          </svg>
                        ) : (
                          <FaTrash size={16} />
                        )}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

export default AdminProducts;
