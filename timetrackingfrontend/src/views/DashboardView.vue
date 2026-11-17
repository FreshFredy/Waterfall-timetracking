<script setup>
import { computed, ref, onMounted } from 'vue'
import StatCard from '../components/ui/StatCard.vue'
import StatusBadge from '../components/ui/StatusBadge.vue'
import EmployeeService from '../services/api.js'

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

// Funktion für den Button "Urlaub beantragen"
const handleRequestVacation = () => {
//TODO Urlaubsformular
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

        // 1. Flex-Konto & Arbeitszeit abrufen
        const flexResponse = await EmployeeService.getFlexAccount(currentId)
        console.log("Flex Response vom Backend:", flexResponse.data)
        
        const balanceVal = flexResponse.data.balance !== undefined && flexResponse.data.balance !== null 
            ? flexResponse.data.balance 
            : (flexResponse.data.balanceHours !== undefined && flexResponse.data.balanceHours !== null ? flexResponse.data.balanceHours : 0);
        
        flexBalance.value = `${balanceVal >= 0 ? '+' : ''}${balanceVal}:00 h`;

        workingTime.value = flexResponse.data.totalWorkingTime !== undefined && flexResponse.data.totalWorkingTime !== null
            ? flexResponse.data.totalWorkingTime 
            : "0:00 h";

        // 2. Urlaubskonto abrufen
        const vacationResponse = await EmployeeService.getVacationBalance(currentId)
        const vacData = vacationResponse.data || {}
        
        console.log("Urlaub vom Backend empfangen:", vacData)

        const annual = vacData.annualDays !== undefined && vacData.annualDays !== null ? vacData.annualDays : 30;
        const taken = vacData.takenDays !== undefined && vacData.takenDays !== null ? vacData.takenDays : 0;
        const planned = vacData.plannedDays !== undefined && vacData.plannedDays !== null ? vacData.plannedDays : 0;
        const remainingLastYear = vacData.remainingDays_last_year !== undefined && vacData.remainingDays_last_year !== null ? vacData.remainingDays_last_year : 0;

        availableVacationDays.value = vacData.remainingDays !== undefined && vacData.remainingDays !== null
            ? vacData.remainingDays 
            : ((annual + remainingLastYear) - (taken + planned));

        const sick = vacData.sickDays !== undefined && vacData.sickDays !== null ? vacData.sickDays : 0;
        sickLeave.value = `${sick} Tage`;

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
const weeklyData = ref([
  { label: '01.–04. Sep', total: '32:30', workHeight: 91, sickHeight: 0 },
  { label: '07.–11. Sep', total: '40:45', workHeight: 69.3, sickHeight: 44.8 },
  { label: '14.–18. Sep', total: '41:00', workHeight: 114.8, sickHeight: 0 },
  { label: '21.–25. Sep', total: '41:00', workHeight: 114.8, sickHeight: 0 },
  { label: '28.–30. Sep', total: '25:15', workHeight: 70.7, sickHeight: 0 },
])

const recentEntries = ref([
  { date: 'Wed, 30.09.2026', range: '08:00 – 17:00', break: '0:30 h', net: '8:30 h' },
  { date: 'Tue, 29.09.2026', range: '08:00 – 16:45', break: '0:30 h', net: '8:15 h' },
  { date: 'Mon, 28.09.2026', range: '08:00 – 17:00', break: '0:30 h', net: '8:30 h' },
])
</script>

<template>
  <div class="dashboard-container">
    <div class="top-row">
      <div class="welcome-col">
        <div class="tagline">YOUR PERSONAL DASHBOARD</div>
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
          
          <button class="btn-primary" @click="handleRequestVacation">
            🏖️ Urlaub beantragen
          </button>
        </div>
      </div>

      <div class="summary-card">
        <h2 class="box-title">30-Tage Zusammenfassung</h2>
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

    <div class="bottom-row">
      <div class="entries-box">
        <div class="entries-header">
          <h2 class="box-title">Recent entries</h2>
          <button @click="emit('navigate', 'time-tracking')" class="all-entries-btn">All entries →</button>
        </div>
        <table class="entries-table">
          <thead>
            <tr>
              <th class="th-date">DATE</th>
              <th class="th-range">TIME RANGE</th>
              <th class="th-break">BREAK</th>
              <th class="th-net">NET</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="entry in recentEntries" :key="entry.date" class="tr-row">
              <td class="td-date">{{ entry.date }}</td>
              <td class="td-range">{{ entry.range }}</td>
              <td class="td-break">{{ entry.break }}</td>
              <td class="td-net">{{ entry.net }}</td>
            </tr>
          </tbody>
        </table>
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

/* Auf untereinander (column) umgestellt, damit der Button unter den Tagen steht */
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

/* Button nimmt die volle Breite ein oder sitzt sauber darunter */
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
.date-selector-btn {
  height: 40px;
  padding: 0 14px;
  background: white;
  border-radius: 8px;
  outline: 1px #DFE7E8 solid;
  outline-offset: -1px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #172D32;
  font-size: 13px;
  font-weight: 600;
  border: none;
  cursor: pointer;
}
.alert-banner {
  align-self: stretch;
  padding: 14px 18px;
  background: #FBF3E4;
  border-radius: 8px;
  display: inline-flex;
  align-items: center;
  gap: 12px;
}
.alert-text {
  flex: 1;
  color: #946722;
  font-size: 13px;
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
.legend { display: inline-flex; gap: 18px; align-items: center; }
.legend-item { display: flex; align-items: center; gap: 6px; font-size: 11px; color: #697B80; }
.legend-dot { width: 8px; height: 8px; border-radius: 2px; }
.legend-dot.work { background: #176A70; }
.legend-dot.sick { background: #B6A5CF; }
.chart-area { height: 183px; display: inline-flex; align-items: flex-end; gap: 14px; }
.yaxis { width: 30px; height: 166px; padding-bottom: 25px; display: flex; flex-direction: column; justify-content: space-between; color: #91A0A4; font-size: 10px; }
.bar-column { flex: 1; height: 183px; display: inline-flex; flex-direction: column; justify-content: flex-end; align-items: center; gap: 8px; }
.bar-val { color: #172D32; font-size: 12px; font-weight: 600; }
.bar-wrapper { width: 58px; border-top-left-radius: 5px; border-top-right-radius: 5px; display: flex; flex-direction: column; overflow: hidden; }
.bar-sick { width: 100%; background: #B6A5CF; }
.bar-work { width: 100%; background: #176A70; }
.bar-label { color: #697B80; font-size: 10px; }
.summary-card { width: 360px; padding: 24px; gap: 18px; }
.summary-subrow { display: flex; justify-content: space-between; align-items: center; }
.summary-month { color: #172D32; font-size: 14px; font-weight: 600; }
.summary-metrics { display: flex; flex-direction: column; gap: 9px; }
.metric-row { display: flex; justify-content: space-between; }
.metric-lbl { color: #697B80; font-size: 12px; }
.metric-val { color: #172D32; font-size: 13px; font-weight: 600; }
.metric-val.highlight { color: #176A70; }
.divider-line { height: 1px; background: #DFE7E8; width: 100%; }
.submission-note { display: flex; align-items: flex-start; gap: 10px; }
.sub-time { color: #172D32; font-size: 12px; font-weight: 500; }
.sub-next { color: #697B80; font-size: 11px; }
.view-timesheet-link, .all-entries-btn { color: #176A70; font-size: 12px; font-weight: 600; background: none; border: none; cursor: pointer; text-align: left; padding: 0; }
.entries-box { flex: 1; padding: 24px; gap: 20px; }
.entries-header { display: flex; justify-content: space-between; align-items: center; }
.entries-table { width: 100%; border-collapse: collapse; text-align: left; }
.th-date { width: 140px; color: #697B80; font-size: 10px; padding-bottom: 8px; }
.th-range { width: 200px; color: #697B80; font-size: 10px; padding-bottom: 8px; }
.th-break { width: 125px; color: #697B80; font-size: 10px; padding-bottom: 8px; }
.th-net { color: #697B80; font-size: 10px; padding-bottom: 8px; }
.tr-row { height: 43px; border-top: 1px #DFE7E8 solid; }
.td-date { color: #172D32; font-size: 12px; }
.td-range, .td-break { color: #697B80; font-size: 12px; }
.td-net { color: #172D32; font-size: 12px; font-weight: 600; }
.today-cta { width: 360px; padding: 24px; background: #E8F3F2; border-radius: 12px; display: inline-flex; flex-direction: column; justify-content: space-between; gap: 15px; }
.cta-top { display: flex; flex-direction: column; gap: 12px; }
.cta-badge { display: inline-flex; align-items: center; gap: 8px; color: #176A70; font-size: 11px; font-weight: 600; }
.cta-title { color: #172D32; font-size: 21px; font-weight: 600; line-height: 27.30px; }
.cta-desc { color: #697B80; font-size: 12px; line-height: 17.40px; }
.cta-button { height: 40px; padding: 0 14px; background: #176A70; border-radius: 8px; border: none; color: white; font-size: 13px; font-weight: 600; display: flex; justify-content: center; align-items: center; gap: 8px; cursor: pointer; }
.cta-button:hover { background: #125458; }
</style>