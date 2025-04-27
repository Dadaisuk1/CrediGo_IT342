import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App.jsx';
import './index.css';
import { AuthProvider } from './context/AuthContext';
import { BrowserRouter } from 'react-router-dom';

// --- Stripe Imports ---
import { loadStripe } from '@stripe/stripe-js';
import { Elements } from '@stripe/react-stripe-js';

const stripePublishableKey = 'pk_test_51RI97KDxw5RRjqxMI5viaALUua9cxmK3iscxCC0eKE3Jg0KWmL9YDPCesI8O7szbwX3LvyXDYa4NkPFi6rsrPFw300Wo9jHMT0';
if (!stripePublishableKey || !stripePublishableKey.startsWith('pk_test_')) {
  console.warn("Stripe Publishable Key is missing or invalid. Please add your test key to main.jsx");
}
const stripePromise = loadStripe(stripePublishableKey);

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <Elements stripe={stripePromise}>
          <App />
        </Elements>
      </AuthProvider>
    </BrowserRouter>
  </React.StrictMode>,
)
