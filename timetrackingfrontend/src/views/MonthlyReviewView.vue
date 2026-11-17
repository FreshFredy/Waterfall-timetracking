<script setup>
import { ref } from 'vue'
import StatCard from '../components/ui/StatCard.vue'
import StatusBadge from '../components/ui/StatusBadge.vue'

const declineReason = ref('')
const status = ref('Pending')

const approveMonth = () => { status.value = 'Approved' }
const declineMonth = () => {
  if (!declineReason.value.trim()) { alert('Please provide a reason for declining.'); return; }
  status.value = 'Declined'
}
</script>

<template>
  <div class="mr-container">
    <div class="mr-top-bar">
      <button class="back-link"><span>←</span><span>All pending Timesheets</span></button>
      <span class="mr-awaiting">3 months awaiting review</span>
    </div>

    <div class="mr-header">
      <div class="tagline">MONTHLY REVIEW · SEPTEMBER 2026</div>
      <h1 class="mr-main-title">Review carefully. Decide clearly.</h1>
      <p class="mr-main-desc">Review Markus’s Timesheet and approve the month or decline it with a reason.</p>
    </div>

    <div class="employee-banner">
      <div class="emp-avatar">MM</div>
      <div class="emp-details">
        <div class="emp-name">Markus Müller</div>
        <div class="emp-sub">Project Manager · Employee ID 0142</div>
      </div>
      <div class="emp-model">
        <div class="model-lbl">Working model</div>
        <div class="model-val">40 h / Week · Mon–Fri</div>
      </div>
      <div class="emp-status-col">
        <StatusBadge :variant="status === 'Approved' ? 'success' : 'pending'" :label="status" />
        <span class="submitted-date">Submitted 01.10., 08:12</span>
      </div>
    </div>

    <div class="kpi-grid">
      <StatCard title="Net working time" value="164:30 h" subtitle="174:30 h on Site − 10:00 h break" />
      <StatCard title="Sickleave credit" value="16:00 h" subtitle="2 days sickleave × 8:00 h" />
      <StatCard title="Monthly target" value="176:00 h" subtitle="22 weekdays × 8:00 h" />
      <StatCard title="Flexchange September" value="+4:30 h" subtitle="180:30 h creditable − 176:00 h target" highlighted />
    </div>

    <div class="mr-content-split">
      <div class="mr-left-col">
        <div class="complete-alert">
          <span>✓</span><span>Complete: All 22 weekdays recorded. No overlapping entries; net working time excludes breaks.</span>
        </div>
        <div class="info-card-box">
          <h2 class="box-title">Daily details</h2>
          <p class="box-subtitle">September 2026 · 22 weekdays · weekends hidden</p>
          <p class="info-footer-txt">Net = End − Start − Break. Sickleave credits 8:00 h per weekday, not working time.</p>
        </div>
      </div>

      <div class="mr-right-col">
        <div class="decision-card">
          <div>
            <h2 class="box-title">Your decision</h2>
            <p class="box-subtitle">As team lead · Sabine Weber</p>
          </div>

          <div class="calculation-recap">
            <div class="calc-row"><span class="c-lbl">Working time</span><span class="c-val">164:30 h</span></div>
            <div class="calc-row"><span class="c-lbl">+ Sickleave credit</span><span class="c-val">16:00 h</span></div>
            <div class="calc-row"><span class="c-lbl">= Creditable time</span><span class="c-val">180:30 h</span></div>
            <div class="calc-row"><span class="c-lbl">− Monthly target</span><span class="c-val">176:00 h</span></div>
            <div class="calc-row highlight"><span>= Flexchange</span><span>+4:30 h</span></div>
          </div>

          <button @click="approveMonth" class="approve-btn"><span>✓</span><span>Approve month</span></button>
          <p class="approve-note">Approval confirms +4:30 h. Markus’s approved flex balance rises to +12:30 h.</p>

          <div class="or-divider">
            <div class="line"></div><span class="or-txt">OR RETURN FOR CORRECTION</span><div class="line"></div>
          </div>

          <label class="input-label">
            Reason for decline *
            <textarea v-model="declineReason" rows="4" placeholder="What should Markus correct?" class="textarea-input"></textarea>
          </label>
          <div class="decline-hint">* Required only when declining. The reason will be shared with Markus.</div>

          <button @click="declineMonth" class="decline-btn"><span>✕</span><span>Decline with reason</span></button>
        </div>

        <div class="history-card">
          <h3 class="box-title">History</h3>
          <div class="hist-item"><div class="hist-title">Month submitted</div><div class="hist-sub">Markus Müller · 01.10.2026, 08:12</div></div>
          <div class="hist-item"><div class="hist-title">Last entry saved</div><div class="hist-sub">30.09.2026, 17:03</div></div>
          <div class="hist-item"><div class="hist-title">Approval pending</div><div class="hist-sub">Assigned to: Sabine Weber</div></div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.mr-container { padding: 32px; display: flex; flex-direction: column; gap: 24px; }
.mr-top-bar { display: flex; justify-content: space-between; align-items: center; width: 100%; }
.back-link { color: #176A70; font-size: 12px; font-weight: 500; background: none; border: none; cursor: pointer; display: flex; align-items: center; gap: 6px; }
.mr-awaiting { color: #697B80; font-size: 11px; }
.mr-header { display: flex; flex-direction: column; gap: 7px; }
.tagline { color: #697B80; font-size: 11px; font-weight: 600; }
.mr-main-title { color: #172D32; font-size: 30px; font-weight: 600; line-height: 36px; }
.mr-main-desc { color: #697B80; font-size: 13px; line-height: 18.85px; }
.employee-banner { padding: 20px; background: white; border-radius: 12px; outline: 1px #DFE7E8 solid; display: flex; align-items: center; gap: 16px; }
.emp-avatar { width: 48px; height: 48px; background: #E8F3F2; border-radius: 100px; display: flex; justify-content: center; align-items: center; color: #176A70; font-size: 18px; font-weight: 600; }
.emp-details { flex: 1; display: flex; flex-direction: column; gap: 4px; }
.emp-name { color: #172D32; font-size: 16px; font-weight: 600; }
.emp-sub { color: #697B80; font-size: 12px; }
.emp-model { display: flex; flex-direction: column; gap: 4px; }
.model-lbl { color: #697B80; font-size: 11px; }
.model-val { color: #172D32; font-size: 12px; font-weight: 500; }
.emp-status-col { display: flex; flex-direction: column; align-items: flex-end; gap: 6px; }
.submitted-date { color: #697B80; font-size: 11px; }
.kpi-grid, .mr-content-split { display: flex; gap: 16px; }
.mr-content-split { gap: 24px; }
.mr-left-col { flex: 1; display: flex; flex-direction: column; gap: 16px; }
.complete-alert { padding: 13px; background: #E8F3F2; border-radius: 8px; display: flex; align-items: center; gap: 9px; color: #176A70; font-size: 11px; }
.info-card-box { padding: 20px; background: white; border-radius: 12px; outline: 1px #DFE7E8 solid; display: flex; flex-direction: column; gap: 8px; }
.box-title { color: #172D32; font-size: 16px; font-weight: 600; }
.box-subtitle { color: #697B80; font-size: 12px; }
.info-footer-txt { color: #697B80; font-size: 11px; }
.mr-right-col { width: 320px; display: flex; flex-direction: column; gap: 20px; }
.decision-card, .history-card { padding: 24px; background: white; border-radius: 12px; outline: 1px #DFE7E8 solid; display: flex; flex-direction: column; gap: 18px; }
.calculation-recap { padding: 14px; background: #F8FAFA; border-radius: 8px; display: flex; flex-direction: column; gap: 10px; }
.calc-row { display: flex; justify-content: space-between; color: #697B80; font-size: 11px; }
.calc-row .c-val { color: #172D32; font-weight: 600; font-size: 12px; }
.calc-row.highlight { color: #176A70; font-weight: 600; font-size: 12px; }
.approve-btn { height: 40px; padding: 0 14px; background: #176A70; border-radius: 8px; color: white; font-size: 13px; font-weight: 600; display: flex; justify-content: center; align-items: center; gap: 8px; border: none; cursor: pointer; }
.approve-note, .decline-hint { color: #697B80; font-size: 11px; }
.or-divider { display: flex; align-items: center; gap: 10px; }
.or-divider .line { flex: 1; height: 1px; background: #DFE7E8; }
.or-txt { color: #91A0A4; font-size: 9px; font-weight: 500; }
.input-label { display: flex; flex-direction: column; gap: 7px; color: #172D32; font-size: 12px; font-weight: 500; }
.textarea-input { padding: 12px; background: white; border-radius: 8px; outline: 1px #DFE7E8 solid; font-size: 13px; border: none; resize: vertical; }
.decline-btn { height: 40px; padding: 0 14px; background: white; border-radius: 8px; outline: 1px #AC4E49 solid; color: #AC4E49; font-size: 13px; font-weight: 600; display: flex; justify-content: center; align-items: center; gap: 8px; border: none; cursor: pointer; }
.hist-item { display: flex; flex-direction: column; gap: 3px; }
.hist-title { color: #172D32; font-size: 12px; font-weight: 500; }
.hist-sub { color: #697B80; font-size: 10px; }
</style>