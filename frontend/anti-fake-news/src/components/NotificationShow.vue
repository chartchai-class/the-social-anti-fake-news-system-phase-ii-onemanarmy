<script setup lang="ts">
import { storeToRefs } from 'pinia';
import { useNotificationStore, type NotificationType } from '../stores/notifications';

const notificationStore = useNotificationStore();
const { notifications } = storeToRefs(notificationStore);

const getStatusClass = (type: NotificationType) => {
  switch (type) {
    case 'success':
      return 'bg-green-600';
    case 'error':
      return 'bg-red-600';
    case 'info':
      return 'bg-blue-600';
    default:
      return 'bg-gray-600';
  }
};
</script>

<template>
  <div class="fixed top-6 right-6 z-50">
    <transition-group
      name="toast-list"
      tag="div"
      class="space-y-2 w-60"
      enter-active-class="transition ease-out duration-300"
      enter-from-class="transform opacity-0 translate-x-full"
      enter-to-class="transform opacity-100 translate-x-0"
      leave-active-class="transition ease-in duration-300 absolute"
      leave-from-class="transform opacity-100 translate-x-0"
      leave-to-class="transform opacity-0 translate-x-full"
    >
      <div
        v-for="notification in notifications"
        :key="notification.id"
        class="p-3 pl-5 rounded-full shadow-lg text-white font-semibold flex items-center space-x-2 transition-all relative"
        :class="getStatusClass(notification.type)"
      >
        <div class="flex-shrink-0">
          <svg v-if="notification.type === 'success'" class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
          </svg>
          <svg v-if="notification.type === 'error'" class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">
            <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clip-rule="evenodd" />
          </svg>
          <svg v-if="notification.type === 'info'" class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">
            <path d="M10 18a8 8 0 100-16 8 8 0 000 16zm-1-8V6h2v2H9z" />
            <path d="M10 12a1 1 0 100 2 1 1 0 000-2z" />
          </svg>
        </div>

        <div class="flex-grow text-sm">
          {{ notification.message }}
        </div>

      </div>
    </transition-group>
  </div>
</template>

