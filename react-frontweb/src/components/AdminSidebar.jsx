import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { FaHome, FaUsers, FaBoxOpen, FaExchangeAlt, FaSignOutAlt } from 'react-icons/fa';
import { IoMdSettings } from 'react-icons/io';
import { FaTimes } from 'react-icons/fa';
import credigoLogo from '../assets/images/credigo_icon.svg'
import ConfirmModal from './ConfirmModal';

const AdminSidebar = ({ sidebarOpen, toggleSidebar }) => {
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
    { id: 'settings', label: 'Settings', icon: <IoMdSettings size={20} />, path: '/admin/settings' },
  ];

  // Mock profile and notifications
  const profile = {
    name: 'Admin User',
    role: 'Administrator',
    avatar: 'https://ui-avatars.com/api/?name=Admin+User&background=232946&color=fff',
  };
  const notifications = [
    { id: 1, message: 'New user registered', read: false },
    { id: 2, message: 'Payment received', read: false },
    { id: 3, message: 'KYC pending review', read: true },
  ];
  const unreadCount = notifications.filter(n => !n.read).length;

  // Dark mode state (local only for now)
  const [darkMode, setDarkMode] = React.useState(false);
  React.useEffect(() => {
    if (darkMode) {
      document.body.classList.add('dark');
    } else {
      document.body.classList.remove('dark');
    }
  }, [darkMode]);

  return (
    <div className={`${sidebarOpen ? 'translate-x-0 w-64' : '-translate-x-full w-0 md:w-20 md:translate-x-0'} bg-[#232946]/80 backdrop-blur-xl shadow-2xl text-white transition-all duration-300 ease-in-out fixed md:relative z-30 h-full border-r border-[#232946]/30`}>
      {/* Brand/Logo Area */}
      <div className="flex items-center justify-center h-20 px-6 border-b border-[#121629]/60 bg-gradient-to-r from-[#232946] via-[#2e3a5c] to-[#3d4977]">
        <img src={credigoLogo} alt="CrediGo Logo" className="w-10 h-10 rounded-sm shadow-lg mr-3" />
        <span className="text-2xl font-extrabold tracking-wide text-[#eebbc3] drop-shadow-lg select-none">CrediGo</span>
      </div>
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
      <div className="h-4" />
      <nav className="mt-2">
        <ul className="space-y-1">

          {navItems.map((item) => (
            <li key={item.id}>
              <button
                onClick={() => navigate(item.path)}
                className={`$
                  {currentPath === item.id
                    ? 'bg-[#121629] border-l-4 border-[#eebbc3]'
                    : 'border-l-4 border-transparent hover:bg-[#2e3a5c]'} flex items-center w-full p-4 transition-colors duration-200`}
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
                currentPath === 'logout'
                  ? 'bg-[#121629] border-[#eebbc3]'
                  : 'border-transparent'
              }`}
            >
              <span className="mr-4"><FaSignOutAlt size={20} /></span>
              <span className={`${sidebarOpen ? 'opacity-100' : 'hidden md:block md:opacity-0 lg:opacity-100'} transition-opacity duration-200`}>
                Logout
              </span>
            </button>
            <ConfirmModal
              open={showLogoutModal}
              title="Confirm Logout"
              message="Are you sure you want to log out?"
              onConfirm={handleLogoutConfirm}
              onCancel={handleLogoutCancel}
            />
          </li>
        </ul>
      </nav>
    </div>
  );
};

export default AdminSidebar;
