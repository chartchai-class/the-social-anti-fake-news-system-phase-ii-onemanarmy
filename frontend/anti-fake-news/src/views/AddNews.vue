<script setup lang="ts">
import { reactive, computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useNewsStore } from '../stores/newsInterface';
import { useNotificationStore } from '../stores/notifications';
import ToastNotification from '../components/NotificationShow.vue';
import type { CreateNewsPayload } from '../stores/newsInterface';
import { useAuthStore } from '../stores/auth';
import axios from 'axios';
import apiClient from '@/services/AxiosClient';

const router = useRouter();
const newsStore = useNewsStore();
const notificationStore = useNotificationStore();
const authStore = useAuthStore();

const formData = reactive({
  topic: '',
  shortDetail: '',
  fullDetail: '',
  image: '',
  reporter: '',
});

const fileInput = ref<HTMLInputElement | null>(null);
const imagePreview = ref<string | null>(null);
const isImageUploading = ref(false);

const isSubmitting = ref(false);
const canSubmit = computed(() => authStore.roles.includes('ROLE_MEMBER') || authStore.roles.includes('ROLE_ADMIN'));

function triggerFileInput() {
  fileInput.value?.click();
}

async function handleNewsImageUpload(event: Event) {
  const target = event.target as HTMLInputElement | null;
  const file = target?.files?.[0];
  if (!file) {
    return;
  }

  const reader = new FileReader();
  reader.onload = (e) => {
    imagePreview.value = (e.target?.result as string) ?? null;
  };
  reader.readAsDataURL(file);

  const uploadForm = new FormData();
  uploadForm.append('file', file);

  isImageUploading.value = true;

  try {
    const response = await apiClient.post('/uploadFile', uploadForm, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    formData.image = response.data;
  } catch (error) {
    console.error('Image upload failed:', error);
    notificationStore.addNotification('Image upload failed. Please try again.', 'error');
    formData.image = '';
    imagePreview.value = null;
  } finally {
    if (target) {
      target.value = '';
    }
    isImageUploading.value = false;
  }
}

const handleAddNews = async () => {
  if (isSubmitting.value || isImageUploading.value) {
    return;
  }

  if (!authStore.isAuthenticated || !canSubmit.value) {
    notificationStore.addNotification('You need a member or admin account to add news.', 'error');
    return;
  }

  if (!formData.image) {
    notificationStore.addNotification('Please upload a news image before submitting.', 'error');
    return;
  }

  isSubmitting.value = true;

  try {
    const newNews: CreateNewsPayload = {
      ...formData,
      dateTime: new Date().toISOString(),
    };

    await newsStore.createNews(newNews);

    // Show success notification
    notificationStore.addNotification('News added successfully.', 'success');

    // Reset form
    Object.keys(formData).forEach(key => {
      formData[key as keyof typeof formData] = '';
    });
    imagePreview.value = null;
    if (fileInput.value) {
      fileInput.value.value = '';
    }

    // Navigate to home after a short delay
    setTimeout(() => {
      router.push('/');
    }, 700);

  } catch (error) {
    console.error('Error adding news:', error);
    if (axios.isAxiosError(error)) {
      if (error.response?.status === 403) {
        notificationStore.addNotification('You are not authorized to add news.', 'error');
      } else if (error.response?.status === 401) {
        notificationStore.addNotification('Session expired. Please log in again.', 'error');
      } else {
        notificationStore.addNotification('Failed to add news. Please try again.', 'error');
      }
    } else {
      notificationStore.addNotification('Failed to add news. Please try again.', 'error');
    }
  } finally {
    isSubmitting.value = false;
  }
};
</script>

<template>
  <div class="bg-gradient-to-b from-black via-gray-900 to-black min-h-screen">

    <ToastNotification />

    <div class="container mx-auto p-4 max-w-4xl font-oswald bg-transparent min-h-screen">

      <router-link
        to="/"
        class="group inline-flex items-center gap-2 px-4 py-2 border border-gray-300 rounded-md shadow-sm text-gray-700 bg-white hover:bg-nike-orange hover:text-white transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="h-4 w-4 text-gray-500 transition-transform duration-200 group-hover:text-white group-hover:-translate-x-1"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
          stroke-width="2"
        >
          <path stroke-linecap="round" stroke-linejoin="round" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
        </svg>
        <span class="font-medium hidden md:inline">Back to Homepage</span>
        <span
          class="absolute left-full top-1/2 -translate-y-1/2 ml-1
                 text-xs bg-black text-white rounded px-2 py-1 opacity-0 pointer-events-none
                 group-hover:opacity-100 group-focus:opacity-100 transition-all duration-200
                 md:hidden z-10 whitespace-nowrap"
        >
          Go back
        </span>
      </router-link>

      <div class="bg-white shadow-lg rounded-xl overflow-hidden">
        <div class="p-6">

          <div class="mb-4">
            <div class="flex items-center justify-between md:items-center md:justify-between gap-2">
              <h1 class="text-4xl font-extrabold text-gray-900 m-0">Add News</h1>
            </div>
            <div class="flex items-center space-x-4 text-sm text-gray-600 mt-2">
              <span class="bg-nike-orange text-white font-bold px-3 py-1 rounded-full uppercase">
                DRAFT
              </span>
              <p>Date: <span class="font-semibold">{{ new Date().toLocaleDateString() }}</span></p>
            </div>
          </div>

          <form @submit.prevent="handleAddNews" class="space-y-8">

            <div class="group">
              <label for="reporter" class="block text-lg font-semibold text-gray-900 mb-3">
                Reporter
              </label>
              <input
                v-model="formData.reporter"
                type="text"
                id="reporter"
                class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200 text-gray-800 text-lg"
                placeholder="Enter reporter name..."
                required
              />
            </div>

            <div class="group">
              <label for="topic" class="block text-lg font-semibold text-gray-900 mb-3">
                News Title
              </label>
              <input
                v-model="formData.topic"
                type="text"
                id="topic"
                class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200 text-gray-800 text-lg"
                placeholder="Enter an interesting title..."
                required
              />
            </div>

            <div class="group">
              <label for="shortDetail" class="block text-lg font-semibold text-gray-900 mb-3">
                Short Description
              </label>
              <textarea
                v-model="formData.shortDetail"
                id="shortDetail"
                rows="4"
                class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200 text-gray-800 text-lg leading-relaxed resize-none"
                placeholder="Give us a brief summary..."
                required
              ></textarea>
            </div>

            <div class="group">
              <label for="fullDetail" class="block text-lg font-semibold text-gray-900 mb-3">
                Full News Content
              </label>
              <textarea
                v-model="formData.fullDetail"
                id="fullDetail"
                rows="8"
                class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200 text-gray-800 text-lg leading-relaxed resize-none"
                placeholder="Write full news content here..."
                required
              ></textarea>
            </div>

            <div class="group">
              <label class="block text-lg font-semibold text-gray-900 mb-3">
                News Image
              </label>
              <div class="flex flex-col items-center gap-4">
                <div
                  class="relative w-full max-w-md cursor-pointer"
                  @click="triggerFileInput"
                >
                  <div
                    class="h-64 w-full rounded-lg border-2 border-dashed border-gray-300 transition-all duration-200 hover:border-blue-500 overflow-hidden bg-gray-50"
                  >
                    <img
                      v-if="imagePreview || formData.image"
                      :src="imagePreview || formData.image"
                      alt="News image preview"
                      class="h-full w-full object-cover"
                    />
                    <div
                      v-else
                      class="flex h-full flex-col items-center justify-center space-y-2 text-gray-500"
                    >
                      <span class="text-base font-medium">Click to upload</span>
                      <span class="text-xs">JPG, JPEG, PNG, GIF</span>
                    </div>
                  </div>
                  <div
                    v-if="imagePreview || formData.image"
                    class="absolute inset-0 flex items-center justify-center rounded-lg bg-black/40 text-white text-sm font-medium opacity-0 transition-opacity duration-200 hover:opacity-100"
                  >
                    Change image
                  </div>
                </div>
                <input
                  ref="fileInput"
                  type="file"
                  accept="image/*"
                  class="hidden"
                  @change="handleNewsImageUpload"
                />
                <div class="text-sm text-gray-500 text-center space-y-1">
                  <p v-if="isImageUploading" class="text-blue-600 font-medium">Uploading image...</p>
                  <p v-else-if="formData.image" class="text-green-600 font-medium">Image uploaded successfully</p>
                </div>
              </div>
            </div>

            <div class="border-t border-gray-200 pt-8">
              <div class="flex justify-end space-x-4">
                <router-link
                  to="/"
                  class="px-6 py-3 border border-gray-300 rounded-lg text-gray-700 bg-white hover:bg-red-500 hover:text-white transition-all duration-200 font-medium transform hover:scale-105 shadow-lg hover:shadow-xl"
                >
                  Cancel
                </router-link>
                <button
                  type="submit"
                  :disabled="isSubmitting || !canSubmit || !formData.image || isImageUploading"
                  class="px-8 py-3 bg-nike-orange hover:bg-nike-yellow text-white font-semibold rounded-lg transition-all duration-200 transform hover:scale-105 shadow-lg hover:shadow-xl disabled:opacity-60 disabled:cursor-not-allowed"
                  :class="{ 'pointer-events-none': isSubmitting || !canSubmit || !formData.image || isImageUploading }"
                >
                  <span class="flex items-center space-x-2">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
                    </svg>
                    <span>Add News</span>
                  </span>
                </button>
              </div>

              <p v-if="!canSubmit" class="mt-2 text-sm text-red-600">
                Only members or admins can publish news. Please sign in with the appropriate role.
              </p>
              <p v-else-if="!formData.image" class="mt-2 text-sm text-red-600">
                Please upload a news image before submitting.
              </p>
              <p v-else-if="isImageUploading" class="mt-2 text-sm text-blue-600">
                Waiting for the image upload to finish...
              </p>
            </div>
          </form>
        </div>
      </div>

      <div class="text-center mt-6">
        <p class="text-sm text-white">
          © One Man Army - All rights reserved.
        </p>
      </div>
    </div>
  </div>
</template>

