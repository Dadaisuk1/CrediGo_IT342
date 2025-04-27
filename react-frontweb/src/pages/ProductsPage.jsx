// src/pages/ProductsPage.jsx
import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
// Import API functions for products and platforms
import { getAvailableProducts, getPlatforms } from '../services/api';

// Simple Product Card Component (keep as is or enhance)
const ProductCard = ({ product }) => (
  <div className="border border-gray-700 rounded-lg overflow-hidden shadow-lg bg-credigo-input-bg hover:shadow-xl transition-shadow duration-200 ease-in-out flex flex-col">
    {product.imageUrl ? (
      <img src={product.imageUrl} alt={product.name} className="w-full h-32 sm:h-40 object-cover" />
    ) : (
      <div className="w-full h-32 sm:h-40 bg-credigo-dark flex items-center justify-center text-gray-500">No Image</div>
    )}
    <div className="p-4 flex flex-col flex-grow"> {/* Use flex-grow here */}
      <h3 className="font-semibold text-lg text-credigo-light mb-1 truncate flex-grow">{product.name}</h3> {/* Use flex-grow here */}
      <p className="text-sm text-gray-400 mb-2">Game: {product.platformName || 'Unknown'}</p>
      <div className="flex justify-between items-center mt-auto"> {/* Use mt-auto here */}
        <span className="text-xl font-bold text-credigo-light">
          ₱{product.price ? parseFloat(product.price).toFixed(2) : '0.00'}
        </span>
        <button
          onClick={() => alert(`Purchase ${product.name} - Not implemented yet`)}
          className="px-3 py-1 bg-credigo-button text-credigo-dark rounded-md hover:bg-opacity-90 text-sm font-semibold transition duration-150"
        >
          Buy
        </button>
      </div>
    </div>
  </div>
);


function ProductsPage() {
  const [products, setProducts] = useState([]);
  const [platforms, setPlatforms] = useState([]);
  const [selectedPlatformId, setSelectedPlatformId] = useState(null); // null means 'All'
  const [loadingProducts, setLoadingProducts] = useState(true);
  const [loadingPlatforms, setLoadingPlatforms] = useState(true);
  const [error, setError] = useState(null);

  // Fetch platforms on component mount
  useEffect(() => {
    const fetchPlatforms = async () => {
      setLoadingPlatforms(true);
      try {
        const response = await getPlatforms();
        setPlatforms(response.data || []);
        console.log("Fetched platforms:", response.data);
      } catch (err) {
        console.error("Failed to fetch platforms:", err);
        setError("Could not load game list."); // Set general error or specific one
      } finally {
        setLoadingPlatforms(false);
      }
    };
    fetchPlatforms();
  }, []);

  // Fetch products when component mounts or selectedPlatformId changes
  useEffect(() => {
    const fetchProducts = async () => {
      setLoadingProducts(true);
      setError(null); // Clear previous product errors
      console.log(`Fetching products for platform ID: ${selectedPlatformId === null ? 'All' : selectedPlatformId}`);
      try {
        // Pass null or the ID to the API function
        const response = await getAvailableProducts(selectedPlatformId);
        setProducts(response.data || []);
        console.log("Fetched products:", response.data);
      } catch (err) {
        console.error("Failed to fetch products:", err);
        setError("Could not load products. Please try again later.");
        setProducts([]); // Clear products on error
      } finally {
        setLoadingProducts(false);
      }
    };

    fetchProducts();
  }, [selectedPlatformId]); // Re-run this effect when selectedPlatformId changes

  const handlePlatformFilter = (platformId) => {
    setSelectedPlatformId(platformId); // Set the selected ID (null for 'All')
  };

  return (
    <div className="font-sans text-credigo-light p-4 md:p-6">
      <h1 className="text-3xl font-bold mb-6 text-credigo-dark text-center md:text-left">Browse Top-ups</h1>

      {/* Platform Filters */}
      <div className="mb-6">
        <h2 className="text-lg font-semibold mb-3 text-credigo-dark/80">Filter by Game:</h2>
        {loadingPlatforms ? (
          <p className="text-gray-400">Loading games...</p>
        ) : (
          <div className="flex flex-wrap gap-2">
            <button
              onClick={() => handlePlatformFilter(null)} // Set filter to null for 'All'
              className={`px-4 py-1.5 rounded-full text-sm font-medium transition duration-150 ${selectedPlatformId === null
                ? 'bg-credigo-button text-credigo-dark' // Active style
                : 'bg-credigo-input-bg border border-gray-600 text-gray-300 hover:bg-gray-700' // Inactive style
                }`}
            >
              All Games
            </button>
            {platforms.map((platform) => (
              <button
                key={platform.id}
                onClick={() => handlePlatformFilter(platform.id)}
                className={`px-4 py-1.5 rounded-full text-sm font-medium transition duration-150 ${selectedPlatformId === platform.id
                  ? 'bg-credigo-button text-credigo-dark' // Active style
                  : 'bg-credigo-input-bg border border-gray-600 text-gray-300 hover:bg-gray-700' // Inactive style
                  }`}
              >
                {platform.name}
              </button>
            ))}
          </div>
        )}
      </div>

      {/* Loading State for Products */}
      {loadingProducts && (
        <div className="text-center py-10">
          <p>Loading products...</p>
          {/* Add a spinner here if desired */}
        </div>
      )}

      {/* Error State */}
      {error && !loadingProducts && (
        <div className="p-4 text-center text-red-400 bg-red-900/50 rounded-lg border border-red-700" role="alert">
          <span className="font-medium">Error:</span> {error}
        </div>
      )}

      {/* Products Grid */}
      {!loadingProducts && !error && products.length > 0 && (
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 md:gap-6">
          {products.map((product) => (
            <ProductCard key={product.id} product={product} />
          ))}
        </div>
      )}

      {/* No Products Found State */}
      {!loadingProducts && !error && products.length === 0 && (
        <div className="text-center py-10 text-gray-500">
          <p>No products found matching your criteria.</p>
        </div>
      )}

    </div>
  );
}

export default ProductsPage;
