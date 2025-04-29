import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import TopUpForm from '../components/TopUpForm';

const AdminWallet = () => {
  const { walletBalance, user } = useAuth();
  const [amount, setAmount] = useState('');
  const [topUpSuccess, setTopUpSuccess] = useState(false);
  const [topUpError, setTopUpError] = useState(null);

  // --- MOCK ADMIN-ONLY ADJUSTMENT (commented out for reference) ---
  /*
  const { setWalletBalance } = useAuth();
  const [message, setMessage] = useState('');
  const handleMockAdjust = (e) => {
    e.preventDefault();
    const amt = parseFloat(amount);
    if (isNaN(amt) || amt === 0) {
      setMessage('Please enter a valid non-zero amount.');
      return;
    }
    setWalletBalance((prev) => (prev !== null ? prev + amt : amt));
    setMessage(`Demo: Wallet ${amt > 0 ? 'credited' : 'debited'} by ₱${Math.abs(amt).toFixed(2)} (frontend only)`);
    setAmount('');
  };
  */

  // --- TopUpForm integration for user wallet top-up ---
  const handlePaymentSuccess = () => {
    setTopUpSuccess(true);
    setTopUpError(null);
    setAmount('');
    // Optionally: refresh wallet balance if PayMongo API is live
  };
  const handlePaymentError = (err) => {
    setTopUpError(typeof err === 'string' ? err : JSON.stringify(err));
  };

  return (
    <div className="max-w-lg mx-auto p-6 bg-gray-900 rounded-lg shadow">
      <h2 className="text-2xl font-bold mb-4">Wallet Top-up (User Demo)</h2>
      <p className="mb-2 text-gray-400">This form uses the PayMongo/TopUpForm logic for all users. No admin privileges required.</p>
      <div className="mb-4">
        <span className="font-semibold">Current Wallet Balance:</span> <span className="text-green-400">₱{walletBalance !== null ? walletBalance.toFixed(2) : '---'}</span>
      </div>
      <div className="mb-4">
        <label htmlFor="amount" className="block text-sm mb-1">Amount to Add</label>
        <input
          id="amount"
          type="number"
          step="0.01"
          value={amount}
          onChange={e => setAmount(e.target.value)}
          placeholder="Enter amount (e.g. 100)"
          className="p-2 rounded border border-gray-600 bg-gray-800 text-white w-full"
        />
      </div>
      <TopUpForm
        amount={amount}
        onPaymentSuccess={handlePaymentSuccess}
        onPaymentError={handlePaymentError}
      />
      {topUpSuccess && <div className="text-green-400 mt-2">Top-up successful! (Demo)</div>}
      {topUpError && <div className="text-red-400 mt-2">{topUpError}</div>}
      {/*
      <h2 className="text-2xl font-bold mb-4">Mock Wallet Adjustment (Demo Only)</h2>
      <form onSubmit={handleMockAdjust} className="flex gap-2 mb-2">
        <input
          type="number"
          step="0.01"
          value={amount}
          onChange={e => setAmount(e.target.value)}
          placeholder="Amount (e.g. 100 or -50)"
          className="p-2 rounded border border-gray-600 bg-gray-800 text-white flex-1"
        />
        <button
          type="submit"
          className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
        >
          Adjust
        </button>
      </form>
      {message && <div className="text-yellow-300 mb-2">{message}</div>}
      <p className="text-xs text-gray-500">For demo only. To persist wallet changes, implement a backend endpoint.</p>
      */}
    </div>
  );
};

export default AdminWallet;
