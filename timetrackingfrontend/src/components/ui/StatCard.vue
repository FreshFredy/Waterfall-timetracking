<script setup>
defineProps({
  title: { type: String, required: true },
  value: { type: String, required: true },
  subtitle: { type: String, default: '' },
  highlighted: { type: Boolean, default: false },
  icon: { type: String, default: '' },
})
</script>

<template>
  <div
    class="stat-card"
    :style="{
      background: highlighted ? '#11545A' : 'white',
      outline: highlighted ? '1px #11545A solid' : '1px #DFE7E8 solid',
    }"
  >
    <div class="card-header">
      <div
        class="title-text"
        :style="{ color: highlighted ? '#DAECEB' : '#697B80' }"
      >
        {{ title }}
      </div>
      <div class="icon-box" :style="{ color: highlighted ? '#B3D6D5' : '#697B80' }">
        {{ icon }}
      </div>
    </div>
    <div
      class="value-text"
      :style="{ color: highlighted ? 'white' : '#172D32' }"
    >
      {{ value }}
    </div>
    <div
      class="subtitle-text"
      :style="{ color: highlighted ? '#C7E1DF' : '#697B80' }"
    >
      {{ subtitle }}
    </div>
  </div>
</template>

<style scoped>
@font-face {
  font-family: 'Symbols Nerd Font';
  src: url('../../assets/fonts/SymbolsNerdFont-Regular.ttf') format('truetype');
}
.stat-card {
  flex: 1 1 0;
  padding: 20px;
  overflow: hidden;
  border-radius: 12px;
  outline-offset: -1px;
  display: inline-flex;
  flex-direction: column;
  justify-content: flex-start;
  align-items: flex-start;
  gap: 12px;
}
.card-header {
  align-self: stretch;
  overflow: hidden;
  display: inline-flex;
  justify-content: space-between;
  align-items: center;
}
.title-text {
  font-size: 12px;
  font-family: Inter, sans-serif;
  font-weight: 500;
  line-height: 17.40px;
}
.icon-box {
  width: 17px;
  height: 17px;
  position: relative;
  overflow: hidden;
}
.icon-shape {
  width: 14.17px;
  height: 14.17px;
  left: 1.42px;
  top: 1.42px;
  position: absolute;
  outline-offset: -0.85px;
}
.value-text {
  font-size: 32px;
  font-family: Inter, sans-serif;
  font-weight: 600;
  line-height: 36.80px;
}
.subtitle-text {
  font-size: 11px;
  font-family: Inter, sans-serif;
  font-weight: 400;
  line-height: 15.95px;
}
.icon-box {
  width: 17px;
  height: 17px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: 'Symbols Nerd Font', 'Symbols Nerd Font Mono', monospace;
  font-size: 16px;
  line-height: 1;
}

</style>