import React, { useState, useRef, useEffect } from 'react';
import { FaUserCircle, FaSignOutAlt, FaCog, FaShieldAlt } from 'react-icons/fa';
import { useAuth } from '../context/AuthContext';
import { isAdmin } from '../utils/auth';

export default function UserMenu({ username, onLogout, onSettings, walletBalance }) {
  const { user } = useAuth();
  const [open, setOpen] = useState(false);
  const [showLogoutConfirm, setShowLogoutConfirm] = useState(false);
  const menuRef = useRef();

  // Close the menu if clicked outside
  useEffect(() => {
    function handleClickOutside(event) {
      if (menuRef.current && !menuRef.current.contains(event.target)) {
        setOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  // Avatar: use DiceBear initials API
  const avatarUrl = `https://api.dicebear.com/7.x/initials/svg?seed=${encodeURIComponent(user?.username || 'user')}`;
  const isUserAdmin = user && isAdmin(localStorage.getItem('authToken'));
  const formattedBalance = walletBalance !== null
    ? new Intl.NumberFormat('en-PH', { style: 'currency', currency: 'PHP' }).format(walletBalance)
    : '---';

  return (
    <div className="relative" ref={menuRef}>
      <button
        className="flex items-center px-3 py-1 rounded hover:bg-credigo-accent/10 focus:outline-none"
        onClick={() => setOpen((v) => !v)}
        aria-haspopup="true"
        aria-expanded={open}
      >
        <span className="relative flex items-center justify-center w-8 h-8">
          <img
            src={avatarUrl}
            alt={user?.username}
            className="w-8 h-8 rounded-full bg-credigo-button border-2 border-white"
            referrerPolicy="no-referrer"
          />
          {isUserAdmin && (
            <FaShieldAlt className="absolute -bottom-1 -right-1 text-yellow-400 bg-white rounded-full p-0.5 text-xs border border-gray-200" title="Admin" />
          )}
        </span>
      </button>
      <div
        className={`absolute right-0 mt-2 w-60 bg-white border border-gray-200 rounded-lg shadow-lg z-50 transition-all duration-200 ${open ? 'opacity-100 scale-100' : 'opacity-0 scale-95 pointer-events-none'}`}
        style={{ minWidth: '15rem', display: open ? 'block' : 'none' }}
      >
        <div className="px-4 py-3 border-b border-gray-100 flex items-center space-x-3">
          <span className="relative flex items-center justify-center w-10 h-10">
            <img
              src={avatarUrl}
              alt={user?.username}
              className="w-10 h-10 rounded-full bg-credigo-button border-2 border-white"
              referrerPolicy="no-referrer"
            />
            {isUserAdmin && (
              <FaShieldAlt className="absolute -bottom-1 -right-1 text-yellow-400 bg-white rounded-full p-0.5 text-xs border border-gray-200" title="Admin" />
            )}
          </span>
          <div className="flex flex-col">
            <span className="font-semibold text-gray-700 text-base">{username} {isUserAdmin && <span className="ml-1 inline-flex items-center px-2 py-0.5 rounded bg-yellow-100 text-yellow-800 text-xs font-medium">Admin</span>}</span>
            <span className="text-xs text-gray-500">{user?.email}</span>
          </div>
        </div>
        <div className="px-4 py-2 border-b border-gray-100 flex items-center justify-between">
          <span className="text-xs text-gray-500">Wallet</span>
          <span className="font-bold text-credigo-button">{formattedBalance}</span>
        </div>
        {isUserAdmin && (
          <button
            onClick={() => { setOpen(false); window.location.href = '/admin/dashboard'; }}
            className="flex items-center w-full px-4 py-2 text-white bg-credigo-button hover:bg-credigo-accent text-sm font-semibold rounded-t-md transition"
          >
            <FaShieldAlt className="mr-2" /> Admin Dashboard
          </button>
        )}
        <button
          onClick={onSettings}
          className="flex items-center w-full px-4 py-2 text-gray-700 hover:bg-gray-100 text-sm"
        >
          <FaCog className="mr-2" /> Settings
        </button>
        <button
          onClick={() => setShowLogoutConfirm(true)}
          className="flex items-center w-full px-4 py-2 text-red-600 hover:bg-red-50 text-sm border-t border-gray-100"
        >
          <FaSignOutAlt className="mr-2" /> Logout
        </button>
        {showLogoutConfirm && (
          <div className="absolute top-0 left-0 w-full h-full bg-black bg-opacity-30 flex items-center justify-center z-50">
            <div className="bg-white rounded shadow-lg p-6 text-center">
              <div className="mb-4 text-lg font-semibold">Confirm Logout</div>
              <div className="mb-6 text-gray-600">Are you sure you want to log out?</div>
              <div className="flex justify-center space-x-4">
                <button className="px-4 py-2 rounded bg-gray-200 hover:bg-gray-300 text-gray-800 font-semibold" onClick={() => setShowLogoutConfirm(false)}>Cancel</button>
                <button className="px-4 py-2 rounded bg-red-500 hover:bg-red-600 text-white font-semibold" onClick={() => { setShowLogoutConfirm(false); onLogout(); }}>Logout</button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
