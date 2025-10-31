<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useRoute } from 'vue-router';
import { useNotificationStore } from '../stores/notifications';
import { useAuthStore, type Role } from '../stores/auth';
import CommentsSection from '../components/CommentsVotes.vue';
import VoteSection from '../components/VoteSection.vue';
import apiClient from '../services/NewsService';

const route = useRoute();
const notificationStore = useNotificationStore();
const authStore = useAuthStore();

const newsId = parseFloat(route.params.id as string);

const news = ref<any>(null);
const activeTab = ref<'comments' | 'vote'>('comments');
const isLoading = ref(false);
const isTabSwitching = ref(false);
const pendingTab = ref<'comments' | 'vote' | null>(null);
const loadingProgress = ref(0);

const allowedVoteRoles: Role[] = ['ROLE_READER', 'ROLE_MEMBER', 'ROLE_ADMIN'];

const canDeleteNews = computed(() => authStore.hasRole('ROLE_ADMIN'));
const canViewVoteTab = computed(() => authStore.hasAnyRole(allowedVoteRoles));
const canViewRemovedNews = computed(() => authStore.hasRole('ROLE_ADMIN'));
const isNewsRemoved = computed(() => news.value?.removed === true);

const newsStatus = computed(() => {
  if (!news.value) return 'equal';
  const realVotes = news.value.voteSummary?.real || 0;
  const fakeVotes = news.value.voteSummary?.fake || 0;
  if (realVotes > fakeVotes) return 'not fake';
  if (realVotes < fakeVotes) return 'fake';
  return 'equal';
});

const isNewsAccessible = computed(() => {
  if (!news.value) return false;
  return !isNewsRemoved.value || canViewRemovedNews.value;
});

const backToHomeUrl = computed(() => {
  const referrer = document.referrer;
  const currentOrigin = window.location.origin;
  if (referrer.includes(currentOrigin) && referrer.includes('?')) {
    const url = new URL(referrer);
    return { path: '/', query: Object.fromEntries(url.searchParams) };
  }
  return '/';
});

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric'
  })
}

let progressInterval: ReturnType<typeof setInterval> | null = null;

const startLoadingProgress = () => {
  loadingProgress.value = 0;
  if (progressInterval) clearInterval(progressInterval);
  progressInterval = setInterval(() => {
    if (loadingProgress.value < 90) {
      loadingProgress.value += Math.random() * 15;
      if (loadingProgress.value > 90) loadingProgress.value = 90;
    }
  }, 150);
};

const finishLoadingProgress = () => {
  loadingProgress.value = 100;
  if (progressInterval) {
    clearInterval(progressInterval);
    progressInterval = null;
  }
};

const loadNewsDetail = async () => {
  try {
    isLoading.value = true;
    startLoadingProgress();
    const response = await apiClient.getNewsById(newsId);
    news.value = response.data;
    finishLoadingProgress();
  } catch (error) {
    console.error('Error loading news detail:', error);
    notificationStore.addNotification('Error loading news details', 'error');
  } finally {
    setTimeout(() => {
      isLoading.value = false;
      loadingProgress.value = 0;
    }, 400);
  }
};

const handleNewsDeletion = async () => {
  try {
    isLoading.value = true;
    await loadNewsDetail();
    notificationStore.addNotification('News has been removed successfully', 'success');
  } catch (error) {
    console.error('Error refreshing news after deletion:', error);
    notificationStore.addNotification('Error refreshing news data', 'error');
  } finally {
    isLoading.value = false;
  }
};

const switchTab = async (tab: 'comments' | 'vote') => {
  if (activeTab.value === tab || isTabSwitching.value) return;
  if (tab === 'vote' && !canViewVoteTab.value) {
    notificationStore.addNotification('You do not have permission to vote on this news.', 'error');
    return;
  }
  try {
    isTabSwitching.value = true;
    pendingTab.value = tab;
    await new Promise(resolve => setTimeout(resolve, 300));
    activeTab.value = tab;
  } finally {
    isTabSwitching.value = false;
    pendingTab.value = null;
  }
};

const handleVoteSuccess = async () => {
  try {
    await loadNewsDetail();
    await new Promise(resolve => setTimeout(resolve, 500));
    await switchTab('comments');
  } catch (error) {
    console.error('Error after vote submission:', error);
  }
};

watch(canViewVoteTab, (value) => {
  if (!value && activeTab.value === 'vote') {
    activeTab.value = 'comments';
  }
});

onMounted(() => {
  authStore.hydrateFromStorage();
  loadNewsDetail();
});
</script>

<template>
  <div class="container mx-auto p-4 font-oswald bg-gradient-to-b from-black via-gray-900 to-black min-h-screen">

    <div v-if="isLoading" class="container mx-auto p-4 max-w-4xl">
      <div v-if="isLoading" class="text-center text-white text-xl py-20">
        <div class="inline-flex flex-col items-center">
          <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mb-4"></div>
          <span v-if="isLoading">Loading news...</span>
        </div>
      </div>
    </div>

    <div v-else-if="news && isNewsAccessible" class="container mx-auto p-4 max-w-4xl">

      <router-link
        :to="backToHomeUrl"
        class="group inline-flex items-center gap-2 px-4 py-2 rounded-md shadow-sm text-gray-700 bg-white hover:bg-nike-orange hover:text-white transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 mb-4"
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="h-4 w-4 text-gray-500 transition-transform group-hover:text-white duration-200 group-hover:-translate-x-1"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
          stroke-width="2"
        >
          <path stroke-linecap="round" stroke-linejoin="round" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
        </svg>
        <span class="font-medium">Back to Homepage</span>
      </router-link>

      <div class="bg-white shadow-lg rounded-xl overflow-hidden">
        <div class="p-6">

          <div v-if="isNewsRemoved" class="mb-4 bg-rose-50 border border-rose-200 rounded-lg p-4">
            <div class="flex items-start">
              <svg class="w-5 h-5 text-rose-600 mt-0.5 mr-3 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L4.082 16.5c-.77.833.192 2.5 1.732 2.5z"/>
              </svg>
              <div>
                <p class="text-sm font-medium text-rose-800">This news has been removed</p>
              </div>
            </div>
          </div>

          <h1 class="text-4xl font-anton mb-6 text-gray-900">{{ news.topic }}</h1>

          <img
            v-if="news.image && (news.image.startsWith('http') || news.image.startsWith('https'))"
            :src="news.image"
            :alt="news.topic"
            class="w-full h-auto rounded-lg mb-6"
          />
          <div class="flex items-center mb-4 space-x-4 text-md text-gray-600">
            <span
              :class="{
                'bg-red-600': newsStatus === 'fake',
                'bg-green-600': newsStatus === 'not fake',
                'bg-gray-500': newsStatus === 'equal',
              }"
              class="text-white font-bold px-3 py-1 rounded-full uppercase"
            >
              {{ newsStatus === 'fake' ? 'Fake' : newsStatus === 'not fake' ? 'Real' : 'Equal' }}
            </span>
            <p class="flex items-center text-lg">
              <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <circle cx="12" cy="8" r="4" stroke="currentColor" stroke-width="2" />
                <path stroke="currentColor" stroke-width="2" stroke-linecap="round" d="M4 20c0-3.3 3.6-6 8-6s8 2.7 8 6"/>
              </svg>
              <span>{{ news.reporter }}</span>
            </p>
            <p class="flex items-center text-lg">
              <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <rect x="3" y="4" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2" />
                <path d="M16 2v4M8 2v4M3 10h18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              {{ formatDate(news.dateTime) }}
            </p>
          </div>

          <p class="text-lg text-gray-800 leading-relaxed mb-10 whitespace-pre-line">{{ news.fullDetail }}</p>

          <div class="mb-4">
            <VoteSummary :vote-summary="news.voteSummary" />
          </div>

          <div class="mb-4">
            <div class="flex space-x-8 border-b border-gray-200">
              <button
                @click="switchTab('comments')"
                :disabled="isTabSwitching"
                :class="{
                  'text-black border-black': activeTab === 'comments',
                  'text-gray-600 border-transparent hover:text-gray-800': activeTab !== 'comments',
                  'opacity-50 cursor-not-allowed': isTabSwitching
                }"
                class="pb-3 px-1 border-b-2 font-medium transition-colors duration-200 relative"
              >
                All Comments
                <div v-if="isTabSwitching && pendingTab === 'comments'" class="absolute -top-1 -right-1">
                  <div class="animate-spin rounded-full h-3 w-3 border-b border-gray-600"></div>
                </div>
              </button>

              <button
                v-if="canViewVoteTab"
                @click="switchTab('vote')"
                :disabled="isTabSwitching"
                :class="{
                  'text-black border-black': activeTab === 'vote',
                  'text-gray-600 border-transparent hover:text-gray-800': activeTab !== 'vote',
                  'opacity-50 cursor-not-allowed': isTabSwitching
                }"
                class="pb-3 px-1 border-b-2 font-medium transition-colors duration-200 relative"
              >
                Vote
                <div v-if="isTabSwitching && pendingTab === 'vote'" class="absolute -top-1 -right-1">
                  <div class="animate-spin rounded-full h-3 w-3 border-b border-gray-600"></div>
                </div>
              </button>
            </div>
            <div v-if="!canViewVoteTab" class="mt-3 text-sm text-gray-500">
              Sign in to vote and comment.
            </div>
          </div>

          <div v-if="isTabSwitching" class="text-center py-8">
            <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500 mx-auto mb-2"></div>
            <span class="text-gray-500">Loading...</span>
          </div>

          <CommentsSection
            v-else-if="activeTab === 'comments'"
            :comments="news.comments"
            :vote-summary="news.voteSummary"
            :news-id="newsId"
            :can-delete-news="canDeleteNews"
            @news-deleted="handleNewsDeletion"
          />

          <VoteSection
            v-else-if="activeTab === 'vote' && canViewVoteTab"
            :news="news"
            @vote-success="handleVoteSuccess"
          />

        </div>
      </div>
    </div>

    <div v-else class="container mx-auto p-4 text-center py-20">
      <div class="flex flex-col items-center">
        <svg class="w-16 h-16 text-gray-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.172 16.172a4 4 0 015.656 0M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
        </svg>
        <h1 class="text-2xl font-bold text-gray-500 mb-2">News not found</h1>
        <p class="text-gray-400">The news article you're looking for doesn't exist or has been removed.</p>
        <router-link
          to="/"
          class="mt-4 px-4 py-2 bg-nike-orange text-white rounded-lg hover:bg-nike-yellow transition-colors duration-200"
        >
          Go back to Home
        </router-link>
      </div>
    </div>
  </div>
</template>

