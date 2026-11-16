<script setup>
import { ref } from 'vue'
import LoginView from './views/LoginView.vue'

// Authentifizierungs-Zustand
const isAuthenticated = ref(false)
const currentUser = ref({
  name: '',
  email: '',
  role: 'employee' // Standardrolle
})

// Aktive Ansicht (Route) innerhalb der App
const activeRoute = ref('dashboard')

// Login-Handler: Empfängt die Daten inklusive Rolle vom Backend
const handleLogin = (userData) => {
  currentUser.value = {
    name: userData.name,
    email: userData.email,
    role: userData.role || 'employee'
  }
  isAuthenticated.value = true

  // Rollenbasiertes Routing beim ersten Login
  if (currentUser.value.role === 'supervisor') {
    activeRoute.value = 'supervisor-view'
  } else if (currentUser.value.role === 'hr') {
    activeRoute.value = 'hr-view'
  } else {
    activeRoute.value = 'dashboard'
  }
}

// Abmelden
const handleLogout = () => {
  isAuthenticated.value = false
  currentUser.value = { name: '', email: '', role: 'employee' }
  activeRoute.value = 'dashboard'
}
</script>

<template>
  <!-- 1. Nicht eingeloggt: Zeige den Login-Screen -->
  <LoginView v-if="!isAuthenticated" @login="handleLogin" />

  <!-- 2. Eingeloggt: Zeige das Dashboard mit rollenabhängiger Navigation -->
  <div v-else class="app-layout">
    <!-- Sidebar / Navigation -->
    <aside class="sidebar">
      <div class="user-profile">
        <div class="avatar">👤</div>
        <div class="user-details">
          <h3>{{ currentUser.name }}</h3>
          <span class="role-badge">{{ currentUser.role.toUpperCase() }}</span>
        </div>
      </div>

      <nav class="nav-menu">
        <!-- Für alle Rollen sichtbar -->
        <button 
          @click="activeRoute = 'dashboard'" 
          :class="{ active: activeRoute === 'dashboard' }" 
          class="nav-btn">
          Mitarbeiter-Übersicht
        </button>

        <!-- Nur für Supervisor und HR sichtbar -->
        <button 
          v-if="currentUser.role === 'supervisor' || currentUser.role === 'hr'" 
          @click="activeRoute = 'supervisor-view'" 
          :class="{ active: activeRoute === 'supervisor-view' }" 
          class="nav-btn">
          Team-Zeiterfassung (Supervisor)
        </button>

        <!-- Nur für HR sichtbar -->
        <button 
          v-if="currentUser.role === 'hr'" 
          @click="activeRoute = 'hr-view'" 
          :class="{ active: activeRoute === 'hr-view' }" 
          class="nav-btn">
          Mitarbeiterverwaltung (HR)
        </button>
      </nav>

      <button @click="handleLogout" class="logout-btn">Abmelden</button>
    </aside>

    <!-- Hauptinhalt (Wechselt je nach aktiver Route und Rolle) -->
    <main class="main-content">
      <header class="content-header">
        <h2>Ansicht: {{ activeRoute.toUpperCase() }}</h2>
        <span class="logged-in-as">Eingeloggt als: {{ currentUser.email }}</span>
      </header>

      <section class="content-body">
        <!-- Hier können Sie Ihre jeweiligen Vue-Komponenten einbinden -->
        <div v-if="activeRoute === 'dashboard'" class="card">
          <h3>Willkommen im Mitarbeiter-Dashboard</h3>
          <p>Hier erfassen Sie Ihre Arbeitszeiten und sehen Ihren Urlaubsstatus.</p>
        </div>

        <div v-else-if="activeRoute === 'supervisor-view'" class="card">
          <h3>Supervisor / Team-Bereich</h3>
          <p>Hier können Sie die Zeiten und Anträge Ihres Teams einsehen und freigeben.</p>
        </div>

        <div v-else-if="activeRoute === 'hr-view'" class="card">
          <h3>HR / Personalverwaltung</h3>
          <p>Hier haben Sie Zugriff auf alle Mitarbeiterdaten, Krankheitstage und Verträge.</p>
        </div>
      </section>
    </main>
  </div>
</template>

<style>
/* Globale Styles / Reset */
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}

body {
  background-color: #F4F6F6;
  color: #172D32;
}

.app-layout {
  display: flex;
  min-height: 100vh;
}

/* Sidebar Styling */
.sidebar {
  width: 280px;
  background: #FFFFFF;
  border-right: 1px solid #DFE7E8;
  display: flex;
  flex-direction: column;
  padding: 24px;
  gap: 24px;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 16px;
  border-bottom: 1px solid #DFE7E8;
}

.avatar {
  font-size: 24px;
  background: #E8F3F2;
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-details h3 {
  font-size: 14px;
  font-weight: 600;
  color: #172D32;
}

.role-badge {
  font-size: 10px;
  font-weight: 700;
  background: #176A70;
  color: white;
  padding: 2px 6px;
  border-radius: 4px;
  display: inline-block;
  margin-top: 4px;
}

.nav-menu {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex-grow: 1;
}

.nav-btn {
  text-align: left;
  padding: 10px 14px;
  background: transparent;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  color: #697B80;
  cursor: pointer;
  transition: all 0.2s;
}

.nav-btn:hover {
  background: #F4F6F6;
  color: #172D32;
}

.nav-btn.active {
  background: #E8F3F2;
  color: #176A70;
  font-weight: 600;
}

.logout-btn {
  padding: 10px;
  background: #FDE8E8;
  color: #C53030;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.logout-btn:hover {
  background: #FBD5D5;
}

/* Main Content Styling */
.main-content {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  padding: 32px;
  gap: 24px;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  padding: 20px 24px;
  border-radius: 12px;
  border: 1px solid #DFE7E8;
}

.content-header h2 {
  font-size: 18px;
  font-weight: 600;
  color: #172D32;
}

.logged-in-as {
  font-size: 12px;
  color: #697B80;
}

.content-body {
  flex-grow: 1;
}

.card {
  background: white;
  padding: 32px;
  border-radius: 12px;
  border: 1px solid #DFE7E8;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.card h3 {
  font-size: 18px;
  color: #172D32;
}

.card p {
  font-size: 14px;
  color: #697B80;
}
</style>