import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { useAuth } from '../context/AuthContext';
import { checkPaymentStatus, manuallyCompletePayment } from '../services/api';

function PaymentSuccessPage() {
  const { search } = useLocation();
  const navigate = useNavigate();
  const { fetchWalletBalance } = useAuth();
  const [loading, setLoading] = useState(true);
  const [completing, setCompleting] = useState(false);
  const [paymentDetails, setPaymentDetails] = useState(null);
  const [error, setError] = useState(null);

  // Extract params from URL
  useEffect(() => {
    const processPayment = async () => {
      try {
        setLoading(true);
        const params = new URLSearchParams(search);
        const redirect = params.get('redirect') || '/wallet';
        const username = params.get('username');

        // Extract payment ID from URL
        // Format: ?reference=xxx&id=link_ABC123
        const paymentId = params.get('id');

        if (!paymentId) {
          setError('No payment ID found in URL');
          setLoading(false);
          return;
        }

        // Check payment status from PayMongo
        const response = await checkPaymentStatus(paymentId);
        setPaymentDetails(response.data);

        // Set loading to false after getting payment details
        setLoading(false);

        // If payment is already marked as paid, no need for manual completion
        if (response.data?.paid) {
          toast.success('Payment completed successfully!');
          // Auto-redirect after a short delay
          setTimeout(() => {
            fetchWalletBalance(); // Refresh wallet balance
            navigate(redirect);
          }, 1500);
        }
      } catch (err) {
        console.error('Error checking payment:', err);
        setError(err.response?.data?.message || err.message || 'Failed to check payment status');
        setLoading(false);
      }
    };

    processPayment();
  }, [search, navigate, fetchWalletBalance]);

  const handleManualCompletion = async () => {
    if (!paymentDetails?.id) {
      toast.error('No payment ID available');
      return;
    }

    try {
      setCompleting(true);
      const response = await manuallyCompletePayment(paymentDetails.id);

      if (response.data?.success) {
        toast.success('Payment manually completed!');
        fetchWalletBalance(); // Refresh wallet balance

        // Extract redirect URL
        const params = new URLSearchParams(search);
        const redirect = params.get('redirect') || '/wallet';

        // Redirect after short delay
        setTimeout(() => {
          navigate(redirect);
        }, 1000);
      } else {
        setError('Manual completion failed');
      }
    } catch (err) {
      console.error('Error completing payment:', err);
      setError(err.response?.data?.message || err.message || 'Manual completion failed');
    } finally {
      setCompleting(false);
    }
  };

  return (
    <div className="font-sans max-w-lg mx-auto p-6 mt-10">
      <div className="bg-white dark:bg-gray-800 shadow-lg rounded-lg p-6">
        <h1 className="text-2xl font-bold mb-4 text-gray-800 dark:text-gray-100">
          Payment {paymentDetails?.paid ? 'Successful' : 'Processing'}
        </h1>

        {loading ? (
          <div className="text-center py-4">
            <p className="text-gray-600 dark:text-gray-300">Checking payment status...</p>
          </div>
        ) : error ? (
          <div className="bg-red-100 dark:bg-red-900/30 border border-red-400 dark:border-red-500 text-red-700 dark:text-red-300 px-4 py-3 rounded mb-4">
            <p>{error}</p>
          </div>
        ) : (
          <>
            <div className="bg-gray-100 dark:bg-gray-700 p-4 rounded-md mb-4">
              <p className="text-sm text-gray-600 dark:text-gray-300 mb-2">
                <span className="font-medium">Payment ID:</span> {paymentDetails?.id}
              </p>
              <p className="text-sm text-gray-600 dark:text-gray-300 mb-2">
                <span className="font-medium">Status:</span>{' '}
                <span className={paymentDetails?.paid ? 'text-green-600 dark:text-green-400' : 'text-yellow-600 dark:text-yellow-400'}>
                  {paymentDetails?.status}
                </span>
              </p>
              <p className="text-sm text-gray-600 dark:text-gray-300">
                <span className="font-medium">Reference:</span> {paymentDetails?.referenceNumber}
              </p>
            </div>

            {!paymentDetails?.paid && (
              <div className="mb-4 p-4 bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200 dark:border-yellow-700 rounded-md">
                <p className="text-sm text-yellow-800 dark:text-yellow-200">
                  Your payment is still being processed or the webhook hasn't reached our server yet.
                </p>
              </div>
            )}

            {/* Manual completion for development/testing only */}
            {!paymentDetails?.paid && (
              <div className="mt-6 border-t border-gray-200 dark:border-gray-700 pt-4">
                <h3 className="text-lg font-medium text-gray-700 dark:text-gray-300 mb-2">Development Tools</h3>
                <p className="text-sm text-gray-600 dark:text-gray-400 mb-3">
                  If the webhook isn't reaching the server, you can manually complete the payment.
                </p>
                <button
                  onClick={handleManualCompletion}
                  disabled={completing}
                  className="w-full bg-indigo-600 hover:bg-indigo-700 text-white py-2 px-4 rounded-md disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {completing ? 'Processing...' : 'Manually Complete Payment'}
                </button>
              </div>
            )}

            {paymentDetails?.paid && (
              <div className="text-center mt-4">
                <p className="text-green-600 dark:text-green-400 font-medium">
                  Redirecting to your wallet...
                </p>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}

export default PaymentSuccessPage;
