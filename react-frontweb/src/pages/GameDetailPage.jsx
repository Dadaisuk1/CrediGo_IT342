import { motion } from 'framer-motion';
import { ArrowLeft, Clock, PlusCircle, Shield, Star } from 'lucide-react';
import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import PurchaseModal from '../components/PurchaseModal';
import WishlistButton from '../components/WishlistButton';
import { useAuth } from '../context/AuthContext';
import { getAvailableProducts, getPlatforms } from '../services/api';

// Component for a single game currency package
const GamePointCard = ({ product, onBuyClick, isInWishlist, onWishlistChange }) => {
  return (
    <motion.div
      whileHover={{ y: -5, transition: { duration: 0.2 } }}
      className="bg-gray-900 border border-gray-800 rounded-xl overflow-hidden hover:border-indigo-400/30 hover:shadow-lg hover:shadow-indigo-900/20 transition-all"
    >
      <div className="relative">
        {product.imageUrl ? (
          <img
            src={product.imageUrl}
            alt={product.name}
            className="w-full h-32 sm:h-40 object-cover"
          />
        ) : (
          <div className="w-full h-32 sm:h-40 bg-gradient-to-r from-indigo-900/50 to-purple-900/50 flex items-center justify-center">
            <span className="text-xl font-bold text-white">{product.name}</span>
          </div>
        )}

        {/* Wishlist button positioned absolutely in the corner */}
        <div className="absolute top-2 right-2">
          <WishlistButton
            productId={product.id}
            isInWishlist={isInWishlist}
            onWishlistChange={(newStatus) => onWishlistChange(product.id, newStatus)}
          />
        </div>

        {/* Sale badge if applicable */}
        {product.onSale && (
          <div className="absolute top-2 left-2 bg-red-500 text-white text-xs font-bold px-2 py-1 rounded-md">
            SALE
          </div>
        )}
      </div>

      <div className="p-4">
        <h3 className="font-semibold text-lg text-white mb-1">{product.name}</h3>
        <p className="text-sm text-gray-400 mb-2">{product.description}</p>

        {/* Benefits/features */}
        <ul className="mb-4 space-y-1">
          {product.features?.map((feature, index) => (
            <li key={index} className="text-xs text-gray-300 flex items-center">
              <span className="text-green-400 mr-1">✓</span> {feature}
            </li>
          )) || (
            <>
              <li className="text-xs text-gray-300 flex items-center">
                <span className="text-green-400 mr-1">✓</span> Instant delivery
              </li>
              <li className="text-xs text-gray-300 flex items-center">
                <span className="text-green-400 mr-1">✓</span> Secure transaction
              </li>
              {product.bonusPoints && (
                <li className="text-xs text-gray-300 flex items-center">
                  <span className="text-green-400 mr-1">✓</span> +{product.bonusPoints} bonus points
                </li>
              )}
            </>
          )}
        </ul>

        <div className="flex justify-between items-center mb-4">
          <div className="flex items-center">
            <div className="flex items-center mr-2">
              <Star className="h-3.5 w-3.5 text-yellow-500 mr-1" />
              <span className="text-xs text-gray-300">{product.rating || '4.8'}</span>
            </div>
            <div className="flex items-center">
              <Shield className="h-3.5 w-3.5 text-green-500 mr-1" />
              <span className="text-xs text-gray-300">Safe</span>
            </div>
          </div>
          <div className="flex items-center">
            <Clock className="h-3.5 w-3.5 text-blue-400 mr-1" />
            <span className="text-xs text-gray-300">Instant</span>
          </div>
        </div>

        <div className="flex justify-between items-center">
          <span className="text-xl font-bold text-white">
            ${product.price ? parseFloat(product.price).toFixed(2) : '0.00'}
          </span>
          <motion.button
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
            onClick={() => onBuyClick(product)}
            className="px-4 py-2 bg-indigo-700 hover:bg-indigo-600 text-white rounded-md transition-all font-medium text-sm flex items-center"
          >
            <PlusCircle className="h-4 w-4 mr-1" /> Buy Now
          </motion.button>
        </div>
      </div>
    </motion.div>
  );
};

// Skeleton loader for game point cards
const SkeletonCard = () => (
  <div className="bg-gray-900/50 border border-gray-800 rounded-xl overflow-hidden animate-pulse">
    <div className="h-40 bg-gray-800"></div>
    <div className="p-4">
      <div className="h-5 bg-gray-800 rounded w-3/4 mb-2"></div>
      <div className="h-4 bg-gray-800 rounded w-1/2 mb-4"></div>
      <div className="space-y-2 mb-4">
        <div className="h-3 bg-gray-800 rounded w-full"></div>
        <div className="h-3 bg-gray-800 rounded w-5/6"></div>
        <div className="h-3 bg-gray-800 rounded w-4/6"></div>
      </div>
      <div className="flex justify-between items-center mb-4">
        <div className="h-4 bg-gray-800 rounded w-16"></div>
        <div className="h-4 bg-gray-800 rounded w-16"></div>
      </div>
      <div className="flex justify-between items-center">
        <div className="h-6 bg-gray-800 rounded w-14"></div>
        <div className="h-8 bg-gray-800 rounded w-24"></div>
      </div>
    </div>
  </div>
);

function GameDetailPage() {
  const { gameId } = useParams();
  const { isAuthenticated, fetchWalletBalance } = useAuth();

  const [game, setGame] = useState(null);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Wishlist functionality
  const [wishlistItems, setWishlistItems] = useState([]);

  // Purchase modal states
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [productToPurchase, setProductToPurchase] = useState(null);
  const [purchaseStatusMessage, setPurchaseStatusMessage] = useState('');

  // Additional state for UI features
  const [sortOption, setSortOption] = useState('price-low-high');
  const [filterOption, setFilterOption] = useState('all');

  useEffect(() => {
    const fetchGameAndProducts = async () => {
      setLoading(true);
      try {
        // Fetch game details
        const platformsResponse = await getPlatforms();
        const gameData = platformsResponse.data.find(platform => platform.id === parseInt(gameId));

        if (!gameData) {
          throw new Error('Game not found');
        }

        setGame(gameData);

        // Fetch products for this game
        const productsResponse = await getAvailableProducts(gameId);

        // Add some example features and bonus information for UI demonstration
        const enhancedProducts = productsResponse.data.map(product => ({
          ...product,
          bonusPoints: Math.random() > 0.5 ? Math.floor(Math.random() * 100) : null,
          rating: (4 + Math.random()).toFixed(1),
          onSale: Math.random() > 0.7
        }));

        setProducts(enhancedProducts);
      } catch (err) {
        console.error('Error fetching game data:', err);
        setError(err.message || 'Failed to load game data. Please try again later.');
      } finally {
        setLoading(false);
      }
    };

    fetchGameAndProducts();
  }, [gameId]);

  // Check if a product is in wishlist
  const isProductInWishlist = (productId) => {
    return wishlistItems.some(item => item.productId === productId);
  };

  // Handle wishlist status change
  const handleWishlistChange = (productId, newStatus) => {
    if (newStatus) {
      // Product was added to wishlist
      const product = products.find(p => p.id === productId);
      if (product) {
        const wishlistItem = {
          productId: product.id,
          productName: product.name,
          productDescription: product.description,
          productPrice: product.price,
          productImageUrl: product.imageUrl,
          platformName: game?.name,
          addedAt: new Date().toISOString()
        };
        setWishlistItems([...wishlistItems, wishlistItem]);
      }
    } else {
      // Product was removed from wishlist
      setWishlistItems(wishlistItems.filter(item => item.productId !== productId));
    }
  };

  // Purchase modal handlers
  const handleOpenPurchaseModal = (product) => {
    setProductToPurchase(product);
    setIsModalOpen(true);
    setPurchaseStatusMessage('');
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setProductToPurchase(null);
  };

  const handlePurchaseSuccess = (transactionDetails) => {
    setPurchaseStatusMessage(`Successfully purchased ${transactionDetails.productName}!`);
    handleCloseModal();
    fetchWalletBalance();
    setTimeout(() => setPurchaseStatusMessage(''), 5000);
  };

  const handlePurchaseError = (errorMessage) => {
    setPurchaseStatusMessage(`Purchase failed: ${errorMessage}`);
    handleCloseModal();
    setTimeout(() => setPurchaseStatusMessage(''), 5000);
  };

  // Sort products based on selected option
  const sortProducts = (productsToSort) => {
    switch (sortOption) {
      case 'price-low-high':
        return [...productsToSort].sort((a, b) => parseFloat(a.price) - parseFloat(b.price));
      case 'price-high-low':
        return [...productsToSort].sort((a, b) => parseFloat(b.price) - parseFloat(a.price));
      case 'name-a-z':
        return [...productsToSort].sort((a, b) => a.name.localeCompare(b.name));
      case 'name-z-a':
        return [...productsToSort].sort((a, b) => b.name.localeCompare(a.name));
      default:
        return productsToSort;
    }
  };

  // Filter products based on selected option
  const filterProducts = (productsToFilter) => {
    switch (filterOption) {
      case 'on-sale':
        return productsToFilter.filter(product => product.onSale);
      case 'with-bonus':
        return productsToFilter.filter(product => product.bonusPoints);
      case 'all':
      default:
        return productsToFilter;
    }
  };

  // Process products with sorting and filtering
  const processedProducts = sortProducts(filterProducts(products));

  // Loading state
  if (loading) {
    return (
      <div className="min-h-screen bg-credigo-dark text-slate-200 pb-12">
        <div className="container mx-auto px-4 py-8">
          <div className="animate-pulse mb-8">
            <div className="h-8 bg-gray-800 rounded w-1/3 mb-2"></div>
            <div className="h-4 bg-gray-800 rounded w-1/2"></div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {Array.from({ length: 8 }).map((_, index) => (
              <SkeletonCard key={index} />
            ))}
          </div>
        </div>
      </div>
    );
  }

  // Error state
  if (error || !game) {
    return (
      <div className="min-h-screen bg-credigo-dark text-slate-200 pb-12">
        <div className="container mx-auto px-4 py-8">
          <div className="bg-red-900/20 border border-red-800 text-red-200 p-6 rounded-lg flex flex-col items-center justify-center">
            <h2 className="text-xl font-bold mb-2">Error Loading Game</h2>
            <p className="mb-4">{error || 'Game not found'}</p>
            <Link
              to="/games"
              className="px-4 py-2 bg-indigo-700 hover:bg-indigo-600 rounded-md text-white transition-colors"
            >
              Return to Game Catalog
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-credigo-dark text-slate-200 pb-12">
      <div className="container mx-auto px-4 py-8">
        {/* Back button */}
        <Link
          to="/games"
          className="inline-flex items-center text-gray-400 hover:text-white transition-colors mb-6"
        >
          <ArrowLeft className="h-4 w-4 mr-1" /> Back to Games
        </Link>

        {/* Game header */}
        <div className="mb-8">
          <div className="flex flex-col lg:flex-row items-start lg:items-center gap-4">
            <div className="w-16 h-16 bg-gray-800 rounded-lg overflow-hidden flex-shrink-0">
              <img
                src={game.imageUrl || `https://placehold.co/64/${game.color || '4a5568'}/ffffff?text=${encodeURIComponent(game.name.charAt(0))}`}
                alt={game.name}
                className="w-full h-full object-cover"
              />
            </div>

            <div>
              <h1 className="text-3xl font-bold text-white">{game.name}</h1>
              <p className="text-gray-400">{game.description || 'Purchase game points and currency'}</p>
            </div>
          </div>
        </div>

        {/* Purchase status message */}
        {purchaseStatusMessage && (
          <div className={`p-4 mb-6 rounded-lg ${
            purchaseStatusMessage.startsWith('Successfully')
              ? 'bg-green-900/20 border border-green-700 text-green-300'
              : 'bg-red-900/20 border border-red-700 text-red-300'
          }`}>
            <p>{purchaseStatusMessage}</p>
          </div>
        )}

        {/* Filter and sort options */}
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-6 gap-4">
          <div className="flex items-center">
            <span className="text-gray-400 mr-2">Filter:</span>
            <select
              value={filterOption}
              onChange={(e) => setFilterOption(e.target.value)}
              className="bg-gray-800 border border-gray-700 text-gray-200 rounded-md px-3 py-1.5 focus:outline-none focus:ring-2 focus:ring-indigo-500"
            >
              <option value="all">All Items</option>
              <option value="on-sale">On Sale</option>
              <option value="with-bonus">With Bonus</option>
            </select>
          </div>

          <div className="flex items-center">
            <span className="text-gray-400 mr-2">Sort by:</span>
            <select
              value={sortOption}
              onChange={(e) => setSortOption(e.target.value)}
              className="bg-gray-800 border border-gray-700 text-gray-200 rounded-md px-3 py-1.5 focus:outline-none focus:ring-2 focus:ring-indigo-500"
            >
              <option value="price-low-high">Price: Low to High</option>
              <option value="price-high-low">Price: High to Low</option>
              <option value="name-a-z">Name: A to Z</option>
              <option value="name-z-a">Name: Z to A</option>
            </select>
          </div>
        </div>

        {/* Products grid */}
        {processedProducts.length === 0 ? (
          <div className="bg-gray-900/30 border border-gray-800 rounded-lg p-8 text-center">
            <h3 className="text-xl font-semibold text-gray-300 mb-2">No Products Available</h3>
            <p className="text-gray-400 mb-4">There are currently no items available for this game.</p>
            <Link
              to="/games"
              className="px-4 py-2 bg-indigo-700 hover:bg-indigo-600 rounded-md text-white transition-colors inline-block"
            >
              Browse Other Games
            </Link>
          </div>
        ) : (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ duration: 0.5 }}
            className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6"
          >
            {processedProducts.map((product) => (
              <GamePointCard
                key={product.id}
                product={product}
                onBuyClick={handleOpenPurchaseModal}
                isInWishlist={isProductInWishlist(product.id)}
                onWishlistChange={handleWishlistChange}
              />
            ))}
          </motion.div>
        )}

        {/* Purchase modal */}
        {isModalOpen && productToPurchase && (
          <PurchaseModal
            isOpen={isModalOpen}
            onClose={handleCloseModal}
            product={productToPurchase}
            onPurchaseSuccess={handlePurchaseSuccess}
            onPurchaseError={handlePurchaseError}
          />
        )}
      </div>
    </div>
  );
}

export default GameDetailPage;
