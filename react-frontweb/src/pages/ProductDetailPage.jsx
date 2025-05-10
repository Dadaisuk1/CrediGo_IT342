import { useEffect, useState } from 'react';
import { FaStar } from 'react-icons/fa';
import { FiStar } from 'react-icons/fi';
import { useNavigate, useParams } from 'react-router-dom';
import PurchaseModal from '../components/PurchaseModal';
import WishlistButton from '../components/WishlistButton';
import { useAuth } from '../context/AuthContext';
import { getProductById, getProductReviews, getWishlist, submitProductReview } from '../services/api';
import { formatCurrency } from '../utils/formatters';

function ProductDetailPage() {
  const { productId } = useParams();
  const navigate = useNavigate();
  const { fetchWalletBalance, isAuthenticated, user } = useAuth();

  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isInWishlist, setIsInWishlist] = useState(false);
  const [wishlistCheckComplete, setWishlistCheckComplete] = useState(false);

  // Purchase modal states
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [purchaseStatusMessage, setPurchaseStatusMessage] = useState('');

  // Review states
  const [reviews, setReviews] = useState([]);
  const [loadingReviews, setLoadingReviews] = useState(false);
  const [reviewError, setReviewError] = useState(null);
  const [userRating, setUserRating] = useState(0);
  const [hoverRating, setHoverRating] = useState(0);
  const [reviewComment, setReviewComment] = useState('');
  const [submittingReview, setSubmittingReview] = useState(false);
  const [hasUserReviewed, setHasUserReviewed] = useState(false);

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

  useEffect(() => {
    // Fetch reviews when product ID changes or after a review is submitted
    const fetchReviews = async () => {
      if (!productId) return;

      setLoadingReviews(true);
      try {
        const response = await getProductReviews(productId);
        setReviews(response.data);

        // Check if user has already submitted a review
        if (isAuthenticated && user) {
          const hasReviewed = response.data.some(review =>
            review.username === user.username || review.userId === user.id
          );
          setHasUserReviewed(hasReviewed);

          // If user has already reviewed, get their rating and comment
          if (hasReviewed) {
            const userReview = response.data.find(review =>
              review.username === user.username || review.userId === user.id
            );
            setUserRating(userReview.rating);
            setReviewComment(userReview.comment);
          }
        }
      } catch (err) {
        console.error('Error fetching product reviews:', err);
        setReviewError('Failed to load reviews');
      } finally {
        setLoadingReviews(false);
      }
    };

    fetchReviews();
  }, [productId, isAuthenticated, user, submittingReview]);

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

  const handleReviewSubmit = async (e) => {
    e.preventDefault();
    if (!isAuthenticated) {
      navigate('/login', { state: { from: `/products/${productId}` } });
      return;
    }

    if (userRating === 0) {
      setReviewError('Please select a rating');
      return;
    }

    setSubmittingReview(true);
    setReviewError(null);

    try {
      await submitProductReview(productId, {
        rating: userRating,
        comment: reviewComment
      });

      setUserRating(0);
      setReviewComment('');
      setSubmittingReview(false);
      // Set to true to block multiple submissions until the effect runs
      setHasUserReviewed(true);
    } catch (err) {
      console.error('Error submitting review:', err);
      setReviewError('Failed to submit review. Please try again.');
      setSubmittingReview(false);
    }
  };

  const calculateAverageRating = () => {
    if (!reviews || reviews.length === 0) return 0;
    const sum = reviews.reduce((total, review) => total + review.rating, 0);
    return (sum / reviews.length).toFixed(1);
  };

  const renderStarRating = (rating) => {
    return (
      <div className="flex">
        {[1, 2, 3, 4, 5].map((star) => (
          <span key={star} className="text-yellow-500">
            {star <= rating ? <FaStar /> : <FiStar />}
          </span>
        ))}
      </div>
    );
  };

  const renderRatingInput = () => {
    return (
      <div className="flex mb-4">
        {[1, 2, 3, 4, 5].map((star) => (
          <button
            key={star}
            type="button"
            className="text-2xl"
            onMouseEnter={() => setHoverRating(star)}
            onMouseLeave={() => setHoverRating(0)}
            onClick={() => setUserRating(star)}
          >
            <span className={`${(hoverRating || userRating) >= star ? 'text-yellow-500' : 'text-gray-300'}`}>
              <FaStar />
            </span>
          </button>
        ))}
      </div>
    );
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

          {/* Rating Display */}
          <div className="flex items-center mb-4">
            <div className="flex mr-2">
              {renderStarRating(calculateAverageRating())}
            </div>
            <span className="text-gray-600">
              {calculateAverageRating()} ({reviews.length} {reviews.length === 1 ? 'review' : 'reviews'})
            </span>
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

      {/* Reviews Section */}
      <div className="mt-12">
        <h2 className="text-2xl font-bold mb-6">Customer Reviews</h2>

        {/* Write Review Form */}
        {isAuthenticated && !hasUserReviewed && (
          <div className="bg-gray-50 p-6 rounded-lg mb-8">
            <h3 className="text-lg font-semibold mb-4">Write a Review</h3>
            {reviewError && (
              <div className="mb-4 text-red-600">{reviewError}</div>
            )}
            <form onSubmit={handleReviewSubmit}>
              <div className="mb-4">
                <label className="block text-gray-700 mb-2">Your Rating</label>
                {renderRatingInput()}
              </div>
              <div className="mb-4">
                <label htmlFor="comment" className="block text-gray-700 mb-2">Your Review</label>
                <textarea
                  id="comment"
                  value={reviewComment}
                  onChange={(e) => setReviewComment(e.target.value)}
                  className="w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
                  rows={4}
                  placeholder="Share your experience with this product..."
                ></textarea>
              </div>
              <button
                type="submit"
                disabled={submittingReview}
                className="bg-primary text-white py-2 px-4 rounded-md hover:bg-primary-dark disabled:opacity-50"
              >
                {submittingReview ? 'Submitting...' : 'Submit Review'}
              </button>
            </form>
          </div>
        )}

        {/* Review List */}
        {loadingReviews ? (
          <div className="text-center py-4">
            <div className="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-primary mx-auto"></div>
          </div>
        ) : reviews.length > 0 ? (
          <div className="space-y-6">
            {reviews.map((review) => (
              <div key={`${review.userId}-${review.createdAt}`} className="border-b border-gray-200 pb-4">
                <div className="flex items-center justify-between">
                  <div className="flex items-center">
                    <div className="mr-3">{renderStarRating(review.rating)}</div>
                    <h4 className="font-semibold">{review.username || 'Anonymous'}</h4>
                  </div>
                  <span className="text-gray-500 text-sm">
                    {new Date(review.reviewTimestamp || review.createdAt).toLocaleDateString()}
                  </span>
                </div>
                <p className="mt-2 text-gray-700">{review.comment}</p>
              </div>
            ))}
          </div>
        ) : (
          <p className="text-gray-600 py-4">No reviews yet. Be the first to review this product!</p>
        )}
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
