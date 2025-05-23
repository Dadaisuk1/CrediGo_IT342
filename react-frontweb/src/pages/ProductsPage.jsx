// src/pages/ProductsPage.jsx
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import PurchaseModal from '../components/PurchaseModal';
import WishlistButton from '../components/WishlistButton';
import { useAuth } from '../context/AuthContext';
import { getAvailableProducts, getPlatforms, getWishlist } from '../services/api';

// Update ProductCard to accept onBuyClick prop and add product details link
const ProductCard = ({ product, onBuyClick, isInWishlist, onWishlistChange }) => {
  const navigate = useNavigate();

  const handleCardClick = (e) => {
    // Prevent navigation if the click was on the Buy button or wishlist button
    if (e.target.tagName.toLowerCase() === 'button' || e.target.closest('button')) {
      return;
    }
    navigate(`/products/${product.id}`);
  };

  return (
    <div
      className="border border-gray-700 rounded-lg overflow-hidden shadow-lg bg-credigo-input-bg hover:shadow-xl transition-shadow duration-200 ease-in-out flex flex-col cursor-pointer relative"
      onClick={handleCardClick}
    >
      {product.imageUrl ? (
        <img src={product.imageUrl} alt={product.name} className="w-full h-32 sm:h-40 object-cover" />
      ) : (
        <div className="w-full h-32 sm:h-40 bg-credigo-dark flex items-center justify-center text-gray-500">No Image</div>
      )}

      {/* Wishlist button positioned absolutely in the corner */}
      <div className="absolute top-2 right-2" onClick={e => e.stopPropagation()}>
        <WishlistButton
          productId={product.id}
          isInWishlist={isInWishlist}
          onWishlistChange={(newStatus) => onWishlistChange(product.id, newStatus)}
        />
      </div>

      <div className="p-4 flex flex-col flex-grow">
        <h3 className="font-semibold text-lg text-credigo-light mb-1 truncate flex-grow">{product.name}</h3>
        <p className="text-sm text-gray-400 mb-2">Game: {product.platformName || 'Unknown'}</p>
        <div className="flex justify-between items-center mt-auto pt-2">
          <span className="text-xl font-bold text-credigo-light">
            ₱{product.price ? parseFloat(product.price).toFixed(2) : '0.00'}
          </span>
          {/* Buy button with onClick propagation stopped */}
          <button
            onClick={(e) => {
              e.stopPropagation(); // Prevent card click handler
              onBuyClick(product);
            }}
            className="px-3 py-1 bg-credigo-button text-credigo-dark rounded-md hover:bg-opacity-90 text-sm font-semibold transition duration-150"
          >
            Buy
          </button>
        </div>
      </div>
    </div>
  );
};

function ProductsPage() {
  const [products, setProducts] = useState([]);
  const [platforms, setPlatforms] = useState([]);
  const [selectedPlatformId, setSelectedPlatformId] = useState(null);
  const [loadingProducts, setLoadingProducts] = useState(true);
  const [loadingPlatforms, setLoadingPlatforms] = useState(true);
  const [error, setError] = useState(null);

  // Wishlist state
  const [wishlistItems, setWishlistItems] = useState([]);
  const [loadingWishlist, setLoadingWishlist] = useState(true);

  // Modal State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [productToPurchase, setProductToPurchase] = useState(null);
  const [purchaseStatusMessage, setPurchaseStatusMessage] = useState('');
  const { fetchWalletBalance, isAuthenticated } = useAuth();

  // Fetch user's wishlist if authenticated
  useEffect(() => {
    const fetchWishlistItems = async () => {
      if (!isAuthenticated) {
        setWishlistItems([]);
        setLoadingWishlist(false);
        return;
      }

      setLoadingWishlist(true);
      try {
        const response = await getWishlist();
        setWishlistItems(response.data || []);
      } catch (err) {
        console.error('Error fetching wishlist:', err);
        // Don't show error to user for wishlist, just log it
      } finally {
        setLoadingWishlist(false);
      }
    };

    fetchWishlistItems();
  }, [isAuthenticated]);

  // Fetch platforms
  useEffect(() => {
    const fetchPlatforms = async () => {
      setLoadingPlatforms(true);
      try {
        const response = await getPlatforms();
        setPlatforms(response.data || []);
      } catch (err) {
        setError("Could not load game list.");
      } finally {
        setLoadingPlatforms(false);
      }
    };
    fetchPlatforms();
  }, []);

  // Fetch products based on filter
  useEffect(() => {
    const fetchProducts = async () => {
      setLoadingProducts(true);
      setError(null); // Clear previous product errors
      setPurchaseStatusMessage(''); // Clear purchase status on filter change
      try {
        const response = await getAvailableProducts(selectedPlatformId);
        setProducts(response.data || []);
      } catch (err) {
        setError("Could not load products. Please try again later.");
        setProducts([]);
      } finally {
        setLoadingProducts(false);
      }
    };
    fetchProducts();
  }, [selectedPlatformId]);

  const handlePlatformFilter = (platformId) => {
    setSelectedPlatformId(platformId);
  };

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
        // Create a wishlist item to match API response format
        const wishlistItem = {
          productId: product.id,
          productName: product.name,
          productDescription: product.description,
          productPrice: product.price,
          productImageUrl: product.imageUrl,
          platformName: product.platformName,
          addedAt: new Date().toISOString()
        };
        setWishlistItems([...wishlistItems, wishlistItem]);
      }
    } else {
      // Product was removed from wishlist
      setWishlistItems(wishlistItems.filter(item => item.productId !== productId));
    }
  };

  // Modal Handlers
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
    setPurchaseStatusMessage(`Successfully purchased ${transactionDetails.productName}! Transaction ID: ${transactionDetails.transactionId}`);
    handleCloseModal();
    fetchWalletBalance();
    setTimeout(() => setPurchaseStatusMessage(''), 5000);
  };

  const handlePurchaseError = (errorMessage) => {
    setPurchaseStatusMessage(`Purchase failed: ${errorMessage}`);
    handleCloseModal();
    setTimeout(() => setPurchaseStatusMessage(''), 5000);
  };

  return (
    <div className="font-sans text-credigo-light p-4 md:p-6">
      <h1 className="text-3xl text-credigo-dark font-bold mb-6 text-center md:text-left">Browse Top-ups</h1>

      {/* Display Purchase Status Message */}
      {purchaseStatusMessage && (
        <div className={`p-3 mb-4 text-sm rounded-lg border ${purchaseStatusMessage.startsWith('Successfully') ? 'bg-green-500/20 border-green-700 text-credigo-dark' : 'bg-red-900/50 border-red-700 text-red-400'}`} role="alert">
          {purchaseStatusMessage}
        </div>
      )}

      {/* Platform Filters */}
      <div className="mb-6">
        <h2 className="text-lg font-semibold mb-3 text-credigo-dark/80">Filter by Game:</h2>
        {loadingPlatforms ? (
          <p className="text-gray-400">Loading games...</p>
        ) : (
          <div className="flex flex-wrap gap-2">
            <button
              onClick={() => handlePlatformFilter(null)}
              className={`px-4 py-1.5 rounded-full text-sm font-medium transition duration-150 ${selectedPlatformId === null ? 'bg-credigo-button text-credigo-dark' : 'bg-credigo-input-bg border border-gray-600 text-gray-300 hover:bg-gray-700'}`}
            >
              All Games
            </button>
            {platforms.map((platform) => (
              <button
                key={platform.id}
                onClick={() => handlePlatformFilter(platform.id)}
                className={`px-4 py-1.5 rounded-full text-sm font-medium transition duration-150 ${selectedPlatformId === platform.id ? 'bg-credigo-button text-credigo-dark' : 'bg-credigo-input-bg border border-gray-600 text-gray-300 hover:bg-gray-700'}`}
              >
                {platform.name}
              </button>
            ))}
          </div>
        )}
      </div>

      {/* Loading/Error/Product Grid */}
      {loadingProducts || loadingWishlist ? (
        <div className="text-center py-10"><p>Loading products...</p></div>
      ) : error ? (
        <div className="p-4 text-center text-red-400 bg-red-900/50 rounded-lg border border-red-700" role="alert">
          <span className="font-medium">Error:</span> {error}
        </div>
      ) : products.length > 0 ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 md:gap-6">
          {products.map((product) => (
            <ProductCard
              key={product.id}
              product={product}
              onBuyClick={handleOpenPurchaseModal}
              isInWishlist={isProductInWishlist(product.id)}
              onWishlistChange={handleWishlistChange}
            />
          ))}
        </div>
      ) : (
        <div className="text-center py-10 text-gray-500">
          <p>No products found matching your criteria.</p>
        </div>
      )}

      {/* Render PurchaseModal */}
      {isModalOpen && productToPurchase && (
        <PurchaseModal
          product={productToPurchase}
          onClose={handleCloseModal}
          onPurchaseSuccess={handlePurchaseSuccess}
          onPurchaseError={handlePurchaseError}
        />
      )}
    </div>
  );
}

export default ProductsPage;
