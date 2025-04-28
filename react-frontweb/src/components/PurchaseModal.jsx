
// src/components/PurchaseModal.jsx
import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext'; // To check balance maybe?
import { purchaseProduct } from '../services/api'; // API function
import { TbCurrencyPeso } from 'react-icons/tb';
import { X } from 'lucide-react'; // Close icon

function PurchaseModal({ product, onClose, onPurchaseSuccess, onPurchaseError }) {
  const { walletBalance } = useAuth(); // Get current balance for display/check
  const [gameAccountId, setGameAccountId] = useState('');
  const [gameServerId, setGameServerId] = useState(''); // Optional server ID
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  if (!product) return null; // Don't render if no product is selected

  const productPrice = parseFloat(product.price || 0);
  const userHasSufficientFunds = walletBalance !== null && walletBalance >= productPrice;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setIsLoading(true);

    if (!gameAccountId) {
      setError("Please enter your Game Account ID.");
      setIsLoading(false);
      return;
    }

    // Check funds again right before submitting (optional but good)
    if (!userHasSufficientFunds) {
      setError(`Insufficient funds. Balance: ${formatCurrency(walletBalance)}, Required: ${formatCurrency(productPrice)}`);
      setIsLoading(false);
      return;
    }


    const purchaseData = {
      productId: product.id,
      gameAccountId: gameAccountId,
      gameServerId: gameServerId || null, // Send null if empty
      quantity: 1, // Assuming quantity is always 1 for now
    };

    try {
      const response = await purchaseProduct(purchaseData);
      console.log("Purchase API Response:", response.data);
      // Handle based on transaction status returned from backend
      if (response.data?.status === 'COMPLETED' || response.data?.status === 'PROCESSING') {
        onPurchaseSuccess(response.data); // Pass transaction details back
      } else {
        // Handle specific failure messages from backend if available
        throw new Error(response.data?.statusMessage || "Purchase failed.");
      }
    } catch (err) {
      console.error("Purchase failed:", err);
      const errorMessage = err.response?.data || err.message || "An error occurred during purchase.";
      setError(errorMessage);
      if (onPurchaseError) onPurchaseError(errorMessage); // Notify parent
    } finally {
      setIsLoading(false);
    }
  };

  // Helper to format currency
  const formatCurrency = (amount) => {
    if (amount === null || amount === undefined) return 'N/A';
    return new Intl.NumberFormat('en-PH', { style: 'currency', currency: 'PHP' }).format(amount);
  };

  return (
    // Modal backdrop
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-60 backdrop-blur-sm"
      onClick={onClose} // Close modal if backdrop is clicked
    >
      {/* Modal content container */}
      <div
        className="relative w-full max-w-lg p-6 md:p-8 mx-4 bg-credigo-input-bg rounded-2xl shadow-xl border border-gray-700 text-credigo-light font-sans"
        onClick={(e) => e.stopPropagation()} // Prevent backdrop click from closing when clicking inside modal
      >
        {/* Close button */}
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-gray-400 hover:text-credigo-light transition duration-150"
          aria-label="Close purchase modal"
        >
          <X size={24} />
        </button>

        <h2 className="text-2xl font-bold mb-4 text-center">Confirm Purchase</h2>

        {/* Product Summary */}
        <div className="mb-6 p-4 bg-credigo-dark rounded-lg border border-gray-600 flex items-center space-x-4">
          {product.imageUrl ? (
            <img src={product.imageUrl} alt={product.name} className="w-16 h-16 object-cover rounded-md flex-shrink-0" />
          ) : (
            <div className="w-16 h-16 bg-gray-700 rounded-md flex-shrink-0 flex items-center justify-center text-xs text-gray-400">No Image</div>
          )}
          <div className="flex-grow">
            <h3 className="font-semibold text-lg">{product.name}</h3>
            <p className="text-sm text-gray-400">{product.platformName}</p>
            <p className="text-lg font-bold mt-1">{formatCurrency(productPrice)}</p>
          </div>
        </div>

        {/* Form for Game ID */}
        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Display General Error */}
          {error && (
            <div className="p-3 text-sm text-red-100 bg-red-500/30 rounded-lg border border-red-500/50" role="alert">
              <span className="font-medium">Error:</span> {error}
            </div>
          )}

          {/* Insufficient Funds Warning */}
          {!userHasSufficientFunds && walletBalance !== null && (
            <div className="p-3 text-sm text-yellow-200 bg-yellow-500/20 rounded-lg border border-yellow-500/50" role="alert">
              <span className="font-medium">Warning:</span> Insufficient funds in wallet. Please top up.
              Current Balance: {formatCurrency(walletBalance)}
            </div>
          )}

          {/* Game Account ID Input */}
          <div>
            <label htmlFor="gameAccountId" className="block text-sm font-medium text-credigo-light/80 mb-1">
              Game Account ID <span className="text-red-400">*</span>
            </label>
            <input
              id="gameAccountId"
              name="gameAccountId"
              type="text"
              required
              value={gameAccountId}
              onChange={(e) => setGameAccountId(e.target.value)}
              className="block w-full px-4 py-2 text-credigo-light placeholder-gray-400 bg-credigo-dark border border-gray-600 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-credigo-button focus:border-transparent sm:text-sm"
              placeholder="Enter your exact in-game ID"
            />
            <p className="mt-1 text-xs text-gray-400">Example: YourUsername#TAG123</p>
          </div>

          {/* Game Server ID Input (Optional) */}
          <div>
            <label htmlFor="gameServerId" className="block text-sm font-medium text-credigo-light/80 mb-1">
              Game Server/Zone ID <span className="text-gray-400">(If applicable)</span>
            </label>
            <input
              id="gameServerId"
              name="gameServerId"
              type="text"
              value={gameServerId}
              onChange={(e) => setGameServerId(e.target.value)}
              className="block w-full px-4 py-2 text-credigo-light placeholder-gray-400 bg-credigo-dark border border-gray-600 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-credigo-button focus:border-transparent sm:text-sm"
              placeholder="Enter server/zone if required by game"
            />
          </div>

          {/* Confirm Button */}
          <div className="pt-2">
            <button
              type="submit"
              disabled={isLoading || !userHasSufficientFunds} // Disable if loading or insufficient funds
              className={`w-full flex justify-center px-4 py-3 text-sm font-semibold text-credigo-dark bg-credigo-button border border-transparent rounded-lg shadow-sm hover:bg-opacity-90 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-credigo-input-bg focus:ring-credigo-button transition duration-150 ease-in-out ${(isLoading || !userHasSufficientFunds) ? 'opacity-50 cursor-not-allowed' : ''
                }`}
            >
              {isLoading ? 'Processing...' : `Confirm Purchase (${formatCurrency(productPrice)})`}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default PurchaseModal;
