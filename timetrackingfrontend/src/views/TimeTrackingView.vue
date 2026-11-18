<script setup>
import { ref, computed } from 'vue'
import StatCard from '../components/ui/StatCard.vue'
import StatusBadge from '../components/ui/StatusBadge.vue'

const form = ref({
  date: '2026-10-01',
  start: '08:30',
  end: '17:00',
  breakMinutes: 30,
  project: 'Client A',
})

const projects = ['Client A', 'Client B', 'Internal']

const timeStats = computed(() => {
  if (!form.value.start || !form.value.end) return { onSite: '0:00 h', breakStr: '0:00 h', net: '0:00 h' }
  const [sh, sm] = form.value.start.split(':').map(Number)
  const [eh, em] = form.value.end.split(':').map(Number)
  const totalMinutes = Math.max(0, eh * 60 + em - (sh * 60 + sm))
  const breakMins = Number(form.value.breakMinutes) || 0
  const netMinutes = Math.max(0, totalMinutes - breakMins)

  const format = (mins) => {
    const h = Math.floor(mins / 60)
    const m = String(mins % 60).padStart(2, '0')
    return `${h}:${m} h`
  }

  return {
    onSite: format(totalMinutes),
    breakStr: format(breakMins),
    net: format(netMinutes),
  }
})

const dailyEntries = ref([
  { date: 'Tue, 01.09.', start: '08:00', end: '17:00', break: '0:30', net: '8:30', credit: '0:00', type: 'Working time' },
  { date: 'Wed, 02.09.', start: '08:30', end: '17:00', break: '0:30', net: '8:00', credit: '0:00', type: 'Working time' },
  { date: 'Thu, 03.09.', start: '08:00', end: '16:45', break: '0:30', net: '8:15', credit: '0:00', type: 'Working time' },
  { date: 'Fri, 04.09.', start: '08:15', end: '16:30', break: '0:30', net: '7:45', credit: '0:00', type: 'Working time' },
  { date: 'Thu, 10.09.', start: '–', end: '–', break: '–', net: '0:00', credit: '8:00', type: 'Sickleave' },
  { date: 'Fri, 11.09.', start: '–', end: '–', break: '–', net: '0:00', credit: '8:00', type: 'Sickleave' },
])

const handleAddEntry = () => {
  alert(`Saved: ${form.value.date} (${timeStats.value.net}) for ${form.value.project}`)
}
</script>

<template>
  <div class="tt-container">
    <div class="tt-header-row">
      <h1 class="tt-title">Zeiterfassung</h1>
      <div class="tt-actions">
        <button class="disabled-submit-btn" disabled><span>Submit Timesheet</span></button>

      </div>
    </div>

    <div class="tt-subbar">

      <div class="submitted-count">

      </div>
    </div>


    <div class="content-split">
      <div class="table-card">
        <div class="table-card-header">
          <div>
            <h2 class="box-title">Timesheet</h2>
          </div>
          <div class="badge-row">

          </div>
        </div>

        <table class="data-table">
          <thead>
            <tr class="th-row">
              <th class="th-date">Datum</th>
              <th class="th-start">Start</th>
              <th class="th-end">Ende</th>
              <th class="th-break">Pause</th>
              <th class="th-net">Arbeitszeit</th>
            </tr>
          </thead>
          
        </table>
      </div>

      <div class="right-sidebar">
        <form @submit.prevent="handleAddEntry" class="form-card">
          <div class="form-card-header">
            <h2 class="box-title">Time tracking</h2>
            <StatusBadge variant="neutral" label="Draft" />
          </div>
          <label class="input-label">
            Datum
            <input v-model="form.date" type="date" class="text-input" />
          </label>

          <div class="time-inputs-row">
            <label class="input-label flex-1">
              Start
              <input v-model="form.start" type="time" class="text-input" />
            </label>
            <label class="input-label flex-1">
              Ende
              <input v-model="form.end" type="time" class="text-input" />
            </label>
          </div>

          <label class="input-label">
            Pausenzeit in Minuten
            <input v-model.number="form.breakMinutes" type="number" min="0" step="5" class="text-input" />
          </label>

          <div class="calc-box">
            <div class="calc-header">
              <span class="calc-val">Arbeitszeit =  {{ timeStats.net }}</span>
            </div>
            <div class="calc-sub">Komplette Zeit  -  Pausen =  Arbeitszeit</div>
          </div>

          <button type="submit" class="submit-btn">
            <span>✓</span><span>Submit</span>
          </button>
        </form>

      </div>
    </div>
  </div>
</template>

<style scoped>
.text-input, .select-input, input[type="date"], input[type="time"], input[type="number"] {
  background-color: #FFFFFF !important;
  color: #000000 !important;
  color-scheme: light; /* Hält auch das Uhr-/Kalender-Icon dunkel */
}

/* Platzhalter-Farbe etwas dezenter machen */
.text-input::placeholder {
  color: #697B80;
}
.input-label { 
  display: flex; 
  flex-direction: column; 
  gap: 7px; 
  color: #172D32; 
  font-size: 12px; 
}
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
  color: #172D32; /* Erzwingt schwarze/dunkle Schrift auf dem weißen Hintergrund */
}
.calc-box {
  background: #F8FAFA;
  padding: 12px;
  border-radius: 8px;
  color: #172D32;
}

.calc-sub {
  color: #697B80; /* Etwas heller für den Untertitel */
  font-size: 11px;
}
.tt-container { padding: 32px; display: flex; flex-direction: column; gap: 24px; }
.tt-header-row { height: 143px; display: flex; align-items: center; gap: 24px; }
.tt-title { flex: 1; color: #172D32; font-size: 30px; font-weight: 600; line-height: 36px; }
.tt-actions { display: flex; align-items: center; gap: 12px; }
.disabled-submit-btn { height: 40px; padding: 0 14px; background: #B3B3B3; border-radius: 8px; color: white; font-size: 13px; font-weight: 600; display: flex; align-items: center; border: none; cursor: not-allowed; }
.tt-subbar { display: flex; justify-content: space-between; align-items: center; }
.month-switcher { display: flex; align-items: center; gap: 8px; }
.switcher-arrow { height: 34px; padding: 0 14px; background: white; border-radius: 8px; outline: 1px #DFE7E8 solid; border: none; cursor: pointer; color: #697B80; font-weight: 600; }
.current-month-box { height: 40px; padding: 0 14px; background: white; border-radius: 8px; outline: 1px #DFE7E8 solid; display: flex; align-items: center; gap: 8px; color: #172D32; font-size: 13px; font-weight: 600; }
.submitted-count { display: flex; align-items: center; gap: 7px; color: #697B80; font-size: 12px; }
.check-icon { color: #176A70; font-weight: bold; }
.kpi-grid, .content-split { display: flex; gap: 16px; }
.content-split { gap: 24px; }
.table-card { flex: 1; background: white; border-radius: 12px; outline: 1px #DFE7E8 solid; display: flex; flex-direction: column; overflow: hidden; }
.table-card-header { padding: 20px; display: flex; flex-direction: column; gap: 16px; }
.box-title { color: #172D32; font-size: 16px; font-weight: 600; }
.box-subtitle { color: #697B80; font-size: 12px; }
.badge-row { display: flex; gap: 16px; }
.data-table { width: 100%; border-collapse: collapse; text-align: left; }
.th-row { height: 36px; background: #F8FAFA; border-top: 1px #DFE7E8 solid; border-bottom: 1px #DFE7E8 solid; color: #697B80; font-size: 10px; font-weight: 600; }
.th-date { padding-left: 20px; width: 136px; }
.th-start, .th-end, .th-break, .th-net { width: 84px; }
.th-credit { width: 104px; }
.data-tr { height: 33px; border-bottom: 1px #DFE7E8 solid; font-size: 12px; }
.td-date { padding-left: 20px; }
.td-net-val { font-weight: 600; }
.tfoot-row { height: 52px; background: #F8FAFA; color: #172D32; font-size: 12px; font-weight: 600; }
.right-sidebar { width: 320px; display: flex; flex-direction: column; gap: 20px; }
.form-card, .month-glance-card { padding: 24px; background: white; border-radius: 12px; outline: 1px #DFE7E8 solid; display: flex; flex-direction: column; gap: 18px; }
.form-card-header { display: flex; justify-content: space-between; align-items: center; }
.form-desc { color: #697B80; font-size: 12px; }
.input-label { display: flex; flex-direction: column; gap: 7px; color: #172D32; font-size: 12px; font-weight: 500; }
.text-input, .select-input { height: 42px; padding: 12px; background: white; border-radius: 8px; outline: 1px #DFE7E8 solid; font-size: 13px; border: none; }
.time-inputs-row { display: flex; gap: 12px; }
.calc-box { padding: 16px; background: #E8F3F2; border-radius: 8px; display: flex; flex-direction: column; gap: 8px; }
.calc-header { display: flex; justify-content: space-between; color: #176A70; font-size: 12px; font-weight: 500; }
.calc-val { font-size: 18px; font-weight: 600; }
.calc-sub { color: #176A70; font-size: 10px; }
.submit-btn { height: 40px; padding: 0 14px; background: #176A70; border-radius: 8px; color: white; font-size: 13px; font-weight: 600; display: flex; justify-content: center; align-items: center; gap: 8px; border: none; cursor: pointer; }
.form-footer-note { color: #697B80; font-size: 10px; }
.glance-row { display: flex; justify-content: space-between; }
.glance-lbl { color: #697B80; font-size: 12px; }
.glance-val { color: #172D32; font-size: 12px; font-weight: 500; }
.glance-footer { color: #697B80; font-size: 11px; }
.divider-line { height: 1px; background: #DFE7E8; width: 100%; }
</style>