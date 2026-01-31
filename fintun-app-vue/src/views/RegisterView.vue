<script setup>
import { ref } from 'vue';
import { useAuthStore } from '../stores/auth';
import { useRouter } from 'vue-router';
import { UserPlus, User, Mail, Smartphone, Fingerprint, Calendar, ArrowRight, AlertCircle } from 'lucide-vue-next';

const auth = useAuthStore();
const router = useRouter();

const form = ref({
  fullName: '',
  phoneNumber: '',
  email: '',
  nationalId: '',
  password: '',
  dateOfBirth: '',
  address: 'Tunisia Central'
});

const error = ref('');
const isLoading = ref(false);

const handleRegister = async () => {
  error.value = '';
  isLoading.value = true;
  try {
    await auth.register(form.value);
    alert('Account created successfully! Please login.');
    router.push('/login');
  } catch (err) {
    error.value = err.message || 'Registration failed';
  } finally {
    isLoading.value = false;
  }
};
</script>

<template>
  <div class="auth-page flex-center">
    <div class="auth-box glass-card register-box">
      <div class="auth-header">
        <h1 class="text-gradient">Create Account</h1>
        <p class="text-secondary">Join Fintun for a premium banking experience.</p>
      </div>

      <form @submit.prevent="handleRegister" class="auth-form">
        <div v-if="error" class="error-badge">
          <AlertCircle :size="18" />
          {{ error }}
        </div>

        <div class="grid-inputs">
          <div class="input-group">
            <label class="input-label">Full Name</label>
            <div class="input-wrapper">
              <User class="input-icon" :size="18" />
              <input v-model="form.fullName" type="text" placeholder="John Doe" required>
            </div>
          </div>

          <div class="input-group">
            <label class="input-label">Phone Number</label>
            <div class="input-wrapper">
              <Smartphone class="input-icon" :size="18" />
              <input v-model="form.phoneNumber" type="tel" placeholder="216 11111111" required>
            </div>
          </div>
        </div>

        <div class="input-group">
          <label class="input-label">Email Address</label>
          <div class="input-wrapper">
            <Mail class="input-icon" :size="18" />
            <input v-model="form.email" type="email" placeholder="john@example.com" required>
          </div>
        </div>

        <div class="grid-inputs">
          <div class="input-group">
            <label class="input-label">National ID</label>
            <div class="input-wrapper">
              <Fingerprint class="input-icon" :size="18" />
              <input v-model="form.nationalId" type="text" placeholder="ID Number" required>
            </div>
          </div>

          <div class="input-group">
            <label class="input-label">Birth Date</label>
            <div class="input-wrapper">
              <Calendar class="input-icon" :size="18" />
              <input v-model="form.dateOfBirth" type="date" required>
            </div>
          </div>
        </div>

        <div class="input-group">
          <label class="input-label">Password</label>
          <div class="input-wrapper">
            <input v-model="form.password" type="password" placeholder="••••••••" required>
          </div>
        </div>

        <button type="submit" class="btn btn-primary auth-btn" :disabled="isLoading">
          <span v-if="isLoading">Processing...</span>
          <template v-else>
            Get Started <ArrowRight :size="18" />
          </template>
        </button>
      </form>

      <div class="auth-footer">
        <p class="text-secondary">
          Already have an account? 
          <router-link to="/login" class="auth-link">Sign In</router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;
  background: radial-gradient(circle at center, #1a103d 0%, var(--bg-primary) 70%);
  padding: 40px 20px;
}

.register-box {
  max-width: 540px;
}

.grid-inputs {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

input {
  padding-left: 48px;
}

.input-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-dim);
}

.auth-header {
  text-align: center;
  margin-bottom: 32px;
}

.auth-btn {
  width: 100%;
  justify-content: center;
  margin-top: 16px;
  padding: 14px;
}

.auth-footer {
  margin-top: 24px;
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

@media (max-width: 640px) {
  .grid-inputs {
    grid-template-columns: 1fr;
  }
}
</style>
