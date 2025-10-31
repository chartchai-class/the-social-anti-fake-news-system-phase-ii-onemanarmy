<script setup lang="ts">
import {  defineProps, defineEmits } from 'vue';

defineProps({
  modelValue: String,
  disabled: Boolean
});

defineEmits(['update:modelValue']);
</script>

<template>
  <div class="mb-6">
    <input
      :value="modelValue"
      @input="$emit('update:modelValue', ($event.target as HTMLInputElement).value)"
      type="text"
      placeholder="Search by news details..."
      :disabled="disabled"
      class="w-full p-3 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-nike-orange"
    />
  </div>
</template>


