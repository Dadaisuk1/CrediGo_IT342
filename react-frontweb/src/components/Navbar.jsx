import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import UserMenu from './UserMenu';
import WalletMenu from './WalletMenu';
import credigoLogo from '../assets/images/credigo_icon.svg';

function Navbar() {
  const { isAuthenticated, user, logout, walletBalance } = useAuth();
  const navigate = useNavigate(); // For programmatic navigation

  const navLinkClass = ({ isActive }) =>
    isActive
      ? 'text-credigo-light font-semibold border-b-2 border-credigo-button pb-1'
      : 'text-gray-400 hover:text-credigo-light transition duration-150 font-semibold pb-1 border-b-2 border-transparent';

  const logoLinkClass = "flex items-center space-x-2";

  const formattedBalance = walletBalance !== null
    ? new Intl.NumberFormat('en-PH', { style: 'currency', currency: 'PHP' }).format(walletBalance)
    : '---';

  const handleBalanceClick = () => {
    navigate('/wallet');
  };

  return (
    <header className="bg-credigo-input-bg p-4 text-credigo-light shadow-md sticky top-0 z-50 font-sans">
      <div className="container mx-auto flex justify-between items-center">

        {/* Logo */}
        <NavLink to="/" className={logoLinkClass}>
          <img src={credigoLogo} alt="CrediGo Logo" className="h-8 w-auto" />
          <span className="font-bold text-xl">CrediGo</span>
        </NavLink>

        {/* Desktop Navigation */}
        <nav className="hidden md:flex items-center space-x-6">
          <NavLink to="/" className={navLinkClass} end>Home</NavLink>
          <NavLink to="/products" className={navLinkClass}>Products</NavLink>
          <NavLink to="/history" className={navLinkClass}>History</NavLink>
          <NavLink to="/wishlist" className={navLinkClass}>Wishlist</NavLink>
          <NavLink to="/about" className={navLinkClass}>About</NavLink>
        </nav>

        {/* Auth Section */}
        <div className="flex items-center space-x-3">
          {isAuthenticated ? (
            <>
              {/* User Menu Dropdown (avatar only) and Wallet Menu */}
              <div className="hidden sm:flex items-center space-x-2">
                <WalletMenu walletBalance={walletBalance} onWallet={() => navigate('/wallet')} />
                <UserMenu
                  username={user?.username}
                  walletBalance={walletBalance}
                  onLogout={logout}
                  onSettings={() => navigate('/settings')}
                />
              </div>
            </>
          ) : (
            <div className="hidden md:flex items-center space-x-3">
              <NavLink
                to="/login"
                className="px-4 py-2 text-sm font-medium text-credigo-light hover:text-credigo-accent transition duration-150"
              >
                Sign in
              </NavLink>
              <NavLink
                to="/register"
                className="px-4 py-2 text-sm font-medium text-credigo-dark bg-credigo-button rounded-md hover:bg-opacity-90 transition duration-150"
              >
                Sign up
              </NavLink>
            </div>
          )}
          {/* Future Mobile Menu Button */}
        </div>

      </div>
      {/* Future Mobile Navigation */}
    </header>
  );
}

export default Navbar;
