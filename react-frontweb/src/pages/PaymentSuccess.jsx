import { Button } from "@/components/ui/button";
import { Spinner } from "@/components/ui/spinner";
import { useToast } from '@/hooks/use-toast';
import { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { checkPaymentStatus } from '../services/api';

// Get base URL for the current environment
const BASE_URL = import.meta.env.DEV
  ? 'http://localhost:5173'
  : 'https://credi-go-it-342.vercel.app';

export default function PaymentSuccess() {
  const location = useLocation();
  const navigate = useNavigate();
  const { toast } = useToast();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [paymentResult, setPaymentResult] = useState(null);
  const [debugInfo, setDebugInfo] = useState(null);

  useEffect(() => {
    // Log current environment info for debugging
    console.log("Payment Success Page - Environment:", import.meta.env.MODE);
    console.log("Current base URL:", BASE_URL);
    console.log("Current URL:", window.location.href);

    const processPayment = async () => {
      try {
        // Get source ID from URL
        const searchParams = new URLSearchParams(location.search);
        const sourceId = searchParams.get('id');

        setDebugInfo({
          currentUrl: window.location.href,
          sourceId,
          expectedUrl: localStorage.getItem('expected_return_url') || 'Not found',
          environment: import.meta.env.MODE
        });

        if (!sourceId) {
          throw new Error('No payment source ID found in URL');
        }

        // Show initial toast to let user know we're checking status
        toast({
          title: "Verifying Payment",
          description: "Please wait while we verify your payment...",
        });

        // Check payment status using your API
        const statusResponse = await checkPaymentStatus(sourceId);
        const paymentData = statusResponse.data;

        setPaymentResult(paymentData);

        // Handle payment success
        if (paymentData.status === 'succeeded' || paymentData.walletCredited) {
          toast({
            title: "Payment Successful!",
            description: "Your wallet has been topped up successfully",
            variant: "default",
          });

          // Update localStorage to mark as completed
          try {
            const storedData = JSON.parse(localStorage.getItem(`payment_intent_${sourceId}`) || '{}');
            localStorage.setItem(`payment_intent_${sourceId}`, JSON.stringify({
              ...storedData,
              status: 'succeeded',
              walletCredited: true,
              completedAt: Date.now()
            }));
          } catch (e) {
            console.error('Error updating payment intent success in localStorage:', e);
          }
        } else {
          // Handle payment pending/processing
          toast({
            title: "Payment Processing",
            description: "Your payment is being processed. Your wallet will be updated shortly.",
            variant: "info",
          });
        }
      } catch (error) {
        console.error("Payment verification failed:", error);
        setError(error.message || "Failed to verify payment");

        toast({
          title: "Payment Verification Failed",
          description: error.message || "There was a problem verifying your payment",
          variant: "destructive",
        });
      } finally {
        setLoading(false);
      }
    };

    processPayment();
  }, [location.search, toast]);

  return (
    <div className="max-w-md mx-auto my-12 p-6 bg-white rounded-lg shadow-lg">
      <div className="text-center">
        <h1 className="text-2xl font-bold text-gray-900 mb-4">Payment Status</h1>

        {loading ? (
          <div className="flex flex-col items-center space-y-4">
            <Spinner className="h-12 w-12 text-primary" />
            <p className="text-gray-600">Verifying your payment...</p>
          </div>
        ) : error ? (
          <div className="text-center">
            <div className="bg-red-100 text-red-800 p-4 rounded-md mb-4">
              <p className="font-semibold">Payment Verification Error</p>
              <p className="text-sm mt-1">{error}</p>

              {debugInfo && (
                <div className="mt-4 p-2 bg-gray-100 rounded text-left text-xs">
                  <div><strong>Environment:</strong> {debugInfo.environment}</div>
                  <div><strong>Current URL:</strong> {debugInfo.currentUrl}</div>
                  <div><strong>Expected URL:</strong> {debugInfo.expectedUrl}</div>
                  <div><strong>Source ID:</strong> {debugInfo.sourceId || 'Not found'}</div>
                </div>
              )}
            </div>
            <Button onClick={() => navigate('/dashboard')} className="mt-4">
              Return to Dashboard
            </Button>
          </div>
        ) : (
          <div className="space-y-6">
            <div className="bg-green-100 text-green-800 p-4 rounded-md">
              {paymentResult?.walletCredited ? (
                <>
                  <h2 className="text-xl font-bold">Payment Successful!</h2>
                  <p className="mt-2">Your wallet has been credited successfully.</p>
                </>
              ) : (
                <>
                  <h2 className="text-xl font-bold">Payment Processing</h2>
                  <p className="mt-2">Your payment is being processed. Please check your wallet balance shortly.</p>
                </>
              )}
            </div>

            <div className="space-y-2">
              <div className="flex justify-between py-2 border-b">
                <span className="font-medium">Status:</span>
                <span className="capitalize">{paymentResult?.status || 'Processing'}</span>
              </div>
              {paymentResult?.amount && (
                <div className="flex justify-between py-2 border-b">
                  <span className="font-medium">Amount:</span>
                  <span>₱{(paymentResult.amount / 100).toFixed(2)}</span>
                </div>
              )}
              <div className="flex justify-between py-2 border-b">
                <span className="font-medium">Payment Method:</span>
                <span>{localStorage.getItem('current_payment_method') || 'GCash'}</span>
              </div>
            </div>

            <div className="pt-4">
              <Button onClick={() => navigate('/Home')} className="w-full">
                Return
              </Button>
            </div>

            {import.meta.env.DEV && (
              <div className="mt-4 text-xs text-left text-gray-500 p-2 bg-gray-100 rounded">
                <details>
                  <summary>Debug Info</summary>
                  <div className="mt-2 space-y-1">
                    <div><strong>Environment:</strong> {import.meta.env.MODE}</div>
                    <div><strong>Current URL:</strong> {window.location.href}</div>
                    <div><strong>Expected URL:</strong> {localStorage.getItem('expected_return_url') || 'Not found'}</div>
                    <div><strong>Source ID:</strong> {new URLSearchParams(location.search).get('id') || 'Not found'}</div>
                  </div>
                </details>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
