<script setup lang="ts">
import { ref, computed } from 'vue';
import { useAuthStore } from '../stores/auth';
import { useNotificationStore } from '../stores/notifications';
import apiClient from '../services/NewsService';
import type { Comment } from '../stores/newsInterface';

// ========================================
// Props Definition
// ========================================
interface Props {
  comment: Comment;
}

const props = defineProps<Props>();

// ========================================
// Emits Definition
// ========================================
const emit = defineEmits(['show-full-image', 'comment-deleted']);

// ========================================
// Store Injections
// ========================================
const authStore = useAuthStore();
const notificationStore = useNotificationStore();

// ========================================
// State Management
// ========================================
const isDeleting = ref(false);

// ========================================
// Computed Properties
// ========================================
/**
 * ตรวจสอบว่าผู้ใช้เป็น Admin หรือไม่
 */
const isAdmin = computed(() => authStore.hasRole('ROLE_ADMIN'));

/**
 * ตรวจสอบว่าผู้ใช้ล็อกอินอยู่หรือไม่
 */
const isAuthenticated = computed(() => authStore.isAuthenticated);

/**
 * ดึงชื่อผู้ใช้ปัจจุบัน
 */
const currentUsername = computed(() => authStore.user?.username || '');

/**
 * ตรวจสอบว่าผู้ใช้สามารถลบ comment นี้ได้หรือไม่
 * - เฉพาะ Admin เท่านั้นที่ลบได้
 */
const canDeleteComment = computed(() => {
  return isAdmin.value;
});

// ========================================
// Constants
// ========================================
const defaultProfileImage = 'https://cdn-icons-png.flaticon.com/512/3177/3177440.png';

// ========================================
// Profile Image Methods
// ========================================
/**
 * ฟังก์ชันดึงรูปโปรไฟล์ตาม username
 * - ถ้าเป็น user ปัจจุบัน ใช้รูปจาก authStore
 * - ถ้าเป็น user อื่น ใช้รูป default
 */
const getProfileImage = (username: string): string => {
  if (authStore.user?.username === username && authStore.userProfileImage) {
    return authStore.userProfileImage;
  }

  // เผื่ออนาคตอาจจะดึงจาก API หรือ store ที่เก็บ profile ของ user อื่นๆ
  return defaultProfileImage;
};

/**
 * Handler เมื่อโหลดรูปโปรไฟล์ไม่สำเร็จ
 * แสดงรูป default แทน
 */
const handleProfileImageError = (event: Event) => {
  const img = event.target as HTMLImageElement;
  img.src = defaultProfileImage;
};

// ========================================
// Delete Comment Methods
// ========================================
/**
 * จัดการการลบ comment
 * 1. ตรวจสอบ comment ID
 * 2. ยืนยันการลบ
 * 3. เรียก API ลบ comment
 * 4. แสดงข้อความสำเร็จ/ล้มเหลว
 * 5. Emit event ไปยัง parent component
 */
const handleDeleteComment = async () => {
  if (!props.comment.id) {
    console.error('Comment ID is missing');
    return;
  }

  if (!confirm('Are you sure you want to delete this comment?')) {
    return;
  }

  try {
    isDeleting.value = true;

    // เรียก API ลบ comment
    await apiClient.deleteComment(props.comment.id);

    // แสดงข้อความสำเร็จ
    notificationStore.addNotification('Comment deleted successfully', 'success');

    // Emit event ไปยัง parent component เพื่อโหลดข้อมูลใหม่
    emit('comment-deleted');

  } catch (error: any) {
    console.error('Error deleting comment:', error);

    // แสดงข้อความ error
    const errorMessage = error.response?.data?.message || 'Failed to delete comment';
    notificationStore.addNotification(errorMessage, 'error');

  } finally {
    isDeleting.value = false;
  }
};

// ========================================
// Date/Time Formatting Methods
// ========================================
/**
 * แปลงวันที่เป็นรูปแบบภาษาไทย
 */
const formatDate = (dateString: string): string => {
  try {
    const date = new Date(dateString);
    return date.toLocaleDateString('th-TH', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  } catch (error) {
    return '';
  }
};

/**
 * แปลงเวลาเป็นรูปแบบ 24 ชั่วโมง
 */
const formatTime = (dateString: string): string => {
  try {
    const date = new Date(dateString);
    return date.toLocaleTimeString('th-TH', {
      hour: '2-digit',
      minute: '2-digit'
    });
  } catch (error) {
    return '';
  }
};

// ========================================
// Image Handling Methods
// ========================================
/**
 * Handler เมื่อคลิกที่รูปภาพ
 * Emit event เพื่อแสดงรูปขนาดเต็ม
 */
const handleImageClick = () => {
  if (props.comment.image) {
    emit('show-full-image', props.comment.image);
  }
};

/**
 * Handler เมื่อโหลดรูปภาพไม่สำเร็จ
 * ซ่อนรูปภาพและ log error
 */
const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement;
  img.style.display = 'none';
  console.error('Failed to load image:', props.comment.image);
};


function doLike() {
  /* ... เขียนโค้ดสำหรับกด Like ที่นี่ ... */
  console.log('Liked comment ID:', props.comment.id)
}
function doDislike() {
  /* ... เขียนโค้ดสำหรับกด Dislike ที่นี่ ... */
  console.log('Disliked comment ID:', props.comment.id)
}
function doUndo() {
  /* ... เขียนโค้ดสำหรับยกเลิก Like/Dislike ที่นี่ ... */
  console.log('Undo vote on comment ID:', props.comment.id)
}
</script>

<template>
  <div v-if="comment" class="bg-gray-50 p-4 rounded-lg mb-4 border relative">
    <button
      v-if="canDeleteComment"
      @click="handleDeleteComment"
      class="absolute top-2 right-2 text-gray-400 hover:text-red-600 transition-colors duration-200 p-2 rounded hover:bg-white z-10"
      title="Delete Comment"
      :disabled="isDeleting"
    >
      <svg v-if="!isDeleting" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
        />
      </svg>
      <div v-else class="animate-spin rounded-full h-5 w-5 border-b-2 border-red-600"></div>
    </button>

    <div class="flex items-center justify-between mb-3 pr-10">
      <div class="flex items-center">
        <img
          :src="getProfileImage(comment.username)"
          :alt="`${comment.username}'s profile`"
          class="w-8 h-8 rounded-full mr-3 border border-gray-300 shadow-sm object-cover"
          @error="handleProfileImageError"
        />
        <p class="font-bold text-gray-800">
          {{ comment.username || 'Anonymous' }}
        </p>
      </div>
      <div v-if="comment.time" class="text-sm text-gray-500">
        <span>{{ formatDate(comment.time) }}</span>
        <span class="mx-2">|</span>
        <span>{{ formatTime(comment.time) }}</span>
      </div>
    </div>

    <div class="ml-11">
      <p v-if="comment.text" class="text-gray-700 mb-2">{{ comment.text }}</p>

      <div class="flex items-center text-sm mb-1">
        <span class="text-gray-600">Vote : </span>
        <span
          :class="{
            'text-green-600': comment.vote === 'real',
            'text-red-600': comment.vote === 'fake'
          }"
          class="font-semibold ml-1"
        >
          {{ comment.vote === 'real' ? 'Real' : 'Fake' }}
        </span>

        <div class="ml-auto flex">
          <LikeComment @like="doLike()" @dislike="doDislike()" @undo="doUndo()" />
        </div>
      </div>

      <img
        v-if="comment.image"
        :src="comment.image"
        alt="Comment Image"
        class="mt-4 w-full h-auto rounded-lg max-h-64 object-cover cursor-pointer hover:opacity-90 transition-opacity"
        @click="handleImageClick"
        @error="handleImageError"
      />
    </div>
  </div>
</template>

