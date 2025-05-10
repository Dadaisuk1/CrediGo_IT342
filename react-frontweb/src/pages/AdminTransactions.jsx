import { format } from 'date-fns';
import { useEffect, useState } from 'react';
import { FaCalendarAlt, FaDownload, FaEye, FaFilter, FaSearch, FaTimes } from 'react-icons/fa';

const AdminTransactions = () => {
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('all');
  const [searchTerm, setSearchTerm] = useState('');
  const [dateRange, setDateRange] = useState({ start: '', end: '' });
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [selectedTransaction, setSelectedTransaction] = useState(null);
  const [showModal, setShowModal] = useState(false);

  // Mock data
  const mockTransactions = [
    {
      id: 'TX-9876',
      userId: 101,
      userName: 'Maria Garcia',
      userEmail: 'maria@example.com',
      type: 'deposit',
      amount: 500.00,
      fee: 5.00,
      status: 'completed',
      date: '2023-06-15T14:30:00Z',
      paymentMethod: 'bank_transfer',
      description: 'Wallet top-up',
      reference: 'REF12345',
      note: 'Verified bank transfer'
    },
    {
      id: 'TX-9875',
      userId: 102,
      userName: 'James Wilson',
      userEmail: 'james@example.com',
      type: 'withdrawal',
      amount: 250.00,
      fee: 2.50,
      status: 'pending',
      date: '2023-06-14T09:45:00Z',
      paymentMethod: 'bank_transfer',
      description: 'Withdrawal to bank account',
      reference: 'REF12344',
      note: 'Pending bank verification'
    },
    {
      id: 'TX-9874',
      userId: 103,
      userName: 'Sofia Martinez',
      userEmail: 'sofia@example.com',
      type: 'purchase',
      amount: 75.99,
      fee: 0,
      status: 'completed',
      date: '2023-06-13T18:20:00Z',
      paymentMethod: 'wallet',
      description: 'Purchase: Premium Game Package',
      reference: 'REF12343',
      productId: 'PROD-876'
    },
    {
      id: 'TX-9873',
      userId: 104,
      userName: 'David Chen',
      userEmail: 'david@example.com',
      type: 'refund',
      amount: 29.99,
      fee: 0,
      status: 'completed',
      date: '2023-06-12T11:05:00Z',
      paymentMethod: 'wallet',
      description: 'Refund: Game Item',
      reference: 'REF12342',
      productId: 'PROD-532'
    },
    {
      id: 'TX-9872',
      userId: 105,
      userName: 'Anna Johnson',
      userEmail: 'anna@example.com',
      type: 'deposit',
      amount: 1000.00,
      fee: 10.00,
      status: 'failed',
      date: '2023-06-11T08:15:00Z',
      paymentMethod: 'credit_card',
      description: 'Wallet top-up',
      reference: 'REF12341',
      note: 'Payment rejected by bank'
    },
    {
      id: 'TX-9871',
      userId: 106,
      userName: 'Michael Brown',
      userEmail: 'michael@example.com',
      type: 'purchase',
      amount: 150.00,
      fee: 0,
      status: 'completed',
      date: '2023-06-10T14:50:00Z',
      paymentMethod: 'wallet',
      description: 'Purchase: Game Credits',
      reference: 'REF12340',
      productId: 'PROD-123'
    },
  ];

  useEffect(() => {
    // Simulate API fetch
    setTimeout(() => {
      setTransactions(mockTransactions);
      setLoading(false);
      setTotalPages(Math.ceil(mockTransactions.length / 10));
    }, 1000);
  }, []);

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return format(date, 'MMM dd, yyyy HH:mm');
  };

  const getStatusBadgeClass = (status) => {
    switch (status) {
      case 'completed':
        return 'bg-green-100 text-green-800';
      case 'pending':
        return 'bg-yellow-100 text-yellow-800';
      case 'failed':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const getTypeClass = (type) => {
    switch (type) {
      case 'deposit':
        return 'text-green-600';
      case 'withdrawal':
        return 'text-red-600';
      case 'purchase':
        return 'text-blue-600';
      case 'refund':
        return 'text-purple-600';
      default:
        return 'text-gray-600';
    }
  };

  const filteredTransactions = transactions.filter(tx => {
    // Filter by type
    if (filter !== 'all' && tx.type !== filter) return false;

    // Filter by search term
    if (searchTerm && !tx.userName.toLowerCase().includes(searchTerm.toLowerCase()) &&
        !tx.id.toLowerCase().includes(searchTerm.toLowerCase())) {
      return false;
    }

    // Filter by date range
    if (dateRange.start && new Date(tx.date) < new Date(dateRange.start)) return false;
    if (dateRange.end && new Date(tx.date) > new Date(`${dateRange.end}T23:59:59`)) return false;

    return true;
  });

  const exportToCSV = () => {
    // Create CSV content
    const headers = ['ID', 'User', 'Type', 'Amount', 'Status', 'Date', 'Payment Method', 'Description'];
    const csvRows = [headers];

    filteredTransactions.forEach(tx => {
      csvRows.push([
        tx.id,
        tx.userName,
        tx.type,
        `₱${tx.amount.toFixed(2)}`,
        tx.status,
        formatDate(tx.date),
        tx.paymentMethod.replace('_', ' '),
        tx.description
      ]);
    });

    const csvContent = csvRows.map(row => row.join(',')).join('\n');

    // Create and download file
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.setAttribute('href', url);
    link.setAttribute('download', `transactions_${format(new Date(), 'yyyy-MM-dd')}.csv`);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  if (loading) {
    return <div className="flex justify-center items-center h-64">
      <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-[#eebbc3]"></div>
    </div>;
  }

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="bg-white rounded-xl shadow-md border border-gray-100 p-6">
        <div className="flex flex-col md:flex-row md:items-center md:justify-between">
          <div>
            <h1 className="text-2xl font-bold text-[#232946]">Transaction Management</h1>
            <p className="text-gray-500 mt-1">View and manage all financial transactions</p>
          </div>
          <div className="flex mt-4 md:mt-0">
            <button
              onClick={exportToCSV}
              className="flex items-center px-4 py-2 bg-[#232946] text-white rounded-lg hover:bg-[#1a2035] transition-colors"
            >
              <FaDownload className="mr-2" /> Export CSV
            </button>
          </div>
        </div>
      </div>

      {/* Filters */}
      <div className="bg-white rounded-xl shadow-md border border-gray-100 p-6">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          {/* Search */}
          <div className="relative">
            <input
              type="text"
              placeholder="Search by ID or name..."
              className="w-full px-10 py-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#eebbc3]"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
            <FaSearch className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
          </div>

          {/* Type Filter */}
          <div className="relative">
            <select
              value={filter}
              onChange={(e) => setFilter(e.target.value)}
              className="w-full px-10 py-2 border border-gray-200 rounded-lg appearance-none focus:outline-none focus:ring-2 focus:ring-[#eebbc3]"
            >
              <option value="all">All Types</option>
              <option value="deposit">Deposits</option>
              <option value="withdrawal">Withdrawals</option>
              <option value="purchase">Purchases</option>
              <option value="refund">Refunds</option>
            </select>
            <FaFilter className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
          </div>

          {/* Date Range - Start */}
          <div className="relative">
            <input
              type="date"
              className="w-full px-10 py-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#eebbc3]"
              value={dateRange.start}
              onChange={(e) => setDateRange({...dateRange, start: e.target.value})}
            />
            <FaCalendarAlt className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
          </div>

          {/* Date Range - End */}
          <div className="relative">
            <input
              type="date"
              className="w-full px-10 py-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#eebbc3]"
              value={dateRange.end}
              onChange={(e) => setDateRange({...dateRange, end: e.target.value})}
              min={dateRange.start}
            />
            <FaCalendarAlt className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
          </div>
        </div>
      </div>

      {/* Transactions Table */}
      <div className="bg-white rounded-xl shadow-md border border-gray-100 overflow-hidden">
        <div className="p-4 border-b border-gray-100 flex justify-between items-center">
          <h2 className="text-lg font-semibold text-[#232946]">Transactions</h2>
          <span className="text-sm text-gray-500">
            Showing {filteredTransactions.length} {filteredTransactions.length === 1 ? 'transaction' : 'transactions'}
          </span>
        </div>

        {filteredTransactions.length === 0 ? (
          <div className="p-10 text-center text-gray-500">
            No transactions match your filter criteria
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="bg-gray-50 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  <th className="px-6 py-3">ID</th>
                  <th className="px-6 py-3">User</th>
                  <th className="px-6 py-3">Type</th>
                  <th className="px-6 py-3">Amount</th>
                  <th className="px-6 py-3">Status</th>
                  <th className="px-6 py-3">Date</th>
                  <th className="px-6 py-3">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {filteredTransactions.map((tx) => (
                  <tr key={tx.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 text-sm font-medium text-gray-900">{tx.id}</td>
                    <td className="px-6 py-4">
                      <div className="text-sm font-medium text-gray-900">{tx.userName}</div>
                      <div className="text-xs text-gray-500">{tx.userEmail}</div>
                    </td>
                    <td className="px-6 py-4">
                      <span className={`capitalize ${getTypeClass(tx.type)}`}>
                        {tx.type}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-sm">
                      <span className={`font-medium ${tx.type === 'deposit' || tx.type === 'refund' ? 'text-green-600' : ''} ${tx.type === 'withdrawal' || tx.type === 'purchase' ? 'text-red-600' : ''}`}>
                        {tx.type === 'deposit' || tx.type === 'refund' ? '+' : '-'}₱{tx.amount.toFixed(2)}
                      </span>
                      {tx.fee > 0 && (
                        <div className="text-xs text-gray-500">
                          Fee: ₱{tx.fee.toFixed(2)}
                        </div>
                      )}
                    </td>
                    <td className="px-6 py-4">
                      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusBadgeClass(tx.status)}`}>
                        {tx.status}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-500">
                      {formatDate(tx.date)}
                    </td>
                    <td className="px-6 py-4 text-sm font-medium">
                      <button
                        onClick={() => {
                          setSelectedTransaction(tx);
                          setShowModal(true);
                        }}
                        className="text-[#eebbc3] hover:text-[#d39ba4]"
                      >
                        <FaEye size={18} />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {/* Pagination */}
        <div className="flex items-center justify-between border-t border-gray-200 px-6 py-4">
          <div className="flex items-center">
            <span className="text-sm text-gray-700">
              Page <span className="font-medium">{currentPage}</span> of{" "}
              <span className="font-medium">{totalPages}</span>
            </span>
          </div>
          <div className="flex space-x-2">
            <button
              onClick={() => setCurrentPage(prev => Math.max(prev - 1, 1))}
              disabled={currentPage === 1}
              className={`px-3 py-1 rounded-lg ${
                currentPage === 1
                  ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
                  : 'bg-white text-[#232946] border border-gray-200 hover:bg-[#f9f9f1]'
              }`}
            >
              Previous
            </button>
            <button
              onClick={() => setCurrentPage(prev => Math.min(prev + 1, totalPages))}
              disabled={currentPage === totalPages}
              className={`px-3 py-1 rounded-lg ${
                currentPage === totalPages
                  ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
                  : 'bg-white text-[#232946] border border-gray-200 hover:bg-[#f9f9f1]'
              }`}
            >
              Next
            </button>
          </div>
        </div>
      </div>

      {/* Transaction Details Modal */}
      {showModal && selectedTransaction && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-2xl w-full max-w-3xl max-h-[90vh] overflow-y-auto">
            <div className="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 flex justify-between items-center">
              <h3 className="text-lg font-semibold text-[#232946]">
                Transaction Details: {selectedTransaction.id}
              </h3>
              <button
                onClick={() => setShowModal(false)}
                className="text-gray-500 hover:text-gray-700"
              >
                <FaTimes />
              </button>
            </div>

            <div className="p-6 space-y-6">
              {/* Transaction Header */}
              <div className="bg-[#f9f9f1] p-4 rounded-lg">
                <div className="flex flex-col md:flex-row md:justify-between md:items-center">
                  <div>
                    <h4 className="text-lg font-medium text-[#232946]">{selectedTransaction.description}</h4>
                    <p className="text-sm text-gray-500">Reference: {selectedTransaction.reference}</p>
                  </div>
                  <span className={`mt-2 md:mt-0 inline-flex items-center px-3 py-1 rounded-full text-sm font-medium ${getStatusBadgeClass(selectedTransaction.status)}`}>
                    {selectedTransaction.status}
                  </span>
                </div>
              </div>

              {/* Transaction Details */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <h4 className="text-sm font-medium text-gray-500 mb-2">Transaction Information</h4>
                  <div className="bg-white border border-gray-200 rounded-lg divide-y divide-gray-200">
                    <div className="px-4 py-3 flex justify-between">
                      <span className="text-sm text-gray-500">Type</span>
                      <span className={`text-sm font-medium ${getTypeClass(selectedTransaction.type)} capitalize`}>{selectedTransaction.type}</span>
                    </div>
                    <div className="px-4 py-3 flex justify-between">
                      <span className="text-sm text-gray-500">Amount</span>
                      <span className="text-sm font-medium">₱{selectedTransaction.amount.toFixed(2)}</span>
                    </div>
                    <div className="px-4 py-3 flex justify-between">
                      <span className="text-sm text-gray-500">Fee</span>
                      <span className="text-sm font-medium">₱{selectedTransaction.fee.toFixed(2)}</span>
                    </div>
                    <div className="px-4 py-3 flex justify-between">
                      <span className="text-sm text-gray-500">Total</span>
                      <span className="text-sm font-medium">₱{(selectedTransaction.amount + selectedTransaction.fee).toFixed(2)}</span>
                    </div>
                    <div className="px-4 py-3 flex justify-between">
                      <span className="text-sm text-gray-500">Date</span>
                      <span className="text-sm font-medium">{formatDate(selectedTransaction.date)}</span>
                    </div>
                    <div className="px-4 py-3 flex justify-between">
                      <span className="text-sm text-gray-500">Payment Method</span>
                      <span className="text-sm font-medium capitalize">{selectedTransaction.paymentMethod.replace('_', ' ')}</span>
                    </div>
                  </div>
                </div>

                <div>
                  <h4 className="text-sm font-medium text-gray-500 mb-2">User Information</h4>
                  <div className="bg-white border border-gray-200 rounded-lg divide-y divide-gray-200">
                    <div className="px-4 py-3 flex justify-between">
                      <span className="text-sm text-gray-500">Name</span>
                      <span className="text-sm font-medium">{selectedTransaction.userName}</span>
                    </div>
                    <div className="px-4 py-3 flex justify-between">
                      <span className="text-sm text-gray-500">Email</span>
                      <span className="text-sm font-medium">{selectedTransaction.userEmail}</span>
                    </div>
                    <div className="px-4 py-3 flex justify-between">
                      <span className="text-sm text-gray-500">User ID</span>
                      <span className="text-sm font-medium">{selectedTransaction.userId}</span>
                    </div>
                  </div>

                  {selectedTransaction.productId && (
                    <div className="mt-4">
                      <h4 className="text-sm font-medium text-gray-500 mb-2">Product Information</h4>
                      <div className="bg-white border border-gray-200 rounded-lg divide-y divide-gray-200">
                        <div className="px-4 py-3 flex justify-between">
                          <span className="text-sm text-gray-500">Product ID</span>
                          <span className="text-sm font-medium">{selectedTransaction.productId}</span>
                        </div>
                      </div>
                    </div>
                  )}
                </div>
              </div>

              {/* Additional Notes */}
              {selectedTransaction.note && (
                <div>
                  <h4 className="text-sm font-medium text-gray-500 mb-2">Notes</h4>
                  <div className="bg-white border border-gray-200 rounded-lg p-4">
                    <p className="text-sm">{selectedTransaction.note}</p>
                  </div>
                </div>
              )}

              {/* Action Buttons */}
              <div className="flex justify-end space-x-4 pt-4 border-t border-gray-200">
                {selectedTransaction.status === 'pending' && (
                  <>
                    <button className="px-4 py-2 border border-red-500 text-red-500 rounded-lg hover:bg-red-50">
                      Reject
                    </button>
                    <button className="px-4 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600">
                      Approve
                    </button>
                  </>
                )}
                <button
                  onClick={() => setShowModal(false)}
                  className="px-4 py-2 bg-[#232946] text-white rounded-lg hover:bg-[#1a2035]"
                >
                  Close
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminTransactions;
