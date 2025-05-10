/**
 * Polyfills for Node.js globals in browser environment
 *
 * This file handles the differences between Node.js and browser environments
 * for libraries like SockJS that expect Node.js globals to be available.
 */

// Define global if it doesn't exist
if (typeof window !== 'undefined') {
  // Add global if it doesn't exist
  if (!window.global) {
    window.global = window;
  }

  // Add process if it doesn't exist
  if (!window.process) {
    window.process = {
      env: {},
      nextTick: fn => setTimeout(fn, 0)
    };
  }

  // Add Buffer if it doesn't exist
  if (!window.Buffer) {
    window.Buffer = {
      isBuffer: () => false
    };
  }
}

export default {};
