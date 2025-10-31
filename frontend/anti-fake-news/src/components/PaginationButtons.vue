<script setup lang="ts">
import { defineProps, defineEmits, computed } from 'vue';

const props = defineProps<{
  totalItems: number;
  itemsPerPage: number;
  currentPage: number;
}>();

const emit = defineEmits(['page-changed']);

const totalPages = computed<number>(() => Math.ceil(props.totalItems / props.itemsPerPage));
const isFirstPage = computed<boolean>(() => props.currentPage === 1);
const isLastPage = computed<boolean>(() => props.currentPage === totalPages.value);

const prevPage = () => {
  if (!isFirstPage.value) {
    emit('page-changed', props.currentPage - 1);
  }
};

const nextPage = () => {
  if (!isLastPage.value) {
    emit('page-changed', props.currentPage + 1);
  }
};

const goToPage = (page: number) => {
  emit('page-changed', page);
};
</script>

<template>
  <div v-if="totalPages > 1" class="flex justify-center items-center space-x-3">
    <button
      @click="prevPage"
      :disabled="isFirstPage"
      class="px-4 py-2 rounded-full bg-white text-gray-600 disabled:opacity-70 disabled:cursor-not-allowed flex items-center hover:bg-gray-400 hover:text-white hover:scale-115 disabled:hover:bg-white disabled:hover:text-gray-600 hover:scale-110 transition-all duration-500"
    >
      <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
      </svg>
      Previous
    </button>

    <div class="flex space-x-3">
      <button
        v-for="page in totalPages"
        :key="page"
        @click="goToPage(page)"
        :class="{
          'bg-nike-orange text-white font-bold': page === currentPage,
          'bg-white text-gray-600 hover:bg-gray-200': page !== currentPage
        }"
        class="px-3 py-1 rounded-full transition-all duration-200 hover:scale-125"
      >
        {{ page }}
      </button>
    </div>

    <button
      @click="nextPage"
      :disabled="isLastPage"
      class="px-4 py-2 rounded-full bg-white text-gray-600 disabled:opacity-70 disabled:cursor-not-allowed flex items-center hover:bg-gray-400 hover:text-white  disabled:hover:bg-white disabled:hover:text-gray-600 hover:scale-110 transition-all duration-500"
    >
      Next
      <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 ml-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
      </svg>
    </button>
  </div>
</template>

