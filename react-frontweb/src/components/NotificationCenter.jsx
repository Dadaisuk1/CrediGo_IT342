import { AlarmClock, Bell, Check, Info, ShieldAlert, Trash2 } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { websocketService } from '../services/websocket';

const NotificationCenter = () => {
    const [notifications, setNotifications] = useState([]);
    const [isOpen, setIsOpen] = useState(false);
    const { user } = useAuth();

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
                return 'bg-green-50 hover:bg-green-100';
            case 'WARNING':
                return 'bg-amber-50 hover:bg-amber-100';
            case 'ERROR':
                return 'bg-red-50 hover:bg-red-100';
            case 'INFO':
            default:
                return 'bg-blue-50 hover:bg-blue-100';
        }
    };

    const unreadCount = notifications.filter(n => !n.read).length;

    return (
        <div className="relative">
            <button
                onClick={toggleNotifications}
                className="p-2 rounded-full hover:bg-gray-700 focus:outline-none"
                aria-label="Notifications"
            >
                <Bell className="h-6 w-6 text-gray-300" />
                {unreadCount > 0 && (
                    <span className="absolute top-0 right-0 inline-flex items-center justify-center px-2 py-1 text-xs font-bold leading-none text-white transform translate-x-1/2 -translate-y-1/2 bg-red-500 rounded-full">
                        {unreadCount > 99 ? '99+' : unreadCount}
                    </span>
                )}
            </button>

            {isOpen && (
                <div className="absolute right-0 mt-2 w-80 md:w-96 bg-white rounded-lg shadow-lg overflow-hidden z-50">
                    <div className="p-4 border-b flex justify-between items-center bg-gray-50">
                        <h3 className="text-lg font-semibold">Notifications</h3>
                        {notifications.length > 0 && (
                            <button
                                onClick={clearAllNotifications}
                                className="text-sm text-gray-500 hover:text-gray-700 flex items-center"
                            >
                                <Trash2 className="h-4 w-4 mr-1" /> Clear all
                            </button>
                        )}
                    </div>
                    <div className="max-h-96 overflow-y-auto">
                        {notifications.length === 0 ? (
                            <div className="p-6 text-center text-gray-500">
                                <Bell className="h-8 w-8 mx-auto mb-2 text-gray-400" />
                                <p>No notifications yet</p>
                                <p className="text-sm mt-1">We'll notify you when something happens</p>
                            </div>
                        ) : (
                            notifications.map((notification) => (
                                <div
                                    key={notification.id}
                                    onClick={() => markAsRead(notification.id)}
                                    className={`p-4 border-b hover:bg-gray-50 cursor-pointer ${
                                        !notification.read ? getNotificationColor(notification.type) : 'bg-white'
                                    } transition-colors duration-200`}
                                >
                                    <div className="flex">
                                        <div className="mr-3 mt-0.5">
                                            {getNotificationIcon(notification.type)}
                                        </div>
                                        <div className="flex-1">
                                            <p className="text-sm">{notification.message}</p>
                                            <div className="flex justify-between items-center mt-1">
                                                <span className="text-xs text-gray-500">
                                                    {new Date(notification.timestamp).toLocaleString()}
                                                </span>
                                                <button
                                                    onClick={(e) => removeNotification(notification.id, e)}
                                                    className="text-gray-400 hover:text-gray-600"
                                                >
                                                    <Trash2 className="h-4 w-4" />
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            ))
                        )}
                    </div>
                </div>
            )}
        </div>
    );
};

export default NotificationCenter;
