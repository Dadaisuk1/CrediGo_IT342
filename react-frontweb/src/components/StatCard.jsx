import React from 'react';

const StatCard = ({ title, value, icon, change, iconBgColor }) => (
  <div className="bg-white rounded-lg shadow p-4 flex items-center space-x-4">
    <div className={`rounded-full p-3 ${iconBgColor} flex items-center justify-center`}>
      {icon}
    </div>
    <div>
      <div className="text-gray-700 text-xl font-semibold">{value}</div>
      <div className="text-gray-500 text-sm">{title}</div>
      {typeof change === 'number' && (
        <div className={`text-xs mt-1 ${change >= 0 ? 'text-green-600' : 'text-red-600'}`}>
          {change >= 0 ? '+' : ''}{change}%
        </div>
      )}
    </div>
  </div>
);

export default StatCard;
