// src/pages/WalletPage.jsx
import React, { useState } from 'react';
import TopUpForm from '../components/TopUpForm';
import { useAuth } from '../context/AuthContext';

function WalletPage() {
  const { walletBalance, fetchWalletBalance, loading: authLoading, error: authError } = useAuth();
  const [topUpError, setTopUpError] = useState(null);

  // Format balance for display
  const formattedBalance = walletBalance !== null
    ? new Intl.NumberFormat('en-PH', { style: 'currency', currency: 'PHP' }).format(walletBalance)
    : 'Loading...';

  return (
    <div className="font-sans p-4 md:p-6 text-credigo-light">
      <h1 className="text-3xl text-credigo-dark font-bold mb-6">My Wallet</h1>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">

        {/* Balance Display Card */}
        <div className="bg-credigo-input-bg p-6 rounded-lg shadow-md border border-gray-700">
          <h2 className="text-xl font-semibold text-credigo-light mb-4">Current Balance</h2>
          {authLoading ? (
            <p className="text-gray-400">Loading balance...</p>
          ) : authError ? (
            <p className="text-red-400">{authError}</p>
          ) : (
            <div className="flex items-center text-4xl font-bold text-credigo-light">
              <span>{formattedBalance}</span>
            </div>
          )}
          <button onClick={fetchWalletBalance} disabled={authLoading} className="mt-4 text-sm text-credigo-button hover:text-opacity-80 disabled:opacity-50 disabled:cursor-not-allowed">
            Refresh Balance
          </button>
        </div>

        {/* Top-up Card */}
        <div className="bg-credigo-input-bg p-6 rounded-lg shadow-md border border-gray-700">
          <h2 className="text-xl font-semibold text-credigo-light mb-4">Add Funds</h2>

          <p className="text-sm text-gray-400 mb-4">Use our secure payment system to add funds to your wallet.</p>

          {topUpError && <div className="text-red-400 text-sm mb-4">{topUpError}</div>}

          <TopUpForm
            onPaymentSuccess={() => {
              console.log("Payment successful!");
              setTopUpError(null);
              // Fetch updated wallet balance
              fetchWalletBalance();
            }}
            onPaymentCancel={() => {
              console.log("Payment cancelled.");
            }}
            onPaymentError={(errorMessage) => {
              console.error("Payment error:", errorMessage);
              setTopUpError(errorMessage || "An error occurred during payment processing.");
            }}
          />
        </div>
      </div>
    </div>
  );
}

export default WalletPage;
