<script setup>
import { ref, watch } from 'vue'
import StatusBadge from '../components/ui/StatusBadge.vue'
import EmployeeService from '../services/api.js'
import { vacationRequestService } from "../services/api";

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
const rejectionReasons = ref({})

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
    if ((!currentSupervisorId || currentSupervisorId === 1) && props.user && props.user.email) {
        const employeesResponse = await EmployeeService.getAll(props.user.email);
        if (employeesResponse.data && employeesResponse.data.length > 0) {
            currentSupervisorId = employeesResponse.data[0].id;
        }
    }
    return currentSupervisorId;
}

// 1. Ausstehende Anträge für den Supervisor vom Backend abrufen
const fetchPendingRequests = async () => {
    try {
        const currentSupervisorId = await getSupervisorId();

        if (!currentSupervisorId || currentSupervisorId === 'undefined') {
            console.warn("Warte auf gültige Supervisor-ID / User-Daten...");
            return;
        }

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
                            if (firstname || lastname) {
                                empName = `${firstname} ${lastname}`.trim();
                            }
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

// 2. Urlaub genehmigen (Echte API-Anbindung)
const handleApprove = async (requestId) => {
  try {
    const supervisorId = await getSupervisorId();
    await vacationRequestService.approve(requestId, supervisorId);
    console.log(`Antrag ${requestId} erfolgreich genehmigt.`);
    await fetchPendingRequests(); // Tabelle aktualisieren
  } catch (error) {
    console.error("Fehler beim Genehmigen des Antrags:", error);
    alert("Antrag konnte nicht genehmigt werden.");
  }
}

// 3. Urlaub ablehnen (Echte API-Anbindung inklusive Grund)
const handleReject = async (requestId) => {
  const reason = rejectionReasons.value[requestId];
  if (!reason || reason.trim() === '') {
    alert('Bitte gib einen Grund für die Ablehnung an.');
    return;
  }

  try {
    const supervisorId = await getSupervisorId();
    await vacationRequestService.reject(requestId, supervisorId, reason);
    console.log(`Antrag ${requestId} erfolgreich abgelehnt mit Grund:`, reason);
    
    // Eingabefeld leeren
    rejectionReasons.value[requestId] = '';
    
    await fetchPendingRequests(); // Tabelle aktualisieren
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
    }
  },
  { immediate: true }
)
</script>

<template>
  <div class="mr-container">
    <div class="mr-header">
      <div class="tagline">Urlaubsrequests</div>
      <h1 class="mr-main-title">Review die Urlaubsanträge deines Teams</h1>
    </div>

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
.th-date { width: 180px; }
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

.data-tr td {
  vertical-align: middle;
}

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
.reason-input::placeholder {
  color: #9CA3AF;
}

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
.approve-btn:hover {
  background: #D1FAE5;
}

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
.reject-btn:hover {
  background: #FFE4E6;
}
</style>