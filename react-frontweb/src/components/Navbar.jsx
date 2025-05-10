import { motion } from 'framer-motion';
import { ChevronDown, Menu } from 'lucide-react';
import { useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import credigoLogo from '../assets/images/credigo_icon.svg';
import { useAuth } from '../context/AuthContext';
import NotificationCenter from './NotificationCenter';
import UserMenu from './UserMenu';
import WalletMenu from './WalletMenu';

function Navbar() {
  const { isAuthenticated, user, logout, walletBalance } = useAuth();
  const navigate = useNavigate();
  const [showGamesDropdown, setShowGamesDropdown] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  // Enhanced active link styling with better indicator and hover effects
  const navLinkClass = ({ isActive }) =>
    isActive
      ? 'relative text-white font-semibold px-3 py-2 rounded-md flex items-center after:content-[""] after:absolute after:left-1/2 after:-translate-x-1/2 after:bottom-0 after:h-0.5 after:w-2/3 after:bg-gradient-to-r after:from-credigo-accent after:via-purple-400 after:to-purple-500 bg-white/10 shadow-inner'
      : 'relative text-gray-300 hover:text-white transition-all duration-200 font-medium px-3 py-2 rounded-md hover:bg-white/5 flex items-center';

  // Dropdown link style
  const dropdownLinkClass = ({ isActive }) =>
    isActive
      ? 'block w-full text-left px-4 py-2 text-sm bg-indigo-50 text-indigo-700 font-medium'
      : 'block w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 hover:text-indigo-600 font-medium';

  // Logo link styling
  const logoLinkClass = "flex items-center space-x-2 group";

  // Format wallet balance
  const formattedBalance = walletBalance !== null
    ? new Intl.NumberFormat('en-PH', { style: 'currency', currency: 'PHP' }).format(walletBalance)
    : '---';

  // Enhance logout handling
  const handleLogout = () => {
    logout();
    navigate('/login'); // Navigate to login page after logout
  };

  // Toggle games dropdown
  const toggleGamesDropdown = () => {
    setShowGamesDropdown(!showGamesDropdown);
  };

  // Toggle mobile menu
  const toggleMobileMenu = () => {
    setMobileMenuOpen(!mobileMenuOpen);
  };

  // Navbar item animations
  const navItemVariants = {
    hidden: { opacity: 0, y: -10 },
    visible: i => ({
      opacity: 1,
      y: 0,
      transition: {
        delay: i * 0.1,
        duration: 0.5,
        ease: "easeOut"
      }
    })
  };

  // Game categories for dropdown
  const gameCategories = [
    { name: "Game Catalog", path: "/games", highlight: true, description: "Browse all games" },
    { name: "All Products", path: "/products", description: "View all available products" },
    { name: "Action", path: "/products?category=action" },
    { name: "Adventure", path: "/products?category=adventure" },
    { name: "RPG", path: "/products?category=rpg" },
    { name: "FPS", path: "/products?category=fps" },
    { name: "Strategy", path: "/products?category=strategy" },
  ];

  return (
    <header className="bg-credigo-dark/90 backdrop-blur-lg border-b border-gray-800/50 py-3 text-credigo-light sticky top-0 z-50 font-sans shadow-md shadow-black/10">
      <div className="container mx-auto px-4 flex justify-between items-center">

        {/* Logo with enhanced hover effect */}
        <NavLink to="/" className={logoLinkClass}>
          <div className="relative w-10 h-10 flex items-center justify-center">
            <div className="absolute inset-0 bg-gradient-to-r from-credigo-accent to-purple-500 rounded-full opacity-20 group-hover:opacity-40 group-hover:scale-110 blur-sm transition-all duration-300"></div>
            <img src={credigoLogo} alt="CrediGo Logo" className="h-8 w-auto relative z-10 group-hover:scale-105 transition-transform duration-300" />
          </div>
          <div className="flex flex-col">
            <span className="font-bold text-xl text-transparent bg-clip-text bg-gradient-to-r from-credigo-accent via-purple-400 to-purple-300 group-hover:from-white group-hover:to-credigo-accent transition-all duration-300">CrediGo</span>
            <span className="text-xs text-gray-400 -mt-1">Game Credits Marketplace</span>
          </div>
        </NavLink>

        {/* Desktop Navigation with improved spacing and hover effects */}
        <nav className="hidden md:flex items-center space-x-2">
          <motion.div custom={0} variants={navItemVariants} initial="hidden" animate="visible">
            <NavLink to="/" className={navLinkClass} end>Home</NavLink>
          </motion.div>

          {/* Games dropdown menu */}
          <motion.div custom={1} variants={navItemVariants} initial="hidden" animate="visible" className="relative">
            <button
              className={`relative text-gray-300 hover:text-white transition-all duration-200 font-medium px-3 py-2 rounded-md hover:bg-white/5 flex items-center ${showGamesDropdown ? 'bg-white/10 text-white' : ''}`}
              onClick={toggleGamesDropdown}
            >
              Games <ChevronDown className={`ml-1 h-4 w-4 transition-transform duration-200 ${showGamesDropdown ? 'rotate-180' : ''}`} />
            </button>

            {showGamesDropdown && (
              <div className="absolute left-0 mt-1 w-56 bg-white rounded-md shadow-lg overflow-hidden z-50 border border-gray-200 animate-in fade-in-50 slide-in-from-top-5">
                {gameCategories.map((category, index) => (
                  <NavLink
                    key={index}
                    to={category.path}
                    className={({isActive}) => `${dropdownLinkClass({isActive})} ${category.highlight ? 'border-l-4 border-indigo-500 font-semibold' : ''}`}
                    onClick={() => setShowGamesDropdown(false)}
                  >
                    <div>
                      {category.name}
                      {category.description && (
                        <span className="block text-xs text-gray-500 mt-0.5">{category.description}</span>
                      )}
                    </div>
                  </NavLink>
                ))}
              </div>
            )}
          </motion.div>

          <motion.div custom={2} variants={navItemVariants} initial="hidden" animate="visible">
            <NavLink to="/history" className={navLinkClass}>Orders</NavLink>
          </motion.div>
          <motion.div custom={3} variants={navItemVariants} initial="hidden" animate="visible">
            <NavLink to="/wishlist" className={navLinkClass}>Wishlist</NavLink>
          </motion.div>
          <motion.div custom={4} variants={navItemVariants} initial="hidden" animate="visible">
            <NavLink to="/about" className={navLinkClass}>About</NavLink>
          </motion.div>
        </nav>

        {/* Auth Section with enhanced spacing and animations */}
        <div className="flex items-center">
          {isAuthenticated ? (
            <>
              {/* User Menu Dropdown and Wallet Menu */}
              <motion.div
                className="hidden sm:flex items-center space-x-4"
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                transition={{ delay: 0.5 }}
              >
                <WalletMenu walletBalance={walletBalance} onWallet={() => navigate('/wallet')} />
                <NotificationCenter />
                <UserMenu
                  username={user?.username}
                  walletBalance={walletBalance}
                  onLogout={handleLogout}
                  onSettings={() => navigate('/settings')}
                />
              </motion.div>
            </>
          ) : (
            <div className="hidden md:flex items-center space-x-3">
              <NavLink
                to="/login"
                className="px-4 py-2 text-sm font-medium border border-gray-700 rounded-md text-gray-300 hover:text-white hover:border-credigo-accent/70 hover:bg-gray-800/50 transition-all duration-300"
              >
                Sign in
              </NavLink>
              <NavLink
                to="/register"
                className="px-4 py-2 text-sm font-medium text-credigo-dark bg-gradient-to-r from-credigo-accent to-purple-500 rounded-md hover:shadow-lg hover:shadow-purple-500/20 transition-all hover:scale-105 active:scale-95 duration-200"
              >
                Sign up
              </NavLink>
            </div>
          )}

          {/* Mobile Menu Button with animation */}
          <button
            className="md:hidden p-2 rounded-md hover:bg-white/10 focus:outline-none transition-colors duration-200"
            onClick={toggleMobileMenu}
            aria-label="Toggle mobile menu"
          >
            <Menu className="h-6 w-6 text-credigo-light" />
          </button>
        </div>
      </div>

      {/* Mobile Navigation Menu */}
      {mobileMenuOpen && (
        <motion.div
          className="md:hidden bg-credigo-dark border-t border-gray-800/50 shadow-lg"
          initial={{ opacity: 0, height: 0 }}
          animate={{ opacity: 1, height: 'auto' }}
          exit={{ opacity: 0, height: 0 }}
          transition={{ duration: 0.3 }}
        >
          <div className="px-4 py-3 space-y-1">
            <NavLink
              to="/"
              className={({ isActive }) =>
                `block px-3 py-2 rounded-md ${isActive ? 'bg-white/10 text-white font-medium' : 'text-gray-300 hover:bg-white/5 hover:text-white'}`
              }
              onClick={() => setMobileMenuOpen(false)}
              end
            >
              Home
            </NavLink>

            {/* Mobile Games menu with prominent Game Catalog link */}
            <NavLink
              to="/games"
              className={({ isActive }) =>
                `block px-3 py-2 rounded-md ${isActive ? 'bg-white/10 text-white font-medium' : 'text-gray-300 hover:bg-white/5 hover:text-white'} border-l-2 border-indigo-500`
              }
              onClick={() => setMobileMenuOpen(false)}
            >
              <div className="flex justify-between items-center">
                <span>Game Catalog</span>
                <span className="text-xs bg-indigo-900/80 text-indigo-300 px-2 py-0.5 rounded-full">All Games</span>
              </div>
            </NavLink>

            {/* Game Categories */}
            <div className="space-y-1">
              <button
                className="flex justify-between items-center w-full px-3 py-2 text-gray-300 hover:bg-white/5 hover:text-white rounded-md"
                onClick={() => setShowGamesDropdown(!showGamesDropdown)}
              >
                <span>Game Categories</span>
                <ChevronDown className={`h-4 w-4 transition-transform duration-200 ${showGamesDropdown ? 'rotate-180' : ''}`} />
              </button>

              {showGamesDropdown && (
                <div className="pl-4 space-y-1 mt-1 border-l border-gray-700 ml-3">
                  {gameCategories.slice(1).map((category, index) => (
                    <NavLink
                      key={index}
                      to={category.path}
                      className={({ isActive }) =>
                        `block px-3 py-2 rounded-md text-sm ${isActive ? 'bg-white/10 text-white font-medium' : 'text-gray-300 hover:bg-white/5 hover:text-white'}`
                      }
                      onClick={() => setMobileMenuOpen(false)}
                    >
                      {category.name}
                    </NavLink>
                  ))}
                </div>
              )}
            </div>

            <NavLink
              to="/history"
              className={({ isActive }) =>
                `block px-3 py-2 rounded-md ${isActive ? 'bg-white/10 text-white font-medium' : 'text-gray-300 hover:bg-white/5 hover:text-white'}`
              }
              onClick={() => setMobileMenuOpen(false)}
            >
              Orders
            </NavLink>
            <NavLink
              to="/wishlist"
              className={({ isActive }) =>
                `block px-3 py-2 rounded-md ${isActive ? 'bg-white/10 text-white font-medium' : 'text-gray-300 hover:bg-white/5 hover:text-white'}`
              }
              onClick={() => setMobileMenuOpen(false)}
            >
              Wishlist
            </NavLink>
            <NavLink
              to="/about"
              className={({ isActive }) =>
                `block px-3 py-2 rounded-md ${isActive ? 'bg-white/10 text-white font-medium' : 'text-gray-300 hover:bg-white/5 hover:text-white'}`
              }
              onClick={() => setMobileMenuOpen(false)}
            >
              About
            </NavLink>

            {!isAuthenticated && (
              <div className="pt-2 pb-3 border-t border-gray-800 mt-2 flex space-x-3">
                <NavLink
                  to="/login"
                  className="w-1/2 text-center px-4 py-2 text-sm font-medium border border-gray-700 rounded-md text-gray-300 hover:text-white hover:border-credigo-accent/70 transition-all duration-300"
                  onClick={() => setMobileMenuOpen(false)}
                >
                  Sign in
                </NavLink>
                <NavLink
                  to="/register"
                  className="w-1/2 text-center px-4 py-2 text-sm font-medium text-credigo-dark bg-gradient-to-r from-credigo-accent to-purple-500 rounded-md hover:shadow-lg hover:shadow-purple-500/20 transition-all"
                  onClick={() => setMobileMenuOpen(false)}
                >
                  Sign up
                </NavLink>
              </div>
            )}
          </div>
        </motion.div>
      )}
    </header>
  );
}

export default Navbar;
