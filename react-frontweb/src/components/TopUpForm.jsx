// src/components/TopUpForm.jsx
import { useToast } from '@/hooks/use-toast';
import React, { useRef, useState } from 'react';
import { FaRegWindowMinimize } from 'react-icons/fa';
import { RiArrowGoBackLine } from 'react-icons/ri';
import { checkPaymentStatus, createWalletTopUpIntent } from '../services/api';

/**
 * Form component for PayMongo wallet top-up payment.
 */
function TopUpForm({ onPaymentSuccess, onPaymentCancel, onPaymentError }) {
  const { toast } = useToast();
  const [error, setError] = useState(null);
  const [processing, setProcessing] = useState(false);
  const [succeeded, setSucceeded] = useState(false);
  const [selectedMethod, setSelectedMethod] = useState('card'); // Default payment method
  const [amount, setAmount] = useState('');
  const [paymentData, setPaymentData] = useState(null);
  const [statusCheckInterval, setStatusCheckInterval] = useState(null);
  const pollTimeoutRef = useRef(null);

  // Payment method specific fields
  const [cardDetails, setCardDetails] = useState({
    cardNumber: '',
    expMonth: '',
    expYear: '',
    cvc: '',
    name: ''
  });

  const [mobileNumber, setMobileNumber] = useState('');

  // Supported PayMongo payment methods
  const paymentMethods = [
    { value: 'card', label: 'Card' },
    { value: 'gcash', label: 'GCash' },
    { value: 'paymaya', label: 'PayMaya' },
  ];

  // Add a new state to track if payment can be managed in background
  const [canCloseForm, setCanCloseForm] = useState(false);

  // Clear the polling interval and timeout when component unmounts
  React.useEffect(() => {
    return () => {
      if (statusCheckInterval) {
        clearInterval(statusCheckInterval);
      }
      if (pollTimeoutRef.current) {
        clearTimeout(pollTimeoutRef.current);
      }
    };
  }, [statusCheckInterval]);

  // Listen for stop polling signals from admin panel
  React.useEffect(() => {
    if (!paymentData?.paymentIntentId) return;

    const handleStorageChange = (e) => {
      // Check if this is a stop polling signal for our payment intent
      if (e.key === `stopPolling_${paymentData.paymentIntentId}`) {
        console.log('Received signal to stop polling from admin panel');
        if (statusCheckInterval) {
          clearInterval(statusCheckInterval);
          setStatusCheckInterval(null);
        }
        if (pollTimeoutRef.current) {
          clearTimeout(pollTimeoutRef.current);
          pollTimeoutRef.current = null;
        }
        // Set success state
        setSucceeded(true);
        setError(null);
        // Show success toast
        toast({
          title: "Payment Successful",
          description: "Your wallet has been topped up successfully!",
          variant: "default",
        });
        // Force refresh wallet balance
        if (onPaymentSuccess) onPaymentSuccess();
      }
    };

    // Also check if there's already a signal in localStorage
    const existingSignal = localStorage.getItem(`stopPolling_${paymentData.paymentIntentId}`);
    if (existingSignal) {
      console.log('Found existing stop polling signal');
      if (statusCheckInterval) {
        clearInterval(statusCheckInterval);
        setStatusCheckInterval(null);
      }
      if (pollTimeoutRef.current) {
        clearTimeout(pollTimeoutRef.current);
        pollTimeoutRef.current = null;
      }
      // Set success state
      setSucceeded(true);
      setError(null);
      // Show success toast
      toast({
        title: "Payment Successful",
        description: "Your wallet has been topped up successfully!",
        variant: "default",
      });
      // Force refresh wallet balance
      if (onPaymentSuccess) onPaymentSuccess();
    }

    window.addEventListener('storage', handleStorageChange);
    return () => {
      window.removeEventListener('storage', handleStorageChange);
    };
  }, [paymentData?.paymentIntentId, statusCheckInterval, onPaymentSuccess, toast]);

  // Function to validate form based on selected payment method
  const validateForm = () => {
    if (!amount || parseFloat(amount) < 50) {
      setError('Minimum amount is PHP 50.00');
      return false;
    }

    if (selectedMethod === 'card') {
      // Validate card details
      if (!cardDetails.cardNumber || cardDetails.cardNumber.length < 15) {
        setError('Please enter a valid card number');
        return false;
      }
      if (!cardDetails.expMonth || !cardDetails.expYear) {
        setError('Please enter a valid expiration date');
        return false;
      }
      if (!cardDetails.cvc || cardDetails.cvc.length < 3) {
        setError('Please enter a valid CVC');
        return false;
      }
      if (!cardDetails.name) {
        setError('Please enter the cardholder name');
        return false;
      }
    } else if (selectedMethod === 'gcash' || selectedMethod === 'paymaya') {
      // Validate mobile number for GCash and PayMaya
      if (!mobileNumber || mobileNumber.length !== 11 || !mobileNumber.startsWith('09')) {
        setError(`Please enter a valid Philippine mobile number (e.g., 09XXXXXXXXX) for ${selectedMethod === 'gcash' ? 'GCash' : 'PayMaya'}`);
        return false;
      }
    }

    setError(null);
    return true;
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setProcessing(true);
    setError(null);
    setSucceeded(false);

    if (!validateForm()) {
      setProcessing(false);
      return;
    }

    try {
      // Prepare payment data based on selected method
      const paymentData = {
        amount: parseFloat(amount),
        paymentType: selectedMethod
      };

      // Add method-specific data
      if (selectedMethod === 'card') {
        // For actual integration, you'd encrypt card data or use a direct PayMongo form
        // Here we're just simulating for development
        paymentData.card = {
          number: cardDetails.cardNumber,
          exp_month: cardDetails.expMonth,
          exp_year: cardDetails.expYear,
          cvc: cardDetails.cvc,
          name: cardDetails.name
        };
      } else if (selectedMethod === 'gcash' || selectedMethod === 'paymaya') {
        paymentData.mobileNumber = mobileNumber;
      }

      console.log("Sending payment data:", paymentData);

      // Call backend to create a PayMongo payment intent
      const response = await createWalletTopUpIntent(paymentData);
      console.log("Payment API response:", response);

      const data = response.data;
      setPaymentData(data);

      // Store payment intent info in localStorage for admin panel detection
      if (data.paymentIntentId) {
        localStorage.setItem(`payment_intent_${data.paymentIntentId}`, JSON.stringify({
          amount: data.amount,
          created: Date.now(),
          status: data.status || 'awaiting_payment_method',
          username: localStorage.getItem('username') || 'Anonymous User',
          paymentType: selectedMethod,
          trackingInBackground: false
        }));

        // Broadcast the event for real-time detection
        window.dispatchEvent(new StorageEvent('storage', {
          key: `payment_intent_${data.paymentIntentId}`,
          newValue: 'created'
        }));
      }

      if (selectedMethod === 'card') {
        // For card payments, begin polling for status
        startPollingPaymentStatus(data.paymentIntentId);
        // After creating intent, user can close the form
        setCanCloseForm(true);
      } else if (data.checkoutUrl) {
        // For GCash/PayMaya, redirect to checkout URL
        window.open(data.checkoutUrl, '_blank');
        startPollingPaymentStatus(data.paymentIntentId);
        // After opening checkout URL, user can close the form
        setCanCloseForm(true);
      } else {
        setError('Payment initiation failed. Please try again.');
      }
    } catch (err) {
      console.error('Payment creation error:', err);

      // Improved error handling
      let errorMessage = 'Failed to initiate payment. Please try again.';

      // Check if there's a response with data
      if (err.response && err.response.data) {
        // If the response data is a string, use it directly
        if (typeof err.response.data === 'string') {
          errorMessage = err.response.data;
        }
        // If it has a message property, use that
        else if (err.response.data.message) {
          errorMessage = err.response.data.message;
        }
      } else if (err.message) {
        // Use the error message if available
        errorMessage = err.message;
      }

      setError(errorMessage);
      if (onPaymentError) onPaymentError(errorMessage);
    } finally {
      setProcessing(false);
    }
  };

  // Function to cancel the payment process
  const handleCancelPayment = () => {
    // Show cancellation toast using shadcn
    toast({
      title: "Payment Cancelled",
      description: "The payment process has been cancelled",
      variant: "destructive",
    });

    // Clear polling interval
    if (statusCheckInterval) {
      clearInterval(statusCheckInterval);
      setStatusCheckInterval(null);
    }

    // Clear timeout
    if (pollTimeoutRef.current) {
      clearTimeout(pollTimeoutRef.current);
      pollTimeoutRef.current = null;
    }

    // Reset state
    setPaymentData(null);

    // Inform parent component
    if (onPaymentCancel) onPaymentCancel();
  };

  const startPollingPaymentStatus = (paymentIntentId) => {
    // Show toast when payment status changes
    toast({
      title: "Processing Payment",
      description: "Please wait while we process your payment...",
    });

    // Clear any existing interval
    if (statusCheckInterval) {
      clearInterval(statusCheckInterval);
    }

    // Clear any existing timeout
    if (pollTimeoutRef.current) {
      clearTimeout(pollTimeoutRef.current);
    }

    // Set a 10-minute timeout to stop polling if payment is not completed
    pollTimeoutRef.current = setTimeout(() => {
      if (statusCheckInterval) {
        clearInterval(statusCheckInterval);
        setStatusCheckInterval(null);
        setError('Payment verification timed out. Please check your payment status in your account.');
        if (onPaymentError) onPaymentError('Payment verification timed out');
      }
    }, 600000); // 10 minutes

    // Poll every 3 seconds
    const interval = setInterval(async () => {
      try {
        const statusResponse = await checkPaymentStatus(paymentIntentId);
        const statusData = statusResponse.data;

        // Update localStorage entry with latest status
        try {
          const storedData = JSON.parse(localStorage.getItem(`payment_intent_${paymentIntentId}`) || '{}');
          localStorage.setItem(`payment_intent_${paymentIntentId}`, JSON.stringify({
            ...storedData,
            status: statusData.status,
            lastChecked: Date.now(),
            walletCredited: statusData.walletCredited
          }));
        } catch (e) {
          console.error('Error updating payment intent in localStorage:', e);
        }

        // Check if payment is successful or wallet has been credited
        if ((statusData.status === 'succeeded' || statusData.walletCredited) && statusData.walletCredited) {
          // Payment successful and wallet credited
          clearInterval(interval);
          clearTimeout(pollTimeoutRef.current); // Clear the timeout since payment succeeded
          setStatusCheckInterval(null);
          setSucceeded(true);

          // Update localStorage to mark as completed
          try {
            const storedData = JSON.parse(localStorage.getItem(`payment_intent_${paymentIntentId}`) || '{}');
            localStorage.setItem(`payment_intent_${paymentIntentId}`, JSON.stringify({
              ...storedData,
              status: 'succeeded',
              walletCredited: true,
              completedAt: Date.now()
            }));
          } catch (e) {
            console.error('Error updating payment intent success in localStorage:', e);
          }

          if (onPaymentSuccess) onPaymentSuccess();
        } else if (statusData.status === 'failed') {
          // Payment failed
          clearInterval(interval);
          clearTimeout(pollTimeoutRef.current); // Clear the timeout since payment failed
          setStatusCheckInterval(null);
          setError(statusData.message || 'Payment failed');

          // Update localStorage to mark as failed
          try {
            const storedData = JSON.parse(localStorage.getItem(`payment_intent_${paymentIntentId}`) || '{}');
            localStorage.setItem(`payment_intent_${paymentIntentId}`, JSON.stringify({
              ...storedData,
              status: 'failed',
              failedAt: Date.now(),
              errorMessage: statusData.message
            }));
          } catch (e) {
            console.error('Error updating payment intent failure in localStorage:', e);
          }

          if (onPaymentError) onPaymentError(statusData.message);
        } else {
          // Log the current status for debugging
          console.log(`Polling payment status: ${statusData.status}, wallet credited: ${statusData.walletCredited}`);
        }
        // If still pending, continue polling
      } catch (err) {
        console.error('Error checking payment status:', err);
        // Don't clear interval yet - might be temporary error
      }
    }, 3000);

    setStatusCheckInterval(interval);
  };

  // New function to move payment tracking to background
  const moveToBackground = () => {
    if (!paymentData?.paymentIntentId) {
      if (onPaymentCancel) onPaymentCancel();
      return;
    }

    // Update localStorage to indicate tracking in background
    try {
      const storedData = JSON.parse(localStorage.getItem(`payment_intent_${paymentData.paymentIntentId}`) || '{}');
      localStorage.setItem(`payment_intent_${paymentData.paymentIntentId}`, JSON.stringify({
        ...storedData,
        trackingInBackground: true,
        status: storedData.status || 'awaiting_payment_method',
        lastUpdated: Date.now()
      }));

      // Let the system know this payment is now tracked in background
      localStorage.setItem('background_payment_active', 'true');

      // Show a notification to the user
      toast({
        title: "Payment Tracking",
        description: "This payment is now being tracked in the background. You can check your balance later or ask an admin to confirm.",
        variant: "default",
      });

      // Close the payment form
      if (onPaymentCancel) onPaymentCancel();
    } catch (e) {
      console.error('Error moving payment tracking to background:', e);
      if (onPaymentCancel) onPaymentCancel();
    }
  };

  // Render payment method specific form fields
  const renderPaymentMethodFields = () => {
    switch (selectedMethod) {
      case 'card':
        return (
          <div className="space-y-3 mt-4 p-4 bg-credigo-dark/60 rounded-lg border border-gray-700">
            <h3 className="font-medium text-sm">Card Details</h3>

            {/* Card Number */}
            <div>
              <label className="block text-xs text-gray-400 mb-1">Card Number</label>
              <input
                type="text"
                placeholder="4343434343434345"
                value={cardDetails.cardNumber}
                onChange={(e) => setCardDetails({...cardDetails, cardNumber: e.target.value.replace(/\D/g, '').slice(0, 16)})}
                className="w-full rounded-lg border border-gray-600 bg-credigo-dark py-2 px-4 text-credigo-light text-sm"
              />
              <p className="text-xs text-gray-500 mt-1">For testing, use: 4343434343434345</p>
            </div>

            {/* Expiration Date */}
            <div className="flex space-x-4">
              <div className="w-1/2">
                <label className="block text-xs text-gray-400 mb-1">Expiration Month</label>
                <select
                  value={cardDetails.expMonth}
                  onChange={(e) => setCardDetails({...cardDetails, expMonth: e.target.value})}
                  className="w-full rounded-lg border border-gray-600 bg-credigo-dark py-2 px-4 text-credigo-light text-sm"
                >
                  <option value="">Month</option>
                  {Array.from({length: 12}, (_, i) => {
                    const month = i + 1;
                    return (
                      <option key={month} value={month}>{month.toString().padStart(2, '0')}</option>
                    );
                  })}
                </select>
              </div>
              <div className="w-1/2">
                <label className="block text-xs text-gray-400 mb-1">Expiration Year</label>
                <select
                  value={cardDetails.expYear}
                  onChange={(e) => setCardDetails({...cardDetails, expYear: e.target.value})}
                  className="w-full rounded-lg border border-gray-600 bg-credigo-dark py-2 px-4 text-credigo-light text-sm"
                >
                  <option value="">Year</option>
                  {Array.from({length: 10}, (_, i) => {
                    const year = new Date().getFullYear() + i;
                    return (
                      <option key={year} value={year.toString().slice(-2)}>{year}</option>
                    );
                  })}
                </select>
              </div>
            </div>

            {/* CVC */}
            <div>
              <label className="block text-xs text-gray-400 mb-1">CVC</label>
              <input
                type="text"
                placeholder="123"
                value={cardDetails.cvc}
                onChange={(e) => setCardDetails({...cardDetails, cvc: e.target.value.replace(/\D/g, '').slice(0, 3)})}
                className="w-full rounded-lg border border-gray-600 bg-credigo-dark py-2 px-4 text-credigo-light text-sm"
              />
            </div>

            {/* Cardholder Name */}
            <div>
              <label className="block text-xs text-gray-400 mb-1">Cardholder Name</label>
              <input
                type="text"
                placeholder="Name on card"
                value={cardDetails.name}
                onChange={(e) => setCardDetails({...cardDetails, name: e.target.value})}
                className="w-full rounded-lg border border-gray-600 bg-credigo-dark py-2 px-4 text-credigo-light text-sm"
              />
            </div>
          </div>
        );

      case 'gcash':
      case 'paymaya':
        return (
          <div className="space-y-3 mt-4 p-4 bg-credigo-dark/60 rounded-lg border border-gray-700">
            <h3 className="font-medium text-sm">{selectedMethod === 'gcash' ? 'GCash' : 'PayMaya'} Details</h3>

            <div>
              <label className="block text-xs text-gray-400 mb-1">Mobile Number</label>
              <input
                type="text"
                placeholder="09XXXXXXXXX"
                value={mobileNumber}
                onChange={(e) => setMobileNumber(e.target.value.replace(/\D/g, '').slice(0, 11))}
                className="w-full rounded-lg border border-gray-600 bg-credigo-dark py-2 px-4 text-credigo-light text-sm"
              />
              <p className="text-xs text-gray-500 mt-1">
                Enter the {selectedMethod === 'gcash' ? 'GCash' : 'PayMaya'} mobile number
              </p>
            </div>

            <div className="text-xs text-gray-500 border-t border-gray-700 pt-2 mt-2">
              <p>After clicking Continue, you'll be directed to complete your payment in a new tab.</p>
              <p>Do not close this window until the payment is complete.</p>
            </div>
          </div>
        );

      default:
        return null;
    }
  };

  return (
    <div className="space-y-4">
      {!paymentData ? (
        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Amount Input */}
          <div>
            <label className="block text-sm font-medium text-credigo-light/80 mb-1">
              Amount (PHP)
            </label>
            <input
              type="number"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              min="50"
              step="0.01"
              placeholder="Minimum PHP 50.00"
              required
              className="w-full rounded-lg border border-gray-600 bg-credigo-dark py-2 px-4 text-credigo-light"
            />
          </div>

          {/* Payment Method Selector */}
          <div>
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
          </div>

          {/* Render Payment Method Specific Fields */}
          {renderPaymentMethodFields()}

          {/* Display Messages */}
          {error && <div className="text-red-400 text-sm font-medium p-3 bg-red-900/20 border border-red-900 rounded-lg">{error}</div>}

          {/* Submit Button */}
          <button
            disabled={processing || succeeded || !amount}
            className={`w-full flex justify-center px-4 py-2 text-sm font-semibold text-credigo-dark bg-credigo-button border border-transparent rounded-lg shadow-sm hover:bg-opacity-90 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-credigo-input-bg focus:ring-credigo-button transition duration-150 ease-in-out ${(processing || succeeded || !amount) ? 'opacity-50 cursor-not-allowed' : ''}`}
          >
            {processing ? 'Processing...' : 'Continue'}
          </button>

          {/* Cancel Button */}
          <button
            type="button"
            onClick={handleCancelPayment}
            disabled={processing}
            className="w-full flex justify-center px-4 py-2 text-sm font-medium text-gray-300 bg-gray-600 border border-transparent rounded-lg shadow-sm hover:bg-gray-500 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-credigo-input-bg focus:ring-gray-500 transition duration-150 ease-in-out mt-2 disabled:opacity-50"
          >
            Cancel
          </button>
        </form>
      ) : (
        <div className="space-y-4">
          <div className="bg-credigo-dark/50 rounded-lg p-4 border border-credigo-button/30">
            <h3 className="text-lg font-semibold mb-2">
              {succeeded ? 'Payment Completed' : 'Payment In Progress'}
            </h3>
            <p className="text-sm mb-4">
              {succeeded
                ? 'Your payment has been confirmed and your wallet has been credited.'
                : selectedMethod === 'card'
                  ? 'Please complete payment in the secure form.'
                  : 'Please complete payment in the new tab. Do not close this page until complete.'}
            </p>
            <div className="border-t border-gray-700 pt-2">
              <div className="flex justify-between text-sm">
                <span>Payment ID:</span>
                <span className="font-mono text-xs text-credigo-button">{paymentData.paymentIntentId}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span>Amount:</span>
                <span>{new Intl.NumberFormat('en-PH', { style: 'currency', currency: 'PHP' }).format(paymentData.amount/100)}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span>Status:</span>
                <span className={`capitalize ${succeeded ? 'text-green-400' : ''}`}>
                  {succeeded ? 'Completed' : paymentData.status}
                </span>
              </div>
            </div>
          </div>

          {succeeded && (
            <div className="text-green-400 text-sm font-medium p-3 bg-green-900/20 border border-green-900 rounded-lg">
              Payment completed successfully! Your wallet has been credited.
            </div>
          )}

          {error && (
            <div className="text-red-400 text-sm font-medium p-3 bg-red-900/20 border border-red-900 rounded-lg">
              {error}
            </div>
          )}

          {!succeeded && !error && (
            <div className="animate-pulse flex items-center justify-center space-x-2 text-sm text-gray-400">
              <div className="w-2 h-2 bg-credigo-button rounded-full"></div>
              <div className="w-2 h-2 bg-credigo-button rounded-full"></div>
              <div className="w-2 h-2 bg-credigo-button rounded-full"></div>
              <span>Checking payment status...</span>
            </div>
          )}

          <div className="flex flex-col sm:flex-row gap-2">
            {/* Show continue in background button if payment is in progress */}
            {!succeeded && !error && canCloseForm && (
              <button
                type="button"
                onClick={moveToBackground}
                className="flex-1 justify-center py-2 px-4 border border-credigo-button bg-transparent rounded-md shadow-sm text-sm font-medium text-credigo-button hover:bg-credigo-button/10 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-credigo-input-bg focus:ring-credigo-button flex items-center"
              >
                <FaRegWindowMinimize className="mr-2" />
                Continue in Background
              </button>
            )}

            <button
              type="button"
              onClick={handleCancelPayment}
              className="flex-1 flex justify-center items-center py-2 px-4 border border-gray-600 bg-gray-700 rounded-md shadow-sm text-sm font-medium text-gray-300 hover:bg-gray-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-credigo-input-bg focus:ring-gray-500"
            >
              {succeeded ? (
                <>Close</>
              ) : (
                <>
                  <RiArrowGoBackLine className="mr-2" />
                  {canCloseForm ? 'Back to Form' : 'Cancel Payment Process'}
                </>
              )}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

export default TopUpForm;
