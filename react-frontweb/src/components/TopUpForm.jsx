// src/components/TopUpForm.jsx
import { useToast } from '@/hooks/use-toast';
import React, { useRef, useState } from 'react';
import { RiArrowGoBackLine } from 'react-icons/ri';
import { checkPaymentStatus, createWalletTopUpIntent } from '../services/api';

// Get base URL for the current environment
const BASE_URL = import.meta.env.DEV
  ? 'http://localhost:5173'
  : 'https://credi-go-it-342.vercel.app';

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

      // Format mobile number for API (ensure no spaces or special characters)
      setMobileNumber(mobileNumber.replace(/\D/g, '').trim());
    }

    setError(null);
    return true;
  };

  // Function to handle GCash/PayMaya payments which involve redirects
  const handleEWalletPayment = (data) => {
    if (!data || !data.checkoutUrl) {
      setError('Failed to get payment URL');
      return;
    }

    // Store source ID and payment details for success page to use
    localStorage.setItem('current_payment_source', data.paymentIntentId);
    localStorage.setItem('current_payment_amount', amount);
    localStorage.setItem('current_payment_method', selectedMethod);
    localStorage.setItem('current_payment_timestamp', Date.now().toString());

    // Store expected return URL to verify in success page
    localStorage.setItem('expected_return_url', `${BASE_URL}/payment/success`);

    // Show notification about redirection
    toast({
      title: `Redirecting to ${selectedMethod === 'gcash' ? 'GCash' : 'PayMaya'}`,
      description: `You'll be redirected to complete the payment. After payment, you'll return to ${BASE_URL}`,
    });

    console.log(`Payment will redirect back to: ${BASE_URL}/payment/success`);

    // Set a small delay to ensure toast is visible before redirect
    setTimeout(() => {
      // Open the checkout URL in the same tab for a smoother experience
      window.location.href = data.checkoutUrl;
    }, 1500);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) {
      return;
    }

    setCanCloseForm(false);
    setProcessing(true);
    setError(null);

    try {
      // Prepare payment request data
      const paymentData = {
        amount: parseFloat(amount),
        paymentType: selectedMethod,
      };

      // Add specific fields for GCash/PayMaya
      if (selectedMethod === 'gcash' || selectedMethod === 'paymaya') {
        paymentData.mobileNumber = mobileNumber;

        // Add complete billing information for GCash/PayMaya
        paymentData.billing = {
          name: "CrediGo Test User",
          email: "test@example.com",
          phone: mobileNumber || "09123456789",
          address: {
            line1: "Test Address Line 1",
            line2: "Test Address Line 2",
            city: "Quezon City",
            state: "Metro Manila",
            postal_code: "1101",
            country: "PH"
          }
        };
      }

      // Add card details for card payments
      if (selectedMethod === 'card') {
        paymentData.card = {
          number: cardDetails.cardNumber,
          exp_month: cardDetails.expMonth,
          exp_year: cardDetails.expYear,
          cvc: cardDetails.cvc,
          name: cardDetails.name
        };
      }

      console.log('Sending payment data:', paymentData);

      // Call the API to initiate payment
      const response = await createWalletTopUpIntent(paymentData);
      console.log('Payment intent response:', response.data);

      setPaymentData(response.data);

      // Store payment intent info in localStorage for admin panel detection
      if (response.data.paymentIntentId) {
        localStorage.setItem(`payment_intent_${response.data.paymentIntentId}`, JSON.stringify({
          amount: response.data.amount,
          created: Date.now(),
          status: response.data.status || 'awaiting_payment_method',
          username: localStorage.getItem('username') || 'Anonymous User',
          paymentType: selectedMethod,
          trackingInBackground: false
        }));

        // Broadcast the event for real-time detection
        window.dispatchEvent(new StorageEvent('storage', {
          key: `payment_intent_${response.data.paymentIntentId}`,
          newValue: 'created'
        }));
      }

      if (selectedMethod === 'card') {
        // For card payments, begin polling for status
        startPollingPaymentStatus(response.data.paymentIntentId);
        // After creating intent, user can close the form
        setCanCloseForm(true);
      } else if (response.data.checkoutUrl) {
        // For GCash/PayMaya, handle the redirect flow
        handleEWalletPayment(response.data);
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

  // Update renderPaymentMethodFields to better handle GCash and PayMaya options
  const renderPaymentMethodFields = () => {
    switch (selectedMethod) {
      case 'card':
        return (
          <div className="space-y-4">
            <div>
              <label htmlFor="cardNumber" className="block text-sm text-credigo-light mb-1">Card Number</label>
              <input
                type="text"
                id="cardNumber"
                placeholder="1234 5678 9012 3456"
                value={cardDetails.cardNumber}
                onChange={(e) => setCardDetails({...cardDetails, cardNumber: e.target.value.replace(/\D/g, '')})}
                maxLength={16}
                className="w-full bg-credigo-dark border-gray-600 rounded-md p-2 text-credigo-light placeholder-gray-400"
                required
              />
            </div>

            <div className="grid grid-cols-3 gap-4">
              <div className="col-span-1">
                <label htmlFor="expMonth" className="block text-sm text-credigo-light mb-1">Month</label>
                <input
                  type="text"
                  id="expMonth"
                  placeholder="MM"
                  value={cardDetails.expMonth}
                  onChange={(e) => setCardDetails({...cardDetails, expMonth: e.target.value.replace(/\D/g, '')})}
                  maxLength={2}
                  className="w-full bg-credigo-dark border-gray-600 rounded-md p-2 text-credigo-light placeholder-gray-400"
                  required
                />
              </div>
              <div className="col-span-1">
                <label htmlFor="expYear" className="block text-sm text-credigo-light mb-1">Year</label>
                <input
                  type="text"
                  id="expYear"
                  placeholder="YY"
                  value={cardDetails.expYear}
                  onChange={(e) => setCardDetails({...cardDetails, expYear: e.target.value.replace(/\D/g, '')})}
                  maxLength={2}
                  className="w-full bg-credigo-dark border-gray-600 rounded-md p-2 text-credigo-light placeholder-gray-400"
                  required
                />
              </div>
              <div className="col-span-1">
                <label htmlFor="cvc" className="block text-sm text-credigo-light mb-1">CVC</label>
                <input
                  type="text"
                  id="cvc"
                  placeholder="123"
                  value={cardDetails.cvc}
                  onChange={(e) => setCardDetails({...cardDetails, cvc: e.target.value.replace(/\D/g, '')})}
                  maxLength={4}
                  className="w-full bg-credigo-dark border-gray-600 rounded-md p-2 text-credigo-light placeholder-gray-400"
                  required
                />
              </div>
            </div>

            <div>
              <label htmlFor="nameOnCard" className="block text-sm text-credigo-light mb-1">Name on Card</label>
              <input
                type="text"
                id="nameOnCard"
                placeholder="John Doe"
                value={cardDetails.name}
                onChange={(e) => setCardDetails({...cardDetails, name: e.target.value})}
                className="w-full bg-credigo-dark border-gray-600 rounded-md p-2 text-credigo-light placeholder-gray-400"
                required
              />
            </div>
          </div>
        );
      case 'gcash':
        return (
          <div className="space-y-4">
            <div className="bg-blue-100 text-blue-800 p-4 rounded-md mb-4">
              <p className="font-semibold">GCash Instructions:</p>
              <ol className="list-decimal pl-4 mt-2 space-y-1">
                <li>Enter your GCash-registered mobile number</li>
                <li>Click "Top-up Now" to generate a payment link</li>
                <li>You'll be redirected to GCash to complete the payment</li>
                <li>After successful payment, your CrediGo wallet will be credited</li>
              </ol>
            </div>

            <div>
              <label htmlFor="gcashNumber" className="block text-sm text-credigo-light mb-1">GCash Mobile Number</label>
              <input
                type="text"
                id="gcashNumber"
                placeholder="09XXXXXXXXX"
                value={mobileNumber}
                onChange={(e) => setMobileNumber(e.target.value.replace(/\D/g, ''))}
                maxLength={11}
                className="w-full bg-credigo-dark border-gray-600 rounded-md p-2 text-credigo-light placeholder-gray-400"
                required
              />
              <p className="text-xs text-gray-400 mt-1">Enter the mobile number associated with your GCash account</p>
            </div>
          </div>
        );
      case 'paymaya':
        return (
          <div className="space-y-4">
            <div className="bg-purple-100 text-purple-800 p-4 rounded-md mb-4">
              <p className="font-semibold">PayMaya Instructions:</p>
              <ol className="list-decimal pl-4 mt-2 space-y-1">
                <li>Enter your PayMaya-registered mobile number</li>
                <li>Click "Top-up Now" to generate a payment link</li>
                <li>You'll be redirected to PayMaya to complete the payment</li>
                <li>After successful payment, your CrediGo wallet will be credited</li>
              </ol>
            </div>

            <div>
              <label htmlFor="paymayaNumber" className="block text-sm text-credigo-light mb-1">PayMaya Mobile Number</label>
              <input
                type="text"
                id="paymayaNumber"
                placeholder="09XXXXXXXXX"
                value={mobileNumber}
                onChange={(e) => setMobileNumber(e.target.value.replace(/\D/g, ''))}
                maxLength={11}
                className="w-full bg-credigo-dark border-gray-600 rounded-md p-2 text-credigo-light placeholder-gray-400"
                required
              />
              <p className="text-xs text-gray-400 mt-1">Enter the mobile number associated with your PayMaya account</p>
            </div>
          </div>
        );
      default:
        return null;
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <div className="space-y-6">
        <div>
          <label className="block text-sm text-credigo-light mb-2">Select Payment Method</label>
          <div className="grid grid-cols-3 gap-2">
            {paymentMethods.map((method) => (
              <button
                key={method.value}
                type="button"
                className={`flex flex-col items-center justify-center p-3 rounded-md border transition-all ${
                  selectedMethod === method.value
                    ? 'border-credigo-button bg-credigo-button bg-opacity-10'
                    : 'border-gray-600 hover:border-gray-500'
                }`}
                onClick={() => setSelectedMethod(method.value)}
              >
                {method.value === 'card' && (
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 mb-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
                  </svg>
                )}
                {method.value === 'gcash' && (
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 mb-1 text-blue-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                )}
                {method.value === 'paymaya' && (
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 mb-1 text-purple-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 7h6m0 10v-3m-3 3h.01M9 17h.01M9 14h.01M12 14h.01M15 11h.01M12 11h.01M9 11h.01M7 21h10a2 2 0 002-2V5a2 2 0 00-2-2H7a2 2 0 00-2 2v14a2 2 0 002 2z" />
                  </svg>
                )}
                <span className="text-xs mt-1">{method.label}</span>
              </button>
            ))}
          </div>
        </div>

        <div>
          <label className="block text-sm text-credigo-light mb-2">Amount (PHP)</label>
          <div className="relative">
            <span className="absolute left-3 top-2 text-gray-400">₱</span>
            <input
              type="number"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              min="50"
              step="1"
              placeholder="Enter amount (min ₱50)"
              className="w-full pl-8 bg-credigo-dark border-gray-600 rounded-md p-2 text-credigo-light placeholder-gray-400"
              required
            />
          </div>
          <p className="text-xs text-gray-400 mt-1">Minimum top-up amount is ₱50.00</p>
        </div>

        {renderPaymentMethodFields()}

        <div className="mt-4">
          <button
            type="submit"
            disabled={processing}
            className={`w-full py-2 rounded text-center ${
              processing ? 'bg-gray-500' : 'bg-credigo-button hover:bg-opacity-90'
            } text-credigo-dark font-medium`}
          >
            {processing ? 'Processing...' : 'Top-up Now'}
          </button>
        </div>

        {error && (
          <div className="bg-red-500/20 border border-red-500/40 rounded-lg p-3 text-red-300 text-sm mt-4">
            {error}
          </div>
        )}

        {canCloseForm && !succeeded && (
          <div className="mt-4 bg-blue-500/20 border border-blue-500/40 rounded-lg p-3 text-blue-300 text-sm">
            <p className="font-medium mb-2">Payment in process</p>
            <p>You can complete the payment in a new browser window and continue browsing the site. Your wallet will be updated automatically.</p>
            <button
              type="button"
              onClick={moveToBackground}
              className="mt-2 text-blue-300 hover:text-blue-100 flex items-center text-sm font-medium"
            >
              <span>Move payment to background</span>
              <RiArrowGoBackLine className="ml-1" />
            </button>
          </div>
        )}
      </div>
    </form>
  );
}

export default TopUpForm;
