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

// Berechnet das morgige Datum als String im Format YYYY-MM-DD für das min-Attribut im Input
const minDate = computed(() => {
  const tomorrow = new Date();
  tomorrow.setDate(tomorrow.getDate() + 1);
  return tomorrow.toISOString().split('T')[0];
})

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

// JavaScript-gerechte Prüfung, ob ein Startdatum in der Vergangenheit liegt oder heute ist
const isAlreadyStartedOrToday = (startDateStr) => {
  if (!startDateStr) return false;
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const startDate = new Date(startDateStr);
  startDate.setHours(0, 0, 0, 0);
  
  return startDate <= today;
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
            console.log("Waiting for valid Employee-ID / Eployee-Data...");
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
        console.error("Error loading vacation data:", error)
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
            let displayStatus = 'Pending';
            
            const rawStatus = req.status ? req.status.toUpperCase() : 'PENDING';
            if (rawStatus === 'APPROVED') {
                variant = 'success';
                displayStatus = 'Approved';
            } else if (rawStatus === 'REJECTED') {
                variant = 'danger';
                displayStatus = 'Rejected';
            } else if (rawStatus === 'CANCELLED') {
                variant = 'neutral';
                displayStatus = 'Cancled';
            }

            const daysCount = req.daysCount || calculateWorkDays(req.startDate, req.endDate);

            const isApproved = rawStatus === 'APPROVED' || rawStatus === 'GENEHMIGT';
            const hasStarted = isAlreadyStartedOrToday(req.startDate);
            const isCancelledOrRejected = rawStatus === 'CANCELLED' || rawStatus === 'REJECTED';
            const canBeCancelled = !isCancelledOrRejected && !(isApproved && hasStarted);

            return {
                id: req.id,
                period: periodStr,
                days: daysCount,
                status: displayStatus,
                rawStatus: rawStatus,
                variant: variant,
                canBeCancelled: canBeCancelled
            };
        });

    } catch (error) {
        console.error("Error loading vacation Application:", error);
    }
};

// 3. Urlaubsantrag abbrechen
const handleCancelRequest = async (requestId) => {

  let currentId = props.employeeId;

  if ((!currentId || currentId === 1) && props.user && props.user.email) {
    const employeesResponse = await EmployeeService.getAll(props.user.email);
    if (employeesResponse.data && employeesResponse.data.length > 0) {
      currentId = employeesResponse.data[0].id;
    }
  }

  try {
    await vacationRequestService.cancel(requestId, currentId);

    await fetchVacationData();
    await fetchVacationRequests();
  } catch (error) {
    console.error("Error canceling vacation:", error);
    alert('error');
  }
}

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

// Urlaub beantragen und ans Backend senden (Prüfung: weder Vergangenheit noch heute)
const handleAddVacation = async () => {
  if (!form.value.startDate) return;

  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const startDateObj = new Date(form.value.startDate);
  startDateObj.setHours(0, 0, 0, 0);

  // Prüfung: Startdatum muss strikt in der Zukunft liegen (weder Vergangenheit noch heute)
  if (startDateObj <= today) {
    alert('Vacation can only be requested starting tommorow.');
    return;
  }

  if (vacationStats.value.days <= 0) {
    alert('Please select at least a range of 1 days.')
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
    console.log("Waiting for valid Employee ID / User-Data...");
    return;
  }

  const requestPayload = {
    startDate: form.value.startDate,
    endDate: form.value.endDate
  };

  try {
    await vacationRequestService.create(currentId, requestPayload);

    form.value.startDate = ''
    form.value.endDate = ''
    
    await fetchVacationData();
    await fetchVacationRequests();

  } catch (error) {
    console.error("error creating vacation request:", error);
    alert('Could not save vacation request.');
  }
}

const yearshow = computed(() => {
  const date = (new Date().getFullYear() - 1);
  return date;
})
</script>

<template>
  <div class="tt-container">
    <div class="tt-header-row">
      <div class="tt-heading">
      <p class="tt-subtitle-top">TIME OFF & RECHARGE</p>
      <h1 class="tt-title">Vacation management </h1>
      <p class="tt-subtitle">Work hard, recharge smart. Keep an eye on your days off.</p>
      </div>
      <div class="tt-actions">
        <div class="quota-pill">
          <span>Remaining vacation days {{yearshow}}:</span>
          <strong>{{remainingVacationDays}} {{ remainingVacationDays === 1 ? 'Day' : 'Days' }}
          </strong>
        </div>
      </div>
    </div>

    <div class="tt-subbar">
      <div class="submitted-count">
        <span>Taken: <strong>{{ takenVacationDays }} {{ takenVacationDays === 1 ? 'Day' : 'Days' }}</strong></span>
        <span>•</span>
        <span>Planned: <strong>{{ plannedVacationDays }} {{ plannedVacationDays === 1 ? 'Day' : 'Days' }}</strong></span>
      </div>
    </div>

    <div class="content-split">
      <div class="table-card">
        <div class="table-card-header">
          <div>
            <h2 class="box-title">My vacation requests</h2>
          </div>
        </div>

        <table class="data-table">
          <thead>
            <tr class="th-row">
              <th class="th-date">Period</th>
              <th class="th-days">Days</th>
              <th class="th-status">Status</th>
              <th class="th-action">Action</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="vacationEntries.length === 0">
              <td colspan="4" class="empty-state">   
                  No leave requests yet.
              </td>
            </tr>
            <tr v-else v-for="entry in vacationEntries" :key="entry.id" class="data-tr">
              <td class="td-date">{{ entry.period }}</td>
              <td class="td-net-val">{{ entry.days }} {{ entry.days === 1 ? 'Tag' : 'Tage' }}</td>
              <td>
                <StatusBadge :variant="entry.variant" :label="entry.status" />
              </td>
              <td>
                <button 
                  v-if="entry.canBeCancelled"
                  @click="handleCancelRequest(entry.id)" 
                  class="cancel-btn"
                  title="Cancel"
                >
                  Abbrechen
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="right-sidebar">
        <form @submit.prevent="handleAddVacation" class="form-card">
          <div class="form-card-header">
            <h2 class="box-title">Apply for vacation</h2>
          </div>

          <div class="time-inputs-row">
            <label class="input-label flex-1">
              From
              <input v-model="form.startDate" type="date" :min="minDate" class="text-input" required />
            </label>
            <label class="input-label flex-1">
              To
              <input v-model="form.endDate" type="date" :min="form.startDate || minDate" class="text-input" required />
            </label>
          </div>

          <div class="calc-box">
            <div class="calc-header">
              <span class="calc-val">Vacation duration: {{ vacationStats.days }} {{ vacationStats.days === 1 ? 'day' : 'days' }}</span>
            </div>
            <div class="calc-sub">Weekends (Sat/Sun) are automatically deducted.</div>
          </div>

          <button type="submit" class="submit-btn">
            <span>✓</span><span>Submit application</span>
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
.tt-title {  color: #172D32; font-size: 30px; font-weight: 600; line-height: 36px; text-align: left;}
.tt-actions { display: flex; align-items: center; gap: 12px; }
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
.th-action { width: 100px; }

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

.cancel-btn {
  padding: 4px 10px;
  background: #FFF1F2;
  color: #E11D48;
  border: 1px solid #FECDD3;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 500;
  cursor: pointer;
}
.cancel-btn:hover {
  background: #FFE4E6;
}

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