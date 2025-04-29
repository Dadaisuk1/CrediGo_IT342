import React from 'react';

const NotAuthorized = () => (
  <div className="flex flex-col items-center justify-center h-screen bg-red-50">
    <h1 className="text-4xl font-bold text-red-600 mb-4">403 - Not Authorized</h1>
    <p className="text-lg text-gray-700">You do not have permission to access this page.</p>
  </div>
);

export default NotAuthorized;
