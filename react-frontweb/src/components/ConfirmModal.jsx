import ConfirmDialog from '@/components/ui/confirm-dialog';

const ConfirmModal = ({ open, title, message, onConfirm, onCancel }) => {
  return (
    <ConfirmDialog
      open={open}
      setOpen={() => onCancel()}
      title={title}
      description={message}
      onConfirm={onConfirm}
      confirmText="Confirm"
      cancelText="Cancel"
    />
  );
};

export default ConfirmModal;
