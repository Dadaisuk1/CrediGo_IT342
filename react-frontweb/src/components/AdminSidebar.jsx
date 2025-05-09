import React from 'react';
import { FaBoxOpen, FaCreditCard, FaExchangeAlt, FaHome, FaSignOutAlt, FaUsers } from 'react-icons/fa';
import { IoMdSettings } from 'react-icons/io';
import { useLocation, useNavigate } from 'react-router-dom';
import credigoLogo from '../assets/images/credigo_icon.svg';
import ConfirmModal from './ConfirmModal';

const AdminSidebar = ({ sidebarOpen }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const currentPath = location.pathname.split('/').pop();

  const [showLogoutModal, setShowLogoutModal] = React.useState(false);
  const handleLogout = () => setShowLogoutModal(true);
  const handleLogoutConfirm = () => {
    setShowLogoutModal(false);
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    navigate('/login');
  };
  const handleLogoutCancel = () => setShowLogoutModal(false);

  const navItems = [
    { id: 'dashboard', label: 'Dashboard', icon: <FaHome size={20} />, path: '/admin/dashboard' },
    { id: 'users', label: 'User Management', icon: <FaUsers size={20} />, path: '/admin/users' },
    { id: 'products', label: 'Product Management', icon: <FaBoxOpen size={20} />, path: '/admin/products' },
    { id: 'transactions', label: 'Transactions', icon: <FaExchangeAlt size={20} />, path: '/admin/transactions' },
    { id: 'payments', label: 'Payment Testing', icon: <FaCreditCard size={20} />, path: '/admin/payments' },
    { id: 'settings', label: 'Settings', icon: <IoMdSettings size={20} />, path: '/admin/settings' },
  ];

  const profile = {
    name: 'Admin User',
    role: 'Administrator',
    avatar: 'https://ui-avatars.com/api/?name=Admin+User&background=232946&color=fff',
  };

  return (
    <aside className="w-64 min-h-screen bg-[#232946] text-white flex flex-col justify-between rounded-tr-3xl rounded-br-3xl shadow-2xl">
      {/* Logo */}
      <button type="button" className="flex items-center space-x-3 mt-6 ml-6 focus:outline-none" onClick={() => navigate('/')}>
        <img src={credigoLogo}  alt="CrediGo Logo" className="w-10 h-10 rounded-sm shadow-lg" />
        <span className="text-2xl font-extrabold tracking-wide text-[#eebbc3] drop-shadow-lg select-none">CrediGo</span>
      </button>

      {/* Profile Section */}
      <div className="flex flex-col items-center justify-center py-7 px-4 bg-gradient-to-br from-[#2e3a5c]/70 via-[#232946]/90 to-[#232946] rounded-2xl shadow-xl mx-4 mt-6 mb-2 border border-[#eebbc3]/20">
        <img
          src={profile.avatar}
          alt="Admin Avatar"
          className="w-20 h-20 rounded-full border-4 border-[#eebbc3] mb-3 shadow-xl"
        />
        <div className="text-lg font-bold text-white mt-1 drop-shadow">{profile.name}</div>
        <div className="text-xs text-[#eebbc3] font-semibold mb-1 tracking-wide uppercase">{profile.role}</div>
      </div>

      {/* Navigation */}
      <nav className="flex-1 mt-2">
        <ul className="space-y-1">
          {navItems.map((item) => (
            <li key={item.id}>
              <button
                onClick={() => navigate(item.path)}
                className={`${
                  location.pathname.includes(item.id)
                    ? 'bg-[#121629] border-l-4 border-[#eebbc3]'
                    : 'border-l-4 border-transparent hover:bg-[#2e3a5c]'
                } flex items-center w-full p-4 transition-colors duration-200`}
              >
                <span className="mr-4">{item.icon}</span>
                <span className={`${sidebarOpen ? 'opacity-100' : 'hidden md:block md:opacity-0 lg:opacity-100'} transition-opacity duration-200`}>
                  {item.label}
                </span>
              </button>
            </li>
          ))}
          <li>
            <button
              onClick={handleLogout}
              className={`border-l-4 flex items-center w-full p-4 transition-colors duration-200 hover:bg-[#2e3a5c] ${
                currentPath === 'logout' ? 'bg-[#121629] border-[#eebbc3]' : 'border-transparent'
              }`}
            >
              <span className="mr-4"><FaSignOutAlt size={20} /></span>
              <span className={`${sidebarOpen ? 'opacity-100' : 'hidden md:block md:opacity-0 lg:opacity-100'} transition-opacity duration-200`}>
                Logout
              </span>
            </button>
            <ConfirmModal
              isOpen={showLogoutModal}
              onConfirm={handleLogoutConfirm}
              onCancel={handleLogoutCancel}
              message="Are you sure you want to log out?"
              title="Confirm Logout"
            />
          </li>
        </ul>
      </nav>

      <div className="h-6" />
    </aside>
  );
};

export default AdminSidebar;
