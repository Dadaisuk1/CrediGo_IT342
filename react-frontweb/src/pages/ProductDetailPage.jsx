import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import PurchaseModal from '../components/PurchaseModal';
import WishlistButton from '../components/WishlistButton';
import { useAuth } from '../context/AuthContext';
import { getProductById, getWishlist } from '../services/api';
import { formatCurrency } from '../utils/formatters';

function ProductDetailPage() {
  const { productId } = useParams();
  const navigate = useNavigate();
  const { fetchWalletBalance } = useAuth();

  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isInWishlist, setIsInWishlist] = useState(false);
  const [wishlistCheckComplete, setWishlistCheckComplete] = useState(false);

  // Purchase modal states
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [purchaseStatusMessage, setPurchaseStatusMessage] = useState('');

  useEffect(() => {
    const fetchProductAndWishlistStatus = async () => {
      setLoading(true);
      try {
        // Fetch product details
        const productResponse = await getProductById(parseInt(productId));
        setProduct(productResponse.data);

        // Check if product is in user's wishlist
        try {
          const wishlistResponse = await getWishlist();
          const wishlistItems = wishlistResponse.data || [];
          const isInWishlist = wishlistItems.some(item => item.productId === parseInt(productId));
          setIsInWishlist(isInWishlist);
        } catch (wishlistError) {
          console.error('Error checking wishlist status:', wishlistError);
          // Don't set main error - just consider not in wishlist
          setIsInWishlist(false);
        }

        setWishlistCheckComplete(true);
      } catch (err) {
        console.error('Error fetching product details:', err);
        setError('Failed to load product details. Please try again later.');
      } finally {
        setLoading(false);
      }
    };

    fetchProductAndWishlistStatus();
  }, [productId]);

  const handleWishlistChange = (newStatus) => {
    setIsInWishlist(newStatus);
  };

  const handlePurchase = () => {
    setIsModalOpen(true);
    setPurchaseStatusMessage('');
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
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

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-[60vh]">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary"></div>
      </div>
    );
  }

  if (error || !product) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded relative" role="alert">
          <strong className="font-bold">Error: </strong>
          <span className="block sm:inline">{error || 'Product not found'}</span>
          <button
            onClick={() => navigate(-1)}
            className="mt-3 bg-red-600 text-white px-4 py-2 rounded hover:bg-red-700"
          >
            Go Back
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      {/* Status Message */}
      {purchaseStatusMessage && (
        <div className={`mb-6 p-3 rounded-lg border ${
          purchaseStatusMessage.startsWith('Successfully')
            ? 'bg-green-50 border-green-200 text-green-700'
            : 'bg-red-50 border-red-200 text-red-700'
        }`} role="alert">
          {purchaseStatusMessage}
        </div>
      )}

      <div className="flex flex-col md:flex-row gap-8">
        {/* Product Image */}
        <div className="w-full md:w-1/2">
          <div className="rounded-lg overflow-hidden shadow-lg">
            <img
              src={product.imageUrl || '/placeholder-product.jpg'}
              alt={product.name}
              className="w-full h-auto object-cover"
            />
          </div>
        </div>

        {/* Product Info */}
        <div className="w-full md:w-1/2">
          <div className="flex justify-between items-start">
            <h1 className="text-3xl font-bold text-gray-800 mb-2">{product.name}</h1>
            {wishlistCheckComplete && (
              <WishlistButton
                productId={product.id}
                isInWishlist={isInWishlist}
                onWishlistChange={handleWishlistChange}
                className="ml-2"
              />
            )}
          </div>

          <div className="bg-gray-100 inline-block px-3 py-1 rounded-full text-sm font-medium mb-4">
            {product.platformName || 'Unknown Platform'}
          </div>

          <div className="text-2xl font-bold text-primary mb-4">
            {formatCurrency(product.price)}
          </div>

          <div className="mb-6">
            <h2 className="text-lg font-semibold mb-2">Description</h2>
            <p className="text-gray-600">{product.description || 'No description available.'}</p>
          </div>

          <div className="mb-6">
            <h2 className="text-lg font-semibold mb-2">Details</h2>
            <ul className="list-disc pl-5 space-y-1 text-gray-600">
              <li>Platform: {product.platformName || 'N/A'}</li>
              <li>Availability: {product.available ? 'In Stock' : 'Out of Stock'}</li>
              <li>Product ID: {product.id}</li>
            </ul>
          </div>

          <button
            onClick={handlePurchase}
            disabled={!product.available}
            className={`w-full py-3 rounded-lg text-center font-semibold transition-colors ${
              product.available
                ? 'bg-primary text-white hover:bg-primary-dark'
                : 'bg-gray-300 text-gray-500 cursor-not-allowed'
            }`}
          >
            {product.available ? 'Purchase Now' : 'Currently Unavailable'}
          </button>
        </div>
      </div>

      {/* Purchase Modal */}
      {isModalOpen && (
        <PurchaseModal
          product={product}
          onClose={handleCloseModal}
          onPurchaseSuccess={handlePurchaseSuccess}
          onPurchaseError={handlePurchaseError}
        />
      )}
    </div>
  );
}

export default ProductDetailPage;
