<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import StatusBadge from '../components/ui/StatusBadge.vue'
import EmployeeService from '../services/api.js'
import { vacationRequestService } from "../services/api";

const props = defineProps({
  activeRoute: { type: String, required: true },
  role: { type: String, default: 'employee' },
  user: {
    type: Object,
    default: () => ({ initials: '', name: '', title: '', email: '' }),
  },
  pendingReviewsCount: { type: Number},
  employeeId: { type: Number}
})

const takenVacationDays = ref("Laden...")
const availableVacationDays = ref("Laden...")
const plannedVacationDays = ref("Laden...")
const remainingVacationDays = ref(0)
const vacationEntries = ref([])

// Hilfsfunktion zur Berechnung von Arbeitstagen (Mo-Fr) zwischen zwei Datumsstrings
const calculateWorkDays = (startStr, endStr) => {
  if (!startStr || !endStr) return 0
  const start = new Date(startStr)
  const end = new Date(endStr)
  if (end < start) return 0

  let workDays = 0
  const current = new Date(start)
  while (current <= end) {
    const dayOfWeek = current.getDay()
    if (dayOfWeek !== 0 && dayOfWeek !== 6) {
      workDays++
    }
    current.setDate(current.getDate() + 1)
  }
  return workDays
}

// 1. Urlaubsbilanz vom Backend abrufen
const fetchVacationData = async () => {
    try {
        let currentId = props.employeeId;

        if ((!currentId || currentId === 1) && props.user && props.user.email) {
            const employeesResponse = await EmployeeService.getAll(props.user.email);
            if (employeesResponse.data && employeesResponse.data.length > 0) {
                currentId = employeesResponse.data[0].id;
            }
        }

        if (!currentId || currentId === 'undefined') {
            console.log("Warte auf gültige Mitarbeiter-ID / User-Daten...");
            return;
        }

        const vacationResponse = await EmployeeService.getVacationBalance(currentId)
        const vacData = vacationResponse.data || {}
        
        const annual = vacData.annualDays ?? 30;
        const taken = vacData.takenDays ?? 0;
        const planned = vacData.plannedDays ?? 0;
        const remainingLastYear = vacData.remainingDays_last_year ?? 0;

        takenVacationDays.value = taken;
        plannedVacationDays.value = planned;

        const calculatedRemaining = vacData.remainingDays !== undefined && vacData.remainingDays !== null
            ? vacData.remainingDays 
            : ((annual + remainingLastYear) - (taken + planned));

        availableVacationDays.value = calculatedRemaining;
        remainingVacationDays.value = calculatedRemaining;

    } catch (error) {
        console.error("Fehler beim Laden der Urlaubsdaten:", error)
        takenVacationDays.value = "–"
        plannedVacationDays.value = "–"
        availableVacationDays.value = "–"
        remainingVacationDays.value = "–"
    }
}

// 2. Reale Urlaubsanträge des Mitarbeiters vom Backend abrufen
const fetchVacationRequests = async () => {
    try {
        let currentId = props.employeeId;

        if ((!currentId || currentId === 1) && props.user && props.user.email) {
            const employeesResponse = await EmployeeService.getAll(props.user.email);
            if (employeesResponse.data && employeesResponse.data.length > 0) {
                currentId = employeesResponse.data[0].id;
            }
        }

        if (!currentId || currentId === 'undefined') {
            return;
        }

        const response = await vacationRequestService.getRequestsOfEmployee(currentId);
        
        vacationEntries.value = response.data.map(req => {
            const formatDate = (dateStr) => {
                if (!dateStr) return '';
                const [y, m, d] = dateStr.split('-');
                return `${d}.${m}.${y}`;
            };

            const periodStr = req.startDate === req.endDate
                ? formatDate(req.startDate)
                : `${formatDate(req.startDate)} – ${formatDate(req.endDate)}`;

            let variant = 'warning';
            let displayStatus = 'Ausstehend';
            
            const rawStatus = req.status ? req.status.toUpperCase() : 'PENDING';
            if (rawStatus === 'APPROVED' || rawStatus === 'GENEHMIGT') {
                variant = 'success';
                displayStatus = 'Genehmigt';
            } else if (rawStatus === 'REJECTED' || rawStatus === 'ABGELEHNT') {
                variant = 'danger';
                displayStatus = 'Abgelehnt';
            } else if (rawStatus === 'CANCELLED' || rawStatus === 'STORNIERT') {
                variant = 'neutral';
                displayStatus = 'Storniert';
            }

            const daysCount = req.daysCount || calculateWorkDays(req.startDate, req.endDate);

            return {
                id: req.id,
                period: periodStr,
                days: daysCount,
                status: displayStatus,
                variant: variant,
            };
        });

    } catch (error) {
        console.error("Fehler beim Laden der Urlaubsanträge:", error);
    }
};

// Reagiert auf Routenwechsel oder Daten-Updates und lädt alle Daten neu
watch(
  () => [props.activeRoute, props.employeeId, props.user?.email],
  ([route]) => {
    if (route) {
        fetchVacationData()
        fetchVacationRequests()
    }
  },
  { immediate: true }
)

// Formular-Zustand
const form = ref({
  startDate: '',
  endDate: '',
})

// Berechnet die Arbeitstage (Mo–Fr) im Formular
const vacationStats = computed(() => {
  if (!form.value.startDate || !form.value.endDate) return { days: 0, hours: '0:00 h' }
  
  const start = new Date(form.value.startDate)
  const end = new Date(form.value.endDate)
  
  if (end < start) return { days: 0, hours: '0:00 h' }

  const workDays = calculateWorkDays(form.value.startDate, form.value.endDate)

  return {
    days: workDays,
    hours: `${workDays * 8}:00 h`,
  }
})

// Urlaub beantragen und ans Backend senden
const handleAddVacation = async () => {
  if (vacationStats.value.days <= 0) {
    alert('Bitte ein gültiges Start- und Enddatum (mindestens 1 Arbeitstag) wählen.')
    return
  }

  let currentId = props.employeeId;

  if ((!currentId || currentId === 1) && props.user && props.user.email) {
    const employeesResponse = await EmployeeService.getAll(props.user.email);
    if (employeesResponse.data && employeesResponse.data.length > 0) {
      currentId = employeesResponse.data[0].id;
    }
  }

  if (!currentId || currentId === 'undefined') {
    console.log("Warte auf gültige Mitarbeiter-ID / User-Daten...");
    return;
  }

  const requestPayload = {
    startDate: form.value.startDate,
    endDate: form.value.endDate
  };

  try {
    await vacationRequestService.create(currentId, requestPayload);

    // Formular zurücksetzen
    form.value.startDate = ''
    form.value.endDate = ''
    
    // Daten und Antragsliste direkt neu vom Backend laden
    await fetchVacationData();
    await fetchVacationRequests();

  } catch (error) {
    console.error("Fehler beim Erstellen des Urlaubsantrags:", error);
    alert('Der Urlaubsantrag konnte nicht gespeichert werden.');
  }
}
</script>

<template>
  <div class="tt-container">
    <div class="tt-header-row">
      <h1 class="tt-title">Urlaubsverwaltung</h1>
      <div class="tt-actions">
        <div class="quota-pill">
          <span>Resturlaub 2026:</span>
          <strong>{{ remainingVacationDays }} Tage</strong>
        </div>
      </div>
    </div>

    <div class="tt-subbar">
      <div class="submitted-count">
        <span>Genommen: <strong>{{ takenVacationDays }} Tage</strong></span>
        <span>•</span>
        <span>Geplant: <strong>{{ plannedVacationDays }} Tage</strong></span>
      </div>
    </div>

    <div class="content-split">
      <div class="table-card">
        <div class="table-card-header">
          <div>
            <h2 class="box-title">Meine Urlaubsanträge</h2>
          </div>
        </div>

        <table class="data-table">
          <thead>
            <tr class="th-row">
              <th class="th-date">Zeitraum</th>
              <th class="th-days">Tage</th>
              <th class="th-status">Status</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="vacationEntries.length === 0">
              <td colspan="3" class="empty-state">
                Noch keine Urlaubsanträge vorhanden.
              </td>
            </tr>
            <tr v-else v-for="entry in vacationEntries" :key="entry.id" class="data-tr">
              <td class="td-date">{{ entry.period }}</td>
              <td class="td-net-val">{{ entry.days }} {{ entry.days === 1 ? 'Tag' : 'Tage' }}</td>
              <td>
                <StatusBadge :variant="entry.variant" :label="entry.status" />
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="right-sidebar">
        <form @submit.prevent="handleAddVacation" class="form-card">
          <div class="form-card-header">
            <h2 class="box-title">Urlaub beantragen</h2>
            <StatusBadge variant="neutral" label="Neu" />
          </div>

          <div class="time-inputs-row">
            <label class="input-label flex-1">
              Von
              <input v-model="form.startDate" type="date" class="text-input" required />
            </label>
            <label class="input-label flex-1">
              Bis
              <input v-model="form.endDate" type="date" class="text-input" required />
            </label>
          </div>

          <div class="calc-box">
            <div class="calc-header">
              <span class="calc-val">Urlaubstage = {{ vacationStats.days }} {{ vacationStats.days === 1 ? 'Tag' : 'Tage' }}</span>
            </div>
            <div class="calc-sub">Wochenenden (Sa/So) werden automatisch abgezogen</div>
          </div>

          <button type="submit" class="submit-btn">
            <span>✓</span><span>Antrag einreichen</span>
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.text-input, .select-input, input[type="date"] {
  background-color: #FFFFFF !important;
  color: #000000 !important;
  color-scheme: light;
}

.text-input::placeholder {
  color: #697B80;
}

.tt-container { padding: 32px; display: flex; flex-direction: column; gap: 24px; }
.tt-header-row { height: 143px; display: flex; align-items: center; gap: 24px; }
.tt-title { flex: 1; color: #172D32; font-size: 30px; font-weight: 600; line-height: 36px; }
.tt-actions { display: flex; align-items: center; gap: 12px; }

.quota-pill {
  height: 40px;
  padding: 0 16px;
  background: #E8F3F2;
  color: #176A70;
  border-radius: 8px;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.tt-subbar { display: flex; justify-content: space-between; align-items: center; }
.submitted-count { display: flex; align-items: center; gap: 8px; color: #697B80; font-size: 12px; }

.content-split { display: flex; gap: 24px; align-items: flex-start; }
.table-card { flex: 1; background: white; border-radius: 12px; outline: 1px #DFE7E8 solid; display: flex; flex-direction: column; overflow: hidden; }
.table-card-header { padding: 20px; display: flex; flex-direction: column; gap: 16px; }
.box-title { color: #172D32; font-size: 16px; font-weight: 600; }

.data-table { width: 100%; border-collapse: collapse; text-align: left; }
.th-row { height: 36px; background: #F8FAFA; border-top: 1px #DFE7E8 solid; border-bottom: 1px #DFE7E8 solid; color: #697B80; font-size: 10px; font-weight: 600; }
.th-date { padding-left: 20px; width: 200px; }
.th-days { width: 100px; }
.th-status { width: 120px; }

.empty-state {
  padding: 32px 20px;
  text-align: center;
  color: #697B80;
  font-size: 13px;
}

.data-tr { height: 44px; border-bottom: 1px #DFE7E8 solid; font-size: 12px; color: #172D32; }
.data-tr:last-child { border-bottom: none; }
.td-date { padding-left: 20px; }
.td-net-val { font-weight: 600; }

.right-sidebar { width: 432px; flex-shrink: 0; display: flex; flex-direction: column; gap: 20px; }
.form-card {
  width: 100%;
  padding: 24px;
  background: white;
  border-radius: 12px;
  outline: 1px #DFE7E8 solid;
  display: flex;
  flex-direction: column;
  gap: 18px;
  color: #172D32;
}
.form-card-header { display: flex; justify-content: space-between; align-items: center; }

.input-label { 
  display: flex; 
  flex-direction: column; 
  gap: 7px; 
  color: #172D32; 
  font-size: 12px; 
  font-weight: 500; 
}
.flex-1 { flex: 1; }
.text-input, .select-input { height: 42px; padding: 0 12px; background: white; border-radius: 8px; outline: 1px #DFE7E8 solid; font-size: 13px; border: none; }
.time-inputs-row { display: flex; gap: 12px; }

.calc-box { padding: 16px; background: #E8F3F2; border-radius: 8px; display: flex; flex-direction: column; gap: 6px; }
.calc-header { display: flex; justify-content: space-between; color: #176A70; font-size: 12px; font-weight: 500; }
.calc-val { font-size: 18px; font-weight: 600; }
.calc-sub { color: #176A70; font-size: 11px; }

.submit-btn { height: 40px; padding: 0 14px; background: #176A70; border-radius: 8px; color: white; font-size: 13px; font-weight: 600; display: flex; justify-content: center; align-items: center; gap: 8px; border: none; cursor: pointer; }
.submit-btn:hover { background: #12565B; }
</style>