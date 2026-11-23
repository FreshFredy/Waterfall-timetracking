<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  modelValue: { type: Number, default: null },       // ID des gewählten Mitarbeiters
  employees: { type: Array, default: () => [] },
  placeholder: { type: String, default: 'Search employee...' },
  direction: { type: String, default: 'down' },
})
const emit = defineEmits(['update:modelValue'])

const fullName = (e) =>
  `${e.firstname ?? e.firstName ?? ''} ${e.lastname ?? e.lastName ?? ''}`.trim()

const query = ref('')
const isOpen = ref(false)
const activeIndex = ref(0)

// "anna sch" findet Anna Schmidt: jedes Wort muss in Name oder E-Mail vorkommen
const filtered = computed(() => {
  const terms = query.value.trim().toLowerCase().split(/\s+/).filter(Boolean)
  const list = terms.length === 0
    ? props.employees
    : props.employees.filter((e) => {
        const haystack = `${fullName(e)} ${e.email ?? ''}`.toLowerCase()
        return terms.every((t) => haystack.includes(t))
      })
  return list.slice(0, 8)
})

// Text im Feld passend zur Auswahl halten
const syncText = () => {
  const selected = props.employees.find((e) => e.id === props.modelValue)
  query.value = selected ? fullName(selected) : ''
}
watch(() => [props.modelValue, props.employees], syncText, { immediate: true })

const onInput = (event) => {
  query.value = event.target.value
  isOpen.value = true
  activeIndex.value = 0
  if (props.modelValue !== null) emit('update:modelValue', null)   // Text geändert = Auswahl aufheben
}

const select = (employee) => {
  emit('update:modelValue', employee.id)
  query.value = fullName(employee)
  isOpen.value = false
}

const move = (delta) => {
  if (!isOpen.value) { isOpen.value = true; return }
  const count = filtered.value.length
  if (count === 0) return
  activeIndex.value = (activeIndex.value + delta + count) % count
}

const selectActive = () => {
  const employee = filtered.value[activeIndex.value]
  if (employee) select(employee)
}

const onBlur = () => {
  isOpen.value = false
  syncText()                       // ohne Auswahl wird der halbe Text wieder geleert
}
</script>

<template>
  <div class="combo">
    <input
      class="combo-input"
      type="text"
      :value="query"
      :placeholder="placeholder"
      autocomplete="off"
      @input="onInput"
      @focus="isOpen = true"
      @blur="onBlur"
      @keydown.down.prevent="move(1)"
      @keydown.up.prevent="move(-1)"
      @keydown.enter.prevent="selectActive"
      @keydown.esc="isOpen = false"
    />

    <ul v-if="isOpen" class="combo-list" :class="{ up: direction === 'up' }">
      <li v-if="filtered.length === 0" class="combo-empty">No employee found</li>
      <li
        v-for="(e, i) in filtered"
        :key="e.id"
        class="combo-item"
        :class="{ active: i === activeIndex }"
        @mousedown.prevent="select(e)"
        @mouseenter="activeIndex = i"
      >
        <span class="combo-name">{{ fullName(e) }}</span>
        <span class="combo-mail">{{ e.email }}</span>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.combo { 
  position: relative; 
  width: 100%; 
}
.combo-input {
  width: 100%;
  height: 42px;
  padding: 12px;
  background: #FFFFFF;
  color: #000000;
  color-scheme: light;
  border: none;
  border-radius: 8px;
  outline: 1px #DFE7E8 solid;
  font-size: 13px;
}
.combo-input:focus { 
  outline: 2px #176A70 solid; 
}
.combo-input::placeholder { 
  color: #697B80; 
}
.combo-list {
  position: absolute;
  left: 0;
  right: 0;
  top: 100%;
  max-height: 260px;
  overflow-y: auto;
  margin: 0;
  padding: 4px;
  list-style: none;
  background: #FFFFFF;
  border: 1px solid #DFE7E8;
  border-radius: 8px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
  z-index: 1000;
}
.combo-item {
  display: flex;
  flex-direction: column;
  gap: 1px;
  padding: 8px 10px;
  border-radius: 6px;
  cursor: pointer;
  text-align: left;
}
.combo-item.active { 
  background: #E8F3F2; 
}
.combo-name { 
  font-size: 13px; 
  font-weight: 600; 
  color: #172D32; }
.combo-mail { 
  font-size: 11px; 
  color: #697B80; 
}
.combo-empty { 
  padding: 10px; 
  font-size: 12px; 
  color: #697B80; 
  text-align: left; 
}
.combo-list.up { 
  top: auto; 
  bottom: 100%; 
}

</style>