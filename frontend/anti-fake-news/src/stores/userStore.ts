import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { UserAuthDTO } from '@/types'
import userService from '../services/UserService'
import { useMessageStore } from './message'

export const useUserStore = defineStore('userManagement', () => {
  const users = ref<UserAuthDTO[]>([])
  const isLoading = ref(false)

  async function fetchAllUsers() {
    isLoading.value = true
    const messageStore = useMessageStore()
    try {
      const response = await userService.getAllUsers()
      users.value = response.data
    } catch (error) {
      console.error('Failed to fetch users:', error)
      messageStore.updateMessage('Failed to load user list.')
    } finally {
      isLoading.value = false
    }
    setTimeout(() => {
      messageStore.resetMessage()
    }, 3000)
  }

  async function promoteUser(userId: number) {
    const messageStore = useMessageStore()
    try {
      const response = await userService.promoteUserToMember(userId)
      const updatedUser = response.data

      const index = users.value.findIndex((u) => u.id === userId)
      if (index !== -1) {
        users.value.splice(index, 1, updatedUser)
        messageStore.updateMessage(`User ${updatedUser.username} promoted to MEMBER.`)
      }
    } catch (error) {
      console.error('Failed to promote user:', error)
      messageStore.updateMessage('Failed to promote user.')
      throw error
    }
    setTimeout(() => {
      messageStore.resetMessage()
    }, 3000)
  }

  async function demoteUser(userId: number) {
    const messageStore = useMessageStore();
    try {
      const response = await userService.demoteUserToReader(userId);
      const updatedUser = response.data;

      const index = users.value.findIndex((u) => u.id === userId);
      if (index !== -1) {
        users.value.splice(index, 1, updatedUser);
        messageStore.updateMessage(`User ${updatedUser.username} demoted to READER.`);
      }
    } catch (error) {
      console.error('Failed to demote user:', error);
      messageStore.updateMessage('Failed to demote user.');
      throw error;
    }
  }

  return {
    users,
    isLoading,
    fetchAllUsers,
    promoteUser,
    demoteUser
  }
})
