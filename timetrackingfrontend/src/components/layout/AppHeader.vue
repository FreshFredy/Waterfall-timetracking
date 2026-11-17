<script setup>
import { computed } from 'vue'
defineProps({
  section: { type: String, required: true },
  page: { type: String, required: true },
})
const currentDate = computed(() => {
  return new Date().toLocaleDateString('de-DE', {
    weekday: 'short',
    day: '2-digit',
    month: 'long',
    year: 'numeric',
  })
})
</script>

<template>
  <header class="header">
    <div class="breadcrumb">
      <div class="section-name">{{ section }}</div>
      <div class="chevron">›</div>
      <div class="page-name">{{ page }}</div>
    </div>
    <div class="header-right">
      <div class="date-text">{{ currentDate }}</div>
      <div class="divider"></div>
      <div class="icon-placeholder">🔔</div>
    </div>
  </header>
</template>

<style scoped>
.header {
  align-self: stretch;
  height: 72px;
  padding-left: 32px;
  padding-right: 32px;
  background: white;
  overflow: hidden;
  border-bottom: 1px #DFE7E8 solid;
  justify-content: space-between;
  align-items: center;
  display: inline-flex;
}
.breadcrumb {
  overflow: hidden;
  justify-content: flex-start;
  align-items: center;
  gap: 10px;
  display: flex;
}
.section-name {
  color: #697B80;
  font-size: 12px;
  font-family: Inter, sans-serif;
  line-height: 17.40px;
}
.chevron {
  color: #697B80;
  font-size: 12px;
}
.page-name {
  color: #172D32;
  font-size: 12px;
  font-family: Inter, sans-serif;
  font-weight: 500;
  line-height: 17.40px;
}
.header-right {
  overflow: hidden;
  justify-content: flex-start;
  align-items: center;
  gap: 20px;
  display: flex;
}
.date-text {
  color: #697B80;
  font-size: 12px;
  font-family: Inter, sans-serif;
  line-height: 17.40px;
}
.divider {
  width: 1px;
  height: 20px;
  background: #DFE7E8;
}
.icon-placeholder {
  cursor: pointer;
  color: #697B80;
  font-size: 14px;
}
</style>