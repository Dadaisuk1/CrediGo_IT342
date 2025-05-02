import { default as jwtDecode } from 'jwt-decode';

export function isAdmin(token) {
  if (!token) return false;

  try {
    const decoded = jwtDecode(token);
    const roles = decoded.roles || decoded.authorities;

    if (typeof roles === 'string') {
      return roles.includes('ADMIN');
    }

    if (Array.isArray(roles)) {
      return roles.some(role => role.includes('ADMIN'));
    }

    return false;
  } catch (e) {
    console.error("Failed to decode token:", e);
    return false;
  }
}