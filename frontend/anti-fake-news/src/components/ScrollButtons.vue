<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue';

const showScrollToTop = ref(false);
const scrollProgress = ref(0);
const showPercentage = ref(false);
let percentageTimer: number;

const handleScroll = () => {
  const scrollTop = window.pageYOffset;
  const docHeight = document.body.scrollHeight - window.innerHeight;

  showScrollToTop.value = scrollTop > 200;

  // Added check to prevent division by zero
  if (docHeight > 0) {
    scrollProgress.value = (scrollTop / docHeight) * 100;
  } else {
    // If the document is too short to scroll, set progress to 0.
    scrollProgress.value = 0;
  }

  // Show percentage when scrolling and hide after a delay
  showPercentage.value = true;
  clearTimeout(percentageTimer);
  percentageTimer = window.setTimeout(() => {
    showPercentage.value = false;
  }, 1500);
};

const scrollToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth',
  });
};

const scrollToBottom = () => {
  window.scrollTo({
    top: document.body.scrollHeight,
    behavior: 'smooth',
  });
};

onMounted(() => {
  // Call handleScroll once on mount to initialize progress and button visibility
  handleScroll();
  window.addEventListener('scroll', handleScroll);
});

onBeforeUnmount(() => {
  window.removeEventListener('scroll', handleScroll);
  clearTimeout(percentageTimer);
});
</script>

<template>
  <div>
    <transition
      enter-active-class="transition-all duration-300 ease-out"
      enter-from-class="translate-y-4 opacity-0"
      enter-to-class="translate-y-0 opacity-100"
      leave-active-class="transition-all duration-200 ease-in"
      leave-from-class="translate-y-0 opacity-100"
      leave-to-class="translate-y-4 opacity-0"
    >
      <button
        v-if="showScrollToTop"
        @click="scrollToTop"
        class="fixed bottom-6 right-6 p-3 bg-nike-orange hover:bg-nike-yellow text-white rounded-full shadow-lg z-50 transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-white"
        aria-label="Scroll to top"
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="h-5 w-5"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M5 10l7-7m0 0l7 7m-7-7v18"
          />
        </svg>
      </button>
    </transition>

    <transition
      enter-active-class="transition-all duration-300 ease-out"
      enter-from-class="translate-y-4 opacity-0"
      enter-to-class="translate-y-0 opacity-100"
      leave-active-class="transition-all duration-200 ease-in"
      leave-from-class="translate-y-0 opacity-100"
      leave-to-class="translate-y-4 opacity-0"
    >
      <button
        v-if="!showScrollToTop"
        @click="scrollToBottom"
        class="fixed bottom-6 right-6 p-3 bg-gray-600 hover:bg-gray-700 text-white rounded-full shadow-lg z-50 transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-gray-500"
        aria-label="Scroll to bottom"
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="h-5 w-5"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M19 14l-7 7m0 0l-7-7m7 7V3"
          />
        </svg>
      </button>
    </transition>

    <transition
      enter-active-class="transition-all duration-300 ease-out"
      enter-from-class="opacity-0 scale-90"
      enter-to-class="opacity-100 scale-100"
      leave-active-class="transition-all duration-500 ease-in"
      leave-from-class="opacity-100 scale-100"
      leave-to-class="opacity-0 scale-90"
    >
      <div
        v-if="showPercentage"
        class="fixed bottom-20 right-4 px-2 py-1 bg-black/40 text-white text-xs rounded backdrop-blur-sm z-40"
      >
        {{ Math.round(scrollProgress) }}%
      </div>
    </transition>
  </div>
</template>

