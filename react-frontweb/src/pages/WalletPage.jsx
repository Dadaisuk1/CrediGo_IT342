// src/pages/WalletPage.jsx
import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { TbCurrencyPeso } from "react-icons/tb";
import { createWalletTopUpIntent } from '../services/api';
import { loadStripe } from '@stripe/stripe-js';
import { Elements } from '@stripe/react-stripe-js';
import TopUpForm from '../components/TopUpForm';

const stripePublishableKey = 'pk_test_YOUR_PUBLISHABLE_KEY_HERE'; // Replace with your actual key
const stripePromise = loadStripe(stripePublishableKey);

function WalletPage() {
  const { user, walletBalance, fetchWalletBalance, loading: authLoading, error: authError } = useAuth();
  const [topUpAmount, setTopUpAmount] = useState('');
  const [clientSecret, setClientSecret] = useState(null);
  const [isProcessingTopUp, setIsProcessingTopUp] = useState(false);
  const [topUpError, setTopUpError] = useState(null);

  const formattedBalance = walletBalance !== null
    ? new Intl.NumberFormat('en-PH', { style: 'currency', currency: 'PHP' }).format(walletBalance)
    : 'Loading...';

  const handleTopUpSubmit = async (e) => {
    e.preventDefault();
    setIsProcessingTopUp(true);
    setTopUpError(null);
    setClientSecret(null);

    const amount = parseFloat(topUpAmount);
    if (isNaN(amount) || amount <= 0) {
      setTopUpError("Please enter a valid positive amount.");
      setIsProcessingTopUp(false);
      return;
    }

    try {
      const response = await createWalletTopUpIntent({ amount });
      if (response.data?.clientSecret) {
        setClientSecret(response.data.clientSecret);
      } else {
        throw new Error("Failed to get client secret from server.");
      }
    } catch (err) {
      setTopUpError(err.response?.data || err.message || "Could not initiate top-up.");
    } finally {
      setIsProcessingTopUp(false);
    }
  };

  const handlePaymentSuccess = () => {
    setClientSecret(null);
    setTopUpAmount('');
    setTimeout(fetchWalletBalance, 1000);
  };

  const handlePaymentCancel = () => {
    setClientSecret(null);
    setTopUpError("Payment cancelled.");
  };

  return (
    <div className="font-sans p-4 md:p-6 text-credigo-light">
      <h1 className="text-3xl font-bold mb-6">My Wallet</h1>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="bg-credigo-input-bg p-6 rounded-lg shadow-md border border-gray-700">
          <h2 className="text-xl font-semibold text-credigo-light mb-4">Current Balance</h2>
          {authLoading ? (
            <p className="text-gray-400">Loading balance...</p>
          ) : authError ? (
            <p className="text-red-400">{authError}</p>
          ) : (
            <div className="flex items-center text-4xl font-bold text-credigo-light">
              <TbCurrencyPeso size={36} className="mr-1 inline-block" />
              <span>{formattedBalance}</span>
            </div>
          )}
          <button onClick={fetchWalletBalance} disabled={authLoading} className="mt-4 text-sm text-credigo-button hover:text-opacity-80 disabled:opacity-50 disabled:cursor-not-allowed">
            Refresh Balance
          </button>
        </div>

        <div className="bg-credigo-input-bg p-6 rounded-lg shadow-md border border-gray-700">
          <h2 className="text-xl font-semibold text-credigo-light mb-4">Add Funds</h2>

          {!clientSecret ? (
            <>
              <p className="text-sm text-gray-400 mb-4">Enter the amount you wish to add.</p>
              <form onSubmit={handleTopUpSubmit} className="space-y-4">
                <div>
                  <label htmlFor="topUpAmount" className="block text-sm font-medium text-credigo-light/80 mb-1">Amount (PHP)</label>
                  <div className="relative">
                    <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3">
                      <TbCurrencyPeso className="h-5 w-5 text-gray-400" aria-hidden="true" />
                    </div>
                    <input
                      type="number"
                      name="topUpAmount"
                      id="topUpAmount"
                      step="0.01"
                      min="50.00"
                      required
                      value={topUpAmount}
                      onChange={(e) => setTopUpAmount(e.target.value)}
                      className="block w-full rounded-lg border border-gray-600 bg-credigo-dark py-2 pl-10 pr-4 text-credigo-light placeholder-gray-400 focus:border-credigo-button focus:outline-none focus:ring-1 focus:ring-credigo-button sm:text-sm"
                      placeholder="e.g., 500.00"
                    />
                  </div>
                </div>
                {topUpError && <div className="text-red-400 text-sm">{topUpError}</div>}
                <button
                  type="submit"
                  disabled={isProcessingTopUp}
                  className={`w-full flex justify-center px-4 py-2 text-sm font-semibold text-credigo-dark bg-credigo-button border border-transparent rounded-lg shadow-sm hover:bg-opacity-90 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-credigo-input-bg focus:ring-credigo-button transition duration-150 ease-in-out ${isProcessingTopUp ? 'opacity-50 cursor-not-allowed' : ''}`}
                >
                  {isProcessingTopUp ? 'Processing...' : 'Proceed to Payment'}
                </button>
              </form>
            </>
          ) : (
            <Elements stripe={stripePromise}>
              <TopUpForm
                clientSecret={clientSecret}
                onPaymentSuccess={handlePaymentSuccess}
                onPaymentCancel={handlePaymentCancel}
              />
            </Elements>
          )}
        </div>
      </div>
    </div>
  );
}

export default WalletPage;
