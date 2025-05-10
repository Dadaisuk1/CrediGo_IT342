import { Client } from '@stomp/stompjs';
import { toast } from 'react-toastify';
import SockJS from 'sockjs-client';
import { API_BASE_URL } from '../config/api.config';

// Polyfill global for SockJS if needed
if (typeof window !== 'undefined' && !window.global) {
    window.global = window;
}

// Use the backend URL from config, ensure it doesn't have trailing slash
const BACKEND_URL = API_BASE_URL.endsWith('/') ? API_BASE_URL.slice(0, -1) : API_BASE_URL;

// Debug log for WebSocket connection details
console.log('WebSocket will connect to:', `${BACKEND_URL}/ws`);

class WebSocketService {
    constructor() {
        this.client = null;
        this.connected = false;
        this.notificationCallback = null;
        this.connectionAttempts = 0;
        this.maxReconnectAttempts = 3;
    }

    connect(userId, onNotification) {
        if (!userId) {
            console.warn('Cannot connect to WebSocket without user ID');
            return;
        }

        this.notificationCallback = onNotification;
        this.connectionAttempts = 0;

        try {
            // Log the connection attempt
            console.log(`Attempting to connect to WebSocket at ${BACKEND_URL}/ws for user ${userId}`);

            this.client = new Client({
                webSocketFactory: () => new SockJS(`${BACKEND_URL}/ws`),
                onConnect: () => {
                    this.connected = true;
                    this.connectionAttempts = 0;
                    console.log(`Connected to WebSocket at ${BACKEND_URL}/ws`);

                    // Subscribe to user-specific notifications
                    this.client.subscribe(`/user/${userId}/topic/notifications`, (message) => {
                        try {
                            const notification = JSON.parse(message.body);
                            console.log('Received notification:', notification);

                            if (this.notificationCallback) {
                                this.notificationCallback(notification);
                            }

                            // Show toast based on notification type
                            const toastType = notification.type?.toLowerCase() || 'info';
                            if (toast[toastType]) {
                                toast[toastType](notification.message);
                            } else {
                                toast.info(notification.message);
                            }
                        } catch (error) {
                            console.error('Error processing notification:', error);
                        }
                    });

                    // Subscribe to global notifications
                    this.client.subscribe('/topic/global', (message) => {
                        try {
                            const notification = JSON.parse(message.body);
                            console.log('Received global notification:', notification);

                            if (this.notificationCallback) {
                                this.notificationCallback(notification);
                            }

                            // Show toast based on notification type
                            const toastType = notification.type?.toLowerCase() || 'info';
                            if (toast[toastType]) {
                                toast[toastType](notification.message);
                            } else {
                                toast.info(notification.message);
                            }
                        } catch (error) {
                            console.error('Error processing global notification:', error);
                        }
                    });
                },
                onDisconnect: () => {
                    this.connected = false;
                    console.log('Disconnected from WebSocket');
                },
                onError: (error) => {
                    console.error('WebSocket Error:', error);
                    console.error(`Failed to connect to ${BACKEND_URL}/ws`);

                    // Log connection details for debugging
                    console.log('Connection details:', {
                        url: `${BACKEND_URL}/ws`,
                        isProduction,
                        connectionAttempts: this.connectionAttempts
                    });

                    this.connected = false;

                    // Only show error toast on first attempt
                    if (this.connectionAttempts === 0) {
                        toast.error('Notification service connection error');
                    }

                    this.connectionAttempts++;
                    if (this.connectionAttempts < this.maxReconnectAttempts) {
                        console.log(`Attempting to reconnect (${this.connectionAttempts}/${this.maxReconnectAttempts})...`);
                    }
                },
                reconnectDelay: 5000,
                heartbeatIncoming: 4000,
                heartbeatOutgoing: 4000
            });

            try {
                this.client.activate();
            } catch (error) {
                console.error('Failed to activate WebSocket client:', error);
            }
        } catch (error) {
            console.error('Error creating STOMP client:', error);
            toast.error('Cannot initialize notification service');
        }
    }

    disconnect() {
        if (this.client) {
            try {
                if (this.connected) {
                    this.client.deactivate();
                }
            } catch (error) {
                console.error('Error disconnecting WebSocket:', error);
            } finally {
                this.connected = false;
                this.notificationCallback = null;
                this.client = null;
            }
        }
    }

    sendMessage(destination, message) {
        if (this.client && this.connected) {
            try {
                this.client.publish({
                    destination,
                    body: JSON.stringify(message)
                });
            } catch (error) {
                console.error('Error sending WebSocket message:', error);
            }
        }
    }

    isConnected() {
        return this.connected;
    }

    // Helper method to test backend API connectivity
    async testServerConnection() {
        try {
            console.log(`Testing connection to backend at: ${BACKEND_URL}`);
            const response = await fetch(`${BACKEND_URL}/actuator/health`, {
                method: 'GET',
                headers: { 'Accept': 'application/json' },
            });

            if (response.ok) {
                const data = await response.json();
                console.log('Backend server health status:', data);
                return {success: true, data};
            } else {
                console.error('Backend server responded with error:', response.status);
                return {success: false, error: `HTTP ${response.status}`};
            }
        } catch (error) {
            console.error('Failed to connect to backend server:', error);
            return {success: false, error: error.message};
        }
    }
}

export const websocketService = new WebSocketService();
