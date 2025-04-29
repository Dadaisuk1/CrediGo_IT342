import { jwtDecode } from 'jwt-decode';

export function isAdmin(token) {
  if (!token) return false;
  try {
    const decoded = jwtDecode(token);
    const roles = decoded.roles;

    if (typeof roles === 'string') {
      return roles === 'ROLE_ADMIN' || roles === 'ADMIN';
    }

    if (Array.isArray(roles)) {
      return roles.includes('ROLE_ADMIN') || roles.includes('ADMIN');
    }

    return false;
  } catch (e) {
    return false;
  }
}
