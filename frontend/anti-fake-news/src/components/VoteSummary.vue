<script setup lang="ts">
import { computed, defineProps } from 'vue';
import type { VoteSummary } from '../stores/newsInterface';

const props = defineProps<{
  voteSummary: VoteSummary;
}>();

const fakeVotes = computed(() => props.voteSummary.fake || 0);
const realVotes = computed(() => props.voteSummary.real || 0);
const totalVotes = computed(() => fakeVotes.value + realVotes.value);

const realPercentage = computed(() => {
  const total = totalVotes.value;
  if (total === 0) return 0;
  return Math.round((realVotes.value / total) * 100);
});

const fakePercentage = computed(() => {
  const total = totalVotes.value;
  if (total === 0) return 0;
  return Math.round((fakeVotes.value / total) * 100);
});

// เอาไว้ render bar width ใหญ่กว่า 100% ถ้าซ้อน จะเหลื่อมออกมี “ทับ”
const overlap = 8 // เปอร์เซ็นต์ overlap

const equalBar = computed(() => 50)
// ถ้า real > fake
const realBarOverlap = computed(() => Math.min(realPercentage.value + overlap, 100))
const fakeBar = computed(() => fakePercentage.value)
// ถ้า fake > real
const fakeBarOverlap = computed(() => Math.min(fakePercentage.value + overlap, 100))
const realBar = computed(() => realPercentage.value)
</script>


<template>
  <div class="bg-gray-100 p-6 rounded-full-xl mb-8">
    <div class="mb-4">
      <h3 class="text-lg font-bold text-gray-800 mb-2">
        Vote Summary :
      </h3>
    </div>

    <!-- Label -->
    <div class="flex justify-between items-center mb-3 px-1">
      <span class="text-base font-medium text-gray-900 flex items-center gap-1">
        Real
        <!-- Checkmark SVG (green) -->
        <svg class="w-5 h-5 text-green-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7" />
        </svg>
      </span>
      <span class="text-base font-medium text-gray-900 flex items-center gap-1">
        <!-- X Mark SVG (red) -->
        <svg class="w-5 h-5 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
        </svg>
        Fake
      </span>
    </div>

    <!-- แถบ bar -->
    <div class="relative w-full bg-gray-200 rounded-full h-5 overflow-visible mb-3" style="z-index:0;">
      <!-- real > fake: real bar ทับ fake เลย -->
      <template v-if="realVotes > fakeVotes">
        <div class="absolute left-0 top-0 h-5 bg-green-500 rounded-full transition-all duration-200"
             :style="{ width: realBarOverlap + '%', zIndex: 20 }"></div>
        <div class="absolute right-0 top-0 h-5 bg-red-500 rounded-full transition-all duration-200"
             :style="{ width: fakeBar + '%', zIndex: 10 }"></div>
      </template>
      <!-- fake > real: fake bar ทับ real เลย -->
      <template v-else-if="fakeVotes > realVotes">
        <div class="absolute right-0 top-0 h-5 bg-red-500 rounded-full transition-all duration-200"
             :style="{ width: fakeBarOverlap + '%', zIndex: 20 }"></div>
        <div class="absolute left-0 top-0 h-5 bg-green-500 rounded-full transition-all duration-200"
             :style="{ width: realBar + '%', zIndex: 10 }"></div>
      </template>
      <!-- เท่ากัน: สอง bar ติดกันพอดีไม่มี overlap -->
      <template v-else>
        <div class="absolute left-0 top-0 h-5 bg-green-500 rounded-l-full transition-all duration-200"
             :style="{ width: equalBar + '%', zIndex: 10 }"></div>
        <div class="absolute right-0 top-0 h-5 bg-red-500 rounded-r-full transition-all duration-200"
             :style="{ width: equalBar + '%', zIndex: 10 }"></div>
      </template>
    </div>
    <div class="flex justify-between items-center px-1 mt-1">
      <span class="text-sm font-semibold text-gray-700">
        {{ realPercentage }}% ({{ realVotes }} votes)
      </span>
      <span class="text-sm font-semibold text-gray-700">
        {{ fakePercentage }}% ({{ fakeVotes }} votes)
      </span>
    </div>

    <div class="text-right border-t pt-3 mt-4">
      <span class="text-sm font-semibold text-gray-800">Total: {{ totalVotes }} Votes</span>
    </div>
  </div>
</template>

