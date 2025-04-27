// src/pages/HistoryPage.jsx
import React, { useState, useEffect } from 'react';
import { getTransactionHistory } from '../services/api'; // Import API function
import { TbCurrencyPeso } from 'react-icons/tb'; // Import Peso icon

function HistoryPage() {
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchHistory = async () => {
      setLoading(true);
      setError(null);
      try {
        const response = await getTransactionHistory();
        setTransactions(response.data || []);
        console.log("Fetched transaction history:", response.data);
      } catch (err) {
        console.error("Failed to fetch transaction history:", err);
        setError("Could not load transaction history. Please try again later.");
        setTransactions([]); // Clear history on error
      } finally {
        setLoading(false);
      }
    };

    fetchHistory();
  }, []); // Runs once on component mount

  // Helper to format date/time
  const formatDateTime = (isoString) => {
    if (!isoString) return 'N/A';
    try {
      return new Date(isoString).toLocaleString('en-US', {
        year: 'numeric', month: 'short', day: 'numeric',
        hour: 'numeric', minute: '2-digit', hour12: true
      });
    } catch (e) {
      return 'Invalid Date';
    }
  };

  // Helper to format currency
  const formatCurrency = (amount) => {
    if (amount === null || amount === undefined) return 'N/A';
    return new Intl.NumberFormat('en-PH', { style: 'currency', currency: 'PHP' }).format(amount);
  };

  // Helper to get status styling
  const getStatusClass = (status) => {
    switch (status) {
      case 'COMPLETED': return 'bg-green-500/20 text-green-300';
      case 'PENDING': return 'bg-yellow-500/20 text-yellow-300';
      case 'PROCESSING': return 'bg-blue-500/20 text-blue-300';
      case 'FAILED': return 'bg-red-500/20 text-red-300';
      case 'REFUNDED': return 'bg-gray-500/20 text-gray-300';
      default: return 'bg-gray-700 text-gray-400';
    }
  };


  return (
    <div className="font-sans text-credigo-light p-4 md:p-6">
      <h1 className="text-3xl font-bold mb-6 text-credigo-dark">Transaction History</h1>

      {/* Loading State */}
      {loading && (
        <div className="text-center py-10">
          <p>Loading history...</p>
          {/* Optional spinner */}
        </div>
      )}

      {/* Error State */}
      {error && !loading && (
        <div className="p-4 text-center text-red-400 bg-red-900/50 rounded-lg border border-red-700" role="alert">
          <span className="font-medium">Error:</span> {error}
        </div>
      )}

      {/* History Table */}
      {!loading && !error && transactions.length > 0 && (
        <div className="overflow-x-auto bg-credigo-input-bg rounded-lg shadow border border-gray-700">
          <table className="min-w-full divide-y divide-gray-700">
            <thead className="bg-credigo-dark/50">
              <tr>
                <th scope="col" className="px-4 py-3 text-left text-xs font-medium text-gray-400 uppercase tracking-wider">Date</th>
                <th scope="col" className="px-4 py-3 text-left text-xs font-medium text-gray-400 uppercase tracking-wider">Product</th>
                <th scope="col" className="px-4 py-3 text-left text-xs font-medium text-gray-400 uppercase tracking-wider">Game Account</th>
                <th scope="col" className="px-4 py-3 text-right text-xs font-medium text-gray-400 uppercase tracking-wider">Amount</th>
                <th scope="col" className="px-4 py-3 text-center text-xs font-medium text-gray-400 uppercase tracking-wider">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-700">
              {transactions.map((tx) => (
                <tr key={tx.transactionId} className="hover:bg-credigo-dark/30">
                  <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-300">{formatDateTime(tx.transactionTimestamp)}</td>
                  <td className="px-4 py-3 whitespace-nowrap text-sm font-medium text-credigo-light">{tx.productName || 'N/A'}</td>
                  <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-400">{tx.gameAccountId || 'N/A'}</td>
                  <td className="px-4 py-3 whitespace-nowrap text-sm text-right text-credigo-light">{formatCurrency(tx.totalAmount)}</td>
                  <td className="px-4 py-3 whitespace-nowrap text-center text-xs font-semibold">
                    <span className={`px-2.5 py-0.5 rounded-full ${getStatusClass(tx.status)}`}>
                      {tx.status ? tx.status.replace('_', ' ') : 'UNKNOWN'}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* No History State */}
      {!loading && !error && transactions.length === 0 && (
        <div className="text-center py-10 text-gray-500 bg-credigo-input-bg rounded-lg shadow border border-gray-700">
          <p>You have no transaction history yet.</p>
        </div>
      )}

    </div>
  );
}

export default HistoryPage;
