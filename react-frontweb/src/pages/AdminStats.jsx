import React, { useState, useEffect } from 'react';
import { FaUsers, FaBoxOpen, FaExchangeAlt } from 'react-icons/fa';
import StatCard from '../components/StatCard';
import StatusBadge from '../components/StatusBadge';
// import adminService from '../api/adminService';

import { getAdminDashboardStats } from '../services/api';

const AdminStats = () => {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchDashboardStats = async () => {
      try {
        setLoading(true);
        const response = await getAdminDashboardStats();
        setStats(response.data);
        setError(null);
      } catch (err) {
        setError('Failed to load dashboard data');
      } finally {
        setLoading(false);
      }
    };
    fetchDashboardStats();
  }, []);

  if (loading) {
    return <div className="flex justify-center items-center h-full">Loading dashboard data...</div>;
  }
  if (error) {
    return <div className="bg-red-100 p-4 rounded-lg text-red-700">{error}</div>;
  }

  return (
    <div className="space-y-6">
      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatCard
          title="Total Users"
          value={stats.totalUsers}
          icon={<FaUsers className="text-[#6285cf]" size={24} />}
          change={5}
          iconBgColor="bg-blue-100"
        />
        <StatCard
          title="Active Users"
          value={stats.activeUsers}
          icon={<FaUsers className="text-green-600" size={24} />}
          change={3}
          iconBgColor="bg-green-100"
        />
        <StatCard
          title="Total Products"
          value={stats.totalProducts}
          icon={<FaBoxOpen className="text-purple-600" size={24} />}
          change={12}
          iconBgColor="bg-purple-100"
        />
        <StatCard
          title="Transactions"
          value={stats.totalTransactions}
          icon={<FaExchangeAlt className="text-orange-600" size={24} />}
          change={8}
          iconBgColor="bg-orange-100"
        />
      </div>

      {/* Chart placeholder */}
      <div className="bg-white rounded-lg shadow p-6">
        <h2 className="text-lg font-semibold text-[#232946] mb-4">Transaction Overview</h2>
        <div className="h-64 bg-gray-100 rounded flex items-center justify-center">
          <p className="text-gray-500">Chart would render here (using Recharts, Chart.js, etc.)</p>
        </div>
      </div>

      {/* Recent Transactions */}
      <div className="bg-white rounded-lg shadow">
        <div className="p-6 border-b border-gray-200">
          <h2 className="text-lg font-semibold text-[#232946]">Recent Transactions</h2>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">User</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Amount</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {stats.recentTransactions.map((transaction) => (
                <tr key={transaction.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">#{transaction.id}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{transaction.user}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">₱{transaction.amount.toFixed(2)}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{transaction.date}</td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <StatusBadge status={transaction.status} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <div className="px-6 py-4 border-t border-gray-200">
          <button className="text-[#6285cf] hover:text-[#445ab1] font-medium">View All Transactions</button>
        </div>
      </div>
    </div>
  );
};

export default AdminStats;
