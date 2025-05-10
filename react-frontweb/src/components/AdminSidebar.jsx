import React from 'react';
import { FaBoxOpen, FaChartBar, FaCreditCard, FaExchangeAlt, FaHome, FaIdCard, FaSignOutAlt, FaUsers, FaWallet } from 'react-icons/fa';
import { useLocation, useNavigate } from 'react-router-dom';
import credigoLogo from '../assets/images/credigo_icon.svg';
import { useAuth } from '../context/AuthContext';
import ConfirmModal from './ConfirmModal';

const AdminSidebar = ({ sidebarOpen = true }) => {
  const { logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const currentPath = location.pathname.split('/').pop();

  const [showLogoutModal, setShowLogoutModal] = React.useState(false);
  const handleLogout = () => setShowLogoutModal(true);
  const handleLogoutConfirm = () => {
    setShowLogoutModal(false);
    console.log('AdminSidebar: Handling logout');
    logout();

    // Use a timeout to ensure React state updates first
    setTimeout(() => {
      console.log('AdminSidebar: Navigating to login page');
      navigate('/login');
      window.location.reload(); // Force a full page reload as a last resort
    }, 100);
  };
  const handleLogoutCancel = () => setShowLogoutModal(false);

  const navItems = [
    { id: 'dashboard', label: 'Dashboard', icon: <FaHome size={18} />, path: '/admin' },
    { id: 'stats', label: 'Statistics', icon: <FaChartBar size={18} />, path: '/admin/stats' },
    { id: 'users', label: 'Users', icon: <FaUsers size={18} />, path: '/admin/users' },
    { id: 'products', label: 'Products', icon: <FaBoxOpen size={18} />, path: '/admin/products' },
    { id: 'transactions', label: 'Transactions', icon: <FaExchangeAlt size={18} />, path: '/admin/transactions' },
    { id: 'wallet', label: 'Wallet', icon: <FaWallet size={18} />, path: '/admin/wallet' },
    { id: 'payments', label: 'Payments', icon: <FaCreditCard size={18} />, path: '/admin/payments' },
    { id: 'kyc', label: 'KYC Verification', icon: <FaIdCard size={18} />, path: '/admin/kyc' },
  ];

  const profile = {
    name: 'Admin User',
    role: 'Administrator',
    avatar: 'https://ui-avatars.com/api/?name=Admin+User&background=232946&color=fff',
  };

  return (
    <aside
      className={`${
        sidebarOpen ? 'w-64' : 'w-20'
      } transition-all duration-300 ease-in-out min-h-screen bg-[#232946] text-white flex flex-col shadow-2xl z-10`}
    >
      {/* Logo */}
      <div className="flex items-center justify-center space-x-3 h-16 border-b border-[#eebbc3]/20 px-4">
        <img
          src={credigoLogo}
          alt="CrediGo Logo"
          className="w-10 h-10 rounded-sm"
        />
        {sidebarOpen && (
          <span className="text-2xl font-extrabold tracking-wide text-[#eebbc3] select-none">
            CrediGo
          </span>
        )}
      </div>

      {/* Profile Section */}
      <div className={`flex ${sidebarOpen ? 'flex-col items-center' : 'justify-center'} py-4 px-3 border-b border-[#eebbc3]/20`}>
        <img
          src={profile.avatar}
          alt="Admin Avatar"
          className={`${sidebarOpen ? 'w-20 h-20' : 'w-12 h-12'} rounded-full border-2 border-[#eebbc3] shadow-md transition-all duration-200`}
        />

        {sidebarOpen && (
          <div className="mt-3 text-center">
            <div className="text-lg font-bold text-white">{profile.name}</div>
            <div className="text-xs text-[#eebbc3] font-semibold tracking-wide uppercase mt-1">{profile.role}</div>
          </div>
        )}
      </div>

      {/* Navigation */}
      <nav className="flex-1 mt-2 px-3 space-y-1 overflow-y-auto">
        {navItems.map((item) => {
          const isActive = location.pathname === item.path ||
                          (item.path !== '/admin' && location.pathname.includes(item.id));

          return (
            <button
              key={item.id}
              onClick={() => navigate(item.path)}
              className={`flex items-center w-full p-3 rounded-lg transition-all duration-200 ${
                isActive
                  ? 'bg-[#eebbc3]/20 text-[#eebbc3]'
                  : 'hover:bg-[#2e3a5c] text-gray-300'
              }`}
            >
              <span className="flex items-center justify-center w-6">{item.icon}</span>
              {sidebarOpen && <span className="ml-3 font-medium">{item.label}</span>}
            </button>
          );
        })}
      </nav>

      {/* Logout */}
      <div className="px-3 py-4 border-t border-[#eebbc3]/20">
        <button
          onClick={handleLogout}
          className="flex items-center w-full p-3 rounded-lg text-gray-300 hover:bg-[#2e3a5c] transition-all duration-200"
        >
          <span className="flex items-center justify-center w-6"><FaSignOutAlt size={18} /></span>
          {sidebarOpen && <span className="ml-3 font-medium">Logout</span>}
        </button>
        <ConfirmModal
          isOpen={showLogoutModal}
          onConfirm={handleLogoutConfirm}
          onCancel={handleLogoutCancel}
          message="Are you sure you want to log out?"
          title="Confirm Logout"
        />
      </div>

      {/* Mini branding footer */}
      {sidebarOpen && (
        <div className="p-4 text-center text-xs text-gray-400">
          <p>Created with ❤️ for CrediGo</p>
        </div>
      )}
    </aside>
  );
};

export default AdminSidebar;
