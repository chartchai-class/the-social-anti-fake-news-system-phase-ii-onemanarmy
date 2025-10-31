<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted } from 'vue';
import Pagination from './PaginationButtons.vue';
import VoteSummary from './VoteSummary.vue';
import CommentItem from './CommentItem.vue';
import type { Comment, VoteSummary as VoteSummaryType } from '../stores/newsInterface';

// ========================================
// Props Definition
// ========================================
interface Props {
  comments: Comment[] | null | undefined;  // รายการ comments ทั้งหมด
  voteSummary: VoteSummaryType;            // ข้อมูลสรุปคะแนนโหวต
}

const props = withDefaults(defineProps<Props>(), {
  comments: () => [],
  voteSummary: () => ({ real: 0, fake: 0 })
});

// ========================================
// Emits Definition
// ========================================
const emit = defineEmits(['comment-deleted']);  // Event สำหรับแจ้ง parent เมื่อมีการลบ comment

// ========================================
// State Management
// ========================================
const commentPage = ref<number>(1);                         // หน้าปัจจุบันของ pagination
const commentsPerPage = ref<number>(3);                     // จำนวน comments ต่อหน้า
const filterOption = ref<'all' | 'real' | 'fake'>('all');  // ตัวกรอง: all, real, หรือ fake
const sortOption = ref<'newest' | 'oldest'>('newest');     // การเรียงลำดับ: ใหม่สุดหรือเก่าสุด
const showImagePopup = ref<boolean>(false);                 // สถานะแสดง/ซ่อน image popup
const selectedImage = ref<string | null>(null);             // URL รูปภาพที่เลือก

// ========================================
// Scroll Position Management
// ========================================
/**
 * บันทึกตำแหน่ง scroll ปัจจุบันลง sessionStorage
 * ใช้เมื่อก่อนรีเฟรชหน้า เพื่อให้กลับมาที่ตำแหน่งเดิมได้
 */
const saveScrollPosition = () => {
  sessionStorage.setItem('commentScrollPosition', window.scrollY.toString());
};

/**
 * กู้คืนตำแหน่ง scroll จาก sessionStorage
 * เรียกใช้หลังจากหน้าโหลดเสร็จ เพื่อเลื่อนกลับไปที่ตำแหน่งเดิม
 */
const restoreScrollPosition = () => {
  const savedPosition = sessionStorage.getItem('commentScrollPosition');
  if (savedPosition) {
    setTimeout(() => {
      window.scrollTo({
        top: parseInt(savedPosition),
        behavior: 'smooth'  // เลื่อนแบบ smooth animation
      });
      // ลบข้อมูลหลังจากใช้งานแล้ว
      sessionStorage.removeItem('commentScrollPosition');
    }, 100);
  }
};

// ========================================
// Event Handlers
// ========================================
/**
 * Handler เมื่อ comment ถูกลบ
 * 1. บันทึกตำแหน่ง scroll ปัจจุบัน
 * 2. แจ้ง parent component
 * 3. รีเฟรชหน้าเว็บ (จะกลับมาที่ตำแหน่งเดิมอัตโนมัติ)
 */
const handleCommentDeleted = () => {
  saveScrollPosition();
  emit('comment-deleted');

  setTimeout(() => {
    window.location.reload();
  }, 500);
};

/**
 * เปิด popup แสดงรูปภาพขนาดเต็ม
 */
const showFullImage = (imageUrl: string) => {
  selectedImage.value = imageUrl;
  showImagePopup.value = true;
};

/**
 * ปิด popup รูปภาพ
 */
const closePopup = () => {
  showImagePopup.value = false;
  selectedImage.value = null;
};

// ========================================
// Lifecycle Hooks
// ========================================
/**
 * เมื่อ component mount เสร็จ
 * - กู้คืนตำแหน่ง scroll (กรณีรีเฟรชหน้า)
 */
onMounted(() => {
  restoreScrollPosition();
});

// ========================================
// Watchers
// ========================================
/**
 * Watch การเปลี่ยนหน้า pagination
 * เมื่อเปลี่ยนหน้า ให้เลื่อนลงไปด้านล่างสุดของหน้า
 */
watch(commentPage, async () => {
  await nextTick();
  setTimeout(() => {
    window.scrollTo({
      top: document.body.scrollHeight,
      behavior: 'smooth'
    });
  }, 100);
});

/**
 * Watch การเปลี่ยน filter หรือ sort option
 * เมื่อเปลี่ยน filter/sort ให้รีเซ็ตกลับไปหน้าแรก
 */
watch([filterOption, sortOption], () => {
  commentPage.value = 1;
});

/**
 * Watch การเปลี่ยนแปลงของ comments
 * ถ้าหน้าปัจจุบันเกินจำนวนหน้าที่มี ให้กลับไปหน้าสุดท้าย
 */
watch(() => props.comments, () => {
  const totalPages = Math.ceil(filteredAndSortedComments.value.length / commentsPerPage.value);
  if (commentPage.value > totalPages && totalPages > 0) {
    commentPage.value = totalPages;
  }
}, { deep: true });

// ========================================
// Computed Properties
// ========================================
/**
 * กรอง comments ที่มีเนื้อหา (text หรือ image)
 * เอาเฉพาะ comments ที่มีข้อมูลจริงๆ ไม่ใช่ค่าว่าง
 */
const commentsWithContent = computed(() => {
  if (!Array.isArray(props.comments)) return [];

  return props.comments.filter(
    (c) => (c.text && c.text.trim().length > 0) || (c.image && c.image.trim().length > 0)
  );
});

/**
 * Comments ที่ผ่านการกรองและเรียงลำดับแล้ว
 * 1. กรองตาม filterOption (all/real/fake)
 * 2. เรียงลำดับตาม sortOption (newest/oldest)
 */
const filteredAndSortedComments = computed<Comment[]>(() => {
  let filtered = [...commentsWithContent.value];

  // กรองตาม vote type
  if (filterOption.value !== 'all') {
    filtered = filtered.filter((c) => c.vote === filterOption.value);
  }

  // เรียงลำดับตามเวลา
  return filtered.sort((a, b) => {
    const timeA = new Date(a.time).getTime();
    const timeB = new Date(b.time).getTime();
    return sortOption.value === 'newest' ? timeB - timeA : timeA - timeB;
  });
});

/**
 * Comments สำหรับหน้าปัจจุบัน (Pagination)
 * นำ comments ที่ filter/sort แล้ว มาตัดเฉพาะส่วนของหน้าปัจจุบัน
 */
const paginatedComments = computed<Comment[]>(() => {
  const start = (commentPage.value - 1) * commentsPerPage.value;
  const end = start + commentsPerPage.value;
  return filteredAndSortedComments.value.slice(start, end);
});
</script>

<template>
  <div ref="commentsSection">
    <!-- <VoteSummary :vote-summary="voteSummary" /> -->
    <div class="flex flex-col sm:flex-row items-start sm:items-center justify-end gap-4 mb-4">
      <h3 class="text-2xl font-bold text-gray-800 flex-1 text-left">Comments :</h3>
      <div class="flex items-center gap-2 w-full sm:w-auto">
        <label for="filter-select" class="text-gray-700 font-medium whitespace-nowrap">Filter by:</label>
        <div class="relative w-full">
          <select
            id="filter-select"
            v-model="filterOption"
            class="block w-full appearance-none rounded-md border border-gray-300 bg-white py-2 pl-3 pr-8 text-sm text-gray-700 shadow-sm focus:border-nike-orange focus:outline-none focus:ring-1 focus:ring-nike-orange"
          >
            <option value="all">All Comments</option>
            <option value="real">Real</option>
            <option value="fake">Fake</option>
          </select>
          <div class="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-2 text-gray-700">
            <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path>
            </svg>
          </div>
        </div>
      </div>

      <div class="flex items-center gap-2 w-full sm:w-auto">
        <label for="sort-select" class="text-gray-700 font-medium whitespace-nowrap">Sort by:</label>
        <div class="relative w-full">
          <select
            id="sort-select"
            v-model="sortOption"
            class="block w-full appearance-none rounded-md border border-gray-300 bg-white py-2 pl-3 pr-8 text-sm text-gray-700 shadow-sm focus:border-nike-orange focus:outline-none focus:ring-1 focus:ring-nike-orange"
          >
            <option value="newest">Newest</option>
            <option value="oldest">Oldest</option>
          </select>
          <div class="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-2 text-gray-700">
            <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path>
            </svg>
          </div>
        </div>
      </div>
    </div>

    <div v-if="filteredAndSortedComments.length > 0">
      <CommentItem
        v-for="(comment, index) in paginatedComments"
        :key="`${comment.id}-${index}-${commentPage}`"
        :comment="comment"
        @comment-deleted="handleCommentDeleted"
        @show-full-image="showFullImage"
      />

      <Pagination
        :total-items="filteredAndSortedComments.length"
        :items-per-page="commentsPerPage"
        :current-page="commentPage"
        @page-changed="commentPage = $event"
        class="mt-6"
      />
    </div>
    <p v-else class="text-gray-500 italic">No comments match the filter.</p>

    <div v-if="showImagePopup" class="fixed inset-0 bg-black bg-opacity-75 flex items-center justify-center z-50 p-4" @click="closePopup">
      <div class="relative" @click.stop>
        <button
          @click="closePopup"
          class="absolute -top-10 right-0 text-white text-4xl font-light hover:text-gray-300 transition-colors"
          aria-label="Close"
        >
          &times;
        </button>
        <img
          v-if="selectedImage"
          :src="selectedImage"
          alt="Full size comment image"
          class="max-w-[90vw] max-h-[90vh] object-contain rounded-lg shadow-xl"
        />
      </div>
    </div>
  </div>
</template>

