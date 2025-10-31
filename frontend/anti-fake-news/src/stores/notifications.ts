import { defineStore } from 'pinia';
import { ref } from 'vue';

export type NotificationType = 'success' | 'error' | 'info';

export interface Notification {
  id: number;
  message: string;
  type: NotificationType;
}

export const useNotificationStore = defineStore('notification', () => {
  const notifications = ref<Notification[]>([]);
  let nextId = 0;

  function addNotification(message: string, type: NotificationType = 'info') {
    const notification = { id: nextId++, message, type };
    notifications.value.push(notification);

    setTimeout(() => {
      removeNotification(notification.id);
    }, 3000);
  }

  function removeNotification(id: number) {
    notifications.value = notifications.value.filter((n) => n.id !== id);
  }

  return { notifications, addNotification, removeNotification };
});
