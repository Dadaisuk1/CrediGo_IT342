// src/components/TopUpForm.jsx
import React, { useState } from 'react';
import { createPaymentIntent } from '../services/api';

/**
 * Form component using PayMongo to confirm a PaymentIntent.
 */
function TopUpForm({ amount, onPaymentSuccess, onPaymentCancel, onPaymentError }) {
  const [error, setError] = useState(null);
  const [processing, setProcessing] = useState(false);
  const [succeeded, setSucceeded] = useState(false);
  const [selectedMethod, setSelectedMethod] = useState('gcash'); // Default payment method

  // Supported PayMongo payment methods
  const paymentMethods = [
    { value: 'gcash', label: 'GCash' },
    { value: 'card', label: 'Card' },
    { value: 'paymaya', label: 'PayMaya' },
  ];

  const handleSubmit = async (event) => {
    event.preventDefault();
    setProcessing(true);
    setError(null);
    setSucceeded(false);
    try {
      // Call backend to create a PayMongo payment intent
      const data = await createPaymentIntent(Number(amount), 'PHP', selectedMethod);
      // Expect backend to return a checkout_url or similar for the selected method
      if (data.checkout_url) {
        // Redirect to PayMongo checkout page (for GCash, Card, PayMaya, etc.)
        window.location.href = data.checkout_url;
      } else {
        setError('No checkout URL returned by backend.');
      }
      setSucceeded(true);
      if (onPaymentSuccess) onPaymentSuccess();
    } catch (err) {
      setError(typeof err === 'string' ? err : JSON.stringify(err));
      if (onPaymentError) onPaymentError(err);
    } finally {
      setProcessing(false);
    }
  };

  // Format amount for display
  const formattedAmount = new Intl.NumberFormat('en-PH', { style: 'currency', currency: 'PHP' }).format(amount || 0);

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      {/* Payment Method Selector */}
      <label className="block text-sm font-medium text-credigo-light/80 mb-1">
        Choose Payment Method
      </label>
      <div className="flex space-x-4 mb-3">
        {paymentMethods.map((method) => (
          <label key={method.value} className={`flex items-center px-3 py-2 rounded-lg cursor-pointer border border-gray-600 bg-credigo-dark text-credigo-light transition-colors duration-150 ${selectedMethod === method.value ? 'ring-2 ring-credigo-button border-credigo-button bg-credigo-button/10' : ''}`}>
            <input
              type="radio"
              name="paymentMethod"
              value={method.value}
              checked={selectedMethod === method.value}
              onChange={() => setSelectedMethod(method.value)}
              className="form-radio h-4 w-4 text-credigo-button mr-2"
            />
            {method.label}
          </label>
        ))}
      </div>

      {/* Display Messages */}
      {error && <div className="text-red-400 text-sm font-medium">{error}</div>}
      {succeeded && <div className="text-green-400 text-sm font-medium">Payment Successful! Redirecting...</div>}

      {/* Submit Button */}
      <button
        disabled={processing || succeeded}
        className={`w-full flex justify-center px-4 py-2 text-sm font-semibold text-credigo-dark bg-credigo-button border border-transparent rounded-lg shadow-sm hover:bg-opacity-90 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-credigo-input-bg focus:ring-credigo-button transition duration-150 ease-in-out ${(processing || succeeded) ? 'opacity-50 cursor-not-allowed' : ''}`}
      >
        {processing ? 'Processing...' : `Pay ${formattedAmount}`}
      </button>

      {/* Optional: Cancel Button */}
      {!succeeded && (
        <button
          type="button"
          onClick={onPaymentCancel}
          disabled={processing}
          className="w-full flex justify-center px-4 py-2 text-sm font-medium text-gray-300 bg-gray-600 border border-transparent rounded-lg shadow-sm hover:bg-gray-500 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-credigo-input-bg focus:ring-gray-500 transition duration-150 ease-in-out mt-2 disabled:opacity-50"
        >
          Cancel
        </button>
      )}
    </form>
  );
}

export default TopUpForm;
