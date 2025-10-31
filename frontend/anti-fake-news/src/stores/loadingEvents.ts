import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useLoadingStore = defineStore('loading', () => {
  const isLoading = ref(false);
  const progress = ref(0);

  let progressInterval: ReturnType<typeof setTimeout> | null = null

  const startLoading = () => {
    isLoading.value = true;
    progress.value = 0;

    if (progressInterval) {
      clearInterval(progressInterval);
    }

    progressInterval = setInterval(() => {
      if (progress.value < 90) {
        progress.value += Math.random() * 15;
        if (progress.value > 90) {
          progress.value = 90;
        }
      }
    }, 150);
  };

  const finishLoading = () => {
    progress.value = 100;

    if (progressInterval) {
      clearInterval(progressInterval);
      progressInterval = null;
    }

    setTimeout(() => {
      isLoading.value = false;
      progress.value = 0;
    }, 400);
  };

  const setProgress = (value: number) => {
    progress.value = Math.min(100, Math.max(0, value));
  };

  return {
    isLoading,
    progress,
    startLoading,
    finishLoading,
    setProgress
  };
});
