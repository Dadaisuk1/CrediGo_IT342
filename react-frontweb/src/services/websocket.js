import { Client } from '@stomp/stompjs';
import { toast } from 'react-toastify';
import SockJS from 'sockjs-client';

class WebSocketService {
    constructor() {
        this.client = null;
        this.connected = false;
        this.notificationCallback = null;
    }

    connect(userId, onNotification) {
        this.notificationCallback = onNotification;

        this.client = new Client({
            webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
            onConnect: () => {
                this.connected = true;
                console.log('Connected to WebSocket');

                // Subscribe to user-specific notifications
                this.client.subscribe(`/user/${userId}/topic/notifications`, (message) => {
                    const notification = JSON.parse(message.body);
                    if (this.notificationCallback) {
                        this.notificationCallback(notification);
                    }
                    toast.info(notification.message);
                });

                // Subscribe to global notifications
                this.client.subscribe('/topic/global', (message) => {
                    const notification = JSON.parse(message.body);
                    if (this.notificationCallback) {
                        this.notificationCallback(notification);
                    }
                    toast.info(notification.message);
                });
            },
            onDisconnect: () => {
                this.connected = false;
                console.log('Disconnected from WebSocket');
            },
            onError: (error) => {
                console.error('WebSocket Error:', error);
                toast.error('Connection error. Please refresh the page.');
            },
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000
        });

        this.client.activate();
    }

    disconnect() {
        if (this.client && this.connected) {
            this.client.deactivate();
            this.notificationCallback = null;
        }
    }

    sendMessage(destination, message) {
        if (this.client && this.connected) {
            this.client.publish({
                destination,
                body: JSON.stringify(message)
            });
        }
    }

    isConnected() {
        return this.connected;
    }
}

export const websocketService = new WebSocketService();
