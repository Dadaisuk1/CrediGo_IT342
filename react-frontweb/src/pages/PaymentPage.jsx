// src/pages/PaymentPage.jsx
import React, { useState } from 'react';
import { createPaymentIntent } from '../services/api';

const PaymentPage = () => {
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
      // If your backend returns a checkout_url, you can redirect:
      if (data.checkout_url) {
        window.location.href = data.checkout_url;
      }
    } catch (err) {
      setError(typeof err === 'string' ? err : JSON.stringify(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: 400, margin: '2rem auto', padding: 24, border: '1px solid #eee', borderRadius: 8 }}>
      <h2>Make a Payment</h2>
      <form onSubmit={handlePay}>
        <input
          type="number"
          placeholder="Amount (PHP)"
          value={amount}
          onChange={e => setAmount(e.target.value)}
          required
          min="1"
          style={{ width: '100%', marginBottom: 12, padding: 8 }}
        />
        <button type="submit" disabled={loading || !amount} style={{ width: '100%' }}>
          {loading ? 'Processing...' : 'Pay'}
        </button>
      </form>
      {paymentInfo && (
        <div style={{ marginTop: 16 }}>
          <h3>Payment Created!</h3>
          <pre style={{ fontSize: 12, background: '#f7f7f7', padding: 8 }}>{JSON.stringify(paymentInfo, null, 2)}</pre>
          {/* If you want to show a QR code or payment link, render here */}
        </div>
      )}
      {error && <div style={{ color: 'red', marginTop: 16 }}>{error}</div>}
    </div>
  );
};

export default PaymentPage;
