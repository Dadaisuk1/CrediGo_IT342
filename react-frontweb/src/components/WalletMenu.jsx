import React, { useState, useRef, useEffect } from 'react';
import { FaWallet } from 'react-icons/fa';

export default function WalletMenu({ walletBalance, onWallet }) {
  const [open, setOpen] = useState(false);
  const menuRef = useRef();

  useEffect(() => {
    function handleClickOutside(event) {
      if (menuRef.current && !menuRef.current.contains(event.target)) {
        setOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const formattedBalance = walletBalance !== null
    ? new Intl.NumberFormat('en-PH', { style: 'currency', currency: 'PHP' }).format(walletBalance)
    : '---';

  return (
    <div className="relative" ref={menuRef}>
      <button
        className="flex items-center justify-center w-8 h-8 rounded-full bg-white text-credigo-button border border-credigo-button shadow hover:bg-credigo-button hover:text-white focus:outline-none focus:ring-2 focus:ring-credigo-accent"
        onClick={() => setOpen((v) => !v)}
        aria-haspopup="true"
        aria-expanded={open}
        title="Wallet"
      >
        <FaWallet className="text-xl" />
      </button>
      <div
        className={`absolute right-0 mt-2 w-52 bg-white border border-credigo-button rounded-lg shadow-lg z-50 transition-all duration-200 ${open ? 'opacity-100 scale-100' : 'opacity-0 scale-95 pointer-events-none'}`}
        style={{ minWidth: '13rem', display: open ? 'block' : 'none' }}
      >
        <div className="px-4 py-3 border-b border-credigo-button flex items-center space-x-3 bg-credigo-button/5">
          <FaWallet className="text-2xl text-credigo-button" />
          <div className="flex flex-col">
            <span className="font-semibold text-credigo-button text-base">Wallet</span>
            <span className="text-xs text-gray-500">Current Balance</span>
          </div>
        </div>
        <div className="px-4 py-2 flex items-center justify-between">
          <span className="font-bold text-credigo-button text-lg">{formattedBalance}</span>
        </div>
        <button
          onClick={() => { setOpen(false); onWallet(); }}
          className="w-full px-4 py-2 text-white bg-credigo-button hover:bg-credigo-accent rounded-b-lg text-sm font-semibold transition"
        >
          Go to Wallet
        </button>
      </div>
    </div>
  );
}
