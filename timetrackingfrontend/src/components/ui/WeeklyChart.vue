<script setup>
import { computed } from 'vue'

const props = defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, default: '' },
  // [{ label: '02.–06. Nov', work: 32.5, sick: 8, target: 40 }], values in hours
  weeks: { type: Array, default: () => [] },
})

const total = (w) => w.work + w.sick
const over = (w) => total(w) - (w.target || 0)          // > 0 above target, < 0 below
const cols = computed(() => Math.max(props.weeks.length, 1))

const axisMax = computed(() => {
  const max = Math.max(0, ...props.weeks.map((w) => Math.max(total(w), w.target || 0)))
  return Math.max(48, Math.ceil(max / 16) * 16)
})
const ticks = computed(() => {
  const list = []
  for (let t = 0; t <= axisMax.value; t += 16) list.push(t)
  return list
})

const pct = (hours) => `${(hours / axisMax.value) * 100}%`
const fmt = (hours) => {
  const minutes = Math.round(hours * 60)
  return `${Math.floor(minutes / 60)}:${String(minutes % 60).padStart(2, '0')}`
}
</script>

<template>
  <div class="weekly-chart">
    <h2 class="chart-title">{{ title }}</h2>
    <p class="chart-subtitle">{{ subtitle }}</p>

    <div class="legend">
      <span class="legend-item"><span class="dot work"></span>Working time</span>
      <span class="legend-item"><span class="dot sick"></span>Sickleave credit</span>
      <span class="legend-item"><span class="dot over"></span>Above target</span>
      <span class="legend-item"><span class="dash"></span>Weekly target</span>
    </div>

    <div class="chart-body">
      <div class="y-axis">
        <span v-for="t in ticks" :key="t" class="tick" :style="{ bottom: pct(t) }">{{ t }} h</span>
      </div>

      <div class="plot">
        <div v-for="t in ticks" :key="'g' + t" class="gridline" :style="{ bottom: pct(t) }"></div>
        <div class="bars" :style="{ '--cols': cols }">
          <div v-for="w in weeks" :key="w.label" class="bar-col">
            <div v-if="w.target > 0" class="target-line" :style="{ bottom: pct(w.target) }"></div>

            <template v-if="total(w) > 0">
              <div class="bar-total" :style="{ bottom: `calc(${pct(total(w))} + 6px)` }">
                <span>{{ fmt(total(w)) }}</span>
                <span v-if="Math.abs(over(w)) >= 0.01" class="flex-pill" :class="over(w) > 0 ? 'plus' : 'minus'">
                  {{ over(w) > 0 ? '+' : '−' }}{{ fmt(Math.abs(over(w))) }}
                </span>
              </div>
              <div class="bar" :style="{ height: pct(total(w)) }">
                <div class="seg sick" :style="{ height: `${(w.sick / total(w)) * 100}%` }"></div>
                <div class="seg work" :style="{ height: `${(w.work / total(w)) * 100}%` }"></div>
                <div v-if="over(w) > 0" class="over-part" :style="{ height: `${(over(w) / total(w)) * 100}%` }"></div>
              </div>
            </template>
          </div>
        </div>
      </div>

      <div></div>
      <div class="x-axis" :style="{ '--cols': cols }">
        <span v-for="w in weeks" :key="w.label">{{ w.label }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.weekly-chart {
  flex: 1 1 0;
  min-width: 380px;
  box-sizing: border-box;
  background: #fff;
  border-radius: 12px;
  outline: 1px #DFE7E8 solid;
  outline-offset: -1px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  text-align: left;
  font-family: Inter, sans-serif;
}
.chart-title { margin: 0 0 4px; font-size: 16px; font-weight: 600; color: #172D32; }
.chart-subtitle { margin: 0 0 16px; font-size: 12px; color: #697B80; }

.legend { display: flex; flex-wrap: wrap; gap: 8px 16px; margin-bottom: 8px; font-size: 11px; color: #697B80; }
.legend-item { display: inline-flex; align-items: center; gap: 6px; }
.dot { width: 9px; height: 9px; border-radius: 2px; }
.work { background: #176A70; }
.sick { background: #B9A5D2; }
.dot.over { background: #E8A33D; }
.dash { width: 14px; border-top: 2px dashed #172D32; opacity: 0.55; }

.chart-body {
  flex: 1;
  min-height: 240px;
  display: grid;
  grid-template-columns: 44px 1fr;
  grid-template-rows: 1fr auto;
}
.y-axis, .plot { position: relative; margin-top: 46px; }
.tick { position: absolute; left: 0; transform: translateY(50%); font-size: 10px; color: #8A9A9E; }
.gridline { position: absolute; left: 0; right: 0; height: 1px; background: #EDF2F2; }

.bars { position: absolute; inset: 0; display: grid; grid-template-columns: repeat(var(--cols), 1fr); }
.bar-col { position: relative; }
.bar {
  position: absolute; bottom: 0; left: 50%;
  width: 56px; max-width: 70%;
  transform: translateX(-50%);
  display: flex; flex-direction: column;
  border-radius: 6px 6px 0 0;
  overflow: hidden;
}
.over-part { position: absolute; top: 0; left: 0; right: 0; background: #E8A33D; }

.target-line {
  position: absolute; left: 8%; right: 8%; height: 0; z-index: 2;
  border-top: 2px dashed #172D32; opacity: 0.55;
}

.bar-total {
  position: absolute; left: 0; right: 0;
  display: flex; flex-direction: column; align-items: center; gap: 2px;
  font-size: 13px; font-weight: 600; color: #172D32;
}
.flex-pill { font-size: 10px; font-weight: 600; padding: 1px 6px; border-radius: 999px; }
.flex-pill.plus { background: #FBEBD0; color: #9A6A12; }
.flex-pill.minus { background: #EEF1F1; color: #697B80; }

.x-axis {
  display: grid; grid-template-columns: repeat(var(--cols), 1fr);
  padding-top: 10px; font-size: 11px; color: #697B80; text-align: center;
}
.x-axis span { white-space: nowrap; }
</style>