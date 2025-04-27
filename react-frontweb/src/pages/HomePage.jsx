import React from 'react';
import { useAuth } from '../context/AuthContext';

function HomePage() {
  const { user } = useAuth();

  return (
    <div className="p-6 bg-white rounded-lg shadow-md">
      <h1 className="text-2xl font-semibold text-gray-800 mb-4">Dashboard</h1>
      <p className="mt-2 text-gray-700">Welcome back, <span className="font-medium capitalize">{user?.username}</span>!</p>
      <p className="mt-1 text-gray-600 text-sm">User ID: {user?.id}</p>
      <p className="mt-1 text-gray-600 text-sm">Email: {user?.email}</p> {/* Assuming email is in user state */}
      <p className="mt-1 text-gray-600 text-sm">Roles: {user?.roles?.join(', ')}</p>

      {/* Add more dashboard widgets or summaries here */}
      <div className="mt-6 border-t pt-4">
        <h2 className="text-lg font-medium text-gray-700">Quick Actions</h2>
        {/* Add links/buttons for common actions */}
      </div>
    </div>
  );
}

export default HomePage;
