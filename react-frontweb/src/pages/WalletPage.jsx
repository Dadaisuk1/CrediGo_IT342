// src/pages/WalletPage.jsx
import React, { useState } from 'react'; // Import useState if adding top-up form
import { useAuth } from '../context/AuthContext';
import { TbCurrencyPeso } from "react-icons/tb"; // Import Peso icon
// Import API function for creating payment intent if adding top-up form
// import { createWalletTopUpIntent } from '../services/api';
// Import Stripe components if integrating Stripe Elements here
// import { loadStripe } from '@stripe/stripe-js';
// import { Elements, CardElement, useStripe, useElements } from '@stripe/react-stripe-js';

// TODO: Replace with your actual Stripe Publishable Key (Test Key)
// const stripePromise = loadStripe('pk_test_YOUR_PUBLISHABLE_KEY');

// Optional: Form component for Stripe Elements (if adding top-up here)
/*
const TopUpForm = ({ clientSecret }) => {
  const stripe = useStripe();
  const elements = useElements();
  const [error, setError] = useState(null);
  const [processing, setProcessing] = useState(false);
  const [succeeded, setSucceeded] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setProcessing(true);
    setError(null);

    if (!stripe || !elements) {
      // Stripe.js has not yet loaded.
      setError("Stripe.js hasn't loaded yet.");
      setProcessing(false);
      return;
    }

    const cardElement = elements.getElement(CardElement);

    const payload = await stripe.confirmCardPayment(clientSecret, {
      payment_method: {
        card: cardElement,
        // billing_details: { name: 'Jenny Rosen' }, // Optional billing details
      },
    });

    setProcessing(false);

    if (payload.error) {
      setError(`Payment failed: ${payload.error.message}`);
      console.error("[stripe error]", payload.error);
    } else {
      setError(null);
      setSucceeded(true);
      console.log("[PaymentIntent]", payload.paymentIntent);
      alert("Top-up successful! Balance will update shortly via webhook.");
      // TODO: Optionally refresh wallet balance or wait for webhook update
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="p-3 border rounded-md bg-credigo-dark border-gray-600">
        <CardElement options={{ style: { base: { color: '#fffffe', '::placeholder': { color: '#a0aec0' } } } }} />
      </div>
      {error && <div className="text-red-400 text-sm">{error}</div>}
      {succeeded && <div className="text-green-400 text-sm">Payment Successful!</div>}
      <button
        disabled={processing || !stripe || !elements || succeeded}
        className={`w-full flex justify-center px-4 py-2 text-sm font-semibold text-credigo-dark bg-credigo-button border border-transparent rounded-lg shadow-sm hover:bg-opacity-90 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-credigo-input-bg focus:ring-credigo-button transition duration-150 ease-in-out ${
          (processing || !stripe || succeeded) ? 'opacity-50 cursor-not-allowed' : ''
        }`}
      >
        {processing ? 'Processing...' : 'Pay Now'}
      </button>
    </form>
  );
};
*/


function WalletPage() {
  const { user, walletBalance, fetchWalletBalance, loading: authLoading, error: authError } = useAuth();
  // State for top-up amount input
  const [topUpAmount, setTopUpAmount] = useState('');
  // State for Stripe client secret
  // const [clientSecret, setClientSecret] = useState(null);
  // State for top-up specific loading/error
  const [isProcessingTopUp, setIsProcessingTopUp] = useState(false);
  const [topUpError, setTopUpError] = useState(null);


  // Format balance for display
  const formattedBalance = walletBalance !== null
    ? new Intl.NumberFormat('en-PH').format(walletBalance)
    : 'Loading...';

  // Handler for initiating top-up (creating PaymentIntent)
  const handleTopUpSubmit = async (e) => {
    e.preventDefault();
    setIsProcessingTopUp(true);
    setTopUpError(null);
    // setClientSecret(null); // Reset previous secret

    const amount = parseFloat(topUpAmount);
    if (isNaN(amount) || amount <= 0) { // Basic validation
      setTopUpError("Please enter a valid positive amount.");
      setIsProcessingTopUp(false);
      return;
    }

    alert("Stripe Elements/Payment Confirmation not fully implemented in this example.");
    setIsProcessingTopUp(false);

    // --- Uncomment below to integrate Stripe Payment Intent creation ---
    /*
    try {
        const response = await createWalletTopUpIntent({ amount });
        if (response.data && response.data.clientSecret) {
            console.log("PaymentIntent created:", response.data.clientSecret);
            setClientSecret(response.data.clientSecret); // Set secret to render Stripe Elements form
        } else {
            throw new Error("Failed to get client secret from server.");
        }
    } catch (err) {
        console.error("Failed to create PaymentIntent:", err);
        setTopUpError(err.response?.data || err.message || "Could not initiate top-up.");
    } finally {
        setIsProcessingTopUp(false);
    }
    */
  };


  return (
    <div className="font-sans p-4 md:p-6">
      <h1 className="text-3xl font-bold mb-6 text-credigo-dark">My Wallet</h1>

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
              <TbCurrencyPeso size={36} className="mr-1 inline-block" />
              <span>{formattedBalance}</span>
            </div>
          )}
          <button
            onClick={fetchWalletBalance} // Allow manual refresh
            disabled={authLoading}
            className="mt-4 text-sm text-credigo-button hover:text-opacity-80 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Refresh Balance
          </button>
        </div>

        {/* Top-up Card */}
        <div className="bg-credigo-input-bg p-6 rounded-lg shadow-md border border-gray-700">
          <h2 className="text-xl font-semibold text-credigo-light mb-4">Add Funds</h2>
          <p className="text-sm text-gray-400 mb-4">Enter the amount you wish to add to your wallet.</p>

          {/* --- Top-up Amount Form --- */}
          {/* {!clientSecret ? ( // Show amount input form if no clientSecret */}
          <form onSubmit={handleTopUpSubmit} className="space-y-4">
            <div>
              <label htmlFor="topUpAmount" className="block text-sm font-medium text-credigo-light/80 mb-1">
                Amount (PHP)
              </label>
              <div className="relative">
                <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3">
                  <TbCurrencyPeso className="h-5 w-5 text-gray-400" aria-hidden="true" />
                </div>
                <input
                  type="number"
                  name="topUpAmount"
                  id="topUpAmount"
                  step="0.01" // Allow cents
                  min="50.00" // Match DTO validation if possible
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
              className={`w-full flex justify-center px-4 py-2 text-sm font-semibold text-credigo-dark bg-credigo-button border border-transparent rounded-lg shadow-sm hover:bg-opacity-90 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-credigo-input-bg focus:ring-credigo-button transition duration-150 ease-in-out ${isProcessingTopUp ? 'opacity-50 cursor-not-allowed' : ''
                }`}
            >
              {isProcessingTopUp ? 'Processing...' : 'Proceed to Payment'}
            </button>
          </form>
          {/* ) : ( */}
          {/* --- Stripe Elements Form (Rendered when clientSecret is available) --- */}
          {/* <p className="text-sm text-gray-400 mb-4">Enter your card details below:</p>
             <Elements stripe={stripePromise} options={{ clientSecret }}>
                <TopUpForm clientSecret={clientSecret} />
             </Elements> */}
          {/* )} */}

        </div>
      </div>

      {/* TODO: Add Wallet Transaction History section here later */}
      {/* <div className="mt-8 bg-credigo-input-bg p-6 rounded-lg shadow-md border border-gray-700">
           <h2 className="text-xl font-semibold text-credigo-light mb-4">Wallet History</h2>
           </div> */}
    </div>
  );
}

export default WalletPage;
