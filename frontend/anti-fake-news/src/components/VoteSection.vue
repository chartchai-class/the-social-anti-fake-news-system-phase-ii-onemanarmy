<script setup lang="ts">
import { ref, computed, nextTick } from 'vue';
import { useNewsStore } from '../stores/newsInterface';
import { useAuthStore } from '../stores/auth';
import { useNotificationStore } from '../stores/notifications';
import type { News } from '../stores/newsInterface';
import ImageUpload from './ImageUpload.vue';
import AddEmojiButton from '@/components/AddEmojiButton.vue'

const props = defineProps<{ news: News }>();
const emit = defineEmits<{ 'vote-success': [] }>();

const newsStore = useNewsStore();
const authStore = useAuthStore();
const notificationStore = useNotificationStore();

const selectedVote = ref<'fake' | 'real' | null>(null);
const voterComment = ref<string>('');
const voterImages = ref<string[]>([]);
const isSubmitting = ref(false);

const commentTextarea = ref<HTMLTextAreaElement | null>(null)

const username = computed(() => authStore.user?.username || 'Anonymous');
const voterImage = computed(() => voterImages.value[0] || '');
const fakeVotes = computed(() => props.news.voteSummary?.fake || 0);
const realVotes = computed(() => props.news.voteSummary?.real || 0);
const totalVotes = computed(() => fakeVotes.value + realVotes.value);

/**
 * คำนวณเปอร์เซ็นต์โหวต Real
 */
const realPercentage = computed(() => {
  if (totalVotes.value === 0) return 0;
  return Math.round((realVotes.value / totalVotes.value) * 100);
});

/**
 * คำนวณเปอร์เซ็นต์โหวต Fake
 */
const fakePercentage = computed(() => {
  if (totalVotes.value === 0) return 0;
  return Math.round((fakeVotes.value / totalVotes.value) * 100);
});

function insertEmoji(emoji: string) {
  const textarea = commentTextarea.value
  if (textarea) {
    const start = textarea.selectionStart
    const end = textarea.selectionEnd
    voterComment.value =
      voterComment.value.slice(0, start) + emoji + voterComment.value.slice(end)
    nextTick(() => {
      textarea.focus()
      textarea.selectionStart = textarea.selectionEnd = start + emoji.length
    })
  } else {
    voterComment.value += emoji
  }
}

// ========================================
// Scroll Position Management
// ========================================
/**
 * บันทึกตำแหน่ง scroll ปัจจุบันลง sessionStorage
 */
const saveScrollPosition = () => {
  sessionStorage.setItem('voteScrollPosition', window.scrollY.toString());
};

/**
 * กู้คืนตำแหน่ง scroll จาก sessionStorage
 */
const restoreScrollPosition = () => {
  const savedPosition = sessionStorage.getItem('voteScrollPosition');
  if (savedPosition) {
    setTimeout(() => {
      window.scrollTo({
        top: parseInt(savedPosition),
        behavior: 'smooth'
      });
      sessionStorage.removeItem('voteScrollPosition');
    }, 100);
  }
};

// ========================================
// Methods
// ========================================
/**
 * เลื่อนไปที่ตำแหน่งเดิมหลังโหวตเสร็จ
 */
const scrollToPreviousPosition = async () => {
  // บันทึกตำแหน่งปัจจุบันก่อนเปลี่ยน tab
  saveScrollPosition();

  // รอให้ tab เปลี่ยนเป็น Comments และข้อมูลโหลดเสร็จ
  await nextTick();

  // รออีกนิดเพื่อให้แน่ใจว่า Comments section โหลดเสร็จ
  setTimeout(() => {
    restoreScrollPosition();
  }, 800);
};

/**
 * ส่งโหวต
 */
const submitVote = async () => {
  if (!selectedVote.value) {
    notificationStore.addNotification('Please select your vote (Real or Fake)', 'error');
    return;
  }

  isSubmitting.value = true;

  try {
    // ตรวจสอบว่าเป็นข่าวที่บันทึกแล้ว (id > 0) หรือยัง
    if (props.news.id > 0) {
      // ส่งไปยัง API
      const result = await newsStore.submitComment(props.news.id, {
        username: username.value,
        text: voterComment.value,
        image: voterImage.value,
        vote: selectedVote.value
      });

      if (result.success) {
        notificationStore.addNotification('Vote submitted successfully!', 'success');
        resetForm();
        emit('vote-success');
        await scrollToPreviousPosition();
      } else {
        notificationStore.addNotification(result.error || 'Failed to submit vote', 'error');
      }
    } else {
      // ใช้ local method สำหรับ unsaved news
      newsStore.addCommentToNews(
        props.news.id,
        username.value,
        voterComment.value,
        voterImage.value || null,
        selectedVote.value
      );
      resetForm();
      emit('vote-success');
      await scrollToPreviousPosition();
    }
  } catch (error) {
    console.error('Error submitting vote:', error);
    notificationStore.addNotification('Failed to submit vote. Please try again.', 'error');
  } finally {
    isSubmitting.value = false;
  }
};

/**
 * รีเซ็ตฟอร์ม
 */
const resetForm = () => {
  selectedVote.value = null;
  voterComment.value = '';
  voterImages.value = [];
};
</script>

<template>
  <div v-if="news" class="grid grid-cols-1 lg:grid-cols-2 gap-8">

    <div class="bg-gray-100 p-6 rounded-xl">

      <div class="mb-6">
        <label class="block text-lg font-semibold text-gray-800 mb-4">vote:</label>
        <div class="flex space-x-4">
          <button
            @click="selectedVote = 'real'"
            :class="{
              'bg-green-500 text-white': selectedVote === 'real',
              'bg-green-100 text-green-600 hover:bg-green-200': selectedVote !== 'real'
            }"
            class="px-6 py-2 rounded-full font-bold transition-colors duration-200 flex items-center gap-1"
          >
            <svg
              :class="selectedVote === 'real' ? 'text-white' : 'text-green-500'"
              class="w-6 h-6"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
              stroke-width="4"
            >
              <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7" />
            </svg>
            REAL
          </button>

          <button
            @click="selectedVote = 'fake'"
            :class="{
              'bg-red-500 text-white': selectedVote === 'fake',
              'bg-red-100 text-red-600 hover:bg-red-200': selectedVote !== 'fake'
            }"
            class="px-4 py-2 rounded-full font-bold transition-colors duration-200 flex items-center gap-1"
          >
            <svg
              :class="selectedVote === 'fake' ? 'text-white' : 'text-red-500'"
              class="w-6 h-6"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
              stroke-width="4"
            >
              <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
            </svg>
            FAKE
          </button>
        </div>
      </div>

      <div class="mb-4">
        <div class="w-full p-3 border border-gray-300 bg-gray-50 rounded-lg text-gray-700 font-medium">
          Voting as: {{ username }}
        </div>
      </div>

      <div class="mb-4">
        <div class="flex items-center mb-2">
          <span class="mr-2 text-sm text-gray-600">Insert Emoji:</span>
          <AddEmojiButton @select="insertEmoji" />
        </div>
        <textarea
          ref="commentTextarea" v-model="voterComment"
          placeholder="Write a comment... (optional)"
          rows="4"
          class="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
        ></textarea>
      </div>

      <div class="mb-6">
        <ImageUpload v-model="voterImages" :max-files="1" />
      </div>

      <button
        @click="submitVote"
        :disabled="!selectedVote || isSubmitting"
        class="w-full bg-nike-orange text-white font-bold py-3 rounded-lg hover:bg-nike-yellow disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors duration-200"
      >
        {{ isSubmitting ? 'Submitting...' : 'Submit' }}
      </button>
    </div>

    <div>
      <div class="bg-white p-6 rounded-xl border">
        <div class="mb-4">
          <h3 class="text-lg font-bold text-gray-800 mb-2">
            Vote Summary :
            <span
              :class="{ 'bg-red-600': fakeVotes > realVotes, 'bg-green-600': realVotes > fakeVotes, 'bg-gray-500': realVotes === fakeVotes }"
              class="text-white font-bold px-3 py-1 rounded text-sm uppercase ml-2"
            >
              {{ fakeVotes > realVotes ? 'FAKE' : realVotes > fakeVotes ? 'REAL' : 'EQUAL' }}
            </span>
          </h3>
        </div>

        <div class="mb-4">
          <div class="flex items-center justify-between mb-2">
            <span class="text-sm font-medium text-gray-700">Real</span>
            <span class="text-sm text-gray-600">{{ realPercentage }}% ({{ realVotes }} votes)</span>
          </div>
          <div class="w-full bg-gray-200 rounded-full h-4">
            <div class="bg-green-500 h-4 rounded-full transition-all duration-300" :style="{ width: `${realPercentage}%` }"></div>
          </div>
        </div>

        <div class="mb-4">
          <div class="flex items-center justify-between mb-2">
            <span class="text-sm font-medium text-gray-700">Fake</span>
            <span class="text-sm text-gray-600">{{ fakePercentage }}% ({{ fakeVotes }} votes)</span>
          </div>
          <div class="w-full bg-gray-200 rounded-full h-4">
            <div class="bg-red-500 h-4 rounded-full transition-all duration-300" :style="{ width: `${fakePercentage}%` }"></div>
          </div>
        </div>

        <div class="text-right border-t pt-3 mt-4">
          <span class="text-sm font-semibold text-gray-800">Total: {{ totalVotes }} Votes</span>
        </div>
      </div>
    </div>
  </div>
</template>

