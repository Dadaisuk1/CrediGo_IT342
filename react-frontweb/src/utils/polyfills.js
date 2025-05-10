// Polyfills for Node.js globals needed in browser environment

// global
if (typeof window !== 'undefined' && !window.global) {
  window.global = window;
}

// process
if (typeof window !== 'undefined' && !window.process) {
  window.process = { env: {} };
}

// Buffer
if (typeof window !== 'undefined' && !window.Buffer) {
  window.Buffer = {
    isBuffer: () => false
  };
}

export default {};
