<script setup>
import { computed, ref, onMounted } from 'vue'
import StatCard from '../components/ui/StatCard.vue'
import StatusBadge from '../components/ui/StatusBadge.vue'
import EmployeeService from '../services/api.js'
import { timesheetService } from '../services/api.js'
import { sickLeaveService } from "../services/api.js";
import WeeklyChart from '../components/ui/WeeklyChart.vue'

const currentTimesheet = ref(null)

const statusLabels = { DRAFT: 'Draft', SUBMITTED: 'Pending', APPROVED: 'Approved', REJECTED: 'Rejected' }
const statusLabel = computed(() => statusLabels[currentTimesheet.value?.status] ?? 'Draft')
const flexSubtitle = ref('')
const monthFlex = ref(0)
const flexIsPending = ref(false)

const formatHours = (h, withSign = false) => {
  const sign = h < 0 ? '-' : (withSign ? '+' : '')
  const totalMin = Math.round(Math.abs(h) * 60)
  return `${sign}${Math.floor(totalMin / 60)}:${String(totalMin % 60).padStart(2, '0')} h`
}

const weeklyData = ref([])

const targetPerDay = 8            // later: the employee's targetH

const buildWeeks = (entries, year, month) => {
  const pad = (n) => String(n).padStart(2, '0')
  const monthName = new Date(year, month - 1, 1).toLocaleString('en-US', { month: 'short' })
  const daysInMonth = new Date(year, month, 0).getDate()

  const weeks = []
  const weekOfDay = {}
  let current = null

  for (let day = 1; day <= daysInMonth; day++) {
    const dow = new Date(year, month - 1, day).getDay()      // 0 = Sunday ... 6 = Saturday
    if (!current || (dow === 1 && current.first !== null)) {
      current = { first: null, last: null, work: 0, sick: 0, bookedDays: new Set() }
      weeks.push(current)
    }
    if (dow !== 0 && dow !== 6) {
      if (current.first === null) current.first = day
      current.last = day
    }
    weekOfDay[day] = current
  }

  for (const entry of entries) {
    const day = Number(String(entry.entryDate).slice(8, 10))
    const week = weekOfDay[day]
    if (!week) continue
    const hours = entry.creditedHours || 0
    if (entry.entryType === 'SICK') week.sick += hours
    else week.work += hours

    const dow = new Date(year, month - 1, day).getDay()
    if (dow !== 0 && dow !== 6) week.bookedDays.add(day)       // only weekdays have a target
  }

  return weeks.map((w) => ({
    label: w.first === w.last
      ? `${pad(w.first)}. ${monthName}`
      : `${pad(w.first)}.–${pad(w.last)}. ${monthName}`,
    work: w.work,
    sick: w.sick,
    target: w.bookedDays.size * targetPerDay,
  }))
}

const monthTarget = computed(() => weeklyData.value.reduce((sum, w) => sum + w.target, 0))
const targetDays = computed(() => Math.round(monthTarget.value / targetPerDay))

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour >= 5 && hour < 12) return 'Good morning'
  if (hour >= 12 && hour < 18) return 'Good afternoon'
  return 'Good evening'
})
const monthshow = computed(() => {
  const date = new Date()
  return date.toLocaleString('en-US', { month: 'long' });
})
const yearshow = computed(() => {
  const date = new Date()
  return date.toLocaleString('en-US', { year: 'numeric' });
})
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
const icons = {
  time: '\uf017',  // clock
  flex: '\uf0ec',  // exchange
  sick: '\uf0f1',  // stethoscope
}
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
            weeklyData.value = buildWeeks(currentSheet.timeEntries || [], year, month)
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
            weeklyData.value = buildWeeks([], year, month)
        }

        // 1. Flex-Konto & Arbeitszeit abrufen
        const flexResponse = await EmployeeService.getFlexAccount(currentId)
        const balance = Number(flexResponse.data.balanceHours ?? flexResponse.data.balance ?? 0)

        const sheet = Array.isArray(timesheetData) && timesheetData.length > 0 ? timesheetData[0] : null
        currentTimesheet.value = sheet

        monthFlex.value = sheet ? Number(sheet.flexChange ?? 0) : 0
        flexIsPending.value = !!sheet && sheet.status !== 'APPROVED'
        const pending = flexIsPending.value ? monthFlex.value : 0

        flexBalance.value = formatHours(balance + pending, true)
        flexSubtitle.value = pending !== 0 ? `Includes ${formatHours(pending, true)} pending` : ''

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
        <div class="tagline">YOUR PERSONAL DASHBOARD</div>
        <h1 class="greeting">{{ greeting }} {{ user.name }}</h1>
        <p class="sub-greeting">Your time at a glance. Here is your {{ monthshow }} recap.</p>
      </div>
    </div>

    <div class="kpi-grid">
      <StatCard class="wide" :title="`Working time in ${monthshow}`" :value="workingTime" :icon="icons.time" />
      <StatCard highlighted title="Your flex balance" :value="flexBalance" :subtitle="flexSubtitle" :icon="icons.flex" />
      <StatCard title="Sickleave" :value="sickLeave" :icon="icons.sick" />
    </div>

    <div class="middle-row">
      <WeeklyChart
        title="A month in good rhythm."
        :subtitle="`Weekly creditable time · ${monthshow} ${yearshow}`"
        :weeks="weeklyData"
      />

      <div class="right-col">
        <div class="summary-card">
          <h2 class="box-title">Your monthly summary</h2>
          <div class="form-card-sub">
            <h3 class="box-subtitle">{{ monthshow }} {{ yearshow }}</h3>
            <StatusBadge variant="neutral" :label="statusLabel" />
          </div>

          <div class="summary-subrow"></div>
          <div class="summary-metrics">
            <div class="metric-row">
              <span class="metric-lbl">Working Time</span>
              <span class="metric-val">{{ workingTime }}</span>
            </div>
            <div class="metric-row">
              <span class="metric-lbl">Targettime · {{ targetDays }} × 8:00 h</span>
              <span class="metric-val">{{ formatHours(monthTarget) }}</span>
            </div>
            <div class="metric-row">
              <span class="metric-lbl">Flexchange{{ flexIsPending ? ' (pending)' : '' }}</span>
              <span class="metric-val" :class="monthFlex >= 0 ? 'pos' : 'neg'">{{ formatHours(monthFlex, true) }}</span>
            </div>
          </div>
          <div class="divider-line"></div>
          <button @click="emit('navigate', 'time-tracking')" class="view-timesheet-link">View Timesheet →</button>
        </div>

        <div class="chart-box vacation-box">
          <div>
            <h2 class="box-title">Vacation overwiev</h2>
            <p class="box-subtitle">Manage your vacation</p>
          </div>
          <div class="vacation-content">
            <div class="vacation-info">
              <span class="vacation-label">Available vacation days:</span>
              <span class="vacation-value">{{ availableVacationDays }} days</span>
            </div>
            <button @click="emit('navigate', 'vacation')" class="view-timesheet-link">To vacation overwiev →</button>
          </div>
        </div>
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
  text-align: left;
}

.box-subtitle {
  font-size: 12px;
  color: #697B80;
  line-height: 23.95px;
  margin: 0;
  max-width: 150px;
  text-align: left;
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
  width: 100%;
  box-sizing: border-box;
  padding: 32px;
  overflow: hidden;
  flex-direction: column;
  justify-content: flex-start;
  align-items: flex-start;
  gap: 16px;
  display: flex;
}
.top-row {
  align-self: stretch;
  overflow: hidden;
  justify-content: space-between;
  align-items: center;
  display: inline-flex;
  text-align: left;
  margin-bottom: 8px;
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
.middle-row { flex-wrap: wrap; }
.chart-box, .summary-card, .entries-box {
  background: white;
  border-radius: 12px;
  outline: 1px #DFE7E8 solid;
  outline-offset: -1px;
  display: inline-flex;
  flex-direction: column;
}
.kpi-grid .wide { flex: 1.4 1 0; }
.chart-box { flex: 1; padding: 24px; gap: 20px; }
.summary-card { width: 360px; padding: 24px; gap: 18px; }
.summary-subrow { display: flex; justify-content: space-between; align-items: center; }
.summary-metrics { display: flex; flex-direction: column; gap: 9px; }
.metric-row { display: flex; justify-content: space-between; }
.metric-lbl { color: #697B80; font-size: 12px; }
.metric-val { color: #172D32; font-size: 13px; font-weight: 600; }
.metric-val.pos { color: #176A70; }
.metric-val.neg { color: #C0392B; }
.divider-line { height: 1px; background: #DFE7E8; width: 100%; }
.view-timesheet-link { color: #176A70; font-size: 12px; font-weight: 600; background: none; border: none; cursor: pointer; text-align: left; padding: 0; }
.right-col { width: 360px; flex: none; display: flex; flex-direction: column; gap: 16px; }
.right-col .summary-card { width: auto; }
.right-col .vacation-box { flex: none; }
.weekly-chart { min-width: 480px; }
.form-card-sub { display: flex; justify-content: space-between; align-items: center; }
</style>