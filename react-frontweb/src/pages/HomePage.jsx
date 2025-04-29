import React from 'react';
import { useAuth } from '../context/AuthContext';
import { jwtDecode } from 'jwt-decode';

function HomePage() {
  const { user, token } = useAuth();

  let roles = [];
  if (token) {
    try {
      const decoded = jwtDecode(token);
      if (decoded.roles) {
        if (Array.isArray(decoded.roles)) {
          roles = decoded.roles.map(role =>
            typeof role === 'string'
              ? role
              : (role.authority || JSON.stringify(role))
          );
        } else {
          roles = [typeof decoded.roles === 'string' ? decoded.roles : JSON.stringify(decoded.roles)];
        }
      }
    } catch (e) {
      roles = ['(unable to decode roles)'];
    }
  }

  return (
    <div className="p-6 bg-white rounded-lg shadow-md">
      <h1 className="text-2xl font-semibold text-gray-800 mb-4">Dashboard</h1>
      <p className="text-gray-500 mb-2">Your roles: <span className="font-mono text-blue-700">{roles.join(', ') || '(none found)'}</span></p>
      <p className="mt-2 text-gray-700">Welcome back, <span className="font-medium capitalize">{user?.username}</span>!</p>

      {/* Add more dashboard widgets or summaries here */}
      <div className="mt-6 border-t pt-4">
        <h2 className="text-lg font-medium text-gray-700">Quick Actions</h2>
        {/* Admin Dashboard button only for admin users */}
        {/* Add links/buttons for common actions */}
      </div>
    </div>
  );
}

export default HomePage;
