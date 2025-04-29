import React, { useEffect, useState } from 'react';
import {
  getAllUsers,
  createUser,
  updateUser,
  deleteUser,
} from '../services/adminUsers';

const AdminUsers = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [actionLoading, setActionLoading] = useState({});
  const [showForm, setShowForm] = useState(false);
  const [editingUser, setEditingUser] = useState(null);
  const [formState, setFormState] = useState({ username: '', email: '', active: true, balance: 0 });

  // Fetch users on mount
  useEffect(() => {
    fetchUsers();
  }, []);

  // Fetch users
  const fetchUsers = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await getAllUsers();
      if (!Array.isArray(res.data)) throw new Error('Invalid response from server. Expected user list.');
      setUsers(res.data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  // If you see "Unexpected token <" errors, it means the backend returned HTML, not JSON. Check backend status and endpoint URL.


  // CRUD Handlers
  const handleEdit = (user) => {
    setEditingUser(user);
    setFormState({
      username: user.username || '',
      email: user.email || '',
      active: user.active !== undefined ? user.active : true,
      balance: user.balance || 0,
    });
    setShowForm(true);
  };

  const handleDelete = async (userId) => {
    if (!window.confirm('Delete this user?')) return;
    setActionLoading((prev) => ({ ...prev, [userId]: true }));
    try {
      await deleteUser(userId);
      await fetchUsers();
    } catch (err) {
      alert(err.message);
    } finally {
      setActionLoading((prev) => ({ ...prev, [userId]: false }));
    }
  };

  const handleFormSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingUser) {
        await updateUser(editingUser.id, formState);
      } else {
        await createUser(formState);
      }
      setShowForm(false);
      setEditingUser(null);
      setFormState({ username: '', email: '', active: true, balance: 0 });
      await fetchUsers();
    } catch (err) {
      alert(err.message);
    }
  };

  const handleAddUser = () => {
    setEditingUser(null);
    setFormState({ username: '', email: '', active: true, balance: 0 });
    setShowForm(true);
  };


  return (
    <div className="p-6">
      <h2 className="text-2xl font-bold mb-4">User Management</h2>
      <button
        className="mb-4 bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700"
        onClick={handleAddUser}
      >
        + Add User
      </button>
      {showForm && (
        <div className="mb-6 p-4 bg-gray-100 border rounded-lg max-w-md">
          <form onSubmit={handleFormSubmit}>
            <div className="mb-2">
              <label className="block text-sm font-semibold">Username</label>
              <input
                className="w-full p-2 rounded border"
                value={formState.username}
                onChange={e => setFormState(f => ({ ...f, username: e.target.value }))}
                required
              />
            </div>
            <div className="mb-2">
              <label className="block text-sm font-semibold">Email</label>
              <input
                className="w-full p-2 rounded border"
                value={formState.email}
                onChange={e => setFormState(f => ({ ...f, email: e.target.value }))}
                required
                type="email"
              />
            </div>
            <div className="mb-2">
              <label className="block text-sm font-semibold">Active Status</label>
              <select
                className="w-full p-2 rounded border"
                value={formState.active}
                onChange={e => setFormState(f => ({ ...f, active: e.target.value === 'true' }))}
              >
                <option value="true">Active</option>
                <option value="false">Inactive</option>
              </select>
            </div>
            <div className="mb-2">
              <label className="block text-sm font-semibold">Balance (₱)</label>
              <input
                className="w-full p-2 rounded border"
                value={formState.balance}
                onChange={e => setFormState(f => ({ ...f, balance: Number(e.target.value) }))}
                type="number"
                step="0.01"
                min="0"
              />
            </div>
            <div className="flex gap-2 mt-4">
              <button type="submit" className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">
                {editingUser ? 'Update' : 'Create'}
              </button>
              <button type="button" className="bg-gray-400 text-white px-4 py-2 rounded" onClick={() => setShowForm(false)}>
                Cancel
              </button>
            </div>
          </form>
        </div>
      )}
      {loading ? (
        <p>Loading users...</p>
      ) : error ? (
        <p className="text-red-500">{error}</p>
      ) : (
        <div className="overflow-x-auto">
          <table className="min-w-full bg-white border border-gray-200 rounded-lg overflow-hidden shadow">
            <thead className="bg-gray-100">
              <tr>
                <th className="py-2 px-4 border-b">ID</th>
                <th className="py-2 px-4 border-b">Username</th>
                <th className="py-2 px-4 border-b">Email</th>
                <th className="py-2 px-4 border-b">Status</th>
                <th className="py-2 px-4 border-b">Balance (₱)</th>
                <th className="py-2 px-4 border-b">Actions</th>
              </tr>
            </thead>
            <tbody>
              {users.map((user) => (
                <tr key={user.id} className={user.active ? '' : 'bg-red-50'}>
                  <td className="py-2 px-4 border-b">{user.id}</td>
                  <td className="py-2 px-4 border-b">{user.username}</td>
                  <td className="py-2 px-4 border-b">{user.email}</td>
                  <td className="py-2 px-4 border-b">
                    <span className={`px-2 py-1 rounded text-xs font-bold ${user.active ? 'bg-green-200 text-green-800' : 'bg-red-200 text-red-800'}`}>
                      {user.active ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td className="py-2 px-4 border-b text-right">₱{!isNaN(Number(user.balance)) ? Number(user.balance).toFixed(2) : '0.00'}</td>
                  <td className="py-2 px-4 border-b flex gap-1">
                    <button
                      className="bg-blue-500 text-white px-2 py-1 rounded text-xs hover:bg-blue-700"
                      onClick={() => handleEdit(user)}
                    >
                      Edit
                    </button>
                    <button
                      className="bg-red-500 text-white px-2 py-1 rounded text-xs hover:bg-red-700"
                      onClick={() => handleDelete(user.id)}
                      disabled={actionLoading[user.id]}
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default AdminUsers;
