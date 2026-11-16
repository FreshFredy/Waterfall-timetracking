<script setup>
import { ref } from 'vue'

const emit = defineEmits(['login'])

const email = ref('')
const password = ref('')
const errorMessage = ref('')
const isLoading = ref(false)

const handleLogin = async () => {
  errorMessage.value = ''
  isLoading.value = true

  try {
    const response = await fetch('/api/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: email.value, password: password.value })
    })

    const textResponse = await response.text()
    const data = textResponse ? JSON.parse(textResponse) : {}

    if (!response.ok) {
      throw new Error(data.error || 'Anmeldung fehlgeschlagen.')
    }

    // Übergibt E-Mail, Name und die vom Backend erkannte Rolle an App.vue
    emit('login', { 
      email: data.email, 
      name: data.name, 
      role: data.role ? data.role.toLowerCase() : 'employee'
    })

  } catch (err) {
    errorMessage.value = err.message || 'Verbindungsfehler zum Server.'
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <div class="logo-badge">⏱️</div>
        <h1 class="login-title">Anmeldung</h1>
        <p class="login-subtitle">Zeiterfassung & Team-Management</p>
      </div>

      <div v-if="errorMessage" class="error-banner">
        {{ errorMessage }}
      </div>

      <form @submit.prevent="handleLogin" class="login-form">
        <label class="input-label">
          E-Mail-Adresse
          <input v-model="email" type="email" class="text-input" placeholder="name@company.com" required />
        </label>

        <label class="input-label">
          Passwort
          <input v-model="password" type="password" class="text-input" placeholder="••••••••" required />
        </label>

        <button type="submit" class="submit-btn" :disabled="isLoading">
          {{ isLoading ? 'Wird angemeldet...' : 'Anmelden' }}
        </button>
      </form>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  min-height: 100vh;
  background: #F4F6F6;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 24px;
}
.login-card {
  width: 100%;
  max-width: 420px;
  background: #FFFFFF;
  padding: 36px;
  border-radius: 16px;
  border: 1px solid #DFE7E8;
  display: flex;
  flex-direction: column;
  gap: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.03);
}
.login-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 8px;
}
.logo-badge {
  width: 48px;
  height: 48px;
  background: #E8F3F2;
  border-radius: 12px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 22px;
  margin-bottom: 4px;
}
.login-title {
  color: #172D32;
  font-size: 24px;
  font-weight: 600;
}
.login-subtitle {
  color: #697B80;
  font-size: 13px;
}
.error-banner {
  padding: 10px;
  background: #FDE8E8;
  color: #C53030;
  font-size: 12px;
  border-radius: 6px;
  text-align: center;
}
.login-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.input-label {
  display: flex;
  flex-direction: column;
  gap: 6px;
  color: #172D32;
  font-size: 12px;
  font-weight: 500;
}
.text-input {
  height: 42px;
  padding: 0 12px;
  border-radius: 8px;
  border: 1px solid #DFE7E8;
  font-size: 13px;
  background: #FFFFFF;
  color: #172D32;
}
.text-input:focus {
  outline: 2px solid #176A70;
  border-color: transparent;
}
.submit-btn {
  height: 42px;
  background: #176A70;
  border-radius: 8px;
  color: white;
  font-size: 13px;
  font-weight: 600;
  border: none;
  cursor: pointer;
  margin-top: 4px;
  transition: background 0.2s;
}
.submit-btn:hover {
  background: #11545A;
}
.submit-btn:disabled {
  background: #B3B3B3;
  cursor: not-allowed;
}
</style>