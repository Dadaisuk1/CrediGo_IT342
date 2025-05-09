import { useToast } from '@/hooks/use-toast';
import jwtDecode from 'jwt-decode';
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

function HomePage() {
  const { user, token } = useAuth();
  const [isLoading, setIsLoading] = useState(false);
  const { toast } = useToast();

  // Show welcome toast when user lands on home page
  useEffect(() => {
    // Show a welcome toast when the component mounts
    toast({
      title: `Welcome back, ${user?.username || 'User'}!`,
      description: "You've successfully logged in to CrediGo.",
    });
  }, [toast, user?.username]);

  // Demo data - in a real app, this would come from an API call
  const userStats = {
    balance: 2450.75,
    pendingTransactions: 3,
    wishlistItems: 5,
    lastLogin: '2 hours ago',
    creditScore: 785
  };

  // Quick links for the user
  const quickLinks = [
    { name: 'Top Up', icon: 'wallet', path: '/home/wallet', color: 'bg-green-100 text-green-600' },
    { name: 'Products', icon: 'shopping-bag', path: '/home/products', color:  'bg-blue-100 text-blue-600' },
    { name: 'History', icon: 'clock', path: '/home/history', color: 'bg-purple-100 text-purple-600' },
    { name: 'Wishlist', icon: 'heart', path: '/home/wishlist', color: 'bg-pink-100 text-pink-600' },
  ];

  // Recent transactions - demo data
  const recentTransactions = [
    { id: 1, type: 'purchase', amount: 120.50, date: '2023-06-15', status: 'completed', description: 'Electronics Store' },
    { id: 2, type: 'topup', amount: 500, date: '2023-06-10', status: 'completed', description: 'Account Deposit' },
    { id: 3, type: 'purchase', amount: 65.25, date: '2023-06-05', status: 'pending', description: 'Online Marketplace' },
  ];

  // Recommendations - demo data
  const recommendations = [
    { id: 1, title: 'Premium Headphones', price: 199.99, discount: '15%', image: 'https://placehold.co/100x100/e6f7ff/0099ff?text=Headphones' },
    { id: 2, title: 'Smart Watch', price: 249.50, discount: '10%', image: 'https://placehold.co/100x100/e6f7ff/0099ff?text=Watch' },
  ];

  let roles = [];
  if (token) {
    try {
      const decoded = jwtDecode(token);
      if (decoded.roles) {
        if (Array.isArray(decoded.roles)) {
          roles = decoded.roles.map(role =>
            typeof role === 'string'
              ? role
              : (role.authority || JSON.stringify(role))
          );
        } else {
          roles = [typeof decoded.roles === 'string' ? decoded.roles : JSON.stringify(decoded.roles)];
        }
      }
    } catch (e) {
      roles = ['(unable to decode roles)'];
    }
  }

  // Function to render icons
  const renderIcon = (iconName) => {
    switch (iconName) {
      case 'wallet':
        return (
          <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
          </svg>
        );
      case 'shopping-bag':
        return (
          <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
          </svg>
        );
      case 'clock':
        return (
          <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        );
      case 'heart':
        return (
          <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
          </svg>
        );
      default:
        return null;
    }
  };

  return (
    <div className="p-6">
      {/* Welcome Header */}
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-8">
        <div>
          <h1 className="text-3xl font-bold text-gray-800">Welcome back, <span className="capitalize">{user?.username || 'User'}</span>!</h1>
          <p className="text-gray-500 mt-1">{new Date().toLocaleDateString('en-US', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}</p>
        </div>
        {roles.includes('ROLE_ADMIN') && (
          <Link to="/admin" className="mt-4 md:mt-0 btn-secondary flex items-center space-x-2">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-6-3a2 2 0 11-4 0 2 2 0 014 0zm-2 4a5 5 0 00-4.546 2.916A5.986 5.986 0 005 10a6 6 0 0012 0c0-.35-.035-.691-.1-1.021A5 5 0 0010 11z" clipRule="evenodd" />
            </svg>
            <span>Admin Dashboard</span>
          </Link>
        )}
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <div className="card bg-gradient-to-br from-blue-50 to-blue-100 border-l-4 border-blue-500">
          <div className="flex justify-between">
            <div>
              <p className="text-sm font-medium text-blue-600">Current Balance</p>
              <p className="text-2xl font-bold text-gray-800">${userStats.balance.toFixed(2)}</p>
            </div>
            <div className="h-12 w-12 bg-blue-100 rounded-full flex items-center justify-center">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
          </div>
          <div className="mt-4">
            <Link to="/home/wallet" className="text-sm text-blue-600 font-medium hover:underline">Add funds &rarr;</Link>
          </div>
        </div>

        <div className="card bg-gradient-to-br from-green-50 to-green-100 border-l-4 border-green-500">
          <div className="flex justify-between">
            <div>
              <p className="text-sm font-medium text-green-600">Credit Score</p>
              <p className="text-2xl font-bold text-gray-800">{userStats.creditScore}</p>
            </div>
            <div className="h-12 w-12 bg-green-100 rounded-full flex items-center justify-center">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
              </svg>
            </div>
          </div>
          <div className="mt-4">
            <span className="text-sm text-green-600 font-medium">Excellent</span>
          </div>
        </div>

        <div className="card bg-gradient-to-br from-purple-50 to-purple-100 border-l-4 border-purple-500">
          <div className="flex justify-between">
            <div>
              <p className="text-sm font-medium text-purple-600">Pending Transactions</p>
              <p className="text-2xl font-bold text-gray-800">{userStats.pendingTransactions}</p>
            </div>
            <div className="h-12 w-12 bg-purple-100 rounded-full flex items-center justify-center">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 text-purple-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
          </div>
          <div className="mt-4">
            <Link to="/home/history" className="text-sm text-purple-600 font-medium hover:underline">View details &rarr;</Link>
          </div>
        </div>

        <div className="card bg-gradient-to-br from-pink-50 to-pink-100 border-l-4 border-pink-500">
          <div className="flex justify-between">
            <div>
              <p className="text-sm font-medium text-pink-600">Items in Wishlist</p>
              <p className="text-2xl font-bold text-gray-800">{userStats.wishlistItems}</p>
            </div>
            <div className="h-12 w-12 bg-pink-100 rounded-full flex items-center justify-center">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 text-pink-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
              </svg>
            </div>
          </div>
          <div className="mt-4">
            <Link to="/home/wishlist" className="text-sm text-pink-600 font-medium hover:underline">View wishlist &rarr;</Link>
          </div>
        </div>
      </div>

      {/* Quick Links */}
      <div className="mb-8">
        <h2 className="text-xl font-semibold text-gray-800 mb-4">Quick Actions</h2>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          {quickLinks.map((link, index) => (
            <Link key={index} to={link.path} className="card hover:shadow-md flex items-center space-x-3 transition-all">
              <div className={`w-10 h-10 rounded-full flex items-center justify-center ${link.color}`}>
                {renderIcon(link.icon)}
              </div>
              <span className="font-medium text-gray-700">{link.name}</span>
            </Link>
          ))}
        </div>
      </div>

      {/* Recent Activity & Recommendations */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Recent Transactions */}
        <div className="lg:col-span-2">
          <h2 className="text-xl font-semibold text-gray-800 mb-4">Recent Transactions</h2>
          <div className="card overflow-hidden">
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Description</th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Amount</th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date</th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {recentTransactions.map((transaction) => (
                    <tr key={transaction.id}>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm font-medium text-gray-900">{transaction.description}</div>
                        <div className="text-sm text-gray-500">{transaction.type === 'purchase' ? 'Purchase' : 'Top Up'}</div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className={`text-sm font-medium ${transaction.type === 'purchase' ? 'text-red-600' : 'text-green-600'}`}>
                          {transaction.type === 'purchase' ? '-' : '+'} ${transaction.amount.toFixed(2)}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {new Date(transaction.date).toLocaleDateString()}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                          transaction.status === 'completed' ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'
                        }`}>
                          {transaction.status}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <div className="py-3 px-6 bg-gray-50 text-right">
              <Link to="/home/history" className="text-sm text-blue-600 font-medium hover:underline">View all transactions &rarr;</Link>
            </div>
          </div>
        </div>

        {/* Recommendations */}
        <div>
          <h2 className="text-xl font-semibold text-gray-800 mb-4">Recommended for You</h2>
          <div className="space-y-4">
            {recommendations.map((item) => (
              <div key={item.id} className="card flex">
                <img src={item.image} alt={item.title} className="w-16 h-16 object-cover rounded" />
                <div className="ml-4">
                  <h3 className="text-sm font-medium text-gray-900">{item.title}</h3>
                  <p className="mt-1 text-sm text-gray-500">
                    <span className="text-gray-900 font-medium">${item.price.toFixed(2)}</span>
                    {item.discount && <span className="ml-2 bg-green-100 text-green-800 text-xs px-2 py-0.5 rounded">{item.discount} OFF</span>}
                  </p>
                  <Link to={`/home/products`} className="mt-2 text-xs text-blue-600 font-medium hover:underline">View details</Link>
                </div>
              </div>
            ))}
          </div>
          <div className="mt-4">
            <Link to="/home/products" className="btn-primary w-full text-center py-2 justify-center flex items-center">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-1" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z" clipRule="evenodd" />
              </svg>
              Explore All Products
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}

export default HomePage;
