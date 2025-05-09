import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";

/**
 * A reusable confirmation dialog using shadcn UI components
 *
 * @param {Object} props Component props
 * @param {boolean} props.open Is the dialog open
 * @param {Function} props.setOpen Function to set open state
 * @param {string} props.title Dialog title
 * @param {string} props.description Dialog description/message
 * @param {Function} props.onConfirm Function to call when confirmed
 * @param {string} props.confirmText Text for confirm button (default: "Continue")
 * @param {string} props.cancelText Text for cancel button (default: "Cancel")
 * @param {string} props.confirmVariant Button variant for confirm (default: "default")
 * @returns {JSX.Element} The ConfirmDialog component
 */
const ConfirmDialog = ({
  open,
  setOpen,
  title,
  description,
  onConfirm,
  confirmText = "Continue",
  cancelText = "Cancel",
  confirmVariant = "default"
}) => {
  const handleConfirm = () => {
    setOpen(false);
    if (onConfirm) onConfirm();
  };

  return (
    <AlertDialog open={open} onOpenChange={setOpen}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>{title}</AlertDialogTitle>
          <AlertDialogDescription>{description}</AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel>{cancelText}</AlertDialogCancel>
          <AlertDialogAction
            onClick={handleConfirm}
            className={confirmVariant === "destructive" ? "bg-red-600 hover:bg-red-700" : ""}
          >
            {confirmText}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
};

export default ConfirmDialog;
