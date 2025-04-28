// src/components/TopUpForm.jsx
import React, { useState } from 'react';
import { CardElement, useStripe, useElements } from '@stripe/react-stripe-js';

/**
 * Form component using Stripe CardElement to confirm a PaymentIntent.
 * This component must be rendered inside an <Elements> provider.
 */
function TopUpForm({ clientSecret, amount, onPaymentSuccess, onPaymentCancel, onPaymentError }) {
  const stripe = useStripe(); // Hook to get the Stripe object instance
  const elements = useElements(); // Hook to get access to mounted Elements (like CardElement)

  const [error, setError] = useState(null);
  const [processing, setProcessing] = useState(false);
  const [succeeded, setSucceeded] = useState(false);

  // Style options for the CardElement
  const cardElementOptions = {
    style: {
      base: {
        color: '#fffffe', // Text color (credigo-light)
        fontFamily: '"Montserrat", "Inter", sans-serif', // Match your main font
        fontSize: '16px',
        '::placeholder': {
          color: '#a0aec0', // Placeholder text color (Tailwind gray-400 equivalent)
        },
        iconColor: '#fffffe', // Color for card brand icons
      },
      invalid: {
        color: '#f87171', // Text color for errors (Tailwind red-400 equivalent)
        iconColor: '#f87171',
      },
    },
    hidePostalCode: true, // Optional: hide postal code field if not needed
  };

  const handleSubmit = async (event) => {
    event.preventDefault(); // Prevent default form submission
    setProcessing(true);
    setError(null);

    if (!stripe || !elements) {
      // Stripe.js has not loaded yet. Make sure to disable
      // form submission until Stripe.js has loaded.
      setError("Stripe.js hasn't loaded yet. Please wait a moment and try again.");
      setProcessing(false);
      return;
    }

    const cardElement = elements.getElement(CardElement);
    if (!cardElement) {
      setError("Card details element could not be found.");
      setProcessing(false);
      return;
    }

    console.log("Confirming card payment with client secret:", clientSecret);

    // Confirm the payment using the clientSecret from the PaymentIntent
    // and the CardElement containing the user's input.
    const payload = await stripe.confirmCardPayment(clientSecret, {
      payment_method: {
        card: cardElement,
        // Optional: Add billing details if needed/collected
        // billing_details: {
        //   name: 'Jenny Rosen', // Example
        // },
      },
    });

    setProcessing(false);

    if (payload.error) {
      // Show error to your customer (e.g., insufficient funds, card declined).
      setError(`Payment failed: ${payload.error.message}`);
      console.error("[Stripe Error]", payload.error);
      if (onPaymentError) onPaymentError(payload.error.message); // Notify parent component
    } else if (payload.paymentIntent && payload.paymentIntent.status === 'succeeded') {
      // PaymentIntent succeeded
      setError(null);
      setSucceeded(true);
      console.log("[PaymentIntent Succeeded]", payload.paymentIntent);
      alert("Top-up payment successful! Your balance will update shortly after the webhook is processed.");
      if (onPaymentSuccess) onPaymentSuccess(); // Notify parent component
    } else {
      // Handle other statuses if necessary (e.g., requires_action)
      setError(`Payment processing status: ${payload.paymentIntent?.status}. Please check later or contact support.`);
      console.warn("[PaymentIntent Status]", payload.paymentIntent?.status);
      if (onPaymentError) onPaymentError(`Payment status: ${payload.paymentIntent?.status}`);
    }
  };

  // Format amount for display
  const formattedAmount = new Intl.NumberFormat('en-PH', { style: 'currency', currency: 'PHP' }).format(amount || 0);

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      {/* Card Element Input */}
      <label className="block text-sm font-medium text-credigo-light/80 mb-1">
        Card Details
      </label>
      <div className="p-3 border rounded-md bg-credigo-dark border-gray-600 focus-within:ring-2 focus-within:ring-credigo-button">
        <CardElement options={cardElementOptions} />
      </div>

      {/* Display Messages */}
      {error && <div className="text-red-400 text-sm font-medium">{error}</div>}
      {succeeded && <div className="text-green-400 text-sm font-medium">Payment Successful! Balance update pending.</div>}

      {/* Submit Button */}
      <button
        disabled={processing || !stripe || !elements || succeeded}
        className={`w-full flex justify-center px-4 py-2 text-sm font-semibold text-credigo-dark bg-credigo-button border border-transparent rounded-lg shadow-sm hover:bg-opacity-90 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-credigo-input-bg focus:ring-credigo-button transition duration-150 ease-in-out ${(processing || !stripe || succeeded) ? 'opacity-50 cursor-not-allowed' : ''
          }`}
      >
        {processing ? 'Processing...' : `Pay ${formattedAmount}`}
      </button>

      {/* Optional: Cancel Button */}
      {!succeeded && (
        <button
          type="button"
          onClick={onPaymentCancel} // Call parent's cancel handler
          disabled={processing} // Disable cancel while processing
          className="w-full flex justify-center px-4 py-2 text-sm font-medium text-gray-300 bg-gray-600 border border-transparent rounded-lg shadow-sm hover:bg-gray-500 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-credigo-input-bg focus:ring-gray-500 transition duration-150 ease-in-out mt-2 disabled:opacity-50"
        >
          Cancel
        </button>
      )}
    </form>
  );
}

export default TopUpForm;
