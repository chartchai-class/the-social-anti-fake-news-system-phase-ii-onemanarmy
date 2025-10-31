<template>
  <SpeedInsights />
  <div class="bg-gradient-to-b from-black via-gray-900 to-black min-h-screen font-sans antialiased text-gray-800">
    <!-- Loading Progress Bar -->
    <LoadingProgressBar />

    <Navbar />
    <RouterView />
    <ToastNotification />
    <ScrollButtons />
  </div>
</template>

<script setup lang="ts">
import { RouterView } from 'vue-router';
import Navbar from './components/BaseNavbar.vue';
import ToastNotification from './components/NotificationShow.vue';
import ScrollButtons from './components/ScrollButtons.vue';
import LoadingProgressBar from './components/LoadingBar.vue';
import { SpeedInsights } from "@vercel/speed-insights/vue"
</script>
