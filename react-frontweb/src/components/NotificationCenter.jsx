import { Bell } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { websocketService } from '../services/websocket';

const NotificationCenter = () => {
    const [notifications, setNotifications] = useState([]);
    const [isOpen, setIsOpen] = useState(false);
    const { user } = useAuth();

    useEffect(() => {
        if (user) {
            websocketService.connect(user.id, (notification) => {
                setNotifications(prev => [{
                    ...notification,
                    timestamp: new Date().toISOString(),
                    read: false
                }, ...prev]);
            });
        }

        return () => {
            websocketService.disconnect();
        };
    }, [user]);

    const toggleNotifications = () => {
        setIsOpen(!isOpen);
    };

    const markAsRead = (index) => {
        setNotifications(prev =>
            prev.map((notification, i) =>
                i === index ? { ...notification, read: true } : notification
            )
        );
    };

    const clearAllNotifications = () => {
        setNotifications([]);
    };

    const unreadCount = notifications.filter(n => !n.read).length;

    return (
        <div className="relative">
            <button
                onClick={toggleNotifications}
                className="p-2 rounded-full hover:bg-gray-100 focus:outline-none"
            >
                <Bell className="h-6 w-6 text-gray-600" />
                {unreadCount > 0 && (
                    <span className="absolute top-0 right-0 inline-flex items-center justify-center px-2 py-1 text-xs font-bold leading-none text-white transform translate-x-1/2 -translate-y-1/2 bg-red-500 rounded-full">
                        {unreadCount}
                    </span>
                )}
            </button>

            {isOpen && (
                <div className="absolute right-0 mt-2 w-80 bg-white rounded-lg shadow-lg overflow-hidden z-50">
                    <div className="p-4 border-b flex justify-between items-center">
                        <h3 className="text-lg font-semibold">Notifications</h3>
                        {notifications.length > 0 && (
                            <button
                                onClick={clearAllNotifications}
                                className="text-sm text-gray-500 hover:text-gray-700"
                            >
                                Clear all
                            </button>
                        )}
                    </div>
                    <div className="max-h-96 overflow-y-auto">
                        {notifications.length === 0 ? (
                            <div className="p-4 text-center text-gray-500">
                                No new notifications
                            </div>
                        ) : (
                            notifications.map((notification, index) => (
                                <div
                                    key={index}
                                    className={`p-4 border-b hover:bg-gray-50 cursor-pointer ${
                                        !notification.read ? 'bg-blue-50' : ''
                                    }`}
                                    onClick={() => markAsRead(index)}
                                >
                                    <p className="text-sm">{notification.message}</p>
                                    <span className="text-xs text-gray-500">
                                        {new Date(notification.timestamp).toLocaleString()}
                                    </span>
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
