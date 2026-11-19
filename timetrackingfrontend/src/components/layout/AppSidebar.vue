<script setup>
defineProps({
  activeRoute: { type: String, required: true },
  role: { type: String, default: 'employee' },
  user: {
    type: Object,
    default: () => ({ initials: '', name: '', title: '', email: '' }),
  },
  pendingReviewsCount: { type: Number},
})

const emit = defineEmits(['navigate', 'open-help', 'logout'])
</script>

<template>
  <aside class="sidebar">
    <!-- OBERER BEREICH: Logo, Titel und Navigation -->
    <div class="sidebar-top-content">
      <div class="brand-container">
        <div class="brand-logo">
          <div class="logo-box"></div>
        </div>
        <div class="brand-title">STC</div>
      </div>
      <div class="brand-subtitle">STC Timetracking Software</div>

      <div class="nav-section">
        <div class="nav-category">Personal</div>
        <button
          @click="emit('navigate', 'dashboard')"
          class="nav-item"
          :style="{ background: activeRoute === 'dashboard' ? '#E8F3F2' : 'transparent' }"
        >
          <div class="nav-icon-box">
          <span
            class="nav-icon-glyph"
            :style="{ color: activeRoute === 'dashboard' ? '#176A70' : '#697B80' }"
          >󰕮</span>
          </div>
          <div
            class="nav-text"
            :style="{ color: activeRoute === 'dashboard' ? '#176A70' : '#697B80', fontWeight: activeRoute === 'dashboard' ? '600' : '500' }"
          >
            Dashboard
          </div>
        </button>

        <button
          @click="emit('navigate', 'timetracking')"
          class="nav-item"
          :style="{ background: activeRoute === 'timetracking' ? '#E8F3F2' : 'transparent' }"
        >
          <div class="nav-icon-box">
          <span
            class="nav-icon-glyph"
            :style="{ color: activeRoute === 'timetracking' ? '#176A70' : '#697B80' }"
          >&#xf073;</span>
          </div>
          <div
            class="nav-text"
            :style="{ color: activeRoute === 'timetracking' ? '#176A70' : '#697B80', fontWeight: activeRoute === 'timetracking' ? '600' : '500' }"
          >
            Timesheet 
          </div>
        </button>

        <!-- NEU: Vacation-Menüpunkt für alle Rollen im Personal-Bereich -->
        <button
          @click="emit('navigate', 'vacation')"
          class="nav-item"
          :style="{ background: activeRoute === 'vacation' ? '#E8F3F2' : 'transparent' }"
        >
          <div class="nav-icon-box">
          <span
            class="nav-icon-glyph"
            :style="{ color: activeRoute === 'vacation' ? '#176A70' : '#697B80' }"
          >&#xF054A;</span>
          </div>
          <div
            class="nav-text"
            :style="{ color: activeRoute === 'vacation' ? '#176A70' : '#697B80', fontWeight: activeRoute === 'vacation' ? '600' : '500' }"
          >
            Vacation
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
          <span
            class="nav-icon-glyph"
            :style="{ color: activeRoute === 'monthly-review' ? '#176A70' : '#697B80' }"
          ></span>
            </div>
            <div
              class="nav-text"
              :style="{ color: activeRoute === 'monthly-review' ? '#176A70' : '#697B80', fontWeight: activeRoute === 'monthly-review' ? '600' : '500' }"
            >
              Monthly review
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
          <span
            class="nav-icon-glyph"
            :style="{ color: activeRoute === 'sickleave' ? '#176A70' : '#697B80' }"
          ></span>
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
    </div>

    <!-- UNTERER BEREICH: Benutzer-Profil & Abmelden -->
    <div class="sidebar-bottom-content">
      <div class="sidebar-footer">
        <div class="user-footer">
          <div class="user-avatar">
            <div class="avatar-text">{{ user.initials }}</div>
          </div>
          <div class="user-info">
            <div class="user-name">{{ user.name }}</div>
            <div class="user-role">{{ user.title }}</div>
            <span class="logged-in-email" v-if="user.email">{{ user.email }}</span>
          </div>
        </div>
        <button @click="emit('logout')" class="logout-btn">Abmelden</button>
      </div>
    </div>
  </aside>
</template>

<style scoped>

@font-face {
  font-family: 'Symbols Nerd Font';
  src: url('../../assets/fonts/SymbolsNerdFont-Regular.ttf') format('truetype');
}

.sidebar {
  width: 280px;
  background: #FFFFFF;
  border-right: 1px solid #DFE7E8;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 24px;
  height: 100vh;
  position: sticky;
  top: 0;
}

.sidebar-top-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.sidebar-bottom-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.brand-container {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-box {
  width: 32px;
  height: 32px;
  background: #176A70;
  border-radius: 8px;
}

.brand-title {
  font-size: 18px;
  font-weight: 700;
  color: #172D32;
}

.brand-subtitle {
  font-size: 11px;
  color: #697B80;
  margin-bottom: 8px;
}

.nav-section {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.nav-category {
  font-size: 10px;
  font-weight: 700;
  color: #91A0A4;
  margin-top: 12px;
  margin-bottom: 4px;
  letter-spacing: 0.5px;
}
.nav-icon-glyph {
  font-family: 'Symbols Nerd Font', 'Symbols Nerd Font Mono', 'JetBrainsMono Nerd Font', monospace;
  font-size: 16px;
  line-height: 18px;
  width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  width: 100%;
  text-align: left;
  transition: background 0.2s;
}

.nav-item:hover {
  background: #F4F6F6;
}

.nav-icon-box {
  width: 18px;
  height: 18px;
  position: relative;
}

.nav-icon-shape {
  width: 14px;
  height: 14px;
  position: absolute;
  top: 2px;
  left: 2px;
  border-radius: 3px;
}

.nav-text {
  font-size: 13px;
}

.group-section {
  display: flex;
  flex-direction: column;
}

.sidebar-footer {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding-top: 16px;
  border-top: 1px solid #DFE7E8;
}

.user-footer {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  width: 36px;
  height: 36px;
  background: #E8F3F2;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-text {
  font-size: 12px;
  font-weight: 700;
  color: #176A70;
}

.user-info {
  display: flex;
  flex-direction: column;
  flex-grow: 1;
}

.user-name {
  font-size: 13px;
  font-weight: 600;
  color: #172D32;
}

.user-role {
  font-size: 11px;
  color: #697B80;
}

.logged-in-email {
  font-size: 11px;
  color: #697B80;
  word-break: break-all;
}

.logout-btn {
  padding: 8px;
  background: #FDE8E8;
  color: #C53030;
  border: none;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.logout-btn:hover {
  background: #FBD5D5;
}
</style>