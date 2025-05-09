import React, { useCallback, useEffect, useState } from 'react';
import { RiRefreshLine } from 'react-icons/ri';
import { toast } from 'react-toastify';
import { confirmTestPayment } from '../services/api';

const AdminPayments = () => {
  const [paymentIntentId, setPaymentIntentId] = useState('');
  const [amount, setAmount] = useState('');
  const [processing, setProcessing] = useState(false);
  const [result, setResult] = useState(null);
  const [paymentHistory, setPaymentHistory] = useState([]);
  const [stoppingPolling, setStoppingPolling] = useState(false);
  const [pendingPayments, setPendingPayments] = useState([]);
  const [loadingPending, setLoadingPending] = useState(false);

  // Function to fetch pending payments
  const fetchPendingPayments = useCallback(async () => {
    setLoadingPending(true);
    try {
      // For demo purposes, we'll simulate this endpoint
      // In a real implementation, replace with actual API call
      // const response = await getPendingPayments();

      // Simulate API response for demo - replace with actual API in production
      // This simulates checking localStorage for any payment intents created by users
      const pendingPaymentIds = [];
      for (let i = 0; i < localStorage.length; i++) {
        const key = localStorage.key(i);
        if (key && key.startsWith('payment_intent_')) {
          try {
            const data = JSON.parse(localStorage.getItem(key));
            // Include both awaiting payments and background tracking payments
            if (data && (data.status === 'awaiting_payment_method' ||
                         data.trackingInBackground === true ||
                         data.status === 'processing')) {
              pendingPaymentIds.push({
                id: key.replace('payment_intent_', ''),
                amount: data.amount,
                created: data.created || Date.now(),
                username: data.username || 'Unknown',
                status: data.status,
                trackingInBackground: data.trackingInBackground || false
              });
            }
          } catch (e) {
            console.error('Error parsing pending payment:', e);
          }
        }
      }

      // Replace this with the real API call in production:
      // const pendingPaymentIds = response.data;
      setPendingPayments(pendingPaymentIds);
    } catch (error) {
      console.error('Error fetching pending payments:', error);
      toast.error('Failed to fetch pending payments');
    } finally {
      setLoadingPending(false);
    }
  }, []);

  // Set up polling for pending payments
  useEffect(() => {
    // Fetch on initial load
    fetchPendingPayments();

    // Set up interval to fetch every 5 seconds
    const interval = setInterval(() => {
      fetchPendingPayments();
    }, 5000);

    // Clean up interval on unmount
    return () => clearInterval(interval);
  }, [fetchPendingPayments]);

  // Listen for new payment intents created by users
  useEffect(() => {
    const handleStorageChange = (e) => {
      if (e.key && e.key.startsWith('payment_intent_')) {
        // Refresh pending payments list when a new payment intent is created
        fetchPendingPayments();
      }
    };

    window.addEventListener('storage', handleStorageChange);
    return () => {
      window.removeEventListener('storage', handleStorageChange);
    };
  }, [fetchPendingPayments]);

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!paymentIntentId || !amount) {
      toast.error('Please fill in all fields');
      return;
    }

    setProcessing(true);
    setResult(null);

    try {
      const response = await confirmTestPayment({
        paymentIntentId,
        amount: parseFloat(amount)
      });

      // Add to history
      const newPayment = {
        id: Date.now(),
        paymentIntentId,
        amount: parseFloat(amount),
        timestamp: new Date().toISOString(),
        status: 'success'
      };

      setPaymentHistory(prev => [newPayment, ...prev]);
      setResult({ success: true, data: response.data });
      toast.success('Payment confirmed successfully!');

      // Remove this payment from pending list if it exists
      setPendingPayments(prev => prev.filter(p => p.id !== paymentIntentId));

      // Reset form
      setPaymentIntentId('');
      setAmount('');
    } catch (error) {
      console.error('Payment confirmation error:', error);
      setResult({
        success: false,
        error: error.response?.data?.message || 'Failed to confirm payment'
      });
      toast.error('Failed to confirm payment');
    } finally {
      setProcessing(false);
    }
  };

  const handleQuickConfirm = async (payment) => {
    setPaymentIntentId(payment.id);
    setAmount(payment.amount / 100); // Convert from cents to whole amount

    // Automatically submit after short delay to allow state update
    setTimeout(() => {
      handleSubmit({ preventDefault: () => {} });

      // Also stop polling immediately
      localStorage.setItem('stopPolling_' + payment.id, Date.now().toString());

      // Broadcast the event
      window.dispatchEvent(new StorageEvent('storage', {
        key: 'stopPolling_' + payment.id,
        newValue: Date.now().toString()
      }));
    }, 100);
  };

  const forceClosePolling = async () => {
    if (!paymentIntentId) {
      toast.error('Please enter a Payment Intent ID');
      return;
    }

    setStoppingPolling(true);

    try {
      // First try to confirm the payment if amount is provided
      if (amount) {
        try {
          await confirmTestPayment({
            paymentIntentId,
            amount: parseFloat(amount)
          });

          // Add to history
          const newPayment = {
            id: Date.now(),
            paymentIntentId,
            amount: parseFloat(amount),
            timestamp: new Date().toISOString(),
            status: 'success (manual stop)'
          };

          setPaymentHistory(prev => [newPayment, ...prev]);
          setResult({
            success: true,
            data: {
              message: "Payment confirmed and polling stopped",
              paymentIntentId
            }
          });
        } catch (error) {
          console.warn('Could not confirm payment automatically when stopping polling:', error);
          // Continue with stopping the polling even if confirmation fails
        }
      }

      // Use localStorage to send a signal to all browser tabs
      localStorage.setItem('stopPolling_' + paymentIntentId, Date.now().toString());

      // Create a small delay to allow the event to propagate
      await new Promise(resolve => setTimeout(resolve, 500));

      toast.success('Payment confirmed and polling stopped!');

      // You can also try to broadcast this via localStorage for cross-tab communication
      window.dispatchEvent(new StorageEvent('storage', {
        key: 'stopPolling_' + paymentIntentId,
        newValue: Date.now().toString()
      }));

      // Reset form if successful
      if (amount) {
        setPaymentIntentId('');
        setAmount('');
      }
    } catch (error) {
      console.error('Error stopping polling:', error);
      toast.error('Failed to send stop polling signal');
    } finally {
      setStoppingPolling(false);
    }
  };

  // Function to format date
  const formatDate = (timestamp) => {
    return new Date(timestamp).toLocaleTimeString();
  };

  return (
    <div className="container mx-auto px-4">
      {/* Real-time Pending Payments Section */}
      <div className="bg-white rounded-lg shadow-md p-6 mb-6">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-2xl font-bold text-gray-800">Pending Payments</h2>
          <button
            onClick={fetchPendingPayments}
            disabled={loadingPending}
            className="flex items-center px-3 py-2 bg-gray-100 hover:bg-gray-200 rounded-md text-sm font-medium text-gray-700"
          >
            <RiRefreshLine className={`mr-1 ${loadingPending ? 'animate-spin' : ''}`} />
            Refresh
          </button>
        </div>

        {pendingPayments.length === 0 ? (
          <div className="text-center py-8 text-gray-500">
            No pending payments awaiting confirmation
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Payment ID</th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Amount</th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">User</th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Time</th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Action</th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {pendingPayments.map((payment) => (
                  <tr key={payment.id} className="hover:bg-gray-50">
                    <td className="px-4 py-3 whitespace-nowrap text-sm font-mono text-gray-600">
                      {payment.id}
                    </td>
                    <td className="px-4 py-3 whitespace-nowrap text-sm font-semibold text-gray-800">
                      PHP {(payment.amount / 100).toFixed(2)}
                    </td>
                    <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-700">
                      {payment.username}
                    </td>
                    <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-500">
                      {formatDate(payment.created)}
                      {payment.trackingInBackground && (
                        <span className="ml-2 px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-blue-100 text-blue-800">
                          Background
                        </span>
                      )}
                    </td>
                    <td className="px-4 py-3 whitespace-nowrap text-sm">
                      <button
                        onClick={() => handleQuickConfirm(payment)}
                        className="bg-green-600 hover:bg-green-700 text-white px-3 py-1 rounded-md text-sm font-medium"
                      >
                        Confirm & Stop
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <div className="flex flex-col md:flex-row gap-6">
        {/* Manual Payment Confirmation */}
        <div className="md:w-1/2 bg-white rounded-lg shadow-md p-6">
          <h2 className="text-2xl font-bold text-gray-800 mb-6">Manual Confirmation</h2>
          <p className="mb-4 text-sm text-gray-600">
            Use this form to manually confirm payments for demonstration purposes.
            This will credit the user's wallet without requiring actual payment processing.
          </p>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Payment Intent ID
              </label>
              <input
                type="text"
                value={paymentIntentId}
                onChange={(e) => setPaymentIntentId(e.target.value)}
                placeholder="pi_xxxxxxxxxxxxxxxx"
                className="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-indigo-500 focus:border-indigo-500"
                required
              />
              <p className="mt-1 text-xs text-gray-500">
                Enter the PayMongo Payment Intent ID to confirm
              </p>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Amount (PHP)
              </label>
              <input
                type="number"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                min="1"
                step="0.01"
                placeholder="100.00"
                className="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-indigo-500 focus:border-indigo-500"
                required
              />
              <p className="mt-1 text-xs text-gray-500">
                Enter the amount in PHP (e.g., 100.00)
              </p>
            </div>

            <div className="flex space-x-2">
              <button
                type="submit"
                disabled={processing}
                className={`flex-1 flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 ${
                  processing ? 'opacity-70 cursor-not-allowed' : ''
                }`}
              >
                {processing ? 'Processing...' : 'Confirm Payment'}
              </button>

              <button
                type="button"
                onClick={forceClosePolling}
                disabled={stoppingPolling || !paymentIntentId}
                className={`flex-1 flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-red-600 hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 ${
                  stoppingPolling || !paymentIntentId ? 'opacity-70 cursor-not-allowed' : ''
                }`}
              >
                {stoppingPolling ? 'Stopping...' : 'Stop Polling'}
              </button>
            </div>
          </form>

          {result && (
            <div className={`mt-4 p-4 rounded-md ${result.success ? 'bg-green-50 text-green-800' : 'bg-red-50 text-red-800'}`}>
              <p className="font-medium">{result.success ? 'Success!' : 'Error'}</p>
              <pre className="mt-2 text-sm overflow-auto max-h-40">
                {JSON.stringify(result.success ? result.data : result.error, null, 2)}
              </pre>
            </div>
          )}
        </div>

        {/* Payment History */}
        <div className="md:w-1/2 bg-white rounded-lg shadow-md p-6">
          <h2 className="text-2xl font-bold text-gray-800 mb-6">Confirmation History</h2>

          {paymentHistory.length === 0 ? (
            <p className="text-gray-500 text-center py-8">No payment confirmations yet</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Payment ID</th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Amount</th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Time</th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {paymentHistory.map((payment) => (
                    <tr key={payment.id}>
                      <td className="px-4 py-3 whitespace-nowrap text-sm font-mono text-gray-600">
                        {payment.paymentIntentId.substring(0, 16)}...
                      </td>
                      <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-800">
                        PHP {payment.amount.toFixed(2)}
                      </td>
                      <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-500">
                        {new Date(payment.timestamp).toLocaleTimeString()}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>

      {/* Instructions */}
      <div className="mt-8 bg-blue-50 border border-blue-200 rounded-lg p-4">
        <h3 className="text-lg font-medium text-blue-800 mb-2">How to Use</h3>
        <ol className="list-decimal list-inside text-blue-700 space-y-2">
          <li>When a user creates a payment, it will automatically appear in the "Pending Payments" section</li>
          <li>Click the "Confirm & Stop" button to process payment and stop polling in one click</li>
          <li>For manual control, copy the Payment Intent ID and amount from the pending payments table</li>
          <li>Paste them into the manual form below and click "Confirm Payment"</li>
          <li>If polling continues after confirmation, use the "Stop Polling" button</li>
        </ol>
        <p className="mt-4 text-sm text-blue-600">
          <strong>Note:</strong> This is for demonstration purposes only. In a production environment,
          payments would be confirmed through PayMongo's webhooks.
        </p>
      </div>
    </div>
  );
};

export default AdminPayments;
