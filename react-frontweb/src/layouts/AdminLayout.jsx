import { useState } from 'react';
import { FaBell, FaSearch } from 'react-icons/fa';
import { Outlet } from 'react-router-dom';
import AdminSidebar from '../components/AdminSidebar';

const AdminLayout = () => {
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const [notifications, setNotifications] = useState(3); // Example notification count

  return (
    <div className="flex min-h-screen bg-[#f9f9f1]">
      {/* Sidebar */}
      <AdminSidebar sidebarOpen={sidebarOpen} />

      {/* Main Content */}
      <div className="flex-1 flex flex-col">
        {/* Top Navigation */}
        <header className="bg-white border-b border-[#eebbc3]/20 shadow-sm h-16 flex items-center justify-between px-6">
          {/* Left side - toggle and breadcrumb */}
          <div className="flex items-center">
            <button
              onClick={() => setSidebarOpen(!sidebarOpen)}
              className="mr-4 p-2 rounded-lg text-[#232946] hover:bg-[#f1f1e6] transition-colors"
            >
              <svg width="20" height="20" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                {sidebarOpen ? (
                  <>
                    <line x1="18" y1="6" x2="6" y2="6" />
                    <line x1="18" y1="12" x2="2" y2="12" />
                    <line x1="18" y1="18" x2="10" y2="18" />
                  </>
                ) : (
                  <>
                    <line x1="2" y1="6" x2="18" y2="6" />
                    <line x1="2" y1="12" x2="18" y2="12" />
                    <line x1="2" y1="18" x2="18" y2="18" />
                  </>
                )}
              </svg>
            </button>
            <div>
              <h1 className="text-xl font-semibold text-[#232946]">Admin Dashboard</h1>
              <p className="text-xs text-[#6c757d]">Manage your CrediGo platform</p>
            </div>
          </div>

          {/* Right side - search and notifications */}
          <div className="flex items-center space-x-4">
            <div className="relative hidden md:block">
              <input
                type="text"
                placeholder="Search..."
                className="w-64 pl-10 pr-4 py-2 rounded-full bg-[#f5f5eb] border border-[#eebbc3]/30 focus:outline-none focus:ring-2 focus:ring-[#eebbc3]/50"
              />
              <FaSearch className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
            </div>

            <div className="relative">
              <button className="p-2 rounded-full hover:bg-[#f1f1e6] text-[#232946]">
                <FaBell size={20} />
                {notifications > 0 && (
                  <span className="absolute top-0 right-0 h-5 w-5 flex items-center justify-center text-xs bg-[#eebbc3] text-white rounded-full">
                    {notifications}
                  </span>
                )}
              </button>
            </div>

            <div className="flex items-center">
              <img
                src="https://ui-avatars.com/api/?name=Admin+User&background=232946&color=fff"
                alt="Admin"
                className="w-8 h-8 rounded-full border-2 border-[#eebbc3]"
              />
            </div>
          </div>
        </header>

        {/* Main Content Area */}
        <main className="flex-1 p-6 overflow-auto">
          <div className="max-w-7xl mx-auto">
            <Outlet />
          </div>
        </main>

        {/* Footer */}
        <footer className="border-t border-[#eebbc3]/20 bg-white p-4 text-center text-sm text-gray-500">
          <p>© 2024 CrediGo - Created with love 💖</p>
        </footer>
      </div>
    </div>
  );
};

export default AdminLayout;
