<script setup>
import { computed, ref, onMounted } from 'vue'
import StatCard from '../components/ui/StatCard.vue'
import StatusBadge from '../components/ui/StatusBadge.vue'
import EmployeeService from '../services/api.js'
import { timesheetService } from '../services/api.js'
import { sickLeaveService } from "../services/api.js";

const props = defineProps({
  activeRoute: { type: String, required: true },
  role: { type: String, default: 'employee' },
  user: {
    type: Object,
    default: () => ({ initials: '', name: '', title: '', email: '' }),
  },
  pendingReviewsCount: { type: Number},
  employeeId: { type: Number,}
})
const formatMinutes = (mins) => {
  if (mins == null || isNaN(mins)) return '0:00'
  const h = Math.floor(mins / 60)
  const m = String(mins % 60).padStart(2, '0')
  return `${h}:${m}`
}

const emit = defineEmits(['navigate'])
const workingTime = ref("Laden...")
const flexBalance = ref("Laden...")
const sickLeave = ref("Laden...")
const availableVacationDays = ref("Laden...")

// Daten vom Backend abrufen
const fetchDashboardData = async () => {
    try {
        let currentId = props.employeeId;
        
        // ID dynamisch über E-Mail auflösen, falls nicht explizit gesetzt
        if ((!currentId || currentId === 1) && props.user && props.user.email) {
            const employeesResponse = await EmployeeService.getAll(props.user.email);
            if (employeesResponse.data && employeesResponse.data.length > 0) {
                currentId = employeesResponse.data[0].id;
            }
        }
        const now = new Date()
        const year = now.getFullYear()
        const month = now.getMonth() + 1 // Aktueller Monat (1-basiert)

        // Arbeitszeiten laden
        const timesheetsResponse = await timesheetService.getTimesheets(currentId, month, year)
        const timesheetData = timesheetsResponse.data

        if (Array.isArray(timesheetData) && timesheetData.length > 0) {
            const currentSheet = timesheetData[0]
            const totalHours = currentSheet.workedHours ?? currentSheet.creditedHours ?? 0

            if (totalHours > 0) {
                const h = Math.floor(totalHours)
                const m = Math.round((totalHours - h) * 60)
                workingTime.value = `${h}:${String(m).padStart(2, '0')} h`
            } else {
                const entries = currentSheet.timeEntries || []
                const totalMinutes = entries.reduce((sum, entry) => {
                    return sum + Math.round((entry.creditedHours || 0) * 60)
                }, 0)
                const h = Math.floor(totalMinutes / 60)
                const m = totalMinutes % 60
                workingTime.value = totalMinutes > 0 ? `${h}:${String(m).padStart(2, '0')} h` : "0:00 h"
            }
        } else {
            workingTime.value = "0:00 h"
        }

        // 1. Flex-Konto & Arbeitszeit abrufen
        const flexResponse = await EmployeeService.getFlexAccount(currentId)
        const balanceVal = flexResponse.data.balance !== undefined && flexResponse.data.balance !== null 
            ? flexResponse.data.balance 
            : (flexResponse.data.balanceHours !== undefined && flexResponse.data.balanceHours !== null ? flexResponse.data.balanceHours : 0);
        
        flexBalance.value = `${balanceVal >= 0 ? '+' : ''}${balanceVal}:00 h`;

        // 2. Urlaubskonto abrufen
        const vacationResponse = await EmployeeService.getVacationBalance(currentId)
        const vacData = vacationResponse.data || {}
        
        const annual = vacData.annualDays ?? 30;
        const taken = vacData.takenDays ?? 0;
        const planned = vacData.plannedDays ?? 0;
        const remainingLastYear = vacData.remainingDays_last_year ?? 0;

        availableVacationDays.value = vacData.remainingDays !== undefined && vacData.remainingDays !== null
            ? vacData.remainingDays 
            : ((annual + remainingLastYear) - (taken + planned));

        // 3. Krankheiten (Sick Leaves) anhand des 'day'-Feldes für den aktuellen Monat filtern
        const sickLeaveResponse = await sickLeaveService.getByEmployee(currentId)
        const sickLeaves = sickLeaveResponse.data || []
        
        let currentMonthSickDays = 0
        sickLeaves.forEach(leave => {
            const dateStr = leave.day || leave.startDate || leave.date
            if (dateStr) {
                const leaveDate = new Date(dateStr)
                if (leaveDate.getFullYear() === year && (leaveDate.getMonth() + 1) === month) {
                    currentMonthSickDays += Number(leave.daysCount ?? leave.days ?? 1)
                }
            }
        })
        sickLeave.value = `${currentMonthSickDays} Tage`

    } catch (error) {
        console.error("Fehler beim Laden der Dashboard-Daten:", error)
        workingTime.value = "Fehler"
        flexBalance.value = "Fehler"
        sickLeave.value = "Fehler"
        availableVacationDays.value = "–"
    }
}

onMounted(() => {
    fetchDashboardData()
})

</script>

<template>
  <div class="dashboard-container">
    <div class="top-row">
      <div class="welcome-col">
        <div class="tagline">DASHBOARD</div>
        <h1 class="greeting">Hallo {{ user.name }}</h1>
        <p class="sub-greeting">Hier ist dein monatlicher Recap.</p>
      </div>
    </div>


    <div class="kpi-grid">
      <StatCard title="Working time " :value="workingTime"/>
      <StatCard title="Flex balance" :value="flexBalance"  />
      <StatCard title="Sickleave " :value="sickLeave" />
    </div>

    <div class="middle-row">
      <div class="chart-box vacation-box">
        <div>
          <h2 class="box-title">Urlaubsplanung</h2>
          <p class="box-subtitle">Verwalte deinen Urlaub</p>
        </div>
        
        <div class="vacation-content">
          <div class="vacation-info">
            <span class="vacation-label">Verfügbare Urlaubstage:</span>
            <span class="vacation-value">{{ availableVacationDays }} Tage</span>
          </div>
          
        <button @click="emit('navigate', 'vacation')" class="view-timesheet-link">Zum Urlaub →</button>
        </div>
      </div>

      <div class="summary-card">
        <h2 class="box-title">Monats - Zusammenfassung</h2>
        <div class="summary-subrow">
        </div>
        <div class="summary-metrics">
          <div class="metric-row"><span class="metric-lbl">Working Time</span><span class="metric-val">{{ workingTime }}</span>
        </div>
          <div class="metric-row"><span class="metric-lbl">Targettime · 22 × 8:00 h</span><span class="metric-val">176:00 h</span></div>
        </div>
        <div class="divider-line"></div>
        <button @click="emit('navigate', 'time-tracking')" class="view-timesheet-link">Zum Timesheet →</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.vacation-box {
  background: #ffffff;
  border-radius: 12px;
  outline: 1px #DFE7E8 solid;
  outline-offset: -1px;
  padding: 24px;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 20px;
}

.box-title {
  font-size: 16px;
  font-weight: 600;
  color: #172D32;
  line-height: 23.20px;
  margin: 0 0 4px 0;
}

.box-subtitle {
  font-size: 12px;
  color: #697B80;
  line-height: 17.40px;
  margin: 0;
}

.vacation-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding-top: 16px;
  border-top: 1px solid #DFE7E8;
}

.vacation-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.vacation-label {
  font-size: 12px;
  color: #697B80;
}

.vacation-value {
  font-size: 20px;
  font-weight: 600;
  color: #176A70;
}

.vacation-submit-btn {
  width: 100%;
}

.dashboard-container {
  padding: 32px;
  overflow: hidden;
  flex-direction: column;
  justify-content: flex-start;
  align-items: flex-start;
  gap: 24px;
  display: flex;
}
.top-row {
  align-self: stretch;
  overflow: hidden;
  justify-content: space-between;
  align-items: center;
  display: inline-flex;
}
.welcome-col {
  flex: 1 1 0;
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.tagline {
  color: #697B80;
  font-size: 11px;
  font-family: Inter, sans-serif;
  font-weight: 600;
}
.greeting {
  color: #172D32;
  font-size: 30px;
  font-family: Inter, sans-serif;
  font-weight: 600;
  line-height: 36px;
}
.sub-greeting {
  color: #697B80;
  font-size: 13px;
  font-family: Inter, sans-serif;
  line-height: 18.85px;
}
.kpi-grid, .middle-row, .bottom-row {
  align-self: stretch;
  display: inline-flex;
  gap: 16px;
}
.middle-row, .bottom-row {
  gap: 24px;
}
.chart-box, .summary-card, .entries-box {
  background: white;
  border-radius: 12px;
  outline: 1px #DFE7E8 solid;
  outline-offset: -1px;
  display: inline-flex;
  flex-direction: column;
}
.chart-box { flex: 1; padding: 24px; gap: 20px; }
.summary-card { width: 360px; padding: 24px; gap: 18px; }
.summary-subrow { display: flex; justify-content: space-between; align-items: center; }
.summary-metrics { display: flex; flex-direction: column; gap: 9px; }
.metric-row { display: flex; justify-content: space-between; }
.metric-lbl { color: #697B80; font-size: 12px; }
.metric-val { color: #172D32; font-size: 13px; font-weight: 600; }
.divider-line { height: 1px; background: #DFE7E8; width: 100%; }
.view-timesheet-link { color: #176A70; font-size: 12px; font-weight: 600; background: none; border: none; cursor: pointer; text-align: left; padding: 0; }
</style>