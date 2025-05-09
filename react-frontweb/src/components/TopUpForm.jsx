// src/components/TopUpForm.jsx
import React, { useState } from 'react';
import { toast } from 'react-toastify';
import { createWalletTopUpIntent } from '../services/api';

/**
 * Form component for PayMongo wallet top-up using Links API.
 */
function TopUpForm({ onPaymentSuccess, onPaymentCancel, onPaymentError }) {
  const [error, setError] = useState(null);
  const [processing, setProcessing] = useState(false);
  const [selectedMethod, setSelectedMethod] = useState('gcash'); // Default to GCash instead of card
  const [amount, setAmount] = useState('');

  // Supported PayMongo payment methods
  const paymentMethods = [
    { value: 'card', label: 'Credit/Debit Card' },
    { value: 'gcash', label: 'GCash' },
    { value: 'paymaya', label: 'Maya (PayMaya)' },
    { value: 'grab_pay', label: 'GrabPay' },
  ];

  // Function to validate form
  const validateForm = () => {
    if (!amount || parseFloat(amount) < 100) {
      setError('Minimum amount is PHP 100.00');
      return false;
    }

    setError(null);
    return true;
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setProcessing(true);
    setError(null);

    if (!validateForm()) {
      setProcessing(false);
      return;
    }

    try {
      // Prepare payment data
      const paymentData = {
        amount: parseFloat(amount),
        paymentType: selectedMethod
      };

      console.log("Sending payment data:", paymentData);

      // Call backend to create a PayMongo payment link
      const response = await createWalletTopUpIntent(paymentData);
      console.log("Payment API response:", response);

      const data = response.data;

      // Store transaction info in localStorage for reference
      if (data.id) {
        localStorage.setItem(`payment_${data.id}`, JSON.stringify({
          amount: data.amount,
          created: Date.now(),
          status: data.status || 'pending',
          username: localStorage.getItem('username') || 'Anonymous User',
          paymentType: selectedMethod
        }));
      }

      // For Links API, redirect to the checkout URL
      if (data.checkoutUrl) {
        // Redirect to PayMongo checkout page
        window.location.href = data.checkoutUrl;
      } else {
        throw new Error('No checkout URL received from the server');
      }
    } catch (error) {
      console.error('Payment creation error:', error);
      setError(error.response?.data?.message || error.message || 'Failed to process payment');
      if (onPaymentError) {
        onPaymentError(error.response?.data?.message || error.message || 'Failed to process payment');
      }
      toast.error('Payment creation failed: ' + (error.response?.data?.message || error.message || 'Unknown error'));
    } finally {
      setProcessing(false);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      {/* Amount Field */}
      <div className="mb-4">
        <label htmlFor="amount" className="block text-sm font-medium text-credigo-light mb-1">
          Amount (PHP)
        </label>
        <div className="relative">
          <span className="absolute inset-y-0 left-0 flex items-center pl-3 text-credigo-light">₱</span>
          <input
            id="amount"
            type="number"
            placeholder="100.00"
            min="100"
            step="0.01"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            className="block w-full pl-8 pr-2 py-2 rounded-md bg-credigo-input-field text-credigo-light border border-gray-600 focus:border-credigo-button focus:outline-none focus:ring-1 focus:ring-credigo-button"
          />
        </div>
        <p className="mt-1 text-xs text-gray-500">Minimum amount: ₱100.00</p>
      </div>

      {/* Payment Method Selection */}
      <div className="mb-4">
        <label className="block text-sm font-medium text-credigo-light mb-1">
          Payment Method
        </label>
        <div className="grid grid-cols-2 gap-2">
          {paymentMethods.map((method) => (
            <button
              key={method.value}
              type="button"
              className={`py-2 px-3 rounded-md border focus:outline-none ${
                selectedMethod === method.value
                  ? 'bg-opacity-20 bg-credigo-button border-credigo-button text-credigo-button'
                  : 'border-gray-600 text-gray-400 hover:text-credigo-light hover:border-gray-400'
              }`}
              onClick={() => setSelectedMethod(method.value)}
            >
              {method.label}
            </button>
          ))}
        </div>
      </div>

      {/* Error Message */}
      {error && (
        <div className="mb-4 p-2 text-sm text-red-500 bg-red-100 bg-opacity-10 rounded">
          {error}
        </div>
      )}

      {/* Submit Button */}
      <button
        type="submit"
        disabled={processing || !amount}
        className="w-full py-2 px-4 bg-credigo-button text-white rounded-md hover:bg-opacity-90 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-credigo-button disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
      >
        {processing ? 'Processing...' : 'Continue to Payment'}
      </button>
    </form>
  );
}

export default TopUpForm;
