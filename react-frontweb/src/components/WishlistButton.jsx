import { Heart } from 'lucide-react';
import { useState } from 'react';
import { toast } from 'react-toastify';
import { addToWishlist, removeFromWishlist } from '../services/api';

/**
 * A reusable button component for adding/removing products from wishlist
 *
 * @param {Object} props - Component props
 * @param {number} props.productId - The ID of the product
 * @param {boolean} props.isInWishlist - Whether the product is already in the user's wishlist
 * @param {Function} props.onWishlistChange - Callback function when wishlist status changes
 * @param {string} props.className - Additional CSS classes
 */
function WishlistButton({ productId, isInWishlist, onWishlistChange, className = '' }) {
  const [loading, setLoading] = useState(false);

  const handleToggleWishlist = async () => {
    if (loading) return;

    setLoading(true);
    try {
      if (isInWishlist) {
        await removeFromWishlist(productId);
        toast.success('Removed from your wishlist');
      } else {
        await addToWishlist(productId);
        toast.success('Added to your wishlist');
      }
      // Notify parent component of the change
      if (onWishlistChange) {
        onWishlistChange(!isInWishlist);
      }
    } catch (error) {
      console.error('Wishlist operation failed:', error);
      toast.error(isInWishlist
        ? 'Failed to remove from wishlist'
        : 'Failed to add to wishlist'
      );
    } finally {
      setLoading(false);
    }
  };

  const baseClasses = 'flex items-center justify-center rounded-full transition-colors focus:outline-none';
  const filledClasses = isInWishlist
    ? 'text-red-500 hover:text-red-600'
    : 'text-gray-400 hover:text-red-500';

  return (
    <button
      className={`${baseClasses} ${filledClasses} ${className}`}
      disabled={loading}
      onClick={handleToggleWishlist}
      aria-label={isInWishlist ? 'Remove from wishlist' : 'Add to wishlist'}
    >
      <Heart
        className={`w-6 h-6 ${isInWishlist ? 'fill-current' : ''} ${loading ? 'animate-pulse' : ''}`}
      />
    </button>
  );
}

export default WishlistButton;
