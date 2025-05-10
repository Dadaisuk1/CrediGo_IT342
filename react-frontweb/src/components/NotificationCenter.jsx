import { AnimatePresence, motion } from 'framer-motion';
import { AlarmClock, Bell, Check, Info, ShieldAlert, Trash2 } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { websocketService } from '../services/websocket';

const NotificationCenter = () => {
    const [notifications, setNotifications] = useState([]);
    const [isOpen, setIsOpen] = useState(false);
    const { user } = useAuth();

    // Define unreadCount at the top of the component
    const unreadCount = notifications.filter(n => !n.read).length;

    // Use for animation of notification items
    const notificationVariants = {
        hidden: { opacity: 0, y: -10 },
        visible: i => ({
            opacity: 1,
            y: 0,
            transition: {
                delay: i * 0.05,
                duration: 0.2,
                ease: "easeOut"
            }
        }),
        exit: { opacity: 0, scale: 0.95, transition: { duration: 0.2 } }
    };

    useEffect(() => {
        if (user?.id) {
            websocketService.connect(user.id, (notification) => {
                setNotifications(prev => [{
                    ...notification,
                    id: `${Date.now()}-${Math.random().toString(36).substring(2, 9)}`,
                    timestamp: notification.timestamp || new Date().toISOString(),
                    read: false
                }, ...prev.slice(0, 49)]); // Keep only the 50 most recent notifications
            });
        }

        return () => {
            websocketService.disconnect();
        };
    }, [user?.id]);

    useEffect(() => {
        // Close dropdown when clicking outside
        const handleClickOutside = (event) => {
            if (isOpen && !event.target.closest('.notification-center-container')) {
                setIsOpen(false);
                if (unreadCount > 0) {
                    setNotifications(prev =>
                        prev.map(notification => ({ ...notification, read: true }))
                    );
                }
            }
        };

        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, [isOpen, unreadCount]);

    const toggleNotifications = () => {
        setIsOpen(!isOpen);
        // Mark all as read when closing
        if (isOpen && unreadCount > 0) {
            setNotifications(prev =>
                prev.map(notification => ({ ...notification, read: true }))
            );
        }
    };

    const markAsRead = (id) => {
        setNotifications(prev =>
            prev.map((notification) =>
                notification.id === id ? { ...notification, read: true } : notification
            )
        );
    };

    const removeNotification = (id, event) => {
        event.stopPropagation();
        setNotifications(prev => prev.filter(notification => notification.id !== id));
    };

    const clearAllNotifications = () => {
        setNotifications([]);
        setIsOpen(false);
    };

    const getNotificationIcon = (type) => {
        switch (type?.toUpperCase()) {
            case 'SUCCESS':
                return <Check className="h-5 w-5 text-green-500" />;
            case 'WARNING':
                return <AlarmClock className="h-5 w-5 text-amber-500" />;
            case 'ERROR':
                return <ShieldAlert className="h-5 w-5 text-red-500" />;
            case 'INFO':
            default:
                return <Info className="h-5 w-5 text-blue-500" />;
        }
    };

    const getNotificationColor = (type) => {
        switch (type?.toUpperCase()) {
            case 'SUCCESS':
                return 'bg-green-900/30 hover:bg-green-800/40 border-l-4 border-green-500';
            case 'WARNING':
                return 'bg-amber-900/30 hover:bg-amber-800/40 border-l-4 border-amber-500';
            case 'ERROR':
                return 'bg-red-900/30 hover:bg-red-800/40 border-l-4 border-red-500';
            case 'INFO':
            default:
                return 'bg-blue-900/30 hover:bg-blue-800/40 border-l-4 border-blue-500';
        }
    };

    return (
        <div className="relative notification-center-container">
            <button
                onClick={toggleNotifications}
                className="flex items-center justify-center w-8 h-8 rounded-full bg-gray-800/80 text-gray-300 border border-indigo-500/30 hover:border-indigo-500/70 shadow-sm hover:shadow-md hover:shadow-indigo-900/20 transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-opacity-50"
                aria-label="Notifications"
                title="Notifications"
            >
                <Bell className="h-5 w-5" />
                <AnimatePresence>
                    {unreadCount > 0 && (
                        <motion.span
                            initial={{ scale: 0 }}
                            animate={{ scale: 1 }}
                            exit={{ scale: 0 }}
                            className="absolute top-0 right-0 inline-flex items-center justify-center px-2 py-1 text-xs font-bold leading-none text-white transform translate-x-1/2 -translate-y-1/2 bg-red-500 rounded-full shadow-md"
                        >
                            {unreadCount > 99 ? '99+' : unreadCount}
                        </motion.span>
                    )}
                </AnimatePresence>
            </button>

            <AnimatePresence>
                {isOpen && (
                    <motion.div
                        initial={{ opacity: 0, y: 10, scale: 0.95 }}
                        animate={{ opacity: 1, y: 0, scale: 1 }}
                        exit={{ opacity: 0, scale: 0.95 }}
                        transition={{ duration: 0.2 }}
                        className="absolute right-0 mt-2 w-80 md:w-96 bg-gray-900 rounded-lg shadow-xl overflow-hidden z-50 border border-gray-800/50"
                    >
                        <div className="px-4 py-3 border-b border-gray-800/50 flex justify-between items-center bg-gradient-to-r from-gray-900 to-indigo-950/70">
                            <div className="flex items-center space-x-3">
                                <div className="bg-indigo-900/30 p-2 rounded-full">
                                    <Bell className="h-5 w-5 text-indigo-400" />
                                </div>
                                <div className="flex flex-col">
                                    <span className="font-semibold text-gray-200 text-base">Notifications</span>
                                    <span className="text-xs text-gray-500">{unreadCount} unread</span>
                                </div>
                            </div>
                            {notifications.length > 0 && (
                                <button
                                    onClick={clearAllNotifications}
                                    className="text-sm text-gray-400 hover:text-red-400 flex items-center transition-colors duration-200"
                                >
                                    <Trash2 className="h-4 w-4 mr-1" /> Clear all
                                </button>
                            )}
                        </div>

                        <div className="max-h-96 overflow-y-auto bg-gray-900">
                            {notifications.length === 0 ? (
                                <div className="p-8 text-center text-gray-400">
                                    <div className="bg-indigo-900/30 p-3 rounded-full w-16 h-16 mx-auto mb-4 flex items-center justify-center">
                                        <Bell className="h-8 w-8 text-indigo-400" />
                                    </div>
                                    <p className="font-medium text-gray-300">No notifications yet</p>
                                    <p className="text-sm mt-1">We'll notify you when something happens</p>
                                </div>
                            ) : (
                                <AnimatePresence>
                                    {notifications.map((notification, index) => (
                                        <motion.div
                                            key={notification.id}
                                            custom={index}
                                            variants={notificationVariants}
                                            initial="hidden"
                                            animate="visible"
                                            exit="exit"
                                            onClick={() => markAsRead(notification.id)}
                                            whileHover={{ x: 2 }}
                                            className={`p-4 border-b border-gray-800/50 hover:bg-opacity-80 cursor-pointer transition-all duration-200 ${
                                                !notification.read ? getNotificationColor(notification.type) : 'bg-gray-800/50 hover:bg-gray-700/60'
                                            }`}
                                        >
                                            <div className="flex">
                                                <div className="mr-3 mt-0.5">
                                                    {getNotificationIcon(notification.type)}
                                                </div>
                                                <div className="flex-1">
                                                    <p className={`text-sm ${!notification.read ? 'font-medium text-gray-200' : 'text-gray-300'}`}>
                                                        {notification.message}
                                                    </p>
                                                    <div className="flex justify-between items-center mt-1">
                                                        <span className="text-xs text-gray-500">
                                                            {new Date(notification.timestamp).toLocaleString()}
                                                        </span>
                                                        <button
                                                            onClick={(e) => removeNotification(notification.id, e)}
                                                            className="text-gray-400 hover:text-red-400 transition-colors duration-200"
                                                        >
                                                            <Trash2 className="h-4 w-4" />
                                                        </button>
                                                    </div>
                                                </div>
                                            </div>
                                        </motion.div>
                                    ))}
                                </AnimatePresence>
                            )}
                        </div>

                        {notifications.length > 5 && (
                            <motion.div
                                whileHover={{ backgroundColor: 'rgba(79, 70, 229, 0.1)' }}
                                className="p-3 border-t border-gray-800/50 bg-gradient-to-r from-gray-900 to-indigo-950/70 text-center"
                            >
                                <motion.button
                                    onClick={() => setIsOpen(false)}
                                    whileHover={{ scale: 1.02 }}
                                    whileTap={{ scale: 0.98 }}
                                    className="text-sm text-indigo-400 hover:text-indigo-300 font-medium transition-colors duration-200"
                                >
                                    Close
                                </motion.button>
                            </motion.div>
                        )}
                    </motion.div>
                )}
            </AnimatePresence>
        </div>
    );
};

export default NotificationCenter;
