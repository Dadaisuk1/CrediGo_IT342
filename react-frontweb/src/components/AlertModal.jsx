import React from 'react';

const AlertModal = ({ open, title, message, onClose, type = 'info' }) => {
  if (!open) return null;
  // Color by type
  const color = type === 'error' ? 'text-red-600' : type === 'success' ? 'text-green-600' : 'text-[#232946]';
  return (
    <div className="fixed inset-0 z-[9999] flex items-center justify-center w-screen h-screen">
      {/* Backdrop: strong darken and blur, covers the whole viewport */}
      <div className="fixed inset-0 bg-black/70 backdrop-blur-[6px] transition-all duration-200 pointer-events-none" />
      <div className="relative bg-white rounded-2xl shadow-2xl p-8 max-w-sm w-full flex flex-col items-center animate-fade-in pointer-events-auto">
        <h2 className={`text-lg font-bold mb-2 text-center ${color}`}>{title}</h2>
        <p className="text-gray-700 mb-6 text-center">{message}</p>
        <button
          onClick={onClose}
          className="px-6 py-2 rounded bg-[#eebbc3] text-[#232946] hover:bg-[#f2a9b1] font-semibold transition shadow"
        >
          OK
        </button>
      </div>
      <style>{`
        @keyframes fade-in {
          from { opacity: 0; transform: translateY(20px); }
          to { opacity: 1; transform: none; }
        }
        .animate-fade-in { animation: fade-in 0.2s ease; }
      `}</style>
    </div>
  );
};

export default AlertModal;
