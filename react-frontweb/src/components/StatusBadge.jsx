import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";

const statusVariants = {
  success: "bg-green-100 text-green-700 hover:bg-green-100",
  pending: "bg-yellow-100 text-yellow-700 hover:bg-yellow-100",
  failed: "bg-red-100 text-red-700 hover:bg-red-100",
  refunded: "bg-blue-100 text-blue-700 hover:bg-blue-100",
  default: "bg-gray-100 text-gray-700 hover:bg-gray-100",
};

const statusLabels = {
  success: 'Success',
  pending: 'Pending',
  failed: 'Failed',
  refunded: 'Refunded',
};

const StatusBadge = ({ status }) => {
  const normalized = status ? status.toLowerCase() : 'default';
  const variant = statusVariants[normalized] || statusVariants.default;
  const label = statusLabels[normalized] || status;

  return (
    <Badge className={cn(variant)}>
      {label}
    </Badge>
  );
};

export default StatusBadge;
