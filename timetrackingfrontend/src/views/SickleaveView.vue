<script setup>
import { ref, computed, onMounted } from 'vue'
import StatusBadge from '../components/ui/StatusBadge.vue'
import { sickLeaveService } from "../services/api.js";
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
  employeeId: '',
  startDate: '',
  endDate: '',
  excused: 'EXCUSED',
  fileName: ''
})

// Such- & Zeitraum-State für die Abfrage unten
const searchEmployeeId = ref('')
const searchedEmployee = ref('')
const searchStartDate = ref('')
const searchEndDate = ref('')

// Speichert die geladenen Krankmeldungen des Mitarbeiters vom Backend
const employeeSickLeaves = ref([])

// Helper to parse YYYY-MM-DD in local time
const parseLocalDate = (dateStr) => {
  if (!dateStr) return null
  const [year, month, day] = dateStr.split('-').map(Number)
  return new Date(year, month - 1, day)
}

const previewDays = computed(() => {
  const start = parseLocalDate(form.value.startDate)
  const end = parseLocalDate(form.value.endDate)
  if (!start || !end || isNaN(start) || isNaN(end) || end < start) return []

  const days = []
  let curr = new Date(start.getFullYear(), start.getMonth(), start.getDate())
  const lastDay = new Date(end.getFullYear(), end.getMonth(), end.getDate())

  while (curr <= lastDay) {
    const dayOfWeek = curr.getDay()
    // Nur Wochentage (Mo-Fr) einbeziehen (0 = Sonntag, 6 = Samstag)
    if (dayOfWeek !== 0 && dayOfWeek !== 6) {
      days.push(new Date(curr))
    }
    curr.setDate(curr.getDate() + 1)
  }
  return days
})

const previewRangeLabel = computed(() => {
  const start = parseLocalDate(form.value.startDate)
  const end = parseLocalDate(form.value.endDate)
  if (!start || !end || isNaN(start) || isNaN(end) || end < start) {
    return 'Ungültiges Datum'
  }
  const fmt = (d) =>
    d.toLocaleDateString('de-DE', { day: '2-digit', month: 'long', year: 'numeric' })
  return form.value.startDate === form.value.endDate
    ? fmt(start)
    : `${fmt(start)} – ${fmt(end)}`
})

// Dynamische Berechnung der Krankheitstage basierend auf den einzelnen `day`-Eintragen und dem Filter
const searchedSickDaysCount = computed(() => {
  if (!employeeSickLeaves.value.length) return 0

  const filterStart = parseLocalDate(searchStartDate.value)
  const filterEnd = parseLocalDate(searchEndDate.value)

  return employeeSickLeaves.value.filter(leave => {
    const sickDay = parseLocalDate(leave.day)
    if (!sickDay) return false

    const afterStart = !filterStart || sickDay >= filterStart
    const beforeEnd = !filterEnd || sickDay <= filterEnd
    return afterStart && beforeEnd
  }).length
})

const handleCancel = () => {
  form.value = { employeeId: '', startDate: '', endDate: '', excused: 'EXCUSED', fileName: '' }
}

// Krankmeldung absenden: Da das Backend einzelne Tage erwartet, senden wir für jeden Werktag einen Request
const handleSubmit = async () => {
  try {
    let hrId
    try {
      hrId = await resolveEmployeeId()
    } catch (error) {
    console.error(error)
    alert("Fehler: Konnte keine HR-ID des angemeldeten Benutzers ermitteln.")
  return
  }

    if (!form.value.employeeId || previewDays.value.length === 0) {
      alert("Bitte Employee ID und einen gültigen Zeitraum angeben.")
      return
    }

    // Für jeden Werktag in der Vorschau einen API-Call ausführen
    for (const day of previewDays.value) {
      // Datum im Format YYYY-MM-DD formatieren
      const year = day.getFullYear()
      const month = String(day.getMonth() + 1).padStart(2, '0')
      const d = String(day.getDate()).padStart(2, '0')
      const formattedDate = `${year}-${month}-${d}`

      const payload = {
        employeeId: Number(form.value.employeeId),
        day: formattedDate,
        excused: form.value.excused, // z.B. EXCUSED oder DOCTORS_NOTE_MISSING
        doctorsNotePath: form.value.fileName || null
      }

      await sickLeaveService.recordSickLeave(hrId, payload)
    }

    handleCancel()
  } catch (error) {
    console.error("Fehler beim Speichern der Krankmeldung:", error)
    alert("Fehler beim Speichern der Krankmeldung.")
  }
}

// Ruft alle Krankmeldungen des Mitarbeiters ab
const handleSearch = async () => {
  const id = searchEmployeeId.value.trim()
  if (!id) return

  searchedEmployee.value = id
  try {
    const response = await sickLeaveService.getByEmployee(id)
    employeeSickLeaves.value = Array.isArray(response.data) ? response.data : []
  } catch (error) {
    console.error("Fehler beim Abrufen der Krankmeldungen:", error)
    employeeSickLeaves.value = []
  }
}
</script>

<template>
  <div class="sl-container">
    <div class="sl-header">
      <span class="tagline">HUMAN RESOURCES</span>
      <h1 class="sl-title">Sickleave</h1>
      <p class="sl-desc">Krankmeldung eines Mitarbeiters erstellen</p>
    </div>

    <div class="sl-split">
      <!-- Left Card: Form -->
      <form class="record-form-card" @submit.prevent="handleSubmit">
        <div class="form-card-header">
          <h2 class="box-title">Sickleave erstellen</h2>
          <StatusBadge variant="neutral" label="Draft" />
        </div>

        <!-- Employee ID -->
        <label class="input-label">
          Employee ID
          <input
            v-model="form.employeeId"
            type="text"
            class="text-input"
            placeholder="Employee ID eingeben..."
            required
          />
        </label>

        <!-- Date inputs -->
        <div class="time-inputs-row">
          <label class="input-label flex-1">
            Startdatum
            <input
              v-model="form.startDate"
              type="date"
              class="text-input"
              required
            />
          </label>

          <label class="input-label flex-1">
            Enddatum
            <input
              v-model="form.endDate"
              type="date"
              class="text-input"
              required
            />
          </label>
        </div>

        <p class="note-txt">
          Start und Enddatum sind inkludiert (Wochenenden werden automatisch übersprungen)
        </p>

        <div class="divider-line"></div>

        <!-- Buttons -->
        <div class="form-actions">
          <button type="button" class="cancel-btn" @click="handleCancel">
            Cancel
          </button>

          <button type="submit" class="submit-btn">
            <span>✓</span>
            <span>Submit</span>
          </button>
        </div>
      </form>

      <!-- Right Card: Preview -->
      <div class="preview-card">
        <div class="preview-top-content">
          <div class="preview-header-texts">
            <h2 class="box-title">Sickleave Vorschau</h2>
            <p class="box-subtitle">{{ previewRangeLabel }}</p>
          </div>

          <div class="preview-kpi-row">
            <div class="preview-kpi-box light">
              <span class="pkpi-lbl">Abwesend</span>
              <span class="pkpi-val">{{ previewDays.length }} Tage</span>
            </div>
          </div>
        </div>
        <div>
          <div class="preview-table-header">
            <span class="pth-date">Datum</span>
          </div>
          <div v-for="day in previewDays" :key="day.getTime()" class="preview-table-row">
            <span class="ptr-date">
              {{ day.toLocaleDateString('de-DE', { weekday: 'short', day: '2-digit', month: '2-digit', year: 'numeric' }) }}
            </span>
          </div>
        </div>
      </div>
    </div> <!-- /.sl-split -->

    <!-- Bottom Card: Simplified Search & Count mit Zeitraum-Auswahl -->
    <div class="history-table-card">
      <div class="hist-table-header">
        <div>
          <h3 class="box-title">Krankheitstage abfragen</h3>
          <p class="box-subtitle">Employee ID und optionalen Zeitraum eingeben</p>
        </div>
      </div>

      <div class="search-section">
        <form class="search-bar-row" @submit.prevent="handleSearch" style="max-width: none; display: flex; gap: 12px; align-items: flex-end; flex-wrap: wrap;">
          <label class="input-label" style="flex: 1; min-width: 150px;">
            Employee ID
            <input
              v-model="searchEmployeeId"
              type="text"
              class="text-input"
              placeholder="Employee ID eingeben..."
              required
            />
          </label>

          <!-- Zeitraum Von -->
          <label class="input-label" style="flex: 1; min-width: 150px;">
            Von (optional)
            <input
              v-model="searchStartDate"
              type="date"
              class="text-input"
            />
          </label>

          <!-- Zeitraum Bis -->
          <label class="input-label" style="flex: 1; min-width: 150px;">
            Bis (optional)
            <input
              v-model="searchEndDate"
              type="date"
              class="text-input"
            />
          </label>

          <button type="submit" class="submit-btn search-btn">
            Anzeigen
          </button>
        </form>

        <!-- Zähler-Ergebnis -->
        <div v-if="searchedEmployee" class="result-display-box">
          <div class="preview-kpi-box light" style="max-width: 320px; margin-top: 20px;">
            <span class="pkpi-lbl">Gesamte Krankheitstage (ID: {{ searchedEmployee }})</span>
            <span class="pkpi-val">{{ searchedSickDaysCount }} Tage</span>
          </div>
        </div>
      </div>
    </div> <!-- /.history-table-card -->

  </div> <!-- /.sl-container -->
</template>

<style scoped>
.sl-container { padding: 32px; display: flex; flex-direction: column; gap: 24px; }
.sl-header { display: flex; flex-direction: column; gap: 7px; }
.tagline { color: #697B80; font-size: 11px; font-weight: 600; letter-spacing: 1.20px; }
.sl-title { color: #172D32; font-size: 30px; font-weight: 600; line-height: 36px; }
.sl-desc { color: #697B80; font-size: 13px; line-height: 18.85px; }
.sl-split { display: flex; gap: 24px; align-items: stretch; }
.record-form-card { width: 432px; flex-shrink: 0; padding: 24px; background: white; border-radius: 12px; outline: 1px #DFE7E8 solid; display: flex; flex-direction: column; gap: 18px; }
.form-card-header { display: flex; justify-content: space-between; align-items: center; }
.box-title { color: #172D32; font-size: 16px; font-weight: 600; margin: 0; }
.box-subtitle { color: #697B80; font-size: 12px; margin: 0; }
.input-label { display: flex; flex-direction: column; gap: 7px; color: #172D32; font-size: 12px; }
.flex-1 { flex: 1; min-width: 0; }
.time-inputs-row { display: flex; gap: 12px; }

.text-input { 
  width: 100%; 
  box-sizing: border-box; 
  height: 43px; 
  padding: 0 12px; 
  border-radius: 8px; 
  outline: 1px #DFE7E8 solid; 
  font-size: 13px; 
  border: none; 
  background-color: #FFFFFF; 
  color: #000000; 
  color-scheme: light; 
}
.text-input::placeholder { color: #697B80; }
.text-input:focus { outline: 2px #176A70 solid; }

.note-txt { color: #697B80; font-size: 11px; margin: 0; }
.divider-line { height: 1px; background: #DFE7E8; width: 100%; }
.form-actions { display: flex; justify-content: center; align-items: center; gap: 12px; margin-top: auto; }
.cancel-btn { height: 40px; padding: 0 18px; background: white; border-radius: 8px; color: #172D32; font-size: 13px; font-weight: 600; outline: 1px #DFE7E8 solid; border: none; cursor: pointer; }
.cancel-btn:hover { background: #F8FAFA; }
.submit-btn { height: 40px; padding: 0 18px; background: #176A70; border-radius: 8px; color: white; font-size: 13px; font-weight: 600; display: flex; justify-content: center; align-items: center; gap: 8px; border: none; cursor: pointer; }
.submit-btn:hover { background: #11545A; }
.preview-card { flex: 1; background: white; border-radius: 12px; outline: 1px #DFE7E8 solid; display: flex; flex-direction: column; justify-content: space-between; overflow: hidden; }
.preview-top-content { padding: 24px; display: flex; flex-direction: column; gap: 18px; }
.preview-header-texts { display: flex; flex-direction: column; gap: 4px; }
.preview-kpi-row { display: flex; gap: 16px; }
.preview-kpi-box { flex: 1; padding: 16px; border-radius: 8px; display: flex; flex-direction: column; gap: 8px; }
.preview-kpi-box.light { background: #F8FAFA; }
.preview-kpi-box.dark { background: #11545A; color: white; }
.pkpi-lbl { color: #697B80; font-size: 12px; }
.pkpi-val { color: #172D32; font-size: 28px; font-weight: 600; }
.preview-table-header { height: 36px; padding: 0 24px; background: #F8FAFA; border-top: 1px #DFE7E8 solid; border-bottom: 1px #DFE7E8 solid; display: flex; align-items: center; color: #697B80; font-size: 10px; font-weight: 600; }
.pth-date { flex: 1; }
.preview-table-row { height: 40px; padding: 0 24px; background: #F9F7FC; border-bottom: 1px #DFE7E8 solid; display: flex; align-items: center; font-size: 12px; }
.ptr-date { flex: 1; color: #172D32; }

.history-table-card { background: white; border-radius: 12px; outline: 1px #DFE7E8 solid; overflow: hidden; }
.hist-table-header { padding: 20px 24px; display: flex; justify-content: space-between; align-items: center; }
.search-section { padding: 0 24px 24px 24px; }
.search-btn { height: 43px; }
</style>