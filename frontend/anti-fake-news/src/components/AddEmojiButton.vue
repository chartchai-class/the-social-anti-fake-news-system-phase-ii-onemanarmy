<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
const emit = defineEmits(['select'])
const pickerOpen = ref(false)
const root = ref<HTMLElement | null>(null)

function togglePicker() {
  pickerOpen.value = !pickerOpen.value
}

// Close the picker when click outside
function onClickOutside(event: MouseEvent) {
  if (pickerOpen.value && root.value && !root.value.contains(event.target as Node)) {
    pickerOpen.value = false
  }
}

onMounted(() => {
  window.addEventListener('mousedown', onClickOutside)
})
onBeforeUnmount(() => {
  window.removeEventListener('mousedown', onClickOutside)
})

const emojis = [
  '😀','😃','😄','😁','😆','😂','🤣','😍','🤩','😘','😎',
  '😜','🥺','😭','👍','👏','🙏','🎉','🔥','💯'
]
function selectEmoji(emoji: string) {
  emit('select', emoji)
  pickerOpen.value = false
}
</script>

<template>
  <div class="relative inline-block" ref="root">
    <button
      class="p-2 rounded-lg hover:bg-gray-100 text-2xl"
      @click="togglePicker"
      aria-label="Add emoji"
    >
      😊
    </button>
    <div
      v-if="pickerOpen"
      class="absolute z-10 mt-2 bg-white shadow-lg border rounded p-2 flex flex-wrap gap-1"
      style="min-width: 220px;"
    >
      <button
        v-for="emoji in emojis"
        :key="emoji"
        class="text-2xl p-1 hover:bg-blue-100 rounded"
        @click="selectEmoji(emoji)"
      >
        {{ emoji }}
      </button>
    </div>
  </div>
</template>

