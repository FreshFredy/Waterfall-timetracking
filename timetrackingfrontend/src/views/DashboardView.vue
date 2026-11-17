<script setup>
import { ref } from 'vue'
import StatCard from '../components/ui/StatCard.vue'
import StatusBadge from '../components/ui/StatusBadge.vue'

const emit = defineEmits(['navigate'])

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
        <h1 class="greeting">Good morning, Markus.</h1>
        <p class="sub-greeting">Your time at a glance. Here is your September recap.</p>
      </div>
      <button class="date-selector-btn">
        <span>📅</span>
        <span>September 2026</span>
      </button>
    </div>

    <div class="alert-banner">
      <span>⏳</span>
      <p class="alert-text">
        <strong>September submitted.</strong> Your monthly timesheet is awaiting review by Sabine Weber.
      </p>
      <StatusBadge variant="pending" label="Pending" />
    </div>

    <div class="kpi-grid">
      <StatCard title="Working time in September" value="164:30 h" subtitle="20 days · after all breaks" />
      <StatCard title="Your flex balance" value="+12:30 h" subtitle="Includes +4:30 h pending" highlighted />
      <StatCard title="Sickleave in September" value="2 days" subtitle="16:00 h credit · 5 days in 2026" />
      <StatCard title="Creditable time" value="180:30 h" subtitle="164:30 h work + 16:00 h sickleave" />
    </div>

    <div class="middle-row">
      <div class="chart-box">
        <div>
          <h2 class="box-title">A month in good rhythm.</h2>
          <p class="box-subtitle">Weekly creditable time · September 2026</p>
        </div>
        <div class="legend">
          <div class="legend-item"><span class="legend-dot work"></span><span>Working time</span></div>
          <div class="legend-item"><span class="legend-dot sick"></span><span>Sickleave credit</span></div>
        </div>
        <div class="chart-area">
          <div class="yaxis">
            <span>48 h</span><span>32 h</span><span>16 h</span><span>0 h</span>
          </div>
          <div v-for="week in weeklyData" :key="week.label" class="bar-column">
            <span class="bar-val">{{ week.total }}</span>
            <div class="bar-wrapper">
              <div v-if="week.sickHeight > 0" :style="{ height: `${week.sickHeight}px` }" class="bar-sick"></div>
              <div :style="{ height: `${week.workHeight}px` }" class="bar-work"></div>
            </div>
            <span class="bar-label">{{ week.label }}</span>
          </div>
        </div>
      </div>

      <div class="summary-card">
        <h2 class="box-title">Your monthly summary</h2>
        <div class="summary-subrow">
          <span class="summary-month">September 2026</span>
          <StatusBadge variant="pending" label="Pending" />
        </div>
        <div class="summary-metrics">
          <div class="metric-row"><span class="metric-lbl">Creditable time</span><span class="metric-val">180:30 h</span></div>
          <div class="metric-row"><span class="metric-lbl">Target · 22 × 8:00 h</span><span class="metric-val">176:00 h</span></div>
          <div class="metric-row"><span class="metric-lbl">Flexchange</span><span class="metric-val highlight">+4:30 h</span></div>
        </div>
        <div class="divider-line"></div>
        <div class="submission-note">
          <span>🕒</span>
          <div>
            <div class="sub-time">Submitted 01.10., 08:12</div>
            <div class="sub-next">Next: approval by Sabine Weber.</div>
          </div>
        </div>
        <button @click="emit('navigate', 'time-tracking')" class="view-timesheet-link">View Timesheet →</button>
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

      <div class="today-cta">
        <div class="cta-top">
          <div class="cta-badge"><span>📅</span><span>TODAY · 01 OCTOBER</span></div>
          <h3 class="cta-title">A new month begins.</h3>
          <p class="cta-desc">No time saved for today yet. Add your first entry.</p>
        </div>
        <button @click="emit('navigate', 'time-tracking')" class="cta-button">
          <span>+</span><span>Time Tracking</span>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
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
.box-title { color: #172D32; font-size: 16px; font-weight: 600; line-height: 23.20px; }
.box-subtitle { color: #697B80; font-size: 12px; line-height: 17.40px; }
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
</style>