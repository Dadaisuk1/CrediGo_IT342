// src/layouts/ProtectedLayout.jsx
import React from 'react';
import { Outlet } from 'react-router-dom'; // Only need Outlet now
import Footer from '../components/Footer';
import Navbar from '../components/Navbar'; // *** Import the Navbar component ***

function ProtectedLayout() {
  // No need for useAuth here anymore if Navbar handles user info/logout

  return (
    // Use flex column and min-h-screen to help push footer down
    <div className="flex flex-col min-h-screen bg-gray-100 font-sans"> {/* Background for content area */}

      {/* *** Use the Navbar component *** */}
      <Navbar />

      {/* Main Content Area - Use flex-grow to take up available space */}
      <main className="container mx-auto p-4 md:p-6 flex-grow">
        {/* Nested routes defined in App.jsx will render here */}
        <Outlet />
      </main>

      {/* Use the Footer component */}
      <Footer />

    </div>
  );
}

export default ProtectedLayout;
