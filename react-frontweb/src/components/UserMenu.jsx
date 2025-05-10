import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { AnimatePresence, motion } from 'framer-motion';
import { useEffect, useRef, useState } from 'react';
import { FaCog, FaShieldAlt, FaSignOutAlt } from 'react-icons/fa';
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

  // Avatar: use Google profile picture if available, otherwise use DiceBear
  const avatarUrl = user?.picture || `https://api.dicebear.com/7.x/initials/svg?seed=${encodeURIComponent(user?.username || 'user')}`;
  const isUserAdmin = user && isAdmin(localStorage.getItem('authToken'));
  const formattedBalance = walletBalance !== null
    ? new Intl.NumberFormat('en-PH', { style: 'currency', currency: 'PHP' }).format(walletBalance)
    : '---';

  const handleLogout = () => {
    setShowLogoutConfirm(false);
    setOpen(false);
    onLogout();
  };

  // Menu item animation variants
  const menuItemVariants = {
    hidden: { opacity: 0, y: 10 },
    visible: i => ({
      opacity: 1,
      y: 0,
      transition: {
        delay: i * 0.05,
        duration: 0.2,
        ease: "easeOut"
      }
    })
  };

  return (
    <div className="relative" ref={menuRef}>
      <button
        className="flex items-center px-3 py-1 rounded-md hover:bg-white/10 focus:outline-none transition-all duration-200"
        onClick={() => setOpen((v) => !v)}
        aria-haspopup="true"
        aria-expanded={open}
      >
        <span className="relative flex items-center justify-center w-8 h-8">
          <div className="absolute inset-0 bg-gradient-to-r from-credigo-accent/30 to-purple-500/30 rounded-full opacity-70 blur-sm"></div>
          <img
            src={avatarUrl}
            alt={user?.username}
            className="w-8 h-8 rounded-full bg-credigo-dark border-2 border-white/20 object-cover relative z-10"
            referrerPolicy="no-referrer"
          />
          {isUserAdmin && (
            <motion.div
              initial={{ scale: 0 }}
              animate={{ scale: 1 }}
              transition={{ delay: 0.2 }}
              className="absolute -bottom-1 -right-1 text-yellow-400 bg-credigo-dark rounded-full p-0.5 text-xs shadow-md border border-yellow-400/50"
              title="Admin"
            >
              <FaShieldAlt />
            </motion.div>
          )}
        </span>
      </button>

      <AnimatePresence>
        {open && (
          <motion.div
            initial={{ opacity: 0, y: 10, scale: 0.95 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, scale: 0.95 }}
            transition={{ duration: 0.2 }}
            className="absolute right-0 mt-2 w-60 bg-white border border-gray-200 rounded-lg shadow-xl z-50 overflow-hidden"
          >
            <div className="px-4 py-3 border-b border-gray-100 flex items-center space-x-3 bg-gradient-to-r from-slate-50 to-indigo-50">
              <span className="relative flex items-center justify-center w-10 h-10">
                <div className="absolute inset-0 bg-gradient-to-r from-credigo-accent/20 to-purple-500/20 rounded-full opacity-70 blur-sm"></div>
                <img
                  src={avatarUrl}
                  alt={user?.username}
                  className="w-10 h-10 rounded-full bg-credigo-dark border-2 border-white object-cover relative z-10"
                  referrerPolicy="no-referrer"
                />
                {isUserAdmin && (
                  <motion.div
                    initial={{ scale: 0 }}
                    animate={{ scale: 1 }}
                    transition={{ delay: 0.2 }}
                    className="absolute -bottom-1 -right-1 text-yellow-400 bg-credigo-dark rounded-full p-0.5 text-xs shadow-md border border-yellow-400/50"
                    title="Admin"
                  >
                    <FaShieldAlt />
                  </motion.div>
                )}
              </span>
              <div className="flex flex-col">
                <span className="font-semibold text-gray-700 text-base">
                  {username}
                  {isUserAdmin && (
                    <motion.span
                      initial={{ opacity: 0, x: -10 }}
                      animate={{ opacity: 1, x: 0 }}
                      transition={{ delay: 0.3 }}
                      className="ml-1 inline-flex items-center px-2 py-0.5 rounded bg-yellow-100 text-yellow-800 text-xs font-medium gap-1"
                    >
                      <FaShieldAlt className="text-xs" /> Admin
                    </motion.span>
                  )}
                </span>
                <span className="text-xs text-gray-500">{user?.email}</span>
              </div>
            </div>

            <div className="px-4 py-2 border-b border-gray-100 flex items-center justify-between bg-gray-50">
              <span className="text-xs text-gray-500">Wallet</span>
              <span className="font-bold text-credigo-accent">{formattedBalance}</span>
            </div>

            <motion.div className="space-y-1 py-1">
              {isUserAdmin && (
                <motion.button
                  custom={0}
                  variants={menuItemVariants}
                  initial="hidden"
                  animate="visible"
                  whileHover={{ backgroundColor: "#f3f4f6" }}
                  onClick={() => { setOpen(false); window.location.href = '/admin/dashboard'; }}
                  className="flex items-center w-full px-4 py-2 text-gray-700 hover:bg-gray-100 text-sm font-medium transition-colors duration-200"
                >
                  <FaShieldAlt className="mr-2 text-yellow-500" /> Admin Dashboard
                </motion.button>
              )}

              <motion.button
                custom={1}
                variants={menuItemVariants}
                initial="hidden"
                animate="visible"
                whileHover={{ backgroundColor: "#f3f4f6" }}
                onClick={() => { setOpen(false); onSettings(); }}
                className="flex items-center w-full px-4 py-2 text-gray-700 hover:bg-gray-100 text-sm transition-colors duration-200"
              >
                <FaCog className="mr-2 text-gray-500" /> Settings
              </motion.button>

              <motion.button
                custom={2}
                variants={menuItemVariants}
                initial="hidden"
                animate="visible"
                whileHover={{ backgroundColor: "#fee2e2" }}
                onClick={() => setShowLogoutConfirm(true)}
                className="flex items-center w-full px-4 py-2 text-red-600 hover:bg-red-50 text-sm border-t border-gray-100 transition-colors duration-200"
              >
                <FaSignOutAlt className="mr-2" /> Logout
              </motion.button>
            </motion.div>

            {/* Shadcn Alert Dialog for Logout Confirmation */}
            <AlertDialog open={showLogoutConfirm} onOpenChange={setShowLogoutConfirm}>
              <AlertDialogContent className="sm:max-w-[425px]">
                <AlertDialogHeader>
                  <AlertDialogTitle>Confirm Logout</AlertDialogTitle>
                  <AlertDialogDescription>
                    Are you sure you want to log out of your account?
                  </AlertDialogDescription>
                </AlertDialogHeader>
                <AlertDialogFooter>
                  <AlertDialogCancel>Cancel</AlertDialogCancel>
                  <AlertDialogAction
                    onClick={handleLogout}
                    className="bg-red-600 hover:bg-red-700"
                  >
                    Logout
                  </AlertDialogAction>
                </AlertDialogFooter>
              </AlertDialogContent>
            </AlertDialog>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
