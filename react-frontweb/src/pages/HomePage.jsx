import { useToast } from '@/hooks/use-toast';
import { motion } from 'framer-motion';
import jwtDecode from 'jwt-decode';
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

function HomePage() {
  const { user, token, walletBalance, walletError } = useAuth();
  const [isLoading, setIsLoading] = useState(false);
  const { toast } = useToast();

  // Show welcome toast when user lands on home page
  useEffect(() => {
    toast({
      title: `Welcome back, ${user?.username || 'User'}!`,
      description: "You've successfully logged in to CrediGo.",
    });
  }, [toast, user?.username]);

  // Featured games data
  const featuredGames = [
    {
      id: 1,
      title: 'PUBG Mobile',
      image: '/src/assets/images/pubg.jpg',
      description: 'Top up your UC directly to your PUBG Mobile account',
      points: 'UC',
      prices: ['60 UC - $0.99', '300 UC - $4.99', '600 UC - $9.99', '1500 UC - $24.99']
    },
    {
      id: 2,
      title: 'Valorant',
      image: '/src/assets/images/neon.webp',
      description: 'Purchase Valorant Points to buy skins and battle passes',
      points: 'VP',
      prices: ['525 VP - $4.99', '1050 VP - $9.99', '2100 VP - $19.99', '3650 VP - $34.99']
    },
    {
      id: 3,
      title: 'Mobile Legends',
      image: '/src/assets/images/mlbb.png',
      description: 'Get Diamonds for your favorite heroes and skins',
      points: 'Diamonds',
      prices: ['100 Diamonds - $2.99', '250 Diamonds - $6.99', '500 Diamonds - $12.99', '1000 Diamonds - $24.99']
    }
  ];

  // Recent/trending games data
  const trendingGames = [
    { id: 4, title: 'Call of Duty Mobile', image: '/src/assets/images/codm.jpg', discount: '10% OFF' },
    { id: 5, title: 'Fortnite', image: '/src/assets/images/certificate.jpg', discount: '15% OFF' },
    { id: 6, title: 'Apex Legends', image: '/src/assets/images/apex.webp', discount: '5% OFF' },
    { id: 7, title: 'Genshin Impact', image: '/src/assets/images/genshin.svg', new: true },
    { id: 8, title: 'League of Legends', image: '/src/assets/images/Water', new: true }
  ];

  // Gaming news data
  const gamingNews = [
    {
      id: 1,
      title: 'PUBG Mobile Season 22 Released',
      date: '2 hours ago',
      image: '/src/assets/images/pubg_20.jpg',
      excerpt: 'New battle pass, skins, and game modes now available'
    },
    {
      id: 2,
      title: 'Valorant Introduces New Agent',
      date: '1 day ago',
      image: '/src/assets/images/neon.webp',
      excerpt: 'Meet the latest addition to the Valorant roster with unique abilities'
    },
    {
      id: 3,
      title: 'Mobile Legends Championship Finals',
      date: '3 days ago',
      image: '/src/assets/images/mlbb_chamption.jpg',
      excerpt: 'Watch the top teams compete for the grand prize'
    }
  ];

  // Recent transactions - for user stats
  const recentTransactions = [
    { id: 1, type: 'purchase', game: 'PUBG Mobile', amount: 120.50, date: '2023-06-15', status: 'completed', points: '600 UC' },
    { id: 2, type: 'topup', amount: 500, date: '2023-06-10', status: 'completed', description: 'Wallet Deposit' },
    { id: 3, type: 'purchase', game: 'Valorant', amount: 65.25, date: '2023-06-05', status: 'pending', points: '1050 VP' },
  ];

  // User account stats
  const userStats = {
    balance: walletBalance !== null ? walletBalance : 0,
    pendingTransactions: 3,
    wishlistItems: 5,
    lastLogin: '2 hours ago',
    creditScore: 785
  };

  const hasAdminRole = () => {
    if (token) {
      try {
        const decoded = jwtDecode(token);
        if (decoded.roles) {
          if (Array.isArray(decoded.roles)) {
            return decoded.roles.includes('ROLE_ADMIN');
          } else {
            return decoded.roles === 'ROLE_ADMIN';
          }
        }
      } catch (e) {
        return false;
      }
    }
    return false;
  };

  // Animation variants
  const containerVariants = {
    hidden: { opacity: 0 },
    visible: {
      opacity: 1,
      transition: {
        staggerChildren: 0.1
      }
    }
  };

  const itemVariants = {
    hidden: { y: 20, opacity: 0 },
    visible: {
      y: 0,
      opacity: 1,
      transition: { duration: 0.5 }
    }
  };

  // Add a component to display when there are API connection issues
  const ApiErrorBanner = () => {
    if (!walletError) return null;

    return (
      <div className="mb-6 p-3 border border-red-900/50 bg-red-900/20 rounded-lg">
        <p className="text-red-300 text-sm flex items-center">
          <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2 text-red-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
          </svg>
          Some services are currently unavailable. Your wallet balance might not be up to date.
        </p>
      </div>
    );
  };

  return (
    <div className="min-h-screen bg-credigo-dark text-slate-200 pb-12">
      {/* Subtle background pattern instead of aggressive blur effects */}
      <div className="fixed inset-0 overflow-hidden pointer-events-none -z-10 opacity-20">
        <div className="absolute -top-20 -right-20 w-64 h-64 bg-indigo-600 rounded-full filter blur-3xl opacity-5"></div>
        <div className="absolute top-40 -left-20 w-72 h-72 bg-blue-500 rounded-full filter blur-3xl opacity-5"></div>
        <div className="absolute bottom-0 right-1/4 w-80 h-80 bg-cyan-400 rounded-full filter blur-3xl opacity-3"></div>
      </div>

      {/* Welcome Header - Darker background */}
      <motion.div
        className="p-6 bg-credigo-dark border-b border-gray-800 mb-8"
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
      >
        <div className="container mx-auto flex flex-col md:flex-row justify-between items-start md:items-center">
          <div>
            <h1 className="text-3xl font-bold text-white">
              Welcome back, <span className="capitalize">{user?.username || 'User'}</span>!
            </h1>
            <p className="text-gray-400 mt-1">{new Date().toLocaleDateString('en-US', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}</p>
          </div>
          {hasAdminRole() && (
            <Link
              to="/admin"
              className="mt-4 md:mt-0 px-4 py-2 bg-gray-800 border border-gray-700 hover:border-indigo-400 rounded-md flex items-center space-x-2 transition-all duration-300"
            >
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-indigo-300" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-6-3a2 2 0 11-4 0 2 2 0 014 0zm-2 4a5 5 0 00-4.546 2.916A5.986 5.986 0 005 10a6 6 0 0012 0c0-.35-.035-.691-.1-1.021A5 5 0 0010 11z" clipRule="evenodd" />
              </svg>
              <span className="text-gray-300">Admin Dashboard</span>
            </Link>
          )}
        </div>
      </motion.div>

      <div className="container mx-auto px-4">
        {/* Display API error banner if needed */}
        <ApiErrorBanner />

        {/* User Stats Cards (darker colors) */}
        <motion.div
          className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-10"
          variants={containerVariants}
          initial="hidden"
          animate="visible"
        >
          <motion.div
            variants={itemVariants}
            className="bg-gray-900 border border-gray-800 rounded-xl p-4 hover:border-blue-400/30 hover:shadow-md hover:shadow-blue-900/20 transition-all"
          >
            <div className="flex justify-between items-center mb-2">
              <p className="text-sm font-medium text-gray-400">Balance</p>
              <div className="h-8 w-8 rounded-full bg-blue-900/30 flex items-center justify-center">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 text-blue-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
            </div>
            <p className="text-xl font-bold text-white">${userStats.balance.toFixed(2)}</p>
            <Link to="/wallet" className="text-xs text-blue-300 hover:text-blue-200 mt-2 inline-block group">
              Add funds <span className="inline-block transition-transform group-hover:translate-x-1">→</span>
            </Link>
          </motion.div>

          <motion.div
            variants={itemVariants}
            className="bg-gray-900 border border-gray-800 rounded-xl p-4 hover:border-green-400/30 hover:shadow-md hover:shadow-green-900/20 transition-all"
          >
            <div className="flex justify-between items-center mb-2">
              <p className="text-sm font-medium text-gray-400">Transactions</p>
              <div className="h-8 w-8 rounded-full bg-green-900/30 flex items-center justify-center">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 text-green-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                </svg>
              </div>
            </div>
            <p className="text-xl font-bold text-white">{userStats.pendingTransactions}</p>
            <Link to="/history" className="text-xs text-green-300 hover:text-green-200 mt-2 inline-block group">
              View history <span className="inline-block transition-transform group-hover:translate-x-1">→</span>
            </Link>
          </motion.div>

          <motion.div
            variants={itemVariants}
            className="bg-gray-900 border border-gray-800 rounded-xl p-4 hover:border-purple-400/30 hover:shadow-md hover:shadow-purple-900/20 transition-all"
          >
            <div className="flex justify-between items-center mb-2">
              <p className="text-sm font-medium text-gray-400">Wishlist</p>
              <div className="h-8 w-8 rounded-full bg-purple-900/30 flex items-center justify-center">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 text-purple-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                </svg>
              </div>
            </div>
            <p className="text-xl font-bold text-white">{userStats.wishlistItems}</p>
            <Link to="/wishlist" className="text-xs text-purple-300 hover:text-purple-200 mt-2 inline-block group">
              View wishlist <span className="inline-block transition-transform group-hover:translate-x-1">→</span>
            </Link>
          </motion.div>

          <motion.div
            variants={itemVariants}
            className="bg-gray-900 border border-gray-800 rounded-xl p-4 hover:border-amber-400/30 hover:shadow-md hover:shadow-amber-900/20 transition-all"
          >
            <div className="flex justify-between items-center mb-2">
              <p className="text-sm font-medium text-gray-400">Last Login</p>
              <div className="h-8 w-8 rounded-full bg-amber-900/30 flex items-center justify-center">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 text-amber-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
            </div>
            <p className="text-xl font-bold text-white">{userStats.lastLogin}</p>
            <span className="text-xs text-amber-300 mt-2 inline-block flex items-center">
              Active now
              <span className="relative ml-1 h-2 w-2">
                <span className="absolute animate-ping h-2 w-2 rounded-full bg-amber-400 opacity-75"></span>
                <span className="absolute h-2 w-2 rounded-full bg-amber-300"></span>
              </span>
            </span>
          </motion.div>
        </motion.div>

        {/* Featured Games - More muted color scheme with darker backgrounds */}
        <motion.section
          className="mb-12"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.2, duration: 0.5 }}
        >
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-2xl font-bold text-white">Featured Games</h2>
            <Link to="/products" className="text-sm text-indigo-300 hover:text-indigo-200 transition-colors group flex items-center">
              View all games <span className="inline-block transition-transform group-hover:translate-x-1 ml-1">→</span>
            </Link>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {featuredGames.map((game, index) => (
              <motion.div
                key={game.id}
                whileHover={{ y: -5, transition: { duration: 0.2 } }}
                className="bg-gray-900 border border-gray-800 rounded-xl overflow-hidden hover:border-indigo-400/30 hover:shadow-lg hover:shadow-indigo-900/20 transition-all"
              >
                <div className="relative h-48">
                  <img src={game.image} alt={game.title} className="w-full h-full object-cover opacity-90 hover:opacity-100 transition-opacity" />
                  <div className="absolute inset-0 bg-gradient-to-t from-gray-900/80 to-transparent"></div>
                  <div className="absolute bottom-0 left-0 right-0 p-4">
                    <h3 className="text-xl font-bold text-white mb-0">{game.title}</h3>
                  </div>
                </div>
                <div className="p-4 pt-2">
                  <p className="text-gray-400 text-sm mb-4">{game.description}</p>

                  <div className="flex justify-between items-center mb-3">
                    <span className="text-xs font-medium text-gray-500 uppercase">{game.points} Pricing</span>
                    <span className="text-xs bg-indigo-900/50 text-indigo-300 rounded-full px-2 py-1 flex items-center">
                      <svg xmlns="http://www.w3.org/2000/svg" className="h-3 w-3 mr-1" viewBox="0 0 20 20" fill="currentColor">
                        <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                      </svg>
                      Popular
                    </span>
                  </div>

                  <div className="grid grid-cols-2 gap-2 mb-4">
                    {game.prices.map((price, index) => (
                      <div key={index} className="text-xs bg-gray-800 rounded-lg p-2 text-center text-gray-300 hover:bg-gray-700 transition-colors">{price}</div>
                    ))}
                  </div>

                  <Link
                    to={`/products/${game.id}`}
                    className="w-full block text-center py-2.5 bg-indigo-700 hover:bg-indigo-600 text-white rounded-md transition-all font-medium flex items-center justify-center group"
                  >
                    <span>Buy Now</span>
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 ml-2 group-hover:translate-x-1 transition-transform" viewBox="0 0 20 20" fill="currentColor">
                      <path fillRule="evenodd" d="M10.293 5.293a1 1 0 011.414 0l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414-1.414L12.586 11H5a1 1 0 110-2h7.586l-2.293-2.293a1 1 0 010-1.414z" clipRule="evenodd" />
                    </svg>
                  </Link>
                </div>
              </motion.div>
            ))}
          </div>
        </motion.section>

        {/* Trending & News Section with darker backgrounds */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Trending Games */}
          <motion.div
            className="lg:col-span-2"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.3, duration: 0.5 }}
          >
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-2xl font-bold text-white">Trending Now</h2>
              <Link to="/products" className="text-sm text-indigo-300 hover:text-indigo-200 transition-colors group flex items-center">
                See all trending <span className="inline-block transition-transform group-hover:translate-x-1 ml-1">→</span>
              </Link>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
              {trendingGames.map((game) => (
                <motion.div
                  key={game.id}
                  whileHover={{ scale: 1.03, transition: { duration: 0.2 } }}
                  className="relative bg-gray-900 border border-gray-800 rounded-lg overflow-hidden group hover:shadow-md hover:shadow-gray-900/40 transition-all"
                >
                  <div className="absolute inset-0 bg-gradient-to-t from-black/70 to-transparent z-10 opacity-60 group-hover:opacity-50 transition-opacity"></div>
                  <img src={game.image} alt={game.title} className="w-full h-28 object-cover" />

                  {game.discount && (
                    <div className="absolute top-2 right-2 bg-amber-600/90 text-white text-xs font-bold px-2 py-1 rounded-md z-20 flex items-center">
                      <svg xmlns="http://www.w3.org/2000/svg" className="h-3 w-3 mr-1" viewBox="0 0 20 20" fill="currentColor">
                        <path fillRule="evenodd" d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z" clipRule="evenodd" />
                      </svg>
                      {game.discount}
                    </div>
                  )}

                  {game.new && (
                    <div className="absolute top-2 right-2 bg-blue-600/90 text-white text-xs font-bold px-2 py-1 rounded-md z-20 flex items-center">
                      <svg xmlns="http://www.w3.org/2000/svg" className="h-3 w-3 mr-1" viewBox="0 0 20 20" fill="currentColor">
                        <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                      </svg>
                      NEW
                    </div>
                  )}

                  <div className="absolute bottom-0 left-0 right-0 p-3 z-20">
                    <h3 className="text-white font-medium text-sm mb-2">{game.title}</h3>
                    <Link
                      to={`/products/${game.id}`}
                      className="text-xs text-indigo-300 opacity-0 group-hover:opacity-100 transition-opacity inline-flex items-center"
                    >
                      View Details
                      <svg xmlns="http://www.w3.org/2000/svg" className="h-3 w-3 ml-1 group-hover:translate-x-1 transition-transform" viewBox="0 0 20 20" fill="currentColor">
                        <path fillRule="evenodd" d="M10.293 5.293a1 1 0 011.414 0l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414-1.414L12.586 11H5a1 1 0 110-2h7.586l-2.293-2.293a1 1 0 010-1.414z" clipRule="evenodd" />
                      </svg>
                    </Link>
                  </div>
                </motion.div>
              ))}
            </div>
          </motion.div>

          {/* Gaming News */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.4, duration: 0.5 }}
          >
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-2xl font-bold text-white">Gaming News</h2>
              <Link to="/news" className="text-sm text-indigo-300 hover:text-indigo-200 transition-colors group flex items-center">
                All news <span className="inline-block transition-transform group-hover:translate-x-1 ml-1">→</span>
              </Link>
            </div>

            <div className="space-y-4">
              {gamingNews.map((news) => (
                <motion.div
                  key={news.id}
                  whileHover={{ x: 5, transition: { duration: 0.2 } }}
                  className="bg-gray-900 border border-gray-800 rounded-lg p-3 flex gap-3 items-center hover:shadow-md hover:shadow-gray-900/30 transition-all"
                >
                  <img src={news.image} alt={news.title} className="w-20 h-20 rounded-md object-cover flex-shrink-0" />
                  <div>
                    <h3 className="font-medium text-white text-sm">{news.title}</h3>
                    <p className="text-gray-400 text-xs mt-1">{news.excerpt}</p>
                    <div className="flex justify-between items-center mt-2">
                      <span className="text-gray-500 text-xs">{news.date}</span>
                      <Link to={`/news/${news.id}`} className="text-xs text-indigo-300 hover:text-indigo-200 inline-flex items-center group">
                        Read more
                        <svg xmlns="http://www.w3.org/2000/svg" className="h-3 w-3 ml-1 group-hover:translate-x-1 transition-transform" viewBox="0 0 20 20" fill="currentColor">
                          <path fillRule="evenodd" d="M10.293 5.293a1 1 0 011.414 0l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414-1.414L12.586 11H5a1 1 0 110-2h7.586l-2.293-2.293a1 1 0 010-1.414z" clipRule="evenodd" />
                        </svg>
                      </Link>
                    </div>
                  </div>
                </motion.div>
              ))}
            </div>
          </motion.div>
        </div>

        {/* Recent Transactions */}
        <motion.section
          className="mt-12"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.5, duration: 0.5 }}
        >
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-2xl font-bold text-white">Recent Transactions</h2>
            <Link to="/history" className="text-sm text-indigo-300 hover:text-indigo-200 transition-colors group flex items-center">
              View full history <span className="inline-block transition-transform group-hover:translate-x-1 ml-1">→</span>
            </Link>
          </div>

          <div className="bg-gray-900 border border-gray-800 rounded-xl overflow-hidden shadow-md">
            <div className="overflow-x-auto">
              <table className="min-w-full">
                <thead>
                  <tr className="border-b border-gray-800">
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-400 uppercase tracking-wider">Description</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-400 uppercase tracking-wider">Amount</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-400 uppercase tracking-wider">Date</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-400 uppercase tracking-wider">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-800">
                  {recentTransactions.map((transaction) => (
                    <tr key={transaction.id} className="hover:bg-gray-800/30 transition-colors">
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm font-medium text-white">{transaction.game || transaction.description}</div>
                        <div className="text-xs text-gray-400">
                          {transaction.type === 'purchase' ? 'Purchase' : 'Top Up'}
                          {transaction.points && ` · ${transaction.points}`}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className={`text-sm font-medium ${transaction.type === 'purchase' ? 'text-rose-300' : 'text-emerald-300'}`}>
                          {transaction.type === 'purchase' ? '-' : '+'} ${transaction.amount.toFixed(2)}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-400">
                        {new Date(transaction.date).toLocaleDateString()}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`px-2 py-1 inline-flex text-xs leading-5 font-medium rounded-full ${
                          transaction.status === 'completed'
                            ? 'bg-emerald-900/30 text-emerald-300'
                            : 'bg-amber-900/30 text-amber-300'
                        }`}>
                          {transaction.status}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </motion.section>

        {/* Call to Action */}
        <motion.div
          className="mt-12 bg-gradient-to-r from-indigo-950/50 to-gray-900 border border-indigo-900/30 rounded-xl p-6 text-center shadow-md"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.6, duration: 0.5 }}
        >
          <h2 className="text-2xl font-bold text-white mb-2">Ready to Level Up Your Gaming?</h2>
          <p className="text-gray-300 mb-6 max-w-2xl mx-auto">
            Get instant game points and credit for all your favorite games. Fast, secure, and hassle-free.
          </p>
          <div className="flex flex-col sm:flex-row justify-center gap-4">
            <Link
              to="/games"
              className="px-6 py-3 bg-indigo-700 hover:bg-indigo-600 text-white font-medium rounded-md transition-all flex items-center justify-center gap-2 group"
            >
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z" />
              </svg>
              <span>Browse Games</span>
              <svg xmlns="http://www.w3.org/2000/svg" className="h-0 w-0 sm:h-4 sm:w-4 ml-0 sm:ml-1 group-hover:translate-x-1 transition-all" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M10.293 5.293a1 1 0 011.414 0l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414-1.414L12.586 11H5a1 1 0 110-2h7.586l-2.293-2.293a1 1 0 010-1.414z" clipRule="evenodd" />
              </svg>
            </Link>
            <Link
              to="/wallet"
              className="px-6 py-3 bg-gray-800 border border-gray-700 hover:border-indigo-500/50 text-white font-medium rounded-md transition-all flex items-center justify-center gap-2"
            >
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
              </svg>
              <span>Top Up Wallet</span>
            </Link>
          </div>
        </motion.div>
      </div>
    </div>
  );
}

export default HomePage;
