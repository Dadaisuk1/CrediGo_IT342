const isDevelopment = process.env.NODE_ENV === 'development';

// Since we haven't deployed yet, use local URLs for now
export const API_BASE_URL = 'http://localhost:8080';
export const FRONTEND_URL = 'http://localhost:5173';  // Default Vite dev server port

// When ready to deploy, uncomment these:
/*
export const API_BASE_URL = isDevelopment
    ? 'http://localhost:8080'
    : 'https://credigo-it342.onrender.com';

export const FRONTEND_URL = isDevelopment
    ? 'http://localhost:5173'
    : 'https://credi-go-it-342.vercel.app';
*/
