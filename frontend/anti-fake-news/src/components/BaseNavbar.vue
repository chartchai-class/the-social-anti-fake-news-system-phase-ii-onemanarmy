<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore, type Role } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const creatorRoles: Role[] = ['ROLE_MEMBER', 'ROLE_ADMIN']
const canCreateNews = computed(() => authStore.hasAnyRole(creatorRoles))

const isLoggedIn = computed(() => authStore.isAuthenticated)
const userUsername = computed(() => authStore.user?.username || 'User')
const userProfileImage = computed(() => authStore.userProfileImage)

const refreshPage = () => {
  if (router.currentRoute.value.path === '/') {
    window.location.reload()
  } else {
    router.push('/')
  }
}

const handleLogout = () => {
  authStore.logout()
  refreshPage()
}

onMounted(() => {
  authStore.hydrateFromStorage()
})
</script>

<template>
  <link href="https://fonts.googleapis.com/css2?family=Orbitron:wght@400;700&display=swap" rel="stylesheet">

  <nav class="bg-white shadow-lg relative overflow-hidde">
    <div class="container mx-auto px-4">
      <div class="flex justify-between items-center py-4 relative">

        <router-link to="/" class="flex items-center space-x-3" @click="refreshPage">
          <img src="/logo.png" alt="One Man Army News Logo" class="md:h-14 h-12 w-auto " />
        </router-link>

        <router-link to="/" class="absolute left-1/2 -translate-x-1/2" @click="refreshPage">
          <span
            class="font-orbitron text-sm font-extrabold text-nike-black hover:text-nike-orange transition-colors drop-shadow-sm duration-500 sm:text-xl md:text-2xl lg:text-3xl xl:text-4xl tracking-widest uppercase"
          >
            One Man Army News
          </span>
        </router-link>

        <div class="flex items-center space-x-2 sm:space-x-4">

          <router-link
            v-if="canCreateNews"
            to="/add-news"
            class="flex items-center space-x-1 px-2 py-2 bg-nike-yellow text-nike-black rounded-lg hover:bg-nike-orange hover:text-nike-white transition-colors duration-500 text-xs sm:text-sm sm:space-x-2 sm:px-3 md:px-4 md:py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <rect x="3" y="3" width="18" height="18" rx="4" stroke-width="2"/>
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v8M8 12h8"/>
            </svg>
            <span class="font-medium hidden xs:inline sm:inline">Add News</span>
          </router-link>

          <router-link
            v-if="!isLoggedIn"
            to="/signin"
            class="px-3 py-2 bg-nike-yellow text-nike-black rounded-lg hover:bg-nike-orange hover:text-nike-white transition-colors duration-500 text-xs sm:text-sm md:px-4 md:py-2 font-medium"
          >
            Sign in
          </router-link>

          <div v-else class="flex items-center space-x-3 sm:space-x-4">
            <router-link
              :to="{ name: 'user-profile' }"
              class="flex items-center space-x-2 rounded-md p-1 hover:bg-gray-100 transition-colors duration-200"
              title="View Profile"
            >
              <img
                :src="userProfileImage || 'https://cdn-icons-png.flaticon.com/512/3177/3177440.png'"
                alt="User Profile"
                class="h-8 w-8 sm:h-9 sm:w-9 rounded-full object-cover"
              />
              <span class="text-sm sm:text-base font-medium text-nike-black hidden md:inline">
                Hi, {{ userUsername }}
              </span>
            </router-link>

            <button
              @click="handleLogout"
              class="p-2 text-gray-700 bg-gray-100 rounded-full hover:bg-gray-200 transition-colors hidden sm:inline-block"
              title="Logout"
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 48 48">
                <path
                  fill="none"
                  stroke="#000000"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="4"
                  d="M23.992 6H6v36h18m9-9l9-9l-9-9m-17 8.992h26"
                />
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>
    <div class="absolute bottom-0 left-0 w-full h-1 bg-gradient-to-r from-nike-orange via-nike-yellow to-yellow-200 transition-all duration-300 ease-out"></div>
  </nav>
</template>

