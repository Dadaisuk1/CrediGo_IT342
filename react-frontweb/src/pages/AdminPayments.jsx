import { useCallback, useEffect, useState } from 'react';
import { RiRefreshLine } from 'react-icons/ri';
import { toast } from 'react-toastify';
import { Alert, AlertDescription, AlertTitle } from "../components/ui/alert";
import { Button } from "../components/ui/button";
import { ScrollArea } from "../components/ui/scroll-area";
import { confirmTestPayment } from '../services/api';

const AdminPayments = () => {
  const [paymentIntentId, setPaymentIntentId] = useState('');
  const [amount, setAmount] = useState('');
  const [targetUsername, setTargetUsername] = useState('');
  const [processing, setProcessing] = useState(false);
  const [result, setResult] = useState(null);
  const [paymentHistory, setPaymentHistory] = useState([]);
  const [stoppingPolling, setStoppingPolling] = useState(false);
  const [pendingPayments, setPendingPayments] = useState([]);
  const [loadingPending, setLoadingPending] = useState(false);
  const [userList, setUserList] = useState([]);
  const [validationError, setValidationError] = useState(null);

  // Load payment history from localStorage on component mount
  useEffect(() => {
    try {
      const storedHistory = localStorage.getItem('confirmed_payments_history');
      if (storedHistory) {
        setPaymentHistory(JSON.parse(storedHistory));
      }
    } catch (error) {
      console.error('Error loading payment history:', error);
    }
  }, []);

  // Helper function to mark a payment as confirmed in localStorage
  const markPaymentAsConfirmed = (paymentId, amountValue, status = 'success') => {
    // Add to confirmed payments record in localStorage
    const confirmedKey = 'confirmed_payment_' + paymentId;
    localStorage.setItem(confirmedKey, 'true');

    // Add to payment history
    const newPayment = {
      id: Date.now(),
      paymentIntentId: paymentId,
      amount: typeof amountValue === 'number' ? amountValue : parseFloat(amountValue),
      timestamp: new Date().toISOString(),
      status
    };

    const updatedHistory = [newPayment, ...paymentHistory];
    setPaymentHistory(updatedHistory);

    // Also store the complete history in localStorage
    localStorage.setItem('confirmed_payments_history', JSON.stringify(updatedHistory));

    return newPayment;
  };

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
            const paymentId = key.replace('payment_intent_', '');

            // Check if this payment is already confirmed by checking localStorage
            const confirmedKey = 'confirmed_payment_' + paymentId;
            if (localStorage.getItem(confirmedKey) === 'true') {
              continue; // Skip confirmed payments
            }

            // Include both awaiting payments and background tracking payments
            if (data && (data.status === 'awaiting_payment_method' ||
                         data.trackingInBackground === true ||
                         data.status === 'processing')) {
              pendingPaymentIds.push({
                id: paymentId,
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
  }, [paymentHistory]);

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

  // Add useEffect to fetch list of valid usernames
  useEffect(() => {
    // In a real app, this would fetch actual users from your API
    // For demo purposes, we'll hard-code some valid users
    setUserList(['testuser', 'admin', 'john_doe']);
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setValidationError(null);
    setProcessing(true);
    setResult(null);

    if (!paymentIntentId || !amount) {
      toast.error('Please enter both payment ID and amount');
      setProcessing(false);
      return;
    }

    // Validate username if provided
    if (targetUsername && !userList.includes(targetUsername)) {
      setValidationError(`Warning: Username "${targetUsername}" may not exist in the system. The payment might fail.`);
      // Continue anyway, but with a warning
    }

    try {
      const response = await confirmTestPayment({
        paymentIntentId,
        amount: parseFloat(amount),
        targetUsername: targetUsername || 'testuser' // Use testuser as fallback instead of Anonymous User
      });

      setResult({
        success: true,
        message: response.data.message || 'Payment confirmed successfully!',
        details: response.data
      });

      // Clear form
      setPaymentIntentId('');
      setAmount('');
      if (response.data.success) {
        toast.success(`Payment confirmed! ${targetUsername ? `Funds added to ${targetUsername}'s wallet.` : ''}`);
      }
    } catch (error) {
      console.error('Error confirming payment:', error);

      // Extract meaningful error message from backend
      let errorMessage = error.message;
      if (error.response) {
        // If it's a backend error with data
        if (error.response.data && typeof error.response.data === 'string' && error.response.data.includes('Wallet not found')) {
          errorMessage = `User "${targetUsername || 'Anonymous User'}" doesn't have a wallet. Please use an existing username.`;
        } else if (error.response.data && error.response.data.message) {
          errorMessage = error.response.data.message;
        } else {
          errorMessage = `Server error (${error.response.status}): ${error.response.statusText}`;
        }
      }

      setResult({
        success: false,
        message: errorMessage,
        error: error.message
      });
      toast.error(`Failed to confirm payment: ${errorMessage}`);
    } finally {
      setProcessing(false);
    }
  };

  const handleQuickConfirm = async (payment) => {
    if (processing) return;

    try {
      // Extract payment information
      const { id, amount } = payment;

      // Convert amount from cents to actual amount
      const actualAmount = amount / 100;

      // Update the form fields with the payment data
      setPaymentIntentId(id);
      setAmount(actualAmount.toString());

      // Ensure the username is always populated
      // Use the payment's username if available, or use one of the valid test users
      const username = payment.username || userList[0] || 'testuser';
      setTargetUsername(username);

      // Show toast to indicate form was populated
      toast.info(`Payment details loaded into form. Click "Confirm Payment" to process.`);

    } catch (error) {
      console.error('Error loading payment details:', error);
      toast.error(`Failed to load payment details: ${error.message}`);
    }
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

          // Mark as confirmed in localStorage
          markPaymentAsConfirmed(paymentIntentId, amount, 'success (manual stop)');

          setResult({
            success: true,
            data: {
              message: "Payment confirmed and polling stopped",
              paymentIntentId
            }
          });

          // Remove this payment from pending list if it exists
          setPendingPayments(prev => prev.filter(p => p.id !== paymentIntentId));
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
          <Button
            onClick={fetchPendingPayments}
            disabled={loadingPending}
            variant="outline"
            size="sm"
            className="flex items-center gap-1"
          >
            <RiRefreshLine className={`${loadingPending ? 'animate-spin' : ''}`} />
            Refresh
          </Button>
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
                      <Button
                        onClick={() => handleQuickConfirm(payment)}
                        variant="default"
                        size="sm"
                      >
                        Load Details
                      </Button>
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

          {validationError && (
            <Alert variant="warning" className="mb-4 bg-yellow-50 border-yellow-200">
              <AlertTitle className="text-yellow-800">Warning</AlertTitle>
              <AlertDescription className="text-yellow-700">
                {validationError}
              </AlertDescription>
            </Alert>
          )}

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

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Target Username
              </label>
              <input
                type="text"
                value={targetUsername}
                onChange={(e) => {
                  setTargetUsername(e.target.value);
                  setValidationError(null); // Clear validation error when input changes
                }}
                placeholder="Enter a valid username"
                className="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-indigo-500 focus:border-indigo-500"
              />
              <p className="mt-1 text-xs text-gray-500">
                Enter a valid username that exists in the system. For testing, try: {userList.join(', ')}
              </p>
            </div>

            <div className="flex space-x-2">
              <Button
                type="submit"
                disabled={processing}
                variant="default"
                className="flex-1"
              >
                {processing ? 'Processing...' : 'Confirm Payment'}
              </Button>

              <Button
                type="button"
                onClick={forceClosePolling}
                disabled={stoppingPolling || !paymentIntentId}
                variant="destructive"
                className="flex-1"
              >
                {stoppingPolling ? 'Stopping...' : 'Stop Polling'}
              </Button>
            </div>
          </form>

          {result && (
            <div className={`mt-4 p-4 rounded-md ${result.success ? 'bg-green-50 text-green-800 border border-green-200' : 'bg-red-50 text-red-800 border border-red-200'}`}>
              <p className="font-medium mb-2">{result.success ? 'Success!' : 'Error'}</p>
              <p className="mb-2">{result.message}</p>
              {result.success && result.details && (
                <pre className="mt-2 text-sm overflow-auto max-h-40 bg-white/50 p-2 rounded">
                  {JSON.stringify(result.details, null, 2)}
                </pre>
              )}
            </div>
          )}
        </div>

        {/* Payment History */}
        <div className="md:w-1/2 bg-white rounded-lg shadow-md p-6">
          <h2 className="text-2xl font-bold text-gray-800 mb-6">Confirmation History</h2>

          {paymentHistory.length === 0 ? (
            <p className="text-gray-500 text-center py-8">No payment confirmations yet</p>
          ) : (
            <ScrollArea className="h-[400px] rounded-md border border-gray-200">
              <div className="p-1">
                <table className="min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-50 sticky top-0">
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
            </ScrollArea>
          )}
        </div>
      </div>

      {/* Instructions */}
      <div className="mt-8 bg-blue-50 border border-blue-200 rounded-lg p-4">
        <h3 className="text-lg font-medium text-blue-800 mb-2">How to Use</h3>
        <ol className="list-decimal list-inside text-blue-700 space-y-2">
          <li>When a user creates a payment, it will automatically appear in the "Pending Payments" section</li>
          <li>Click the "Load Details" button to populate the form with payment information</li>
          <li>Enter a <strong>valid username</strong> that exists in the system (e.g., testuser, admin)</li>
          <li>Click "Confirm Payment" to process the payment and add funds to the wallet</li>
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
