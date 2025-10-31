import type { MessageState } from '@/types'
import { defineStore } from 'pinia'

export const useMessageStore = defineStore('message', {
  state: (): MessageState => ({
    message: ''
  }),
  actions: {
    updateMessage(message: string) {
      this.message = message
    },
    resetMessage() {
      this.message = ''
    }
  }
})