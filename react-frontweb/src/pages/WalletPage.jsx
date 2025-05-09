// src/pages/WalletPage.jsx
import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import TopUpForm from '../components/TopUpForm';
import { useAuth } from '../context/AuthContext';
import { getWalletTransactions } from '../services/api';

function WalletPage() {
  const { wallet, user, fetchWalletBalance } = useAuth();
  const [topUpError, setTopUpError] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [pendingTransactions, setPendingTransactions] = useState([]);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    fetchTransactions();
  }, []);

  const fetchTransactions = async () => {
    try {
      setLoading(true);
      const response = await getWalletTransactions();
      setTransactions(response.data || []);

      // Filter pending transactions
      const pending = (response.data || []).filter(tx =>
        tx.transactionType === 'PENDING' || tx.transactionType === 'PENDING_DEPOSIT'
      );
      setPendingTransactions(pending);
    } catch (error) {
      console.error('Error fetching transactions:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleManualCompletion = (paymentId) => {
    // Extract the payment ID from the description
    const regex = /\(([^)]+)\)/;
    const match = regex.exec(paymentId);
    const extractedPaymentId = match ? match[1] : null;

    if (!extractedPaymentId) {
      toast.error('Could not extract payment ID');
      return;
    }

    // Navigate to payment success page with the ID
    navigate(`/payment/success?id=${extractedPaymentId}&redirect=/wallet`);
  };

  return (
    <div className="font-sans max-w-3xl mx-auto p-6">
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-lg p-6 mb-6">
        <div className="flex justify-between items-center mb-4">
          <h1 className="text-2xl font-bold text-gray-800 dark:text-white">My Wallet</h1>
          {wallet && (
            <div className="bg-green-100 dark:bg-green-900 px-4 py-2 rounded-md">
              <span className="text-green-800 dark:text-green-200 font-semibold">₱{wallet.balance ? wallet.balance.toFixed(2) : '0.00'}</span>
            </div>
          )}
        </div>

        {pendingTransactions.length > 0 && (
          <div className="mb-6 p-4 bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200 dark:border-yellow-700 rounded-lg">
            <h3 className="text-lg font-medium text-yellow-800 dark:text-yellow-200 mb-2">Pending Transactions</h3>
            <p className="text-sm text-yellow-700 dark:text-yellow-300 mb-3">
              You have {pendingTransactions.length} pending transaction(s). These transactions may be waiting for PayMongo webhook processing.
            </p>
            <div className="space-y-2">
              {pendingTransactions.map(tx => (
                <div key={tx.id} className="flex justify-between items-center p-3 bg-white dark:bg-gray-700 rounded shadow-sm">
                  <div>
                    <p className="text-sm font-medium text-gray-700 dark:text-gray-300">{tx.description}</p>
                    <p className="text-xs text-gray-500 dark:text-gray-400">Amount: ₱{tx.amount.toFixed(2)}</p>
                  </div>
                  <button
                    onClick={() => handleManualCompletion(tx.description)}
                    className="px-3 py-1 bg-blue-600 hover:bg-blue-700 text-white text-sm rounded"
                  >
                    Complete
                  </button>
                </div>
              ))}
            </div>
          </div>
        )}

        <div className="bg-gray-100 dark:bg-gray-700 p-4 rounded-lg">
          <h2 className="text-xl font-semibold text-gray-800 dark:text-white mb-4">Add Funds</h2>

          {topUpError && <div className="text-red-400 text-sm mb-4">{topUpError}</div>}

          <TopUpForm
            onPaymentSuccess={() => {
              console.log("Payment successful!");
              setTopUpError(null);
              // Fetch updated wallet balance
              fetchWalletBalance();
              fetchTransactions();
            }}
            onPaymentCancel={() => {
              console.log("Payment cancelled.");
            }}
            onPaymentError={(errorMessage) => {
              console.error("Payment error:", errorMessage);
              setTopUpError(errorMessage || "An error occurred during payment processing.");
            }}
          />
        </div>
      </div>

      {/* Transaction History Section */}
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-lg p-6">
        <h2 className="text-xl font-semibold text-gray-800 dark:text-white mb-4">Recent Transactions</h2>

        {loading ? (
          <p className="text-center text-gray-500 dark:text-gray-400 py-4">Loading transactions...</p>
        ) : transactions.length === 0 ? (
          <p className="text-center text-gray-500 dark:text-gray-400 py-4">No transactions yet</p>
        ) : (
          <div className="space-y-3">
            {transactions.slice(0, 5).map(tx => (
              <div key={tx.id} className="p-3 border-b border-gray-200 dark:border-gray-700 last:border-0">
                <div className="flex justify-between">
                  <div>
                    <p className="font-medium text-gray-800 dark:text-gray-200">{tx.description}</p>
                    <p className="text-xs text-gray-500 dark:text-gray-400">
                      {new Date(tx.transactionTimestamp).toLocaleString()}
                    </p>
                  </div>
                  <div className={`font-semibold ${tx.transactionType.includes('DEPOSIT') ? 'text-green-600 dark:text-green-400' : 'text-red-600 dark:text-red-400'}`}>
                    {tx.transactionType.includes('DEPOSIT') ? '+' : '-'}₱{tx.amount.toFixed(2)}
                  </div>
                </div>
                <div className="mt-1">
                  <span className={`text-xs px-2 py-1 rounded-full ${
                    tx.transactionType.includes('PENDING')
                      ? 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200'
                      : tx.transactionType.includes('DEPOSIT')
                        ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
                        : 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
                  }`}>
                    {tx.transactionType}
                  </span>
                </div>
              </div>
            ))}
          </div>
        )}

        {transactions.length > 5 && (
          <div className="mt-4 text-center">
            <button
              className="text-blue-600 dark:text-blue-400 hover:underline text-sm"
              onClick={() => navigate('/history')}
            >
              View all transactions
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

export default WalletPage;
