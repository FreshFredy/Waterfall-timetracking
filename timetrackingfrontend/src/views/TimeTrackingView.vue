<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import StatusBadge from '../components/ui/StatusBadge.vue'
import { timesheetService } from '../services/api.js'
import EmployeeService from '../services/api.js'

const props = defineProps({
  employeeId: { type: Number },
  user: {
    type: Object,
    default: () => ({ email: '' }),
  },
})

const resolveEmployeeId = async () => {
  if (props.employeeId) {
    return props.employeeId
  }

  if (props.user && props.user.email) {
    console.log("Versuche ID für E-Mail aufzulösen:", props.user.email)
    const employeesResponse = await EmployeeService.getAll(props.user.email)
    console.log("Antwort von EmployeeService.getAll:", employeesResponse)
    
    if (employeesResponse.data && employeesResponse.data.length > 0) {
      return employeesResponse.data[0].id
    }
  }

  throw new Error("Konnte keine Employee-ID ermitteln: Weder props.employeeId noch eine gültige E-Mail / Employee-Daten vorhanden.")
}

const form = ref({
  date: new Date().toISOString().substr(0, 10),
  start: '08:30',
  end: '17:00',
  breakMinutes: 30,
  entryType: 'WORK', // Bleibt im Hintergrund auf WORK (oder anpassbar)
})

// Navigation State für Monat und Jahr
const selectedDate = ref(new Date())

// Daten vom Backend
const currentTimesheet = ref(null)
const dailyEntries = ref([])
const isLoading = ref(false)
const errorMessage = ref('')

// Hilfsfunktion: Minuten in "H:MM" formatieren
const formatMinutes = (mins) => {
  if (mins == null || isNaN(mins)) return '0:00'
  const h = Math.floor(mins / 60)
  const m = String(mins % 60).padStart(2, '0')
  return `${h}:${m}`
}

// Berechnung für die Live-Vorschau im rechten Formular
const timeStats = computed(() => {
  if (!form.value.start || !form.value.end) return { onSite: '0:00 h', breakStr: '0:00 h', net: '0:00 h' }
  const [sh, sm] = form.value.start.split(':').map(Number)
  const [eh, em] = form.value.end.split(':').map(Number)
  const totalMinutes = Math.max(0, eh * 60 + em - (sh * 60 + sm))
  const breakMins = Number(form.value.breakMinutes) || 0
  const netMinutes = Math.max(0, totalMinutes - breakMins)

  return {
    onSite: `${formatMinutes(totalMinutes)} h`,
    breakStr: `${formatMinutes(breakMins)} h`,
    net: `${formatMinutes(netMinutes)} h`,
  }
})

// Gibt z. B. "November 2026" aus
const formattedPeriod = computed(() => {
  return selectedDate.value.toLocaleDateString('de-DE', {
    month: 'long',
    year: 'numeric'
  })
})

// 1. Timesheet und Einträge vom Backend laden (GET) über timesheetService
const fetchTimesheetData = async () => {
  isLoading.value = true
  errorMessage.value = ''
  
  try {
    const resolvedId = await resolveEmployeeId()
    const year = selectedDate.value.getFullYear()
    const month = selectedDate.value.getMonth() + 1 // JavaScript Monate sind 0-basiert (0 = Januar)
    
    const response = await timesheetService.getTimesheets(resolvedId, month, year)
    const data = response.data
    
    if (Array.isArray(data) && data.length > 0) {
      currentTimesheet.value = data[0]
      const rawEntries = data[0].timeEntries || []
      dailyEntries.value = rawEntries.map(mapBackendEntryToRow)
    } else {
      currentTimesheet.value = null
      dailyEntries.value = []
    }
  } catch (err) {
    console.error('API Fehler:', err)
    errorMessage.value = 'Daten konnten nicht vom Server geladen werden.'
  } finally {
    isLoading.value = false
  }
}

// Hilfsfunktion: Backend-DTO-Felder auf Tabellenzeile mappen
const mapBackendEntryToRow = (entry) => {
  let formattedDate = entry.entryDate || '–'
  if (entry.entryDate) {
    const d = new Date(entry.entryDate)
    if (!isNaN(d)) {
      formattedDate = d.toLocaleDateString('de-DE', {
        weekday: 'short',
        day: '2-digit',
        month: '2-digit',
      })
    }
  }

  const startStr = entry.startTime ? entry.startTime.substring(0, 5) : '–'
  const endStr = entry.endTime ? entry.endTime.substring(0, 5) : '–'
  const breakMins = entry.breakMinutes ?? 0
  
  const netHours = entry.creditedHours ?? 0
  const netMinsFormatted = formatMinutes(Math.round(netHours * 60))

  // Backend-Typ abfangen (unterstützt entryType, entry_type und type)
  const rawType = entry.entryType ?? entry.entry_type ?? entry.type ?? 'WORK'

  const typeMapping = {
    'WORK': 'Arbeit',
    'SICK': 'Krank',
    'krank': 'Krank',
    'VACATION': 'Urlaub',
    'URLAUB': 'Urlaub'
  }

  const displayType = typeMapping[rawType] ?? rawType

  return {
    id: entry.id,
    date: formattedDate,
    start: startStr,
    end: endStr,
    break: breakMins > 0 ? formatMinutes(breakMins) : '0:00',
    net: `${netMinsFormatted}`,
    type: displayType,
  }
}

// 2. Neuen Zeiteintrag ans Backend senden (POST) über timesheetService
const handleAddEntry = async () => {
  errorMessage.value = ''
  
  const payload = {
    entryDate: form.value.date,
    startTime: `${form.value.start}:00`,    
    endTime: `${form.value.end}:00`,        
    breakMinutes: form.value.breakMinutes,  
    entryType: form.value.entryType // Sendet den Typ mit (Standard 'WORK')
  }

  try {
    const resolvedId = await resolveEmployeeId()
    const response = await timesheetService.recordWorkTime(resolvedId, payload)
    const updatedTimesheet = response.data
    currentTimesheet.value = updatedTimesheet
    const rawEntries = updatedTimesheet.timeEntries || []
    dailyEntries.value = rawEntries.map(mapBackendEntryToRow)
    
    // Nach erfolgreichem Speichern Daten für den aktuellen Monat neu laden
    fetchTimesheetData()
  } catch (err) {
    console.error('Speicherfehler:', err)
    errorMessage.value = 'Eintrag konnte nicht gespeichert werden.'
  }
}

const changeMonth = (delta) => {
  const newDate = new Date(selectedDate.value)
  newDate.setMonth(newDate.getMonth() + delta)
  selectedDate.value = newDate
}

const changeYear = (delta) => {
  const newDate = new Date(selectedDate.value)
  newDate.setFullYear(newDate.getFullYear() + delta)
  selectedDate.value = newDate
}

const resetToToday = () => {
  selectedDate.value = new Date()
}

// Bei Änderung der Monats-/ Jahresnavigation automatisch Daten neu laden
watch(selectedDate, () => {
  fetchTimesheetData()
})

// Beim Öffnen der Seite initial Daten laden
onMounted(() => {
  fetchTimesheetData()
})
</script>

<template>
  <div class="tt-container">
    <div class="tt-header-row">
      <h1 class="tt-title">Zeiterfassung</h1>
      <div class="tt-actions">
        <!-- Submit Timesheet bleibt deaktiviert -->
        <button class="disabled-submit-btn" disabled><span>Submit Timesheet</span></button>
      </div>
    </div>

    <div v-if="errorMessage" class="error-banner">
      {{ errorMessage }}
    </div>

    <div class="content-split">
      <div class="table-card">
        <div class="table-card-header">
          <div>
            <h2 class="box-title">Timesheet</h2>
          </div>

          <!-- Monats- & Jahresnavigation mit modernem Button-Styling -->
          <div class="date-navigation">
            <div class="nav-group">
              <button @click="changeYear(-1)" class="nav-btn" title="Vorheriges Jahr">«</button>
              <button @click="changeMonth(-1)" class="nav-btn" title="Vorheriger Monat">‹</button>
            </div>

            <span class="current-period">{{ formattedPeriod }}</span>

            <div class="nav-group">
              <button @click="changeMonth(1)" class="nav-btn" title="Nächster Monat">›</button>
              <button @click="changeYear(1)" class="nav-btn" title="Nächstes Jahr">»</button>
            </div>

            <button @click="resetToToday" class="today-btn">Heute</button>
          </div>
        </div>

        <table class="data-table">
          <thead>
            <tr class="th-row">
              <th class="th-date">Datum</th>
              <th class="th-start">Start</th>
              <th class="th-end">Ende</th>
              <th class="th-break">Pause</th>
              <th class="th-break">Typ</th>
              <th class="th-net">Arbeitszeit</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="isLoading">
              <td colspan="6" class="empty-state">Lade Zeiteinträge...</td>
            </tr>
            <tr v-else-if="dailyEntries.length === 0">
              <td colspan="6" class="empty-state">Keine Einträge für diesen Monat vorhanden.</td>
            </tr>
            <tr v-else v-for="(entry, index) in dailyEntries" :key="entry.id || index" class="data-tr">
              <td class="td-date">{{ entry.date }}</td>
              <td>{{ entry.start }}</td>
              <td>{{ entry.end }}</td>
              <td>{{ entry.break }}</td>
              <td>{{ entry.type }}</td>
              <td class="td-net-val">{{ entry.net }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="right-sidebar">
        <form @submit.prevent="handleAddEntry" class="form-card">
          <div class="form-card-header">
            <h2 class="box-title">Time tracking</h2>
            <StatusBadge variant="neutral" :label="currentTimesheet?.status || 'Draft'" />
          </div>
          <label class="input-label">
            Datum
            <input v-model="form.date" type="date" class="text-input" required />
          </label>

          <div class="time-inputs-row">
            <label class="input-label flex-1">
              Start
              <input v-model="form.start" type="time" class="text-input" required />
            </label>
            <label class="input-label flex-1">
              Ende
              <input v-model="form.end" type="time" class="text-input" required />
            </label>
          </div>

          <label class="input-label">
            Pausenzeit in Minuten
            <input v-model.number="form.breakMinutes" type="number" min="0" step="5" class="text-input" />
          </label>

          <div class="calc-box">
            <div class="calc-header">
              <span class="calc-val">Arbeitszeit = {{ timeStats.net }}</span>
            </div>
            <div class="calc-sub">Komplette Zeit - Pausen = Arbeitszeit</div>
          </div>

          <button type="submit" class="submit-btn">
            <span>✓</span><span>Zeit einreichen</span>
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.text-input, .select-input, input[type="date"], input[type="time"], input[type="number"] {
  background-color: #FFFFFF !important;
  color: #000000 !important;
  color-scheme: light;
}
.text-input::placeholder { color: #697B80; }
.input-label { display: flex; flex-direction: column; gap: 7px; color: #172D32; font-size: 12px; font-weight: 500; }
.form-card {
  width: 432px;
  flex-shrink: 0;
  padding: 24px;
  background: white;
  border-radius: 12px;
  outline: 1px #DFE7E8 solid;
  display: flex;
  flex-direction: column;
  gap: 18px;
  color: #172D32;
}
.tt-container { padding: 32px; display: flex; flex-direction: column; gap: 24px; }
.tt-header-row { height: 143px; display: flex; align-items: center; gap: 24px; }
.tt-title { flex: 1; color: #172D32; font-size: 30px; font-weight: 600; line-height: 36px; }
.tt-actions { display: flex; align-items: center; gap: 12px; }
.disabled-submit-btn { height: 40px; padding: 0 14px; background: #B3B3B3; border-radius: 8px; color: white; font-size: 13px; font-weight: 600; display: flex; align-items: center; border: none; cursor: not-allowed; }
.content-split { display: flex; gap: 24px; }
.table-card { flex: 1; background: white; border-radius: 12px; outline: 1px #DFE7E8 solid; display: flex; flex-direction: column; overflow: hidden; }

/* Header Layout */
.table-card-header { 
  padding: 20px; 
  display: flex; 
  justify-content: space-between; 
  align-items: center; 
  flex-wrap: wrap;
  gap: 12px;
}

/* Modernes Design für die Datumsnavigation & Buttons */
.date-navigation {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #FFFFFF;
  border: 1px solid #DFE7E8;
  padding: 4px 8px;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.02);
}

.nav-group {
  display: flex;
  gap: 2px;
}

.current-period {
  font-weight: 600;
  font-size: 13px;
  color: #172D32;
  min-width: 130px;
  text-align: center;
}

.nav-btn {
  background: transparent;
  border: none;
  border-radius: 4px;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #4B5563;
  font-size: 15px;
  transition: all 0.2s ease;
}

.nav-btn:hover {
  background-color: #F3F4F6;
  color: #176A70;
}

.today-btn {
  background: #E8F3F2;
  border: 1px solid #D1E5E4;
  border-radius: 6px;
  padding: 4px 10px;
  color: #176A70;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-left: 4px;
}

.today-btn:hover {
  background: #D1E5E4;
}

.box-title { color: #172D32; font-size: 16px; font-weight: 600; }
.data-table { width: 100%; border-collapse: collapse; text-align: left; }
.th-row { height: 36px; background: #F8FAFA; border-top: 1px #DFE7E8 solid; border-bottom: 1px #DFE7E8 solid; color: #697B80; font-size: 10px; font-weight: 600; }
.th-date { padding-left: 20px; width: 136px; }
.th-start, .th-end, .th-break, .th-net { width: 84px; }
.data-tr { height: 38px; border-bottom: 1px #DFE7E8 solid; font-size: 12px; color: #172D32; }
.td-date { padding-left: 20px; }
.td-net-val { font-weight: 600; }
.empty-state { padding: 24px 20px; color: #697B80; font-size: 12px; text-align: center; }
.right-sidebar { width: 432px; display: flex; flex-direction: column; gap: 20px; }
.form-card-header { display: flex; justify-content: space-between; align-items: center; }
.text-input, .select-input { height: 42px; padding: 12px; background: white; border-radius: 8px; outline: 1px #DFE7E8 solid; font-size: 13px; border: none; }
.time-inputs-row { display: flex; gap: 12px; }
.flex-1 { flex: 1; }
.calc-box { padding: 16px; background: #E8F3F2; border-radius: 8px; display: flex; flex-direction: column; gap: 8px; }
.calc-header { display: flex; justify-content: space-between; color: #176A70; font-size: 12px; font-weight: 500; }
.calc-val { font-size: 18px; font-weight: 600; }
.calc-sub { color: #176A70; font-size: 10px; }
.submit-btn { height: 40px; padding: 0 14px; background: #176A70; border-radius: 8px; color: white; font-size: 13px; font-weight: 600; display: flex; justify-content: center; align-items: center; gap: 8px; border: none; cursor: pointer; }
.error-banner { padding: 12px 16px; background: #FEE2E2; color: #991B1B; border-radius: 8px; font-size: 13px; }
</style>