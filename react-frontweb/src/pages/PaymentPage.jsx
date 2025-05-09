// src/pages/PaymentPage.jsx
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { useToast } from "@/hooks/use-toast";
import { useState } from 'react';
// import { createPaymentIntent } from '../services/api';

const PaymentPage = () => {
  const { toast } = useToast();
  const [amount, setAmount] = useState('');
  const [paymentInfo, setPaymentInfo] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  const handlePay = async (e) => {
    e.preventDefault();
    setError(null);
    setPaymentInfo(null);
    setLoading(true);

    try {
      // Amount should be integer (e.g., 100 for PHP 100)
      const data = await createPaymentIntent(Number(amount));
      setPaymentInfo(data);

      // Show success toast
      toast({
        title: "Payment Created",
        description: `Successfully created a payment for PHP ${amount}`,
        variant: "default",
      });

      // If your backend returns a checkout_url, you can redirect:
      if (data.checkout_url) {
        window.location.href = data.checkout_url;
      }
    } catch (err) {
      const errorMessage = typeof err === 'string' ? err : JSON.stringify(err);
      setError(errorMessage);

      // Show error toast
      toast({
        title: "Payment Error",
        description: "Could not create the payment. Please try again.",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-md mx-auto my-8 px-4">
      <Card>
        <CardHeader>
          <CardTitle>Make a Payment</CardTitle>
          <CardDescription>Enter the amount to pay</CardDescription>
        </CardHeader>

        <CardContent>
          <form onSubmit={handlePay}>
            <div className="space-y-4">
              <div className="space-y-2">
                <label htmlFor="amount" className="text-sm font-medium">
                  Amount (PHP)
                </label>
                <Input
                  id="amount"
                  type="number"
                  placeholder="Amount (PHP)"
                  value={amount}
                  onChange={e => setAmount(e.target.value)}
                  required
                  min="1"
                />
              </div>

              <Button
                type="submit"
                className="w-full"
                disabled={loading || !amount}
              >
                {loading ? 'Processing...' : 'Pay Now'}
              </Button>
            </div>
          </form>

          {paymentInfo && (
            <div className="mt-6 p-3 bg-slate-50 rounded-md">
              <h3 className="font-medium mb-2">Payment Created!</h3>
              <pre className="text-xs bg-slate-100 p-2 rounded overflow-auto">
                {JSON.stringify(paymentInfo, null, 2)}
              </pre>
            </div>
          )}

          {error && (
            <div className="mt-4 p-3 bg-red-50 text-red-700 rounded-md text-sm">
              {error}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

export default PaymentPage;
