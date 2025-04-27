// src/components/TopUpForm.jsx
import React, { useState } from 'react';
import { CardElement, useStripe, useElements } from '@stripe/react-stripe-js';
import { useAuth } from '../context/AuthContext';

function TopUpForm({ clientSecret, onPaymentSuccess, onPaymentCancel }) {
  const stripe = useStripe();
  const elements = useElements();
  const { fetchWalletBalance } = useAuth();

  const [error, setError] = useState(null);
  const [processing, setProcessing] = useState(false);
  const [succeeded, setSucceeded] = useState(false);

  const cardElementOptions = {
    // Customize the appearance of the CardElement
    // See https://stripe.com/docs/js/elements_object/create_element?type=card#elements_create-options
    style: {
      base: {
        color: '#fffffe', // Match light text color
        fontFamily: '"Inter", sans-serif', // Match your font
        fontSize: '16px',
        '::placeholder': {
          color: '#a0aec0', // Placeholder text color
        },
        iconColor: '#fffffe', // Color for card icons
      },
      invalid: {
        color: '#f87171', // Color for invalid input (Tailwind red-400)
        iconColor: '#f87171',
      },
    },
    hidePostalCode: true, // Optionally hide the postal code field
  };


  const handleSubmit = async (event) => {
    event.preventDefault(); // Prevent default form submission
    setProcessing(true);
    setError(null);

    if (!stripe || !elements) {
      // Stripe.js has not loaded yet. Make sure to disable
      // form submission until Stripe.js has loaded.
      setError("Stripe.js hasn't loaded yet.");
      setProcessing(false);
      return;
    }

    // Get a reference to the mounted CardElement
    const cardElement = elements.getElement(CardElement);

    if (!cardElement) {
      setError("Card details element not found.");
      setProcessing(false);
      return;
    }

    console.log("Confirming card payment with client secret:", clientSecret);

    // Use cardElement to confirm the PaymentIntent
    // See: https://stripe.com/docs/js/payment_intents/confirm_card_payment
    const { error: confirmError, paymentIntent } = await stripe.confirmCardPayment(
      clientSecret, // The client secret obtained from your backend
      {
        payment_method: {
          card: cardElement,
          // Optional: Add billing details if needed
          // billing_details: {
          //   name: 'Jenny Rosen', // Get from user profile or form
          // },
        },
        // Optional: Specify where to redirect after payment (usually handled by webhook instead)
        // return_url: 'http://localhost:5173/payment-success',
      }
    );

    setProcessing(false);

    if (confirmError) {
      // Show error to your customer (e.g., insufficient funds, card declined)
      setError(confirmError.message || "An unexpected error occurred during payment.");
      console.error("[stripe confirm error]", confirmError);
      setSucceeded(false);
    } else if (paymentIntent && paymentIntent.status === 'succeeded') {
      // The payment has been processed!
      setError(null);
      setSucceeded(true);
      console.log("[PaymentIntent succeeded]", paymentIntent);
      alert("Top-up successful! Your balance will be updated shortly.");
      // Call the success callback passed from WalletPage
      if (onPaymentSuccess) {
        onPaymentSuccess();
      }
      // Fetch the new balance after a short delay to allow webhook processing
      setTimeout(fetchWalletBalance, 3000); // Refresh balance after 3 seconds
    } else {
      setError("Payment status: " + paymentIntent?.status ?? 'Unknown');
      setSucceeded(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="p-3 border rounded-lg bg-credigo-dark border-gray-600">
        <CardElement options={cardElementOptions} />
      </div>

      {/* Display errors or success message */}
      {error && <div className="text-red-400 text-sm font-medium">{error}</div>}
      {succeeded && <div className="text-green-400 text-sm font-medium">Payment Successful! Balance updating...</div>}

      <div className="flex items-center justify-between pt-2">
        {/* Optional: Cancel button */}
        <button
          type="button"
          onClick={onPaymentCancel} // Call cancel handler from props
          className="px-4 py-2 text-sm font-medium text-gray-400 hover:text-credigo-light transition"
        >
          Cancel
        </button>
        {/* Pay Button */}
        <button
          disabled={processing || !stripe || !elements || succeeded}
          className={`px-4 py-2 text-sm font-semibold text-credigo-dark bg-credigo-button border border-transparent rounded-lg shadow-sm hover:bg-opacity-90 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-credigo-input-bg focus:ring-credigo-button transition duration-150 ease-in-out ${(processing || !stripe || succeeded) ? 'opacity-50 cursor-not-allowed' : ''
            }`}
        >
          {processing ? 'Processing...' : 'Pay Now'}
        </button>
      </div>
    </form>
  );
}

export default TopUpForm;
