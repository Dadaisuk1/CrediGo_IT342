// src/pages/WishlistPage.jsx
import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { getWishlist, removeFromWishlist } from '../services/api'; // Import API functions
import { TbCurrencyPeso } from 'react-icons/tb';
import { Trash2 } from 'lucide-react'; // Import trash icon for remove button

function WishlistPage() {
  const [wishlistItems, setWishlistItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [removingItemId, setRemovingItemId] = useState(null); // Track which item is being removed

  // Function to fetch wishlist data
  const fetchWishlist = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await getWishlist();
      setWishlistItems(response.data || []);
      console.log("Fetched wishlist:", response.data);
    } catch (err) {
      console.error("Failed to fetch wishlist:", err);
      setError("Could not load your wishlist. Please try again later.");
      setWishlistItems([]);
    } finally {
      setLoading(false);
    }
  }, []);

  // Fetch wishlist on component mount
  useEffect(() => {
    fetchWishlist();
  }, [fetchWishlist]);

  // Function to handle removing an item
  const handleRemoveItem = async (productId) => {
    if (removingItemId === productId) return; // Prevent double clicks
    setRemovingItemId(productId); // Set loading state for this specific item
    setError(null); // Clear previous errors

    try {
      await removeFromWishlist(productId);
      console.log(`Successfully removed product ID: ${productId} from wishlist.`);
      // Refresh the wishlist after successful removal
      // Option 1: Refetch all items
      // fetchWishlist();
      // Option 2: Filter out the removed item from local state (more efficient)
      setWishlistItems(prevItems => prevItems.filter(item => item.productId !== productId));

    } catch (err) {
      console.error(`Failed to remove product ID ${productId} from wishlist:`, err);
      setError(`Could not remove item (ID: ${productId}). Please try again.`);
    } finally {
      setRemovingItemId(null); // Clear loading state for this item
    }
  };

  // Helper to format currency
  const formatCurrency = (amount) => {
    if (amount === null || amount === undefined) return 'N/A';
    return new Intl.NumberFormat('en-PH', { style: 'currency', currency: 'PHP' }).format(amount);
  };

  return (
    <div className="font-sans text-credigo-light p-4 md:p-6">
      <h1 className="text-3xl font-bold mb-6">My Wishlist</h1>

      {/* Loading State */}
      {loading && (
        <div className="text-center py-10"><p>Loading wishlist...</p></div>
      )}

      {/* Error State */}
      {error && !loading && (
        <div className="p-4 mb-4 text-center text-red-400 bg-red-900/50 rounded-lg border border-red-700" role="alert">
          <span className="font-medium">Error:</span> {error}
        </div>
      )}

      {/* Wishlist Items Grid/List */}
      {!loading && !error && wishlistItems.length > 0 && (
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 md:gap-6">
          {wishlistItems.map((item) => (
            <div key={item.productId} className="border border-gray-700 rounded-lg overflow-hidden shadow-lg bg-credigo-input-bg flex flex-col">
              {/* Image Placeholder/Actual Image */}
              {item.productImageUrl ? (
                <img src={item.productImageUrl} alt={item.productName} className="w-full h-32 sm:h-40 object-cover" />
              ) : (
                <div className="w-full h-32 sm:h-40 bg-credigo-dark flex items-center justify-center text-gray-500">No Image</div>
              )}
              {/* Content */}
              <div className="p-4 flex flex-col flex-grow">
                <h3 className="font-semibold text-lg text-credigo-light mb-1 truncate flex-grow">{item.productName}</h3>
                <p className="text-sm text-gray-400 mb-2">Game: {item.platformName || 'Unknown'}</p>
                <div className="flex justify-between items-center mt-auto pt-2">
                  <span className="text-xl font-bold text-credigo-light">
                    {formatCurrency(item.productPrice)}
                  </span>
                  {/* Remove Button */}
                  <button
                    onClick={() => handleRemoveItem(item.productId)}
                    disabled={removingItemId === item.productId} // Disable only the button being clicked
                    className={`p-1.5 rounded-md text-red-400 hover:bg-red-500/20 hover:text-red-300 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-credigo-input-bg focus:ring-red-500 transition duration-150 ease-in-out ${removingItemId === item.productId ? 'opacity-50 cursor-wait' : ''
                      }`}
                    aria-label={`Remove ${item.productName} from wishlist`}
                  >
                    {removingItemId === item.productId ? (
                      <span className="text-xs">Removing...</span> // Simple text indicator
                      // Or use a spinner icon
                    ) : (
                      <Trash2 className="w-5 h-5" />
                    )}
                  </button>
                </div>
                {/* Optional: Add 'Buy Now' button or link to product page */}
                {/* <Link to={`/products/${item.productId}`} className="mt-2 text-center w-full px-3 py-1 bg-credigo-button text-credigo-dark rounded-md hover:bg-opacity-90 text-sm font-semibold transition duration-150">View</Link> */}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* No Items State */}
      {!loading && !error && wishlistItems.length === 0 && (
        <div className="text-center py-10 text-gray-500 bg-credigo-input-bg rounded-lg shadow border border-gray-700">
          <p>Your wishlist is empty.</p>
          <Link to="/products" className="mt-2 inline-block text-credigo-button hover:text-opacity-80">Browse products</Link>
        </div>
      )}

    </div>
  );
}

export default WishlistPage;
