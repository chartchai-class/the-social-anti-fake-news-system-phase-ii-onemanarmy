<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { useLoadingStore } from '../stores/loadingEvents';

const router = useRouter();
const loadingStore = useLoadingStore();

// Handle page reload/refresh
const handleBeforeUnload = () => {
  loadingStore.startLoading();
};

const handleLoad = () => {
  loadingStore.finishLoading();
};

onMounted(() => {
  // Router navigation events
  router.beforeEach((to, from) => {
    // Start loading for all routes except news detail
    if (from.path === '/' && to.path.includes('/news/')) {
      // Don't show progress bar when navigating from home to news detail
      return;
    }
    loadingStore.startLoading();
  });

  router.afterEach((to, from) => {
    // Add a small delay to ensure the page has rendered
    setTimeout(() => {
      if (from.path === '/' && to.path.includes('/news/')) {
        // Don't finish loading when navigating from home to news detail
        return;
      }
      loadingStore.finishLoading();
    }, 100);
  });

  // Page reload/refresh events
  window.addEventListener('beforeunload', handleBeforeUnload);
  window.addEventListener('load', handleLoad);

  // If page is already loading when component mounts
  if (document.readyState === 'loading') {
    loadingStore.startLoading();

    // Listen for DOM content loaded
    document.addEventListener('DOMContentLoaded', () => {
      loadingStore.finishLoading();
    });
  }
});

onUnmounted(() => {
  window.removeEventListener('beforeunload', handleBeforeUnload);
  window.removeEventListener('load', handleLoad);
});
</script>

<template>
  <Transition
    enter-active-class="transition-opacity duration-200"
    leave-active-class="transition-opacity duration-300"
    enter-from-class="opacity-0"
    leave-to-class="opacity-0"
  >
    <div
      v-if="loadingStore.isLoading"
      class="fixed top-0 left-0 w-full h-1 bg-gray-200 z-50 shadow-sm"
    >
      <div
        class="h-full bg-gradient-to-r from-yellow-200 via-nike-yellow to-nike-orange transition-all duration-500 ease-out shadow-sm relative overflow-hidden"
        :style="{ width: `${loadingStore.progress}%` }"
      >
        <div class="absolute inset-0 bg-gradient-to-r from-transparent via-white to-transparent opacity-20 animate-shimmer"></div>
        <div class="h-full bg-gradient-to-r from-transparent to-white opacity-30 animate-pulse"></div>
      </div>
    </div>
  </Transition>
</template>
