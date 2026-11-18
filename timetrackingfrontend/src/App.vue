<script setup>
import { ref, computed } from 'vue'
import LoginView from './views/LoginView.vue'
import DashboardView from './views/DashboardView.vue'
import TimeTrackingView from './views/TimeTrackingView.vue'
import SickleaveView from './views/SickleaveView.vue'
import VacationView from './views/VacationView.vue'
import MonthlyReviewView from './views/MonthlyReviewView.vue'
import AppHeader from './components/layout/AppHeader.vue'
import AppFooter from './components/layout/AppFooter.vue'
import AppSidebar from './components/layout/AppSidebar.vue'
import NotificationSidebar from './components/layout/NotificationSidebar.vue'
import employeeDataService, { notificationService } from './services/api' // employeeService importieren

// Authentifizierungs-Zustand
const isAuthenticated = ref(false)
const currentUser = ref({
  id: null, 
  name: '',
  email: '',
  role: 'employee',
  initials: '',
  title: ''
})

// Aktive Ansicht (Route) innerhalb der App
const activeRoute = ref('timetracking')
const pendingReviewsCount = ref(3)

// Benachrichtigungs-Zustand
const isNotificationsOpen = ref(false)
const notifications = ref([])

const unreadNotificationsCount = computed(() => {
  return notifications.value.filter(n => !n.read).length
})

// Benachrichtigungen vom Backend laden
const loadNotifications = async (employeeId) => {
  try {
    const response = await notificationService.getNotifications(employeeId, false)
    notifications.value = response.data
  } catch (error) {
    console.error("Fehler beim Laden der Benachrichtigungen:", error)
  }
}

// Einzelne Benachrichtigung als gelesen markieren
const handleMarkAsRead = async (notificationId) => {
  try {
    await notificationService.markAsRead(notificationId, currentUser.value.id)
    const item = notifications.value.find(n => n.id === notificationId)
    if (item) item.read = true
  } catch (error) {
    console.error("Fehler beim Aktualisieren der Benachrichtigung:", error)
  }
}

// Alle als gelesen markieren
const handleMarkAllAsRead = async () => {
  try {
    await notificationService.markAllAsRead(currentUser.value.id)
    notifications.value.forEach(n => { n.read = true })
  } catch (error) {
    console.error("Fehler beim Markieren aller Benachrichtigungen:", error)
  }
}

// Login-Handler (ID über E-Mail ermitteln)
const handleLogin = async (userData) => {
  console.log("Empfangene Login-Daten vom Backend:", userData)

  const role = userData.role ? userData.role.toLowerCase() : 'employee'
  const fullName = userData.name || 'User'
  const email = userData.email

  const nameParts = fullName.split(' ')
  const initials = nameParts.length >= 2 
    ? `${nameParts[0][0]}${nameParts[1][0]}`.toUpperCase() 
    : fullName.substring(0, 2).toUpperCase()

  let employeeId = null

  try {
    // ID über die E-Mail-Adresse vom Employee-Endpoint abfragen
    // (Deine api.js hat: getAll(email = null) -> filtert per ?email=...)
    const response = await employeeDataService.getAll(email)
    
    // Je nachdem, ob das Backend eine Liste oder ein einzelnes Objekt zurückgibt:
    const employeeData = Array.isArray(response.data) ? response.data[0] : response.data
    
    if (employeeData && (employeeData.id !== undefined || employeeData.employeeId !== undefined)) {
      employeeId = employeeData.id !== undefined ? employeeData.id : employeeData.employeeId
    }
  } catch (error) {
    console.error("Fehler beim Abrufen der Mitarbeiter-ID über die E-Mail:", error)
  }

  currentUser.value = {
    id: employeeId,
    name: fullName,
    email: email,
    role: role,
    initials: initials,
    title: role.toUpperCase()
  }
  
  isAuthenticated.value = true

  if (!employeeId) {
    console.error("FEHLER: Es konnte keine Mitarbeiter-ID zur E-Mail ermittelt werden!")
  } else {
    // Benachrichtigungen gezielt für diesen User laden
    await loadNotifications(employeeId)
  }

  if (role === 'supervisor') {
    activeRoute.value = 'monthly-review'
  } else if (role === 'hr') {
    activeRoute.value = 'sickleave'
  } else {
    activeRoute.value = 'timetracking'
  }
}

const handleNavigate = (targetRoute) => {
  if (targetRoute === 'time-tracking') {
    activeRoute.value = 'timetracking'
  } else {
    activeRoute.value = targetRoute
  }
}

const headerMeta = computed(() => {
  switch (activeRoute.value) {
    case 'dashboard':
      return { section: 'Overview', page: 'Personal Dashboard' }
    case 'timetracking':
      return { section: 'Time & Attendance', page: 'Time Tracking' }
    case 'vacation':
      return { section: 'Time & Attendance', page: 'Vacation' }
    case 'sickleave':
      return { section: 'Human Resources', page: 'Sickleave' }
    case 'monthly-review':
      return { section: 'Team Management', page: 'Monthly Review' }
    default:
      return { section: 'Workspace', page: activeRoute.value }
  }
})

const handleLogout = () => {
  isAuthenticated.value = false
  isNotificationsOpen.value = false
  notifications.value = []
  currentUser.value = { id: null, name: '', email: '', role: 'employee', initials: 'MM', title: 'Employee' }
  activeRoute.value = 'timetracking'
}

</script>

<template>
  <LoginView v-if="!isAuthenticated" @login="handleLogin" />

  <div v-else class="app-layout">
    <AppSidebar
      :active-route="activeRoute"
      :role="currentUser.role"
      :user="currentUser"
      :pending-reviews-count="pendingReviewsCount"
      @navigate="handleNavigate"
      @open-help="handleOpenHelp"
      @logout="handleLogout"
    />

    <div class="main-wrapper">
      <AppHeader 
        :section="headerMeta.section" 
        :page="headerMeta.page" 
        :unread-count="unreadNotificationsCount"
        @toggle-notifications="isNotificationsOpen = !isNotificationsOpen"
      />

      <main class="content-body">
        <DashboardView 
          v-if="activeRoute === 'dashboard'" 
          :user="currentUser"
          @navigate="handleNavigate" 
        />
        <TimeTrackingView 
          v-else-if="activeRoute === 'timetracking'"
          :user="currentUser"
          :active-route="activeRoute" 
        />
        <VacationView 
          v-else-if="activeRoute === 'vacation'"
          :user="currentUser"
          :active-route="activeRoute" 
        />
        <SickleaveView 
          v-else-if="activeRoute === 'sickleave' && currentUser.role === 'hr'"
          :user="currentUser"
          :active-route="activeRoute" 
        />
        <MonthlyReviewView 
          v-else-if="activeRoute === 'monthly-review' && currentUser.role === 'supervisor'"
          :user="currentUser"
          :active-route="activeRoute" 
        />
      </main>

      <AppFooter />
    </div>

    <!-- Benachrichtigungs-Sidebar -->
    <NotificationSidebar
      :is-open="isNotificationsOpen"
      :notifications="notifications"
      @close="isNotificationsOpen = false"
      @mark-as-read="handleMarkAsRead"
      @mark-all-read="handleMarkAllAsRead"
    />
  </div>
</template>

<style>
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