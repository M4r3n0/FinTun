<script setup>
import { ref } from 'vue';
import { useAuthStore } from '../stores/auth';
import { useRouter } from 'vue-router';
import { LogIn, UserPlus, Phone, Lock, AlertCircle } from 'lucide-vue-next';

const auth = useAuthStore();
const router = useRouter();

const phoneNumber = ref('');
const password = ref('');
const error = ref('');
const isLoading = ref(false);

const handleLogin = async () => {
  error.value = '';
  isLoading.value = true;
  try {
    await auth.login(phoneNumber.value, password.value);
    router.push('/');
  } catch (err) {
    error.value = err.message || 'Login failed';
  } finally {
    isLoading.value = false;
  }
};
</script>

<template>
  <div class="auth-page flex-center">
    <div class="auth-box glass-card">
      <div class="auth-header">
        <h1 class="text-gradient">Welcome back</h1>
        <p class="text-secondary">Secure access to your Tunisian digital wallet.</p>
      </div>

      <form @submit.prevent="handleLogin" class="auth-form">
        <div v-if="error" class="error-badge">
          <AlertCircle :size="18" />
          {{ error }}
        </div>

        <div class="input-group">
          <label class="input-label">Phone Number</label>
          <div class="input-wrapper">
            <Phone class="input-icon" :size="18" />
            <input 
              v-model="phoneNumber" 
              type="tel" 
              placeholder="216 12345678" 
              required
            >
          </div>
        </div>

        <div class="input-group">
          <label class="input-label">Password</label>
          <div class="input-wrapper">
            <Lock class="input-icon" :size="18" />
            <input 
              v-model="password" 
              type="password" 
              placeholder="••••••••" 
              required
            >
          </div>
        </div>

        <button type="submit" class="btn btn-primary auth-btn" :disabled="isLoading">
          <span v-if="isLoading">Signing in...</span>
          <template v-else>
            Sign In <LogIn :size="18" />
          </template>
        </button>
      </form>

      <div class="auth-footer">
        <p class="text-secondary">
          New to Fintun? 
          <router-link to="/register" class="auth-link">Create an account</router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;
  background: radial-gradient(circle at center, #1a103d 0%, var(--bg-primary) 70%);
}

.auth-box {
  width: 100%;
  max-width: 440px;
  animation: slideUp 0.6s cubic-bezier(0.23, 1, 0.32, 1);
}

.auth-header {
  text-align: center;
  margin-bottom: 32px;
}

.auth-header h1 {
  font-size: 2rem;
  margin-bottom: 8px;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.input-wrapper {
  position: relative;
}

.input-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-dim);
}

input {
  padding-left: 48px;
}

.auth-btn {
  width: 100%;
  justify-content: center;
  margin-top: 16px;
  padding: 14px;
}

.auth-footer {
  margin-top: 32px;
  text-align: center;
  font-size: 0.9rem;
}

.auth-link {
  color: var(--primary);
  font-weight: 600;
}

.error-badge {
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.2);
  color: var(--danger);
  padding: 12px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 0.9rem;
  margin-bottom: 16px;
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
