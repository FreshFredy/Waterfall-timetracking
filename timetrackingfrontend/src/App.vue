<script setup>
import { ref, computed } from 'vue'
import LoginView from './views/LoginView.vue'
import DashboardView from './views/DashboardView.vue'
import TimeTrackingView from './views/TimeTrackingView.vue'
import SickleaveView from './views/SickleaveView.vue'
import MonthlyReviewView from './views/MonthlyReviewView.vue'
import AppHeader from './components/layout/AppHeader.vue'
import AppFooter from './components/layout/AppFooter.vue'
import AppSidebar from './components/layout/AppSidebar.vue' 

// Authentifizierungs-Zustand
const isAuthenticated = ref(false)
const currentUser = ref({
  name: '',
  email: '',
  role: 'employee',
  initials: '',
  title: ''
})

// Aktive Ansicht (Route) innerhalb der App
const activeRoute = ref('timetracking')
const pendingReviewsCount = ref(3)

// Login-Handler: Empfängt die Daten vom Backend (AuthController)
const handleLogin = (userData) => {
  const role = userData.role ? userData.role.toLowerCase() : 'employee'
  const fullName = userData.name || 'User'
  
  // Initialen aus dem Namen generieren (z.B. "Markus Müller" -> "MM")
  const nameParts = fullName.split(' ')
  const initials = nameParts.length >= 2 
    ? `${nameParts[0][0]}${nameParts[1][0]}`.toUpperCase() 
    : fullName.substring(0, 2).toUpperCase()

  currentUser.value = {
    name: fullName,
    email: userData.email,
    role: role,
    initials: initials,
    title: role.toUpperCase()
  }
  
  isAuthenticated.value = true

  // Rollenbasiertes Routing direkt nach dem Login
  if (role === 'supervisor') {
    activeRoute.value = 'monthly-review'
  } else if (role === 'hr') {
    activeRoute.value = 'sickleave'
  } else {
    activeRoute.value = 'timetracking'
  }
}

// Navigation aus Unterkomponenten abfangen
const handleNavigate = (targetRoute) => {
  if (targetRoute === 'time-tracking') {
    activeRoute.value = 'timetracking'
  } else {
    activeRoute.value = targetRoute
  }
}

// Dynamische Breadcrumbs für den AppHeader
const headerMeta = computed(() => {
  switch (activeRoute.value) {
    case 'dashboard':
      return { section: 'Overview', page: 'Personal Dashboard' }
    case 'timetracking':
      return { section: 'Time & Attendance', page: 'Time Tracking' }
    case 'sickleave':
      return { section: 'Human Resources', page: 'Sickleave' }
    case 'monthly-review':
      return { section: 'Team Management', page: 'Monthly Review' }
    default:
      return { section: 'Workspace', page: activeRoute.value }
  }
})

// Abmelden
const handleLogout = () => {
  isAuthenticated.value = false
  currentUser.value = { name: '', email: '', role: 'employee', initials: 'MM', title: 'Employee' }
  activeRoute.value = 'timetracking'
}

const handleOpenHelp = () => {
  alert('Hilfe-Center wird geöffnet...')
}
</script>

<template>
  <!-- 1. Nicht eingeloggt: Zeige den Login-Screen -->
  <LoginView v-if="!isAuthenticated" @login="handleLogin" />

  <!-- 2. Eingeloggt: Zeige das Layout mit der Sidebar, Header, Content & Footer -->
  <div v-else class="app-layout">
    
    <!-- Deine Sidebar-Komponente mit Props & Events -->
    <AppSidebar
      :active-route="activeRoute"
      :role="currentUser.role"
      :user="currentUser"
      :pending-reviews-count="pendingReviewsCount"
      @navigate="handleNavigate"
      @open-help="handleOpenHelp"
      @logout="handleLogout"
    />

    <!-- Hauptbereich rechts neben der Sidebar -->
    <div class="main-wrapper">
      <AppHeader 
        :section="headerMeta.section" 
        :page="headerMeta.page" 
      />

      <main class="content-body">
        <DashboardView 
          v-if="activeRoute === 'dashboard'" 
          :user="currentUser"
          @navigate="handleNavigate" 
        />
        <TimeTrackingView 
          v-else-if="activeRoute === 'timetracking'" 
        />
        <SickleaveView 
          v-else-if="activeRoute === 'sickleave' && currentUser.role === 'hr'" 
        />
        <MonthlyReviewView 
          v-else-if="activeRoute === 'monthly-review' && currentUser.role === 'supervisor'" 
        />
      </main>

      <AppFooter />
    </div>
  </div>
</template>

<style>
/* Globale Styles / Reset */
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
  font-family: Inter, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}

body {
  background-color: #F4F6F6;
  color: #172D32;
}

.app-layout {
  display: flex;
  min-height: 100vh;
}

.main-wrapper {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.content-body {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}
</style>