<script setup>
import { computed } from 'vue'

defineProps({
  section: { type: String, required: true },
  page: { type: String, required: true },
  unreadCount: { type: Number, default: 0 }
})

const emit = defineEmits(['toggle-notifications'])

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
      <button 
        type="button" 
        class="notification-btn" 
        aria-label="Benachrichtigungen öffnen"
        @click="emit('toggle-notifications')"
      >
        <span class="bell-icon">🔔</span>
      </button>
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
.notification-btn {
  position: relative;
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 6px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s;
}
.notification-btn:hover {
  background-color: #F4F6F6;
}
.bell-icon {
  font-size: 16px;
  line-height: 1;
}
.badge {
  position: absolute;
  top: 2px;
  right: 2px;
  background-color: #E04F5F;
  color: white;
  font-size: 10px;
  font-weight: 600;
  min-width: 15px;
  height: 15px;
  border-radius: 999px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
}
</style>