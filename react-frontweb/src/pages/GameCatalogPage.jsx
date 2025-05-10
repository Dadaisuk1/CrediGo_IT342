import { motion } from 'framer-motion';
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getPlatforms } from '../services/api';

const GameCard = ({ game }) => {
  return (
    <motion.div
      whileHover={{ y: -5 }}
      className="bg-gray-900 border border-gray-800 rounded-xl overflow-hidden hover:border-indigo-500/30 hover:shadow-lg hover:shadow-indigo-900/20 transition-all duration-200"
    >
      <Link to={`/games/${game.id}`} className="block h-full">
        <div className="relative h-48">
          <img
            src={game.imageUrl || `https://placehold.co/400x200/${game.color || '4a5568'}/ffffff?text=${encodeURIComponent(game.name)}`}
            alt={game.name}
            className="w-full h-full object-cover opacity-90 hover:opacity-100 transition-opacity"
          />
          <div className="absolute inset-0 bg-gradient-to-t from-gray-900/90 to-transparent"></div>
          <div className="absolute bottom-0 left-0 right-0 p-4">
            <h3 className="text-xl font-bold text-white mb-1">{game.name}</h3>
            <p className="text-sm text-gray-300">{game.description || 'Purchase game points and currency'}</p>
          </div>
        </div>

        <div className="p-4">
          <div className="flex justify-between items-center">
            <span className="text-xs bg-indigo-900/50 text-indigo-300 rounded-full px-2 py-1 flex items-center">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-3 w-3 mr-1" viewBox="0 0 20 20" fill="currentColor">
                <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
              </svg>
              {game.productCount || 0} Options
            </span>

            <motion.div
              whileHover={{ x: 5 }}
              className="text-indigo-400 text-sm flex items-center"
            >
              View Details
              <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 ml-1" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M10.293 5.293a1 1 0 011.414 0l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414-1.414L12.586 11H5a1 1 0 110-2h7.586l-2.293-2.293a1 1 0 010-1.414z" clipRule="evenodd" />
              </svg>
            </motion.div>
          </div>
        </div>
      </Link>
    </motion.div>
  );
};

const SkeletonGameCard = () => (
  <div className="bg-gray-900/50 border border-gray-800 rounded-xl overflow-hidden animate-pulse">
    <div className="h-48 bg-gray-800"></div>
    <div className="p-4">
      <div className="h-5 bg-gray-800 rounded w-3/4 mb-2"></div>
      <div className="h-4 bg-gray-800 rounded w-1/2 mb-4"></div>
      <div className="flex justify-between">
        <div className="h-6 bg-gray-800 rounded w-20"></div>
        <div className="h-6 bg-gray-800 rounded w-24"></div>
      </div>
    </div>
  </div>
);

function GameCatalogPage() {
  const [games, setGames] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    const fetchGames = async () => {
      setLoading(true);
      try {
        // Use the existing platforms endpoint to get our games list
        const response = await getPlatforms();

        // Process the platforms data to include necessary details
        const processedGames = response.data.map(game => ({
          ...game,
          // Assign random color codes for placeholder images if no imageUrl
          color: game.colorCode || ['4F46E5', '7C3AED', 'EC4899', '8B5CF6', 'F59E0B'][Math.floor(Math.random() * 5)]
        }));

        setGames(processedGames);
      } catch (err) {
        console.error('Error fetching games:', err);
        setError('Could not load games. Please try again later.');
      } finally {
        setLoading(false);
      }
    };

    fetchGames();
  }, []);

  // Filter games based on search term
  const filteredGames = games.filter(game =>
    game.name.toLowerCase().includes(searchTerm.toLowerCase())
  );

  // Container animation variants
  const containerVariants = {
    hidden: { opacity: 0 },
    visible: {
      opacity: 1,
      transition: {
        staggerChildren: 0.1
      }
    }
  };

  return (
    <div className="min-h-screen bg-credigo-dark text-slate-200 pb-12">
      <div className="container mx-auto px-4 py-8">
        {/* Header */}
        <motion.div
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
          className="mb-8"
        >
          <h1 className="text-3xl font-bold text-white mb-2">Game Catalog</h1>
          <p className="text-gray-400">Browse our collection of games and purchase in-game currency</p>
        </motion.div>

        {/* Search Bar */}
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.2 }}
          className="mb-8"
        >
          <div className="relative max-w-lg mx-auto">
            <input
              type="text"
              className="w-full bg-gray-800 border border-gray-700 rounded-lg py-3 px-4 pl-10 text-gray-200 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-all duration-200"
              placeholder="Search games..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
            <div className="absolute left-3 top-3 text-gray-400">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z" clipRule="evenodd" />
              </svg>
            </div>
          </div>
        </motion.div>

        {/* Error Message */}
        {error && (
          <div className="bg-red-900/20 border border-red-800 text-red-200 px-4 py-3 rounded-lg mb-8" role="alert">
            <p>{error}</p>
          </div>
        )}

        {/* Game Grid */}
        <motion.div
          variants={containerVariants}
          initial="hidden"
          animate="visible"
          className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6"
        >
          {loading ? (
            // Show skeleton loaders while loading
            Array.from({ length: 8 }).map((_, index) => (
              <SkeletonGameCard key={index} />
            ))
          ) : filteredGames.length > 0 ? (
            // Show filtered games
            filteredGames.map((game) => (
              <GameCard key={game.id} game={game} />
            ))
          ) : (
            // No results found
            <div className="col-span-full text-center py-12">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-16 w-16 mx-auto text-gray-600 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
              <h3 className="text-xl text-gray-400 font-medium">No games found matching "{searchTerm}"</h3>
              <p className="text-gray-500 mt-2">Try a different search term or browse all games</p>
              {searchTerm && (
                <button
                  onClick={() => setSearchTerm('')}
                  className="mt-4 bg-indigo-700 text-white px-4 py-2 rounded-md hover:bg-indigo-600 transition-colors"
                >
                  Clear Search
                </button>
              )}
            </div>
          )}
        </motion.div>
      </div>
    </div>
  );
}

export default GameCatalogPage;
