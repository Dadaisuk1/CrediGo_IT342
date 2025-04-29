// src/pages/WalletPage.jsx
import React, { useState, useEffect } from 'react'; // Added useEffect for potential future use
import { useAuth } from '../context/AuthContext';
import { TbCurrencyPeso } from "react-icons/tb";
import { createWalletTopUpIntent } from '../services/api'; // API function to create intent

// Stripe imports (Ensure Elements provider is in main.jsx or wrap here)
// import { loadStripe } from '@stripe/stripe-js'; // Only needed if provider is NOT in main.jsx
// import { Elements } from '@stripe/react-stripe-js'; // Only needed if provider is NOT in main.jsx

// Import the actual form component
import TopUpForm from '../components/TopUpForm'; // *** Import the form ***

// const stripePublishableKey = 'pk_test_YOUR_PUBLISHABLE_KEY_HERE'; // Key should be loaded in main.jsx
// const stripePromise = loadStripe(stripePublishableKey); // Only needed if provider is NOT in main.jsx

function WalletPage() {
  const { user, walletBalance, fetchWalletBalance, loading: authLoading, error: authError } = useAuth();
  const [topUpAmount, setTopUpAmount] = useState(''); // Amount user wants to add
  const [clientSecret, setClientSecret] = useState(null); // Stripe PaymentIntent client secret
  const [isProcessingIntent, setIsProcessingIntent] = useState(false); // Loading state for creating intent
  const [topUpError, setTopUpError] = useState(null); // Error state for top-up process

  // Format balance for display
  const formattedBalance = walletBalance !== null
    ? new Intl.NumberFormat('en-PH', { style: 'currency', currency: 'PHP' }).format(walletBalance)
    : 'Loading...';

  // Handler for submitting the amount to get a clientSecret
  const handleTopUpSubmit = async (e) => {
    e.preventDefault();
    setIsProcessingIntent(true); // Start loading
    setTopUpError(null);
    setClientSecret(null); // Reset previous secret

    const amount = parseFloat(topUpAmount);
    if (isNaN(amount) || amount <= 0) {
      setTopUpError("Please enter a valid positive amount.");
      setIsProcessingIntent(false);
      return;
    }
    // Ensure minimum amount if needed (e.g., >= 50.00)
    if (amount < 50.00) {
      setTopUpError("Minimum top-up amount is P50.00.");
      setIsProcessingIntent(false);
      return;
    }

    try {
      // Call backend to create the Payment Intent
      const response = await createWalletTopUpIntent({ amount });
      if (response.data?.clientSecret) {
        console.log("PaymentIntent created, clientSecret received.");
        setClientSecret(response.data.clientSecret); // Set secret to show the Stripe form
      } else {
        throw new Error("Failed to get client secret from server.");
      }
    } catch (err) {
      console.error("Failed to create PaymentIntent:", err);
      setTopUpError(err.response?.data || err.message || "Could not initiate top-up.");
    } finally {
      setIsProcessingIntent(false); // Stop loading
    }
  };

  // --- Callback functions for TopUpForm ---
  const handlePaymentSuccess = () => {
    console.log("TopUpForm reported payment success.");
    setClientSecret(null); // Hide the Stripe form
    setTopUpError(null);   // Clear any previous errors
    // Demo: Immediately update wallet balance in UI
    if (typeof setWalletBalance === 'function') {
      const amt = parseFloat(topUpAmount);
      if (!isNaN(amt) && amt > 0) {
        setWalletBalance(prev => (prev !== null ? prev + amt : amt));
      }
    }
    setTopUpAmount('');    // Clear the amount input
    alert("Payment successful! Your balance will be updated shortly after the webhook is processed.");
    // Fetch balance again after a short delay to allow webhook processing
    setTimeout(fetchWalletBalance, 2000); // Refresh balance after 2 seconds
  };


  const handlePaymentCancel = () => {
    console.log("TopUpForm payment cancelled.");
    setClientSecret(null); // Hide the Stripe form, show amount input again
    setTopUpError(null);   // Clear errors
  };

  const handlePaymentError = (errorMessage) => {
    console.error("TopUpForm reported payment error:", errorMessage);
    setTopUpError(errorMessage || "An error occurred during payment.");
    // Keep clientSecret so user can potentially retry with the same intent?
    // Or setClientSecret(null) to force starting over? Let's reset for simplicity.
    setClientSecret(null);
  };
  // --- End Callbacks ---

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

          {/* --- MOCK WALLET ADJUSTMENT FORM (for demo, like AdminWallet) --- */}
          {/*
          <form onSubmit={(e) => {
            e.preventDefault();
            const amt = parseFloat(topUpAmount);
            if (isNaN(amt) || amt === 0) {
              alert('Please enter a valid non-zero amount.');
              return;
            }
            // This assumes setWalletBalance is available from useAuth
            setWalletBalance && setWalletBalance((prev) => (prev !== null ? prev + amt : amt));
            alert(`Demo: Wallet ${amt > 0 ? 'credited' : 'debited'} by ₱${Math.abs(amt).toFixed(2)} (frontend only)`);
          }} className="flex gap-2 mb-4">
            <input
              type="number"
              step="0.01"
              value={topUpAmount}
              onChange={e => setTopUpAmount(e.target.value)}
              placeholder="Amount (e.g. 100 or -50)"
              className="p-2 rounded border border-gray-600 bg-gray-800 text-white flex-1"
            />
            <button
              type="submit"
              className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
            >
              Adjust (Mock)
            </button>
          </form>
          <p className="text-xs text-gray-500 mb-2">For demo only. This does not persist to backend.</p>
          */}

          {/* Conditionally render Amount Form OR Stripe Form */}
          {!clientSecret ? (
            // --- Show Amount Input Form ---
            <>
              <p className="text-sm text-gray-400 mb-4">Enter the amount you wish to add (Min P50.00).</p>
              <form onSubmit={handleTopUpSubmit} className="space-y-4">
                <div>
                  <label htmlFor="topUpAmount" className="block text-sm font-medium text-credigo-light/80 mb-1">Amount (PHP)</label>
                  <div className="relative">
                    <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3">
                      <TbCurrencyPeso className="h-5 w-5 text-gray-400" aria-hidden="true" />
                    </div>
                    <input
                      type="number" name="topUpAmount" id="topUpAmount"
                      step="0.01" min="50.00" required
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
                  disabled={isProcessingIntent}
                  className={`w-full flex justify-center px-4 py-2 text-sm font-semibold text-credigo-dark bg-credigo-button border border-transparent rounded-lg shadow-sm hover:bg-opacity-90 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-credigo-input-bg focus:ring-credigo-button transition duration-150 ease-in-out ${isProcessingIntent ? 'opacity-50 cursor-not-allowed' : ''}`}
                >
                  {isProcessingIntent ? 'Processing...' : 'Proceed to Payment'}
                </button>
              </form>
            </>
          ) : (
            // --- Show Stripe Form ---
            // Elements provider should be wrapping App in main.jsx
            <>
              <p className="text-sm text-gray-400 mb-4">Enter your card details below to complete the top-up of {new Intl.NumberFormat('en-PH', { style: 'currency', currency: 'PHP' }).format(parseFloat(topUpAmount || '0'))}:</p>
              {/* Render the TopUpForm component */}
              <TopUpForm
                clientSecret={clientSecret}
                amount={parseFloat(topUpAmount || '0')} // Pass amount to form
                onPaymentSuccess={handlePaymentSuccess}
                onPaymentCancel={handlePaymentCancel}
                onPaymentError={handlePaymentError}
              />
            </>
          )}
        </div>
      </div>
    </div>
  );
}

export default WalletPage;
