import { AnimatePresence, motion } from 'framer-motion';
import { useEffect, useRef, useState } from 'react';
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
        className="flex items-center justify-center w-8 h-8 rounded-full bg-white/10 text-credigo-accent border border-credigo-accent/30 hover:border-credigo-accent/70 shadow-sm hover:shadow-md hover:shadow-credigo-accent/10 transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-credigo-accent focus:ring-opacity-50"
        onClick={() => setOpen((v) => !v)}
        aria-haspopup="true"
        aria-expanded={open}
        title="Wallet"
      >
        <FaWallet className="text-xl" />
      </button>

      <AnimatePresence>
        {open && (
          <motion.div
            initial={{ opacity: 0, y: 10, scale: 0.95 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, scale: 0.95 }}
            transition={{ duration: 0.2 }}
            className="absolute right-0 mt-2 w-56 bg-white rounded-lg shadow-xl z-50 border border-gray-200 overflow-hidden"
          >
            <div className="px-4 py-3 border-b border-gray-100 flex items-center space-x-3 bg-gradient-to-r from-slate-50 to-indigo-50">
              <div className="bg-credigo-accent/10 p-2 rounded-full">
                <FaWallet className="text-2xl text-credigo-accent" />
              </div>
              <div className="flex flex-col">
                <span className="font-semibold text-credigo-dark text-base">Wallet</span>
                <span className="text-xs text-gray-500">Current Balance</span>
              </div>
            </div>

            <div className="px-5 py-4 flex items-center justify-between bg-white">
              <motion.span
                initial={{ scale: 0.9 }}
                animate={{ scale: 1 }}
                className="font-bold text-credigo-dark text-lg"
              >
                {formattedBalance}
              </motion.span>
            </div>

            <motion.button
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
              onClick={() => { setOpen(false); onWallet(); }}
              className="w-full px-4 py-3 text-white bg-gradient-to-r from-credigo-accent to-purple-500 text-sm font-semibold transition-all duration-200 hover:shadow-md hover:shadow-purple-500/20 flex items-center justify-center gap-2"
            >
              <FaWallet className="text-sm" /> Go to Wallet
            </motion.button>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
