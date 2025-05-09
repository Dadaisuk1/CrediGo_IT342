const isDevelopment = process.env.NODE_ENV === 'development';

export const API_BASE_URL = isDevelopment
    ? 'http://localhost:8080'
    : 'https://credigo-it342.onrender.com';

export const FRONTEND_URL = isDevelopment
    ? 'http://localhost:5173'  // Default Vite dev server port
    : 'https://credi-go-it-342.vercel.app';
