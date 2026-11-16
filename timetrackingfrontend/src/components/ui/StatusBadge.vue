<script setup>
import { computed } from 'vue'

const props = defineProps({
  variant: { type: String, default: 'neutral' },
  label: { type: String, required: true },
})

const badgeStyle = computed(() => {
  switch (props.variant) {
    case 'pending':
      return { background: '#FBF3E4', color: '#946722', dot: '#946722' }
    case 'success':
      return { background: '#E8F3F2', color: '#176A70', dot: '#176A70' }
    case 'purple':
      return { background: '#F1EDF6', color: '#72608B', dot: '#72608B' }
    default:
      return { background: '#EFF3F3', color: '#697B80', dot: '#697B80' }
  }
})
</script>

<template>
  <span
    class="status-badge"
    :style="{ background: badgeStyle.background, color: badgeStyle.color }"
  >
    <span class="dot" :style="{ background: badgeStyle.dot }"></span>
    {{ label }}
  </span>
</template>

<style scoped>
.status-badge {
  padding: 4px 9px;
  overflow: hidden;
  border-radius: 100px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  font-family: Inter, sans-serif;
  font-weight: 600;
  line-height: 15.95px;
}
.dot {
  width: 5px;
  height: 5px;
  border-radius: 100px;
}
</style>