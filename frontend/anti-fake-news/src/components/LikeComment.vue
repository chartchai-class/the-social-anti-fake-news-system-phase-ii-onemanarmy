<script setup lang="ts">
import { ref, watch } from 'vue'

// รับค่าเริ่มต้นจาก parent (optional)
const props = defineProps({
  initLike:   { type: Number, default: 0 },
  initDislike:{ type: Number, default: 0 },
  userReacted: { type: String, default: '' } // 'like', 'dislike', ''
})
// ส่ง event กลับ parent
const emit = defineEmits(['like', 'dislike', 'undo'])

// State ปุ่ม กับ count (local)
const likeCount = ref(props.initLike)
const dislikeCount = ref(props.initDislike)
const liked = ref(props.userReacted === 'like')
const disliked = ref(props.userReacted === 'dislike')

// Watch เปลี่ยนค่าเริ่มต้น (ถ้า parent อัพเดท)
watch(() => props.initLike, v => { likeCount.value = v })
watch(() => props.initDislike, v => { dislikeCount.value = v })
watch(() => props.userReacted, v => {
  liked.value = v === 'like'
  disliked.value = v === 'dislike'
})

// Logic กด like-dislike
function toggleLike() {
  if (liked.value) {
    liked.value = false
    likeCount.value--
    emit('undo', 'like')
  } else {
    if (disliked.value) {
      disliked.value = false
      dislikeCount.value--
    }
    liked.value = true
    likeCount.value++
    emit('like')
  }
}

function toggleDislike() {
  if (disliked.value) {
    disliked.value = false
    dislikeCount.value--
    emit('undo', 'dislike')
  } else {
    if (liked.value) {
      liked.value = false
      likeCount.value--
    }
    disliked.value = true
    dislikeCount.value++
    emit('dislike')
  }
}
</script>

<template>
  <div class="flex items-center gap-3">
    <button
      class="flex items-center px-3 py-1 rounded-lg transition border text-gray-500 hover:text-green-600 hover:border-green-400"
      :class="{'bg-green-50 text-green-600 font-bold border-green-400': liked}"
      @click="toggleLike"
      :aria-pressed="liked"
      title="Like"
    >
      <span class="text-xl mr-1">👍</span>
      <span>{{ likeCount }}</span>
    </button>
    <button
      class="flex items-center px-3 py-1 rounded-lg transition border text-gray-500 hover:text-red-600 hover:border-red-400"
      :class="{'bg-red-50 text-red-600 font-bold border-red-400': disliked}"
      @click="toggleDislike"
      :aria-pressed="disliked"
      title="Dislike"
    >
      <span class="text-xl mr-1">👎</span>
      <span>{{ dislikeCount }}</span>
    </button>
  </div>
</template>


