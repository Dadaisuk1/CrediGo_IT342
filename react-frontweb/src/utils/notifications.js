import { useToast } from "@/hooks/use-toast";

/**
 * Utility for using shadcn toast notifications globally
 * @returns {Object} notification methods
 */
export const useNotifications = () => {
  const { toast } = useToast();

  return {
    /**
     * Show a success notification
     * @param {string} message - The message to display
     * @param {string} [title="Success"] - Optional title
     */
    success: (message, title = "Success") => {
      toast({
        title,
        description: message,
        variant: "default",
      });
    },

    /**
     * Show an error notification
     * @param {string} message - The message to display
     * @param {string} [title="Error"] - Optional title
     */
    error: (message, title = "Error") => {
      toast({
        title,
        description: message,
        variant: "destructive",
      });
    },

    /**
     * Show an info notification
     * @param {string} message - The message to display
     * @param {string} [title="Information"] - Optional title
     */
    info: (message, title = "Information") => {
      toast({
        title,
        description: message,
      });
    },

    /**
     * Show a warning notification
     * @param {string} message - The message to display
     * @param {string} [title="Warning"] - Optional title
     */
    warning: (message, title = "Warning") => {
      toast({
        title,
        description: message,
        variant: "warning",
      });
    }
  };
};
