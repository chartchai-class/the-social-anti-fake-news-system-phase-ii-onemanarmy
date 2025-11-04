<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useAuthStore, type Role } from '../stores/auth';
import { useUserStore } from '../stores/userStore';
import { useRouter } from 'vue-router';
import type { UserAuthDTO } from '@/types';

const authStore = useAuthStore();
const userStore = useUserStore();
const router = useRouter();

// Current logged-in user data
const currentUser = computed(() => authStore.user);

// Check if user is admin
const isAdmin = computed(() => authStore.hasRole('ROLE_ADMIN'));

// State for promote button loading
const isPromotingUser = ref<number | null>(null);
const isDemotingUser = ref<number | null>(null);

// Fetch all users on mount (admin only)
onMounted(() => {
    if (isAdmin.value) {
        userStore.fetchAllUsers();
    }
});

// Role badge styling
const getRoleClass = (role: Role | string): string => {
  const styles: Record<string, string> = {
    // (v1) ADMIN = สีแดง
    'ROLE_ADMIN':
      'bg-red-600 text-white px-3 py-1 rounded-full text-xs font-bold shadow-md',
    // (v1) MEMBER = สีส้ม (สีหลัก)
    'ROLE_MEMBER':
      'bg-nike-orange text-white px-3 py-1 rounded-full text-xs font-bold shadow-md',
    // (v1) READER = สีเหลือง (สีรอง)
    'ROLE_READER':
      'bg-nike-yellow text-nike-black px-3 py-1 rounded-full text-xs font-bold shadow-md'
  }
  return (
    styles[role] ||
    'bg-gray-400 text-white px-3 py-1 rounded-full text-xs font-bold shadow-md'
  )
}
// Check if user can be promoted
const canPromote = (user: UserAuthDTO): boolean => {
    return user.roles.includes('ROLE_READER')
        && !user.roles.includes('ROLE_MEMBER')
        && !user.roles.includes('ROLE_ADMIN');
};

// Handle user promotion
const handlePromote = async (userId: number) => {
    if (confirm('Are you sure you want to promote this user to MEMBER?')) {
        isPromotingUser.value = userId;
        try {
            await userStore.promoteUser(userId);
            await userStore.fetchAllUsers();
        } catch (error) {
            console.error('Promotion failed:', error);
            alert('Failed to promote user. Please try again.');
        } finally {
            isPromotingUser.value = null;
        }
    }
};
// เช็คว่าเป็น MEMBER แต่ไม่ใช่ ADMIN
const canDemote = (user: UserAuthDTO): boolean => {
    return user.roles.includes('ROLE_MEMBER') && !user.roles.includes('ROLE_ADMIN');
}

// Handle Demote
const handleDemote = async (userId: number) => {
  if (confirm('Are you sure you want to demote this user back to READER?')) {
    isDemotingUser.value = userId;
    try {
      await userStore.demoteUser(userId);
    } catch (error) {
      console.error("Demotion failed:", error);
       alert('Failed to demote user. Please try again.');
    } finally {
      isDemotingUser.value = null;
    }
  }
};

// Handle logout
const handleLogout = () => {
    authStore.logout();
    refreshPage();
};

// Refresh page helper
const refreshPage = () => {
    if (router.currentRoute.value.path === '/') {
        window.location.reload();
    } else {
        router.push('/');
    }
};
</script>

<template>
  <div
    class="font-oswald bg-gradient-to-b from-black via-gray-900 to-black min-h-screen py-4"
  >
    <div class="container mx-auto max-w-6xl p-4 sm:p-6 lg:p-8">
      <div class="mb-6">
        <h1 class="text-3xl font-bold text-white mb-2 font-orbitron">
          Account Details
        </h1>
      </div>

      <div
        v-if="currentUser"
        class="bg-white shadow-lg rounded-2xl mb-10 border border-gray-100 p-8"
      >
        <div class="flex flex-col sm:flex-row items-center sm:items-start">
          <div class="relative group">
            <img
              :src="
                currentUser.profileImage ||
                'https://cdn-icons-png.flaticon.com/512/3177/3177440.png'
              "
              alt="Profile Picture"
              class="h-32 w-32 sm:h-40 sm:w-40 rounded-full object-cover ring-4 ring-white shadow-xl transition-transform duration-300 group-hover:scale-105"
            />
            <div
              class="absolute inset-0 rounded-full bg-nike-orange opacity-0 group-hover:opacity-10 transition-opacity duration-300"
            ></div>
          </div>

          <div class="flex-1 text-center sm:text-left sm:ml-8 mt-6 sm:mt-0">
            <h2 class="text-2xl font-bold text-nike-black mb-1">
              {{ currentUser.firstname }} {{ currentUser.lastname }}
            </h2>
            <p class="text-lg text-gray-700 mb-4">@{{ currentUser.username }}</p>

            <div class="flex flex-wrap justify-center sm:justify-start gap-2 mb-6">
              <span
                v-for="role in currentUser.roles"
                :key="role"
                :class="getRoleClass(role)"
                class="px-4 py-2 text-sm font-semibold rounded-full uppercase tracking-wide shadow-sm transition-transform hover:scale-105"
              >
                {{ role.replace('ROLE_', '') }}
              </span>
            </div>

            <div class="inline-flex items-center px-4 py-2 bg-gray-100 rounded-lg">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                class="h-5 w-5 text-nike-orange mr-2"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
                stroke-width="2"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"
                />
              </svg>
              <span class="text-gray-700 font-medium">{{ currentUser.email }}</span>
            </div>

            <div class="mt-6 sm:hidden">
              <button
                @click="handleLogout"
                class="w-full px-6 py-3 bg-red-600 text-white font-semibold rounded-lg shadow-md hover:bg-red-700 transform hover:scale-[1.02] transition-all duration-200 focus:outline-none focus:ring-4 focus:ring-red-300"
              >
                <span class="flex items-center justify-center">
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    class="h-5 w-5 mr-2"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                  >
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"
                    />
                  </svg>
                  Logout
                </span>
              </button>
            </div>
          </div>
        </div>
      </div>

      <div
        v-else
        class="bg-white shadow-lg rounded-2xl p-20 text-center border border-gray-100 mb-10"
      >
        <div
          class="animate-spin h-12 w-12 border-4 border-nike-orange border-t-transparent rounded-full mx-auto mb-4"
        ></div>
        <p class="text-xl text-gray-500">Loading your account details...</p>
      </div>

      <section v-if="isAdmin" class="opacity-0 animate-[fadeIn_0.5s_ease-out_forwards]">
        <div class="mb-6">
          <h2 class="text-3xl font-bold text-white mb-2 font-orbitron">
            User Management
            <span class="text-lg text-gray-300 font-normal ml-2">(Admin Panel)</span>
          </h2>
        </div>

        <div
          v-if="userStore.isLoading"
          class="bg-white rounded-2xl shadow-lg p-16 text-center border border-gray-100"
        >
          <div
            class="animate-spin h-12 w-12 border-4 border-nike-orange border-t-transparent rounded-full mx-auto mb-4"
          ></div>
          <p class="text-gray-500 text-lg">Fetching users...</p>
        </div>

        <div
          v-else
          class="bg-white rounded-2xl shadow-lg overflow-hidden border border-gray-100"
        >
          <div class="overflow-x-auto">
            <table class="min-w-full divide-y divide-gray-200">
              <thead class="bg-gray-100">
                <tr>
                  <th
                    class="px-6 py-4 text-left text-xs font-bold text-gray-700 uppercase tracking-wider"
                  >
                    User
                  </th>
                  <th
                    class="px-6 py-4 text-left text-xs font-bold text-gray-700 uppercase tracking-wider"
                  >
                    Username
                  </th>
                  <th
                    class="px-6 py-4 text-left text-xs font-bold text-gray-700 uppercase tracking-wider"
                  >
                    Roles
                  </th>
                  <th
                    class="px-6 py-4 text-left text-xs font-bold text-gray-700 uppercase tracking-wider"
                  >
                    Action
                  </th>
                </tr>
              </thead>

              <tbody class="bg-white divide-y divide-gray-200">
                <tr
                  v-for="user in userStore.users"
                  :key="user.id"
                  class="hover:bg-gray-50 transition-colors duration-150"
                >
                  <td class="px-6 py-4">
                    <div class="flex items-center gap-3">
                      <img
                        :src="
                          user.profileImage ||
                          'https://cdn-icons-png.flaticon.com/512/3177/3177440.png'
                        "
                        alt="User avatar"
                        class="h-12 w-12 rounded-full object-cover ring-2 ring-gray-200"
                      />
                      <div>
                        <p class="font-semibold text-nike-black">
                          {{ user.firstname }} {{ user.lastname }}
                        </p>
                        <p class="text-sm text-gray-500">{{ user.email }}</p>
                      </div>
                    </div>
                  </td>

                  <td class="px-6 py-4">
                    <span class="text-gray-700 font-medium">@{{ user.username }}</span>
                  </td>

                  <td class="px-6 py-4">
                    <div class="flex flex-wrap gap-2">
                      <span
                        v-for="role in user.roles"
                        :key="role"
                        :class="getRoleClass(role)"
                        class="transition-transform hover:scale-105"
                      >
                        {{ role.replace('ROLE_', '') }}
                      </span>
                    </div>
                  </td>

                  <td class="px-6 py-4">
                    <button
                      v-if="canPromote(user)"
                      @click="handlePromote(user.id!)"
                      :disabled="isPromotingUser === user.id"
                      class="inline-flex items-center px-4 py-2 bg-nike-orange text-white text-sm font-semibold rounded-lg shadow-md hover:shadow-lg hover:bg-nike-yellow hover:text-nike-black transform hover:scale-105 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none focus:outline-none focus:ring-4 focus:ring-orange-300"
                    >
                      <span v-if="isPromotingUser === user.id" class="flex items-center">
                        <span
                          class="animate-spin h-4 w-4 border-2 border-white border-t-transparent rounded-full mr-2"
                        ></span>
                        Upgrading...
                      </span>
                      <span v-else class="flex items-center"> Upgrade to Member </span>
                    </button>

                    <button
                      v-if="canDemote(user)"
                      @click="handleDemote(user.id!)"
                      :disabled="isPromotingUser === user.id || isDemotingUser === user.id"
                      class="inline-flex items-center px-4 py-2 bg-nike-yellow text-nike-black text-sm font-semibold rounded-lg shadow-md hover:shadow-lg hover:bg-yellow-400 transform hover:scale-105 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none focus:outline-none focus:ring-4 focus:ring-yellow-300"
                    >
                      <span v-if="isDemotingUser === user.id" class="flex items-center">
                        <span
                          class="animate-spin h-4 w-4 border-2 border-black border-t-transparent rounded-full mr-1"
                        ></span>
                        Downgrading...
                      </span>
                      <span v-else class="flex items-center"> Demote to Reader </span>
                    </button>

                    <span
                      v-if="!canPromote(user) && !canDemote(user)"
                      class="inline-flex items-center px-4 py-2 bg-gray-100 text-gray-600 text-sm font-medium rounded-lg"
                    >
                      <svg
                        xmlns="http://www.w3.org/2000/svg"
                        class="h-4 w-4 mr-2"
                        fill="none"
                        viewBox="0 0 24 24"
                        stroke="currentColor"
                      >
                        <path
                          stroke-linecap="round"
                          stroke-linejoin="round"
                          stroke-width="2"
                          d="M5 13l4 4L19 7"
                        />
                      </svg>
                      {{ user.roles.includes('ROLE_ADMIN') ? 'Admin' : 'Member' }}
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div v-if="userStore.users.length === 0" class="text-center py-12">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              class="h-16 w-16 text-gray-300 mx-auto mb-4"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"
              />
            </svg>
            <p class="text-gray-500 text-lg">No users found</p>
          </div>
        </div>
      </section>

      <div class="text-center mt-10">
        <p class="text-sm text-white">© One Man Army - All rights reserved.</p>
      </div>
    </div>
  </div>
</template>

