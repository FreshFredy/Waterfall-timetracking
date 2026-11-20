<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import StatusBadge from '../components/ui/StatusBadge.vue'
import { timesheetService, projectService } from '../services/api.js'
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

  throw new Error("Could not determine an employee ID: Neither `props.employeeId` nor a valid email address / employee data is available..")
}

const form = ref({
  date: new Date().toISOString().substr(0, 10),
  start: '08:30',
  end: '17:00',
  breakMinutes: 30,
  entryType: 'WORK',
  projectId: null
})

// Editier-State
const isEditing = ref(false)
const editingEntryId = ref(null)

// Navigation State für Monat und Jahr
const selectedDate = ref(new Date())

// Daten vom Backend
const currentTimesheet = ref(null)
const dailyEntries = ref([])
const projects = ref([])
const isLoading = ref(false)
const isSubmitting = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

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

// Prüfen, ob der aktuelle Timesheet eingereicht werden kann
const canSubmitTimesheet = computed(() => {
  if (!currentTimesheet.value || !currentTimesheet.value.id) return false
  const status = currentTimesheet.value.status ? currentTimesheet.value.status.toUpperCase() : 'DRAFT'
  return status === 'DRAFT' || status === 'OPEN' || status === 'APPROACHED' || status === 'REJECTED'
})

// Projekte für das Dropdown laden (GET /projects)
const loadProjects = async () => {
  try {
    const response = await projectService.getAll()
    projects.value = response.data
  } catch (err) {
    console.error('Projekte konnten nicht geladen werden:', err)
  }
}

// 1. Timesheet und Einträge vom Backend laden (GET)
const fetchTimesheetData = async () => {
  isLoading.value = true
  errorMessage.value = ''
  successMessage.value = ''
  
  try {
    const resolvedId = await resolveEmployeeId()
    const year = selectedDate.value.getFullYear()
    const month = selectedDate.value.getMonth() + 1
    
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
    errorMessage.value = 'Data could not be loaded from the server.'
  } finally {
    isLoading.value = false
  }
}

// Hilfsfunktion: Backend-DTO-Felder auf Tabellenzeile mappen
const mapBackendEntryToRow = (entry) => {
  let formattedDate = entry.entryDate || '–'
  let rawDateValue = entry.entryDate || new Date().toISOString().substr(0, 10)
  
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

  const rawType = entry.entryType ?? entry.entry_type ?? entry.type ?? 'WORK'

  const typeMapping = {
    'WORK': 'Work',
    'SICK': 'Sick',
    'krank': 'Sick',
    'VACATION': 'Vacation',
    'URLAUB': 'Vacation'
  }

  const displayType = typeMapping[rawType] ?? rawType

  return {
    id: entry.id,
    rawDate: rawDateValue,
    date: formattedDate,
    start: startStr,
    end: endStr,
    breakMinutes: breakMins,
    break: breakMins > 0 ? formatMinutes(breakMins) : '0:00',
    net: `${netMinsFormatted}`,
    project: entry.projectName ?? '–',      // Anzeige in der Tabelle
    rawProjectId: entry.projectId ?? null,  // fürs Bearbeiten / Dropdown
    type: displayType,
    rawType: rawType
  }
}

// In den Bearbeitungsmodus wechseln
const handleEditClick = (entry) => {
  isEditing.value = true
  editingEntryId.value = entry.id
  errorMessage.value = ''
  
  form.value = {
    date: entry.rawDate,
    start: entry.start !== '–' ? entry.start : '08:30',
    end: entry.end !== '–' ? entry.end : '17:00',
    breakMinutes: entry.breakMinutes,
    entryType: entry.rawType || 'WORK',
    projectId: entry.rawProjectId
  }
}

// Bearbeitung abbrechen
const cancelEdit = () => {
  isEditing.value = false
  editingEntryId.value = null
  errorMessage.value = ''
  form.value = {
    date: new Date().toISOString().substr(0, 10),
    start: '08:30',
    end: '17:00',
    breakMinutes: 30,
    entryType: 'WORK',
    projectId: null
  }
}

// 2. Neuen Zeiteintrag erstellen (POST) oder bestehenden aktualisieren (PUT)
const handleFormSubmit = async () => {
  errorMessage.value = ''
  successMessage.value = ''

  // HIER DIE PRÜFUNG: Wenn wir NICHT im Editier-Modus sind, prüfen ob für das Datum schon ein Eintrag existiert
  if (!isEditing.value) {
    const existingEntryForDay = dailyEntries.value.find(entry => entry.rawDate === form.value.date)
    if (existingEntryForDay) {
      errorMessage.value = `Für den ${form.value.date} existiert bereits ein Zeiteintrag. Pro Tag ist nur ein Eintrag erlaubt. Du kannst den bestehenden Eintrag stattdessen bearbeiten.`
      return
    }
  }
  
  const payload = {
    entryDate: form.value.date,
    startTime: form.value.start.length === 5 ? `${form.value.start}:00` : form.value.start,    
    endTime: form.value.end.length === 5 ? `${form.value.end}:00` : form.value.end,        
    breakMinutes: form.value.breakMinutes,  
    entryType: form.value.entryType,
    projectID: form.value.projectId ?? null
  }

  try {
    const resolvedId = await resolveEmployeeId()

    if (isEditing.value && editingEntryId.value) {
      if (!currentTimesheet.value || !currentTimesheet.value.id) {
        throw new Error("Keine Timesheet-ID vorhanden für das Update.")
      }
      await timesheetService.changeWorkTime(currentTimesheet.value.id, editingEntryId.value, payload)
    } else {
      await timesheetService.recordWorkTime(resolvedId, payload)
    }
    
    cancelEdit()
    fetchTimesheetData()
  } catch (err) {
    console.error('Speicherfehler:', err)
    errorMessage.value = isEditing.value ? 'Eintrag konnte nicht aktualisiert werden.' : 'Eintrag konnte nicht gespeichert werden.'
  }
}

// 3. Timesheet einreichen (Submit)
const handleTimesheetSubmit = async () => {
  if (!currentTimesheet.value || !currentTimesheet.value.id) return

  isSubmitting.value = true
  errorMessage.value = ''
  successMessage.value = ''

  try {
    const response = await timesheetService.submit(currentTimesheet.value.id)
    currentTimesheet.value = response.data
    successMessage.value = 'Timesheet wurde erfolgreich eingereicht!'
  } catch (err) {
    console.error('Submit-Fehler:', err)
    errorMessage.value = 'Timesheet konnte nicht eingereicht werden.'
  } finally {
    isSubmitting.value = false
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

watch(selectedDate, () => {
  fetchTimesheetData()
})

onMounted(() => {
  loadProjects()
  fetchTimesheetData()
})
</script>

<template>
  <div class="tt-container">
    <div class="tt-header-row">
      <div class="tt-heading">
        <p class="tt-subtitle-top">YOUR WORKDAY LOG</p>
      <h1 class="tt-title">Time tracking</h1>
      <p class="tt-subtitle">Log your hours effortlessly. Stay on top of your day.</p>
      </div>
      <div class="tt-actions">
        <!-- Aktivierter Submit-Button -->
        <button 
          v-if="canSubmitTimesheet" 
          @click="handleTimesheetSubmit" 
          class="submit-timesheet-btn" 
          :disabled="isSubmitting"
        >
          <span>{{ isSubmitting ? 'Being submitted...' : 'Submit Timesheet' }}</span>
        </button>
        <button v-else class="disabled-submit-btn" disabled>
          <span>Submit Timesheet</span>
        </button>
      </div>
    </div>

    <div v-if="errorMessage" class="error-banner">
      {{ errorMessage }}
    </div>

    <div v-if="successMessage" class="success-banner">
      {{ successMessage }}
    </div>

    <div class="content-split">
      <div class="table-card">
        <div class="table-card-header">
          <div>
            <h2 class="box-title">Timesheet</h2>
          </div>

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

            <button @click="resetToToday" class="today-btn">Today</button>
          </div>
        </div>

        <table class="data-table">
          <thead>
            <tr class="th-row">
              <th class="th-date">Date</th>
              <th class="th-start">Start</th>
              <th class="th-end">End</th>
              <th class="th-break">Pause</th>
              <th class="th-type">Type</th>
              <th class="th-net">Working hours</th>
              <th class="th-project">Project</th>
              <th class="th-action">Action</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="isLoading">
              <td colspan="8" class="empty-state">Load time entries...</td>
            </tr>
            <tr v-else-if="dailyEntries.length === 0">
              <td colspan="8" class="empty-state">No entries available for this month.</td>
            </tr>
            <tr v-else v-for="(entry, index) in dailyEntries" :key="entry.id || index" class="data-tr" :class="{ 'editing-row': editingEntryId === entry.id }">
              <td class="td-date"><span class="project-text">{{ entry.date }}</span></td>
              <td>{{ entry.start }}</td>
              <td>{{ entry.end }}</td>
              <td>{{ entry.break }}</td>
              <td>{{ entry.type }}</td>
              <td class="td-net-val">{{ entry.net }}</td>
              <td class="td-project-val"><span class="project-text">{{ entry.project }}</span></td>
              <td class="td-action">
                <button @click="handleEditClick(entry)" class="edit-btn" title="Eintrag bearbeiten">
                  Edit
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="right-sidebar">
        <form @submit.prevent="handleFormSubmit" class="form-card" :class="{ 'edit-mode-border': isEditing }">
          <div class="form-card-header">
            <h2 class="box-title">{{ isEditing ? 'Edit entrie' : 'Time tracking' }}</h2>
            <StatusBadge variant="neutral" :label="currentTimesheet?.status || 'Draft'" />
          </div>

          <div v-if="isEditing" class="edit-banner">
            <span>Edit Entry</span>
            <button type="button" @click="cancelEdit" class="cancel-edit-text">Cancel</button>
          </div>

          <label class="input-label">
            Date
            <input v-model="form.date" type="date" class="text-input" required />
          </label>

          <div class="time-inputs-row">
            <label class="input-label flex-1">
              Start
              <input v-model="form.start" type="time" class="text-input" required />
            </label>
            <label class="input-label flex-1">
              End
              <input v-model="form.end" type="time" class="text-input" required />
            </label>
          </div>

          <label class="input-label">
            Break duration in minutes
            <input v-model.number="form.breakMinutes" type="number" min="0" step="5" class="text-input" />
          </label>

          <div class="calc-box">
            <div class="calc-header">
              <span class="calc-val">Working hours : {{ timeStats.net }}</span>
            </div>
            <div class="calc-sub">Total time - break = Working hours</div>
          </div>

          <label class="input-label">
            Project
            <select v-model="form.projectId" class="select-input">
              <option :value="null">– No project –</option>
              <option v-for="p in projects" :key="p.id" :value="p.id">{{ p.name }}</option>
            </select>
          </label>

          <div class="form-actions-row">
            <button v-if="isEditing" type="button" @click="cancelEdit" class="cancel-btn">
              Cancel
            </button>
            <button type="submit" class="submit-btn" :class="{ 'update-btn': isEditing }">
              <span>✓</span><span>{{ isEditing ? 'Save changes' : 'Submit time' }}</span>
            </button>
          </div>
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
  transition: all 0.3s ease;
}
.edit-mode-border {
  outline: 2px #176A70 solid;
  background-color: #F8FAFA;
}
.edit-banner {
  background: #E8F3F2;
  padding: 8px 12px;
  border-radius: 6px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #176A70;
  font-weight: 600;
}
.cancel-edit-text {
  background: none;
  border: none;
  color: #176A70;
  text-decoration: underline;
  cursor: pointer;
  font-size: 11px;
}
.tt-container { padding: 32px; display: flex; flex-direction: column; gap: 24px; }
.tt-header-row { height: 143px; display: flex; align-items: center; gap: 24px; }

.tt-actions { display: flex; align-items: center; gap: 12px; }
.disabled-submit-btn { height: 40px; padding: 0 14px; background: #B3B3B3; border-radius: 8px; color: white; font-size: 13px; font-weight: 600; display: flex; align-items: center; border: none; cursor: not-allowed; }
.submit-timesheet-btn { height: 40px; padding: 0 16px; background: #176A70; border-radius: 8px; color: white; font-size: 13px; font-weight: 600; display: flex; align-items: center; border: none; cursor: pointer; transition: background 0.2s; }
.submit-timesheet-btn:hover { background: #0F4C50; }
.submit-timesheet-btn:disabled { background: #B3B3B3; cursor: not-allowed; }

.content-split { display: flex; gap: 24px;  }
.table-card { flex: 1; 
  background: white; 
  border-radius: 12px; 
  outline: 1px #DFE7E8 solid; 
  display: flex; 
  flex-direction: column;
    align-items: center;
  overflow: hidden; 
  text-align: center;
  
}

.table-card-header { 
  padding: 20px; 
  display: flex; 
  justify-content: space-between; 
  flex-direction: column;
  align-items: center; 
  flex-wrap: wrap;
  text-align: left;
  gap: 12px;
}

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


.th-date { padding-left: 20px; width: 120px; }
.th-start, .th-end, .th-break, .th-type { width: 75px; }
.th-net { width: 90px; }
.th-action { width: 90px; padding-right: 20px; text-align: right; }

.data-tr { height: 38px; border-bottom: 1px #DFE7E8 solid; font-size: 12px; color: #172D32; }
.data-tr:hover { background-color: #F8FAFA; }
.editing-row { background-color: #E8F3F2 !important; }

.td-date { padding-left: 20px; }
.td-net-val { font-weight: 600; }
.td-action { padding-right: 20px; text-align: right; }

.edit-btn {
  background: transparent;
  border: 1px solid #DFE7E8;
  border-radius: 6px;
  padding: 4px 8px;
  font-size: 11px;
  color: #176A70;
  cursor: pointer;
  transition: all 0.2s;
}
.edit-btn:hover {
  background: #176A70;
  color: white;
  border-color: #176A70;
}

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

.form-actions-row { display: flex; gap: 8px; }
.submit-btn { height: 40px; flex: 1; padding: 0 14px; background: #176A70; border-radius: 8px; color: white; font-size: 13px; font-weight: 600; display: flex; justify-content: center; align-items: center; gap: 8px; border: none; cursor: pointer; }
.update-btn { background: #0F4C50; }
.cancel-btn { height: 40px; padding: 0 12px; background: #F3F4F6; border: 1px solid #DFE7E8; border-radius: 8px; color: #4B5563; font-size: 13px; font-weight: 600; cursor: pointer; }
.cancel-btn:hover { background: #E5E7EB; }

.error-banner { padding: 12px 16px; background: #FEE2E2; color: #991B1B; border-radius: 8px; font-size: 13px; }
.success-banner { padding: 12px 16px; background: #D1FAE5; color: #065F46; border-radius: 8px; font-size: 13px; }

.tt-title { 

  color: 
  #172D32; 
  font-size: 30px; 
  font-weight: 600; 
  line-height: 36px; 
}
.tt-subtitle { 
  color: #697B80;
  font-size: 13px;
  font-family: Inter, sans-serif;
  line-height: 18.85px;
}
.tt-subtitle-top{
    font-weight: 600;
    color: #697B80;
  font-size: 13px;
  font-family: Inter, sans-serif;
  line-height: 18.85px;
}
.tt-heading {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  text-align: left;
}
.td-project-val{
  width: 100px;
  text-align:center;
}
.th-row { 
  height: 36px; 
  background: #F8FAFA; 
  border-top: 1px #DFE7E8 solid; 
  border-bottom: 1px #DFE7E8 solid; 
  color: #697B80; 
  font-size: 10px; 
  font-weight: 600; 
}
.data-table { 
  width: 100%; 
  border-collapse: collapse; 
  text-align: center; 
}
.project-text {
  display: block;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
td{
  text-align: center;
}

</style>