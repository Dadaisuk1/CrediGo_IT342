const isDevelopment = process.env.NODE_ENV === 'development';

// Use production URLs by default, fallback to local for development
export const API_BASE_URL = isDevelopment
    ? 'http://localhost:8080'
    : 'https://credigo-it342.onrender.com';

export const FRONTEND_URL = isDevelopment
    ? 'http://localhost:5173'
    : 'https://credi-go-it-342.vercel.app';

// When ready to deploy, uncomment these:
/*
export const API_BASE_URL = isDevelopment
    ? 'http://localhost:8080'
    : 'https://credigo-it342.onrender.com';

export const FRONTEND_URL = isDevelopment
    ? 'http://localhost:5173'
    : 'https://credi-go-it-342.vercel.app';
*/
