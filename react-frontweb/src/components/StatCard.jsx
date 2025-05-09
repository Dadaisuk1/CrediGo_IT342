import { Card, CardContent } from "@/components/ui/card";

const StatCard = ({ title, value, icon, change, iconBgColor }) => (
  <Card className="shadow-sm">
    <CardContent className="p-4 flex items-center space-x-4">
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
    </CardContent>
  </Card>
);

export default StatCard;
