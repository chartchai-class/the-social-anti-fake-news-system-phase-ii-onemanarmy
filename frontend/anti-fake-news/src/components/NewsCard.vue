<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import type { News } from '../stores/newsInterface';
import { useNewsStore } from '../stores/newsInterface';
import { useMessageStore } from '../stores/message';
import { useAuthStore } from '../stores/auth';

interface Props {
  news: News;
  searchQuery?: string;
}

const props = withDefaults(defineProps<Props>(), {
  searchQuery: ''
});

const emit = defineEmits<{
  (e: 'news-removed', newsId: number): void
}>();

const router = useRouter();
const route = useRoute();
const isNavigating = ref(false);
const authStore = useAuthStore();
const newsStore = useNewsStore();
const messageStore = useMessageStore();

const isAdmin = computed(() => authStore.hasRole('ROLE_ADMIN'));

/**
 * Logic:
 * - Real votes > Fake votes => 'not fake'
 * - Fake votes > Real votes => 'fake'
 * - Equal votes => 'equal'
 */
const statusForDisplay = computed(() => {
  const realVotes = props.news.voteSummary?.real || 0;
  const fakeVotes = props.news.voteSummary?.fake || 0;

  if (realVotes > fakeVotes) {
    return 'not fake';
  } else if (realVotes < fakeVotes) {
    return 'fake';
  } else {
    return 'equal';
  }
});

const handleNewsClick = async () => {
  try {
    isNavigating.value = true;
    const currentQuery = { ...route.query };
    await router.push({
      path: `/news/${props.news.id}`,
      query: {
        from: route.path,    // เก็บ path ต้นทาง
        ...currentQuery      // เก็บ query parameters เดิม
      }
    });
  } catch (error) {
    console.error('Navigation error:', error);
  } finally {
    isNavigating.value = false;  // สิ้นสุดสถานะ loading
  }
};

/**
 * returns ข้อความสถานะ ('Real', 'Fake', หรือ 'Equal')
 */
const getStatusText = () => {
  const realVotes = props.news.voteSummary?.real || 0;
  const fakeVotes = props.news.voteSummary?.fake || 0;

  if (realVotes > fakeVotes) {
    return 'Real';   
  } else if (fakeVotes > realVotes) {
    return 'Fake';
  } else {
    return 'Equal';
  }
};

/**
 * จัดการเมื่อโหลดภาพไม่สำเร็จ
 * ซ่อน element ภาพเพื่อไม่ให้แสดงภาพเสีย
 */
const handleImageError = (event: Event) => {
  const target = event.target as HTMLImageElement;
  target.style.display = 'none';  // ซ่อนภาพเมื่อโหลดไม่สำเร็จ
};

/**
 * ฟอร์แมตวันที่ให้อยู่ในรูปแบบที่อ่านง่าย
 *
 * @param dateString - string วันที่จาก API
 * @returns {string} วันที่ที่ฟอร์แมตแล้ว (เช่น "Jan 15, 2024")
 */
const formatDate = (dateString: string) => {
  const date = new Date(dateString);
  return date.toLocaleDateString('en-US', {
    month: 'short',   // เดือนแบบย่อ (Jan, Feb, ...)
    day: 'numeric',   // วันที่ (1, 2, ...)
    year: 'numeric'   // ปี (2024)
  });
};

/**
 * จัดการการลบข่าว (Admin Only)
 *
 * @param event - MouseEvent เพื่อป้องกันการ propagate ไปยัง parent
 */
const handleDeleteNews = async (event: MouseEvent) => {
  event.stopPropagation(); // ป้องกัน click ลามไปที่ card หลัก

  // ตรวจสอบว่าข่าวมี ID ที่ถูกต้อง
  if (!props.news || props.news.id < 0) {
    console.warn("Cannot remove unsaved or invalid news item.");
    return;
  }

  // ยืนยันการลบกับผู้ใช้
  if (confirm(`Are you sure you want to remove this news: "${props.news.topic}"? \n(It will be hidden from regular users)`)) {
    try {
      // เรียก API ลบข่าว
      await newsStore.removeNews(props.news.id);

      // แสดงข้อความสำเร็จ
      messageStore.updateMessage(`News "${props.news.topic}" removed successfully.`);
      setTimeout(() => messageStore.resetMessage(), 3000);

      // รีเฟรชหน้าเว็บทั้งหมดหลังจากลบสำเร็จ
      setTimeout(() => {
        window.location.reload();
      }, 500);

    } catch (error: unknown) {
      console.error('Error removing news (in component):', error);

      // ข้อความ error
      let errorMsg = 'Failed to remove news.';
      if (typeof error === 'object' && error !== null && 'response' in error) {
        const apiError = error as { response?: { data?: { message?: string } } };
        errorMsg = apiError.response?.data?.message || 'Failed to remove news.';
      } else if (error instanceof Error) {
        errorMsg = error.message;
      }

      messageStore.updateMessage(errorMsg);
      setTimeout(() => messageStore.resetMessage(), 4000);
    }
  }
};
</script>

<template>
  <div
    class="relative block bg-white shadow-lg rounded-3xl overflow-hidden transition-all duration-300 hover:scale-105 cursor-pointer border-2"

    :class="[
      'transition-all duration-300',
      {
        'border-green-600 hover:border-4': statusForDisplay === 'not fake',
        'border-red-600 hover:border-4': statusForDisplay === 'fake',
        'border-gray-500 hover:border-4': statusForDisplay === 'equal',

        // Removed Status (Admin Only) - ลด opacity และไม่ให้ hover scale
        'opacity-60 grayscale hover:scale-100 border-gray-400': news.status === 'removed'
      }
    ]"
    @click="handleNewsClick"
  >
    <button
      v-if="isAdmin && news.status !== 'removed'"
      @click.stop="handleDeleteNews($event)"
      class="absolute top-2 right-2 z-20 p-1.5 bg-red-600 text-white rounded-full shadow-md hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2 transition-transform duration-200 hover:scale-110 opacity-70 group-hover:opacity-100"
      title="Remove News"
    >
      <svg
        xmlns="http://www.w3.org/2000/svg"
        class="h-4 w-4"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
        stroke-width="3"
      >
        <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
      </svg>
    </button>

    <div v-if="news.status === 'removed'" class="absolute top-2 left-2 z-10">
      <span
        class="inline-flex items-center px-2 py-1 rounded-full text-xs bg-rose-100 text-rose-800 font-medium"
      >
        <svg class="w-3 h-3 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L4.082 16.5c-.77.833.192 2.5 1.732 2.5z"
          />
        </svg>
        Removed
      </span>
    </div>

    <div
      v-if="news.image && (news.image.startsWith('http') || news.image.startsWith('https'))"
      class="relative"
    >
      <img
        :src="news.image"
        :alt="news.topic"
        class="w-full h-48 object-cover transition-transform duration-300 hover:scale-105"
        @error="handleImageError"
      />
      <div class="absolute inset-0 bg-black bg-opacity-30 flex items-center justify-center p-4">
        <h3 class="text-white text-lg font-bold text-center drop-shadow-lg line-clamp-2">
          <span>{{ news.topic }}</span>
        </h3>
      </div>
    </div>

    <div
      v-else
      class="relative bg-gradient-to-br from-gray-100 to-gray-200 w-full h-48 flex items-center justify-center"
    >
      <div class="text-center">
        <svg
          class="w-12 h-12 text-gray-400 mx-auto mb-2"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
          ></path>
        </svg>
        <h3 class="text-gray-500 text-lg text-center font-bold px-4 line-clamp-2">
          <span>{{ news.topic }}</span>
        </h3>
      </div>
    </div>

    <div class="p-4 flex justify-between items-center">
      <div class="flex items-center space-x-2">
        <span
          class="px-3 py-1 rounded-full text-xs font-bold transition-colors duration-200"

          :class="{
            'bg-green-100 text-green-700': statusForDisplay === 'not fake',
            'bg-red-100 text-red-700': statusForDisplay === 'fake',
            'bg-gray-200 text-gray-700': statusForDisplay === 'equal'
          }"
        >
          {{ getStatusText() }}
        </span>

        <div class="flex items-center space-x-2 text-xs text-gray-500">
          <span class="flex items-center">
            <svg class="w-3 h-3 mr-1 text-green-500" fill="currentColor" viewBox="0 0 20 20">
              <path
                d="M2 10.5a1.5 1.5 0 113 0v6a1.5 1.5 0 01-3 0v-6zM6 10.333v5.43a2 2 0 001.106 1.79l.05.025A4 4 0 008.943 18h5.416a2 2 0 001.962-1.608l1.2-6A2 2 0 0015.56 8H12V4a2 2 0 00-2-2 1 1 0 00-1 1v.667a4 4 0 01-.8 2.4L6.8 7.933a4 4 0 00-.8 2.4z"
              ></path>
            </svg>
            {{ news.voteSummary?.real || 0 }}
          </span>
          <span class="flex items-center">
            <svg class="w-3 h-3 mr-1 text-red-500" fill="currentColor" viewBox="0 0 20 20">
              <path
                d="M18 9.5a1.5 1.5 0 11-3 0v-6a1.5 1.5 0 013 0v6zM14 9.667v-5.43a2 2 0 00-1.106-1.79l-.05-.025A4 4 0 0011.057 2H5.641a2 2 0 00-1.962 1.608l-1.2 6A2 2 0 004.44 12H8v4a2 2 0 002 2 1 1 0 001-1v-.667a4 4 0 01.8-2.4l1.4-1.866a4 4 0 00.8-2.4z"
              ></path>
            </svg>
            {{ news.voteSummary?.fake || 0 }}
          </span>
        </div>
      </div>

      <div class="flex items-center text-sm text-gray-500 ml-3">
        <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"
          ></path>
        </svg>
        {{ news.comments?.length || 0 }} comments
      </div>
    </div>

    <div class="p-6 pt-0">
      <p class="text-gray-600 mb-4 line-clamp-3 leading-relaxed">
        <span>{{ news.shortDetail }}</span>
      </p>

      <div class="flex justify-between items-center text-sm text-gray-500 mb-4">
        <p class="flex items-center">
          <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <circle cx="12" cy="8" r="4" stroke="currentColor" stroke-width="2" />
            <path
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              d="M4 20c0-3.3 3.6-6 8-6s8 2.7 8 6"
            />
          </svg>
          <span>{{ news.reporter }}</span>
        </p>
        <p class="flex items-center">
          <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <rect x="3" y="4" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2" />
            <path
              d="M16 2v4M8 2v4M3 10h18"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
            />
          </svg>
          {{ formatDate(news.dateTime) }}
        </p>
      </div>

      <div class="mt-6 flex justify-end items-center">
        <span class="text-nike-orange text-sm font-medium flex items-center gap-1">
          Read more
          <svg
            class="w-6 h-4 text-nike-orange"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 36 24"
          >
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 5l7 7-7 7" />
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M17 5l7 7-7 7"
            />
          </svg>
        </span>
      </div>

      </div>
  </div>

  <div
    v-if="isNavigating"
    class="absolute inset-0 bg-white bg-opacity-90 z-10 flex items-center justify-center rounded-xl"
  >
    <div class="flex flex-col items-center">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500 mb-2"></div>
      <span class="text-gray-600 text-sm">Loading news...</span>
    </div>
  </div>
</template>

