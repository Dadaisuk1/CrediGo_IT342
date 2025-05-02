// src/utils/auth.js
import jwtDecode from 'jwt-decode';

export function isAdmin(token) {
  if (!token) return false;

  try {
    const decoded = jwtDecode(token);
    console.log("Decoded token:", decoded); // Debugging line

    const roles = decoded.roles || decoded.authorities;

    if (typeof roles === 'string') {
      return roles.includes('ADMIN') || roles.includes('ROLE_ADMIN');
    }

    if (Array.isArray(roles)) {
      return roles.some(role =>
        role.includes('ADMIN') || role.includes('ROLE_ADMIN')
      );
    }

    return false;
  } catch (e) {
    console.error("Failed to decode token", e);
    return false;
  }
}