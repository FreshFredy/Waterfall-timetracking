<script setup>
import { ref, watch } from 'vue'
import StatusBadge from '../components/ui/StatusBadge.vue'
import EmployeeService from '../services/api.js'
import { vacationRequestService, timesheetService } from "../services/api"

const props = defineProps({
  activeRoute: { type: String, required: true },
  role: { type: String, default: 'employee' },
  user: {
    type: Object,
    default: () => ({ initials: '', name: '', title: '', email: '', id: null }),
  },
  pendingReviewsCount: { type: Number },
  employeeId: { type: Number }
})

const pendingRequests = ref([])
const submittedTimesheets = ref([])
const rejectionReasons = ref({})
const timesheetRejectionReasons = ref({})

// State für das geöffnete Timesheet-Detailmodal
const selectedTimesheet = ref(null)
const isDetailModalOpen = ref(false)

// Hilfsfunktion zur Berechnung von Arbeitstagen (Mo-Fr)
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

// Hilfsfunktion zur Ermittlung der gültigen Supervisor-ID
const getSupervisorId = async () => {
    let currentSupervisorId = props.employeeId;
    if (!currentSupervisorId && props.user && props.user.email) {
        const employeesResponse = await EmployeeService.getAll(props.user.email);
        if (employeesResponse.data && employeesResponse.data.length > 0) {
            currentSupervisorId = employeesResponse.data[0].id;
        }
    }
    if (!currentSupervisorId && props.user && props.user.id) {
        currentSupervisorId = props.user.id;
    }
    return currentSupervisorId;
}

// 1. Ausstehende Urlaubsanträge abrufen
const fetchPendingRequests = async () => {
    try {
        const currentSupervisorId = await getSupervisorId();
        if (!currentSupervisorId) return;

        const response = await vacationRequestService.getPending(currentSupervisorId);
        
        const requestsWithEmployeeNames = await Promise.all(
            response.data.map(async (req) => {
                let empName = `Mitarbeiter ID: ${req.employeeId}`;
                try {
                    if (req.employeeId) {
                        const empResponse = await EmployeeService.get(req.employeeId);
                        if (empResponse.data) {
                            const e = empResponse.data;
                            const firstname = e.firstname || e.firstName || '';
                            const lastname = e.lastname || e.lastName || '';
                            if (firstname || lastname) empName = `${firstname} ${lastname}`.trim();
                        }
                    }
                } catch (err) {
                    console.error(`Konnte Mitarbeiter mit ID ${req.employeeId} nicht laden:`, err);
                }

                const formatDate = (dateStr) => {
                    if (!dateStr) return '';
                    const [y, m, d] = dateStr.split('-');
                    return `${d}.${m}.${y}`;
                };

                const periodStr = req.startDate === req.endDate
                    ? formatDate(req.startDate)
                    : `${formatDate(req.startDate)} – ${formatDate(req.endDate)}`;

                const daysCount = req.workingDays || calculateWorkDays(req.startDate, req.endDate);

                return {
                    id: req.id,
                    employeeName: `${empName} (ID: ${req.employeeId})`,
                    period: periodStr,
                    days: daysCount,
                    startDate: req.startDate,
                    endDate: req.endDate
                };
            })
        );

        pendingRequests.value = requestsWithEmployeeNames;
    } catch (error) {
        console.error("Fehler beim Laden der ausstehenden Anträge:", error);
    }
}

// 2. Eingereichte Timesheets abrufen
const fetchSubmittedTimesheets = async () => {
    try {
        const employeesResponse = await EmployeeService.getAll();
        const employees = employeesResponse.data || [];
        let allSubmitted = [];

        for (const emp of employees) {
            try {
                const tsResponse = await timesheetService.getTimesheets(emp.id);
                const timesheets = tsResponse.data || [];

                const submitted = timesheets.filter(ts => {
                    const status = (ts.status || '').toUpperCase();
                    return status === 'SUBMITTED' || status === 'EINGEREICHT';
                });

                for (const ts of submitted) {
                    const firstname = emp.firstname || emp.firstName || '';
                    const lastname = emp.lastname || emp.lastName || '';
                    const empName = (firstname || lastname) ? `${firstname} ${lastname}`.trim() : `Mitarbeiter ID: ${emp.id}`;

                    let totalMins = 0;
                    if (ts.timeEntries) {
                        ts.timeEntries.forEach(entry => {
                            totalMins += Math.round((entry.creditedHours || 0) * 60);
                        });
                    }
                    const h = Math.floor(totalMins / 60);
                    const m = String(totalMins % 60).padStart(2, '0');

                    allSubmitted.push({
                        id: ts.id,
                        employeeId: emp.id,
                        employeeName: `${empName} (ID: ${emp.id})`,
                        period: `${ts.month || '–'}.${ts.year || '–'}`,
                        status: ts.status || 'Submitted',
                        totalHours: `${h}:${m}`,
                        rawTimesheet: ts
                    });
                }
            } catch (err) {
                // Einzelne Fehler überspringen
            }
        }

        submittedTimesheets.value = allSubmitted;
    } catch (error) {
        console.error("Fehler beim Laden der eingereichten Timesheets:", error);
    }
}

// Timesheet im Detail öffnen
const handleViewTimesheet = (tsItem) => {
  selectedTimesheet.value = { ...tsItem.rawTimesheet, employeeDisplayName: tsItem.employeeName };
  isDetailModalOpen.value = true;
}

const closeDetailModal = () => {
  isDetailModalOpen.value = false;
  selectedTimesheet.value = null;
}

// Hilfsfunktion: Minuten formatieren
const formatMinutes = (mins) => {
  if (mins == null || isNaN(mins)) return '0:00'
  const h = Math.floor(mins / 60)
  const m = String(mins % 60).padStart(2, '0')
  return `${h}:${m}`
}

// Timesheet Genehmigen (Approve)
const handleApproveTimesheet = async (timesheetId) => {
  try {
    const supervisorId = await getSupervisorId();
    if (!supervisorId) {
      alert("Fehler: Konnte keine Supervisor-ID ermitteln.");
      return;
    }

    await timesheetService.approve(timesheetId, supervisorId);
    alert("Timesheet erfolgreich genehmigt.");
    closeDetailModal();
    await fetchSubmittedTimesheets();
  } catch (error) {
    console.error("Fehler beim Genehmigen des Timesheets:", error);
    const msg = error.response?.data?.message || error.message || 'Unbekannter Fehler';
    alert(`Timesheet konnte nicht genehmigt werden: ${msg}`);
  }
}

// Timesheet Ablehnen (Reject) mit Grund
const handleRejectTimesheet = async (timesheetId) => {
  const reason = timesheetRejectionReasons.value[timesheetId];
  if (!reason || reason.trim() === '') {
    alert('Bitte gib einen Grund für die Ablehnung an.');
    return;
  }

  try {
    const supervisorId = await getSupervisorId();
    if (!supervisorId) {
      alert("Fehler: Konnte keine Supervisor-ID ermitteln.");
      return;
    }

    await timesheetService.reject(timesheetId, supervisorId, reason);
    alert("Timesheet erfolgreich abgelehnt.");
    timesheetRejectionReasons.value[timesheetId] = '';
    closeDetailModal();
    await fetchSubmittedTimesheets();
  } catch (error) {
    console.error("Fehler beim Ablehnen des Timesheets:", error);
    const msg = error.response?.data?.message || error.message || 'Unbekannter Fehler';
    alert(`Timesheet konnte nicht abgelehnt werden: ${msg}`);
  }
}

// Urlaub genehmigen
const handleApprove = async (requestId) => {
  try {
    const supervisorId = await getSupervisorId();
    await vacationRequestService.approve(requestId, supervisorId);
    await fetchPendingRequests();
  } catch (error) {
    console.error("Fehler beim Genehmigen des Antrags:", error);
    alert("Antrag konnte nicht genehmigt werden.");
  }
}

// Urlaub ablehnen
const handleReject = async (requestId) => {
  const reason = rejectionReasons.value[requestId];
  if (!reason || reason.trim() === '') {
    alert('Bitte gib einen Grund für die Ablehnung an.');
    return;
  }

  try {
    const supervisorId = await getSupervisorId();
    await vacationRequestService.reject(requestId, supervisorId, reason);
    rejectionReasons.value[requestId] = '';
    await fetchPendingRequests();
  } catch (error) {
    console.error("Fehler beim Ablehnen des Antrags:", error);
    alert("Antrag konnte nicht abgelehnt werden.");
  }
}

// Reagiert auf Routenwechsel
watch(
  () => [props.activeRoute, props.employeeId, props.user?.email],
  ([route]) => {
    if (route) {
        fetchPendingRequests();
        fetchSubmittedTimesheets();
    }
  },
  { immediate: true }
)
</script>

<template>
  <div class="mr-container">
    <div class="mr-header">
      <div class="tagline">Übersicht & Anträge</div>
      <h1 class="mr-main-title">Review die Urlaubsanträge & Zeiterfassungen deines Teams</h1>
    </div>

    <!-- 1. Tabelle: Ausstehende Urlaubsanträge -->
    <div class="table-card">
      <div class="table-card-header">
        <h2 class="box-title">Ausstehende Anträge</h2>
      </div>

      <table class="data-table">
        <thead>
          <tr class="th-row">
            <th class="th-employee">Mitarbeiter</th>
            <th class="th-date">Zeitraum</th>
            <th class="th-days">Tage</th>
            <th class="th-reason">Grund für Ablehnung</th>
            <th class="th-actions">Aktionen</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="pendingRequests.length === 0">
            <td colspan="5" class="empty-state">
              Keine ausstehenden Urlaubsanträge vorhanden.
            </td>
          </tr>
          <tr v-else v-for="req in pendingRequests" :key="req.id" class="data-tr">
            <td class="td-employee font-semibold">{{ req.employeeName }}</td>
            <td class="td-date">{{ req.period }}</td>
            <td class="td-net-val">{{ req.days }} {{ req.days === 1 ? 'Tag' : 'Tage' }}</td>
            <td class="td-center">
              <input 
                v-model="rejectionReasons[req.id]" 
                type="text" 
                placeholder="Grund (nur bei Ablehnung nötig)" 
                class="reason-input"
              />
            </td>
            <td class="td-actions-group">
              <button @click="handleApprove(req.id)" class="approve-btn" title="Antrag genehmigen">
                Genehmigen
              </button>
              <button @click="handleReject(req.id)" class="reject-btn" title="Antrag ablehnen">
                Ablehnen
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 2. Tabelle: Eingereichte Timesheets -->
    <div class="table-card">
      <div class="table-card-header">
        <h2 class="box-title">Eingereichte Timesheets der Mitarbeiter</h2>
      </div>

      <table class="data-table">
        <thead>
          <tr class="th-row">
            <th class="th-employee">Mitarbeiter</th>
            <th class="th-date">Periode (Monat.Jahr)</th>
            <th class="th-days">Status</th>
            <th class="th-reason">Gesamtstunden</th>
            <th class="th-actions">Aktionen</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="submittedTimesheets.length === 0">
            <td colspan="5" class="empty-state">
              Keine eingereichten Timesheets vorhanden.
            </td>
          </tr>
          <tr v-else v-for="ts in submittedTimesheets" :key="ts.id" class="data-tr">
            <td class="td-employee font-semibold">{{ ts.employeeName }}</td>
            <td class="td-date">{{ ts.period }}</td>
            <td class="td-net-val">
              <StatusBadge variant="neutral" :label="ts.status" />
            </td>
            <td class="td-center">{{ ts.totalHours }} h</td>
            <td class="td-actions-group">
              <button @click="handleViewTimesheet(ts)" class="view-btn" title="Timesheet ansehen">
                Ansehen
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Modal für Timesheet-Details und Freigabe -->
    <div v-if="isDetailModalOpen" class="modal-overlay">
      <div class="modal-card">
        <div class="modal-header">
          <div>
            <h2 class="box-title">Timesheet Details</h2>
            <p class="modal-subtitle">{{ selectedTimesheet?.employeeDisplayName }} (Monat: {{ selectedTimesheet?.month }}/{{ selectedTimesheet?.year }})</p>
          </div>
          <button @click="closeDetailModal" class="close-modal-btn">✕</button>
        </div>

        <!-- Entries Tabelle im Modal -->
        <div class="modal-table-container">
          <table class="data-table">
            <thead>
              <tr class="th-row">
                <th class="th-date">Datum</th>
                <th>Start</th>
                <th>Ende</th>
                <th>Pause</th>
                <th>Typ</th>
                <th class="th-net">Arbeitszeit</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="!selectedTimesheet?.timeEntries || selectedTimesheet.timeEntries.length === 0">
                <td colspan="6" class="empty-state">Keine Einträge in diesem Timesheet.</td>
              </tr>
              <tr v-else v-for="entry in selectedTimesheet.timeEntries" :key="entry.id" class="data-tr">
                <td class="td-date">{{ entry.entryDate }}</td>
                <td>{{ entry.startTime ? entry.startTime.substring(0, 5) : '–' }}</td>
                <td>{{ entry.endTime ? entry.endTime.substring(0, 5) : '–' }}</td>
                <td>{{ entry.breakMinutes ?? 0 }} Min</td>
                <td>{{ entry.entryType || 'WORK' }}</td>
                <td class="td-net-val">{{ formatMinutes(Math.round((entry.creditedHours || 0) * 60)) }} h</td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Ablehnungsgrund & Aktionen für den Timesheet -->
        <div class="modal-footer-actions">
          <div class="reject-section">
            <input 
              v-model="timesheetRejectionReasons[selectedTimesheet.id]" 
              type="text" 
              placeholder="Grund für Ablehnung eingeben..." 
              class="reason-input-modal"
            />
            <button @click="handleRejectTimesheet(selectedTimesheet.id)" class="reject-btn">
              Ablehnen
            </button>
          </div>
          <button @click="handleApproveTimesheet(selectedTimesheet.id)" class="approve-btn-large">
            Timesheet Genehmigen
          </button>
        </div>
      </div>
    </div>

  </div>  
</template>

<style scoped>
.mr-container { padding: 32px; display: flex; flex-direction: column; gap: 24px; }
.mr-header { display: flex; flex-direction: column; gap: 8px; }
.tagline { color: #176A70; font-size: 13px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; }
.mr-main-title { color: #172D32; font-size: 30px; font-weight: 600; line-height: 36px; }

.table-card { background: white; border-radius: 12px; outline: 1px #DFE7E8 solid; display: flex; flex-direction: column; overflow: hidden; }
.table-card-header { padding: 20px; display: flex; flex-direction: column; gap: 16px; }
.box-title { color: #172D32; font-size: 16px; font-weight: 600; }

.data-table { width: 100%; border-collapse: collapse; text-align: left; }
.th-row { height: 36px; background: #F8FAFA; border-top: 1px #DFE7E8 solid; border-bottom: 1px #DFE7E8 solid; color: #697B80; font-size: 10px; font-weight: 600; }
.th-employee { padding-left: 20px; width: 250px; }
.th-date { width: 140px; padding-left: 20px; }
.th-days { width: 90px; }
.th-reason { width: 260px; }
.th-actions { width: 180px; padding-right: 20px; text-align: right; }

.empty-state {
  padding: 32px 20px;
  text-align: center;
  color: #697B80;
  font-size: 13px;
}

.data-tr { height: 56px; border-bottom: 1px #DFE7E8 solid; font-size: 12px; color: #172D32; }
.data-tr:last-child { border-bottom: none; }
.data-tr td { vertical-align: middle; }

.td-employee { padding-left: 20px; font-weight: 600; }
.td-net-val { font-weight: 600; }

.reason-input {
  width: 90%;
  height: 34px;
  padding: 0 10px;
  background: #FFFFFF;
  color: #000000;
  border: 1px solid #DFE7E8;
  border-radius: 6px;
  font-size: 12px;
}
.reason-input::placeholder { color: #9CA3AF; }

.td-actions-group {
  padding-right: 20px;
  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: flex-end;
  height: 56px;
}

.approve-btn {
  padding: 6px 12px;
  background: #ECFDF5;
  color: #059669;
  border: 1px solid #A7F3D0;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
}
.approve-btn:hover { background: #D1FAE5; }

.reject-btn {
  padding: 6px 12px;
  background: #FFF1F2;
  color: #E11D48;
  border: 1px solid #FECDD3;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
}
.reject-btn:hover { background: #FFE4E6; }

.view-btn {
  padding: 6px 12px;
  background: #E8F3F2;
  color: #176A70;
  border: 1px solid #D1E5E4;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
}
.view-btn:hover { background: #D1E5E4; }

/* Modal Styling */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.modal-card {
  background: white;
  width: 750px;
  max-height: 90vh;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 10px 25px rgba(0,0,0,0.2);
}
.modal-header {
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  border-bottom: 1px solid #DFE7E8;
}
.modal-subtitle {
  font-size: 12px;
  color: #697B80;
  margin-top: 4px;
}
.close-modal-btn {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  color: #697B80;
}
.modal-table-container {
  overflow-y: auto;
  max-height: 50vh;
  padding: 0 20px;
}
.modal-footer-actions {
  padding: 20px;
  border-top: 1px solid #DFE7E8;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #F8FAFA;
}
.reject-section {
  display: flex;
  gap: 8px;
  flex: 1;
  margin-right: 16px;
}
.reason-input-modal {
  flex: 1;
  height: 38px;
  padding: 0 10px;
  border: 1px solid #DFE7E8;
  border-radius: 6px;
  font-size: 12px;
  background: white;
  color: #000;
}
.approve-btn-large {
  padding: 10px 20px;
  background: #059669;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
}
.approve-btn-large:hover {
  background: #047857;
}
</style>