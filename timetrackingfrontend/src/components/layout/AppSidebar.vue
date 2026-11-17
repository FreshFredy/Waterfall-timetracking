<script setup>
defineProps({
  activeRoute: { type: String, required: true },
  role: { type: String, default: 'employee' },
  user: {
    type: Object,
    default: () => ({ initials: 'MM', name: 'Markus Müller', title: 'Employee' }),
  },
  pendingReviewsCount: { type: Number, default: 3 },
})

const emit = defineEmits(['navigate', 'open-help'])
</script>

<template>
  <aside class="sidebar">
    <div class="brand-container">
      <div class="brand-logo">
        <div class="logo-box"></div>
      </div>
      <div class="brand-title">STC</div>
    </div>
    <div class="brand-subtitle">Your room for Time Tracking</div>

    <div class="nav-section">
      <div class="nav-category">Personal</div>
      <button
        @click="emit('navigate', 'dashboard')"
        class="nav-item"
        :style="{ background: activeRoute === 'dashboard' ? '#E8F3F2' : 'transparent' }"
      >
        <div class="nav-icon-box">
          <div
            class="nav-icon-shape"
            :style="{ outline: activeRoute === 'dashboard' ? '1.70px #176A70 solid' : '1.70px #697B80 solid' }"
          ></div>
        </div>
        <div
          class="nav-text"
          :style="{ color: activeRoute === 'dashboard' ? '#176A70' : '#697B80', fontWeight: activeRoute === 'dashboard' ? '600' : '500' }"
        >
          Dashboard
        </div>
      </button>

      <button
        @click="emit('navigate', 'time-tracking')"
        class="nav-item"
        :style="{ background: activeRoute === 'time-tracking' ? '#E8F3F2' : 'transparent' }"
      >
        <div class="nav-icon-box">
          <div
            class="nav-icon-shape"
            :style="{ outline: activeRoute === 'time-tracking' ? '1.70px #176A70 solid' : '1.70px #697B80 solid' }"
          ></div>
        </div>
        <div
          class="nav-text"
          :style="{ color: activeRoute === 'time-tracking' ? '#176A70' : '#697B80', fontWeight: activeRoute === 'time-tracking' ? '600' : '500' }"
        >
          Time Tracking
        </div>
      </button>

      <div v-if="role === 'supervisor'" class="group-section">
        <div class="nav-category">TEAM MANAGEMENT</div>
        <button
          @click="emit('navigate', 'monthly-review')"
          class="nav-item"
          :style="{ background: activeRoute === 'monthly-review' ? '#E8F3F2' : 'transparent' }"
        >
          <div class="nav-icon-box">
            <div
              class="nav-icon-shape"
              :style="{ outline: activeRoute === 'monthly-review' ? '1.70px #176A70 solid' : '1.70px #697B80 solid' }"
            ></div>
          </div>
          <div
            class="nav-text"
            :style="{ color: activeRoute === 'monthly-review' ? '#176A70' : '#697B80', fontWeight: activeRoute === 'monthly-review' ? '600' : '500' }"
          >
            Monthly review
          </div>
          <div v-if="pendingReviewsCount > 0" class="badge-count">
            {{ pendingReviewsCount }}
          </div>
        </button>
      </div>

      <div v-if="role === 'hr'" class="group-section">
        <div class="nav-category">HUMAN RESOURCES</div>
        <button
          @click="emit('navigate', 'sickleave')"
          class="nav-item"
          :style="{ background: activeRoute === 'sickleave' ? '#E8F3F2' : 'transparent' }"
        >
          <div class="nav-icon-box">
            <div
              class="nav-icon-shape"
              :style="{ outline: activeRoute === 'sickleave' ? '1.70px #176A70 solid' : '1.70px #697B80 solid' }"
            ></div>
          </div>
          <div
            class="nav-text"
            :style="{ color: activeRoute === 'sickleave' ? '#176A70' : '#697B80', fontWeight: activeRoute === 'sickleave' ? '600' : '500' }"
          >
            Sickleave
          </div>
        </button>
      </div>
    </div>

    <div class="spacer"></div>

    <div class="help-box">
      <div class="help-icon-box"></div>
      <div class="help-title">All about your time.</div>
      <div class="help-desc">Help with tracking, balances and approvals.</div>
      <div class="help-link" @click="emit('open-help')">Open help ↗</div>
    </div>

    <div class="user-footer">
      <div class="user-avatar">
        <div class="avatar-text">{{ user.initials }}</div>
      </div>
      <div class="user-info">
        <div class="user-name">{{ user.name }}</div>
        <div class="user-role">{{ user.title }}</div>
      </div>
      <div class="user-arrow">›</div>
    </div>
  </aside>
</template>

<style scoped>
.sidebar {
  width: 216px;
  align-self: stretch;
  padding-top: 30px;
  padding-bottom: 20px;
  padding-left: 16px;
  padding-right: 16px;
  background: white;
  overflow: hidden;
  border-right: 1px #DFE7E8 solid;
  flex-direction: column;
  justify-content: flex-start;
  align-items: flex-start;
  gap: 30px;
  display: inline-flex;
}
.brand-container {
  padding-left: 12px;
  overflow: hidden;
  justify-content: flex-start;
  align-items: center;
  gap: 10px;
  display: inline-flex;
}
.brand-logo {
  width: 32px;
  height: 32px;
  background: #176A70;
  overflow: hidden;
  border-radius: 9px;
  justify-content: center;
  align-items: center;
  display: flex;
}
.logo-box {
  width: 19px;
  height: 20px;
  border: 2.60px white solid;
}
.brand-title {
  color: #172D32;
  font-size: 27px;
  font-family: Inter, sans-serif;
  font-weight: 400;
}
.brand-subtitle {
  padding-left: 12px;
  padding-right: 12px;
  color: #697B80;
  font-size: 11px;
  font-family: Inter, sans-serif;
  line-height: 15.95px;
}
.nav-section {
  align-self: stretch;
  overflow: hidden;
  flex-direction: column;
  justify-content: flex-start;
  align-items: flex-start;
  gap: 6px;
  display: flex;
}
.nav-category {
  color: #91A0A4;
  font-size: 10px;
  font-family: Inter, sans-serif;
  font-weight: 600;
  line-height: 14.50px;
}
.nav-item {
  align-self: stretch;
  height: 44px;
  padding-left: 12px;
  padding-right: 12px;
  border-radius: 8px;
  justify-content: flex-start;
  align-items: center;
  gap: 10px;
  display: inline-flex;
  border: none;
  cursor: pointer;
  text-align: left;
  width: 100%;
}
.nav-icon-box {
  width: 18px;
  height: 18px;
  position: relative;
  overflow: hidden;
}
.nav-icon-shape {
  width: 13.50px;
  height: 13.50px;
  left: 2.25px;
  top: 2.25px;
  position: absolute;
  outline-offset: -0.85px;
}
.nav-text {
  font-size: 12px;
  font-family: Inter, sans-serif;
  line-height: 17.40px;
  flex: 1;
}
.badge-count {
  width: 20px;
  height: 20px;
  background: #EFF3F3;
  border-radius: 6px;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #172D32;
  font-size: 10px;
  font-weight: 600;
}
.group-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
  padding-top: 24px;
}
.spacer {
  align-self: stretch;
  flex: 1 1 0;
  min-height: 120px;
}
.help-box {
  align-self: stretch;
  padding: 14px;
  background: #F8FAFA;
  border-radius: 8px;
  flex-direction: column;
  justify-content: flex-start;
  align-items: flex-start;
  gap: 10px;
  display: flex;
}
.help-icon-box {
  width: 19px;
  height: 19px;
  border: 1.70px #176A70 solid;
  border-radius: 4px;
}
.help-title {
  color: #172D32;
  font-size: 12px;
  font-family: Inter, sans-serif;
  font-weight: 600;
  line-height: 17.40px;
}
.help-desc {
  color: #697B80;
  font-size: 11px;
  font-family: Inter, sans-serif;
  line-height: 15.95px;
}
.help-link {
  color: #176A70;
  font-size: 11px;
  font-family: Inter, sans-serif;
  font-weight: 600;
  cursor: pointer;
}
.user-footer {
  align-self: stretch;
  padding-top: 16px;
  border-top: 1px #DFE7E8 solid;
  justify-content: flex-start;
  align-items: center;
  gap: 9px;
  display: inline-flex;
}
.user-avatar {
  width: 34px;
  height: 34px;
  background: #E8F3F2;
  border-radius: 100px;
  display: flex;
  justify-content: center;
  align-items: center;
}
.avatar-text {
  color: #176A70;
  font-size: 12px;
  font-weight: 600;
}
.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.user-name {
  color: #172D32;
  font-size: 12px;
  font-weight: 600;
}
.user-role {
  color: #697B80;
  font-size: 10px;
}
.user-arrow {
  color: #697B80;
  font-size: 14px;
}
</style>