<script setup>
import { ref, computed } from 'vue'
import StatusBadge from '../components/ui/StatusBadge.vue'

const form = ref({ employeeId: '0142', startDate: '2026-10-01', endDate: '2026-10-02', fileName: '' })
const handleFileUpload = (e) => { const file = e.target.files?.[0]; if (file) form.value.fileName = file.name; }

const previewDays = computed(() => {
  const start = new Date(form.value.startDate)
  const end = new Date(form.value.endDate)
  if (isNaN(start) || isNaN(end) || end < start) return []
  const days = []
  const curr = new Date(start)
  while (curr <= end) {
    const dayOfWeek = curr.getDay()
    if (dayOfWeek !== 0 && dayOfWeek !== 6) {
      days.push(curr.toLocaleDateString('en-GB', { weekday: 'short', day: '2-digit', month: '2-digit', year: 'numeric' }))
    }
    curr.setDate(curr.getDate() + 1)
  }
  return days
})

const totalCreditHours = computed(() => `${previewDays.value.length * 8}:00 h`)
</script>

<template>
  <div class="sl-container">
    <div class="sl-header">
      <span class="tagline">HUMAN RESOURCES</span>
      <h1 class="sl-title">Sickleave</h1>
      <p class="sl-desc">Krankmeldung eines Mitarbeiters erstellen</p>
    </div>

    <div class="sl-split">
      <div class="record-form-card">
        <div class="form-card-header">
          <h2 class="box-title">Record Sickleave</h2>
          <StatusBadge variant="neutral" label="Draft" />
        </div>
        <p class="form-desc">ID</p>

        <div class="input-label">
          Employee
          <div class="emp-selector-box">
            <div class="avatar-sm">MM</div>
            <div class="emp-sel-info">
              <div class="emp-sel-name">Markus Müller</div>
              <div class="emp-sel-id">Employee ID 0142</div>
            </div>
            <span class="dropdown-arrow">⌄</span>
          </div>
        </div>

        <div class="time-inputs-row">
          <label class="input-label flex-1">
            Start date
            <input v-model="form.startDate" type="date" class="text-input" />
          </label>
          <label class="input-label flex-1">
            End date
            <input v-model="form.endDate" type="date" class="text-input" />
          </label>
        </div>
        <p class="note-txt">Start and end dates are included.</p>

        <div class="input-label">
          Doctors note
          <label class="upload-label">
            <span class="upload-txt">{{ form.fileName || 'Upload file' }}</span>
            <span>↑</span>
            <input type="file" class="hidden-file" @change="handleFileUpload" />
          </label>
        </div>

        <div class="divider-line"></div>
        <button class="submit-btn"><span>✓</span><span>Submit</span></button>
        <div class="form-footer-note">Submit records the absence and credits October.</div>
      </div>

      <div class="preview-card">
        <div class="preview-top-content">
          <div class="preview-header-texts">
            <h2 class="box-title">Sickleave preview</h2>
            <p class="box-subtitle">01–02 October 2026 · Not recorded yet</p>
          </div>

          <div class="preview-kpi-row">
            <div class="preview-kpi-box light">
              <span class="pkpi-lbl">Working days</span>
              <span class="pkpi-val">{{ previewDays.length }} days</span>
              <span class="pkpi-sub">{{ previewDays.length }} days × 8:00 h</span>
            </div>
            <div class="preview-kpi-box dark">
              <span class="pkpi-lbl light-text">Sickleave credit</span>
              <span class="pkpi-val light-text">{{ totalCreditHours }}</span>
              <span class="pkpi-sub light-text">Will be credited to October</span>
            </div>
          </div>

          <div>
            <div class="model-meta-title">Working model · Markus Müller</div>
            <div class="model-meta-val">40 h / Week Monday - Friday · 8:00 h / Day</div>
          </div>
        </div>

        <div>
          <div class="preview-table-header">
            <span class="pth-date">Date</span>
            <span class="pth-hours">Scheduled hours</span>
            <span class="pth-credit">Credit</span>
          </div>
          <div v-for="day in previewDays" :key="day" class="preview-table-row">
            <span class="ptr-date">{{ day }}</span>
            <span class="ptr-hours">8:00 h</span>
            <span class="ptr-credit">8:00 h</span>
          </div>
          <div class="preview-info-box">ⓘ Credit is based on the employee’s work schedule, not working time.</div>
        </div>
      </div>
    </div>

    <div class="history-table-card">
      <div class="hist-table-header">
        <div>
          <h3 class="box-title">Recorded Sickleave</h3>
          <p class="box-subtitle">Markus Müller · September 2026</p>
        </div>
        <span class="box-subtitle">Previously recorded · Draft not included</span>
      </div>

      <table class="data-table">
        <thead>
          <tr class="th-row">
            <th class="th-date-l">Date</th>
            <th class="th-type-l">Type</th>
            <th class="th-credit-l">Credit</th>
            <th class="th-status-l">Status</th>
          </tr>
        </thead>
        <tbody>
          <tr class="data-tr">
            <td class="td-date-l">Thu, 10.09.2026</td>
            <td class="td-type-l">Sickleave</td>
            <td class="td-credit-l">8:00 h</td>
            <td><StatusBadge variant="success" label="Recorded" /></td>
          </tr>
          <tr class="data-tr">
            <td class="td-date-l">Fri, 11.09.2026</td>
            <td class="td-type-l">Sickleave</td>
            <td class="td-credit-l">8:00 h</td>
            <td><StatusBadge variant="success" label="Recorded" /></td>
          </tr>
        </tbody>
        <tfoot>
          <tr class="tfoot-row-l">
            <td class="td-date-l">September total</td>
            <td class="td-type-l">2 working days</td>
            <td class="td-credit-l">16:00 h</td>
            <td class="td-status-l">Credited</td>
          </tr>
        </tfoot>
      </table>
    </div>
  </div>
</template>

<style scoped>
.sl-container { padding: 32px; display: flex; flex-direction: column; gap: 24px; }
.sl-header { display: flex; flex-direction: column; gap: 7px; }
.tagline { color: #697B80; font-size: 11px; font-weight: 600; letter-spacing: 1.20px; }
.sl-title { color: #172D32; font-size: 30px; font-weight: 600; line-height: 36px; }
.sl-desc { color: #697B80; font-size: 13px; line-height: 18.85px; }
.sl-split { display: flex; gap: 24px; }
.record-form-card { width: 432px; padding: 24px; background: white; border-radius: 12px; outline: 1px #DFE7E8 solid; display: flex; flex-direction: column; gap: 18px; }
.form-card-header { display: flex; justify-content: space-between; align-items: center; }
.box-title { color: #172D32; font-size: 16px; font-weight: 600; }
.box-subtitle { color: #697B80; font-size: 12px; }
.form-desc { color: #697B80; font-size: 12px; }
.input-label { display: flex; flex-direction: column; gap: 7px; color: #172D32; font-size: 12px; }
.emp-selector-box { height: 64px; padding: 0 12px; border-radius: 8px; outline: 1px #DFE7E8 solid; display: flex; align-items: center; gap: 10px; }
.avatar-sm { width: 32px; height: 32px; background: #E8F3F2; border-radius: 100px; display: flex; justify-content: center; align-items: center; color: #176A70; font-size: 12px; font-weight: 600; }
.emp-sel-info { flex: 1; display: flex; flex-direction: column; gap: 3px; }
.emp-sel-name { color: #172D32; font-size: 13px; font-weight: 500; }
.emp-sel-id { color: #697B80; font-size: 11px; }
.dropdown-arrow { color: #697B80; }
.time-inputs-row { display: flex; gap: 12px; }
.text-input { height: 43px; padding: 0 12px; border-radius: 8px; outline: 1px #DFE7E8 solid; font-size: 13px; border: none; }
.note-txt { color: #697B80; font-size: 11px; }
.upload-label { height: 43px; padding: 0 12px; border-radius: 8px; outline: 1px #DFE7E8 solid; display: flex; justify-content: space-between; align-items: center; cursor: pointer; color: #697B80; }
.upload-txt { color: #172D32; font-size: 13px; }
.hidden-file { display: none; }
.divider-line { height: 1px; background: #DFE7E8; width: 100%; }
.submit-btn { height: 40px; padding: 0 14px; background: #176A70; border-radius: 8px; color: white; font-size: 13px; font-weight: 600; display: flex; justify-content: center; align-items: center; gap: 8px; border: none; cursor: pointer; }
.form-footer-note { color: #697B80; font-size: 10px; }
.preview-card { flex: 1; background: white; border-radius: 12px; outline: 1px #DFE7E8 solid; display: flex; flex-direction: column; justify-content: space-between; overflow: hidden; }
.preview-top-content { padding: 24px; display: flex; flex-direction: column; gap: 18px; }
.preview-header-texts { display: flex; flex-direction: column; gap: 4px; }
.preview-kpi-row { display: flex; gap: 16px; }
.preview-kpi-box { flex: 1; padding: 16px; border-radius: 8px; display: flex; flex-direction: column; gap: 8px; }
.preview-kpi-box.light { background: #F8FAFA; }
.preview-kpi-box.dark { background: #11545A; color: white; }
.pkpi-lbl { color: #697B80; font-size: 12px; }
.pkpi-val { color: #172D32; font-size: 28px; font-weight: 600; }
.pkpi-sub { color: #697B80; font-size: 11px; }
.light-text { color: white !important; }
.model-meta-title { color: #697B80; font-size: 11px; }
.model-meta-val { color: #172D32; font-size: 12px; font-weight: 500; }
.preview-table-header { height: 36px; padding: 0 24px; background: #F8FAFA; border-top: 1px #DFE7E8 solid; border-bottom: 1px #DFE7E8 solid; display: flex; align-items: center; color: #697B80; font-size: 10px; font-weight: 600; }
.pth-date { flex: 1; }
.pth-hours { width: 160px; }
.pth-credit { width: 100px; }
.preview-table-row { height: 40px; padding: 0 24px; background: #F9F7FC; border-bottom: 1px #DFE7E8 solid; display: flex; align-items: center; font-size: 12px; }
.ptr-date { flex: 1; color: #172D32; }
.ptr-hours { width: 160px; color: #697B80; }
.ptr-credit { width: 100px; color: #72608B; font-weight: 600; }
.preview-info-box { padding: 16px 24px; color: #697B80; font-size: 11px; }
.history-table-card { background: white; border-radius: 12px; outline: 1px #DFE7E8 solid; overflow: hidden; }
.hist-table-header { padding: 20px 24px; display: flex; justify-content: space-between; align-items: center; }
.data-table { width: 100%; border-collapse: collapse; text-align: left; }
.th-row { height: 36px; background: #F8FAFA; border-top: 1px #DFE7E8 solid; border-bottom: 1px #DFE7E8 solid; color: #697B80; font-size: 10px; font-weight: 600; }
.th-date-l { padding-left: 24px; }
.th-type-l { width: 240px; }
.th-credit-l { width: 200px; }
.th-status-l { width: 144px; }
.data-tr { height: 44px; border-bottom: 1px #DFE7E8 solid; font-size: 12px; }
.td-date-l { padding-left: 24px; color: #172D32; }
.td-type-l { color: #72608B; }
.td-credit-l { color: #172D32; font-weight: 600; }
.tfoot-row-l { height: 48px; background: #F8FAFA; font-size: 12px; }
.td-status-l { color: #697B80; font-size: 11px; }
</style>