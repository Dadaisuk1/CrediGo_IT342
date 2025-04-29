import React from 'react';

const statusColors = {
  success: 'bg-green-100 text-green-700',
  pending: 'bg-yellow-100 text-yellow-700',
  failed: 'bg-red-100 text-red-700',
  refunded: 'bg-blue-100 text-blue-700',
  default: 'bg-gray-100 text-gray-700',
};

const statusLabels = {
  success: 'Success',
  pending: 'Pending',
  failed: 'Failed',
  refunded: 'Refunded',
};

const StatusBadge = ({ status }) => {
  const normalized = status ? status.toLowerCase() : 'default';
  const color = statusColors[normalized] || statusColors.default;
  const label = statusLabels[normalized] || status;
  return (
    <span className={`inline-block px-3 py-1 rounded-full text-xs font-semibold ${color}`}>{label}</span>
  );
};

export default StatusBadge;
