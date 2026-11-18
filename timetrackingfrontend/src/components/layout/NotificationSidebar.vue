<script setup>
defineProps({
  isOpen: { type: Boolean, required: true },
  notifications: { type: Array, default: () => [] }
})

const emit = defineEmits(['close', 'mark-as-read', 'mark-all-read'])
</script>

<template>
  <Teleport to="body">
    <!-- Hintergrund-Overlay -->
    <div 
      v-if="isOpen" 
      class="backdrop" 
      @click="emit('close')"
    ></div>

    <!-- Rechte Sidebar -->
    <aside class="notification-drawer" :class="{ 'is-open': isOpen }">
      <div class="drawer-header">
        <div class="title-group">
          <h2>Notifications</h2>
          <span class="count-pill">{{ notifications.length }}</span>
        </div>
        <button type="button" class="close-btn" @click="emit('close')">✕</button>
      </div>

      <!-- Button zum Alle-als-gelesen-markieren (wird nur angezeigt, wenn ungelesene da sind) -->
      <div class="drawer-actions" v-if="notifications.some(n => !n.read)">
        <button type="button" class="mark-all-btn" @click="emit('mark-all-read')">
          Alle als gelesen markieren
        </button>
      </div>

      <div class="drawer-content">
        <div v-if="notifications.length === 0" class="empty-state">
          Keine neuen Benachrichtigungen vorhanden.
        </div>

        <!-- Einzelne Benachrichtigungen mit Klick-Event -->
        <div 
          v-for="item in notifications" 
          :key="item.id" 
          class="notification-item"
          :class="{ unread: !item.read }"
          @click="emit('mark-as-read', item.id)"
        >
          <div class="item-top">
            <span class="item-title">{{ item.title }}</span>
            <span class="item-time">{{ item.time }}</span>
          </div>
          <p class="item-message">{{ item.message }}</p>
        </div>
      </div>
    </aside>
  </Teleport>
</template>

<style scoped>
.backdrop {
  position: fixed;
  inset: 0;
  background-color: rgba(23, 45, 50, 0.35);
  z-index: 998;
  transition: opacity 0.25s ease;
}

.notification-drawer {
  position: fixed;
  top: 0;
  right: 0;
  width: 380px;
  max-width: 100vw;
  height: 100vh;
  background: #FFFFFF;
  border-left: 1px solid #DFE7E8;
  box-shadow: -4px 0 24px rgba(23, 45, 50, 0.08);
  z-index: 999;
  display: flex;
  flex-direction: column;
  transform: translateX(100%);
  transition: transform 0.25s cubic-bezier(0.16, 1, 0.3, 1);
}

.notification-drawer.is-open {
  transform: translateX(0);
}

.drawer-header {
  height: 72px;
  padding: 0 24px;
  border-bottom: 1px solid #DFE7E8;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.title-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.drawer-header h2 {
  font-size: 16px;
  font-weight: 600;
  color: #172D32;
}

.count-pill {
  background: #F4F6F6;
  color: #697B80;
  font-size: 12px;
  font-weight: 500;
  padding: 2px 8px;
  border-radius: 999px;
}

.close-btn {
  background: transparent;
  border: none;
  color: #697B80;
  font-size: 16px;
  cursor: pointer;
  padding: 6px;
  border-radius: 6px;
}

.close-btn:hover {
  background: #F4F6F6;
  color: #172D32;
}

.drawer-actions {
  padding: 10px 24px;
  border-bottom: 1px solid #DFE7E8;
  background: #FAFBFB;
  display: flex;
  justify-content: flex-end;
}

.mark-all-btn {
  background: none;
  border: none;
  color: #172D32;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  text-decoration: underline;
}

.drawer-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.empty-state {
  text-align: center;
  color: #697B80;
  font-size: 13px;
  margin-top: 40px;
}

.notification-item {
  padding: 14px;
  border-radius: 8px;
  border: 1px solid #DFE7E8;
  background: #FFFFFF;
  cursor: pointer;
  transition: background-color 0.15s;
}

.notification-item:hover {
  background: #F9FAFA;
}

.notification-item.unread {
  background: #F4F8F9;
  border-left: 3px solid #172D32;
}

.item-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 4px;
}

.item-title {
  font-size: 13px;
  font-weight: 600;
  color: #172D32;
}

.item-time {
  font-size: 11px;
  color: #697B80;
  white-space: nowrap;
}

.item-message {
  font-size: 12px;
  color: #697B80;
  line-height: 1.45;
}
</style>