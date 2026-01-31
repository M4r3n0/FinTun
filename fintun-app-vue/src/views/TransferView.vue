<script setup>
import { ref, onMounted } from 'vue';
import { useAuthStore } from '../stores/auth';
import api from '../services/api';
import { useRouter } from 'vue-router';
import { 
  Search, 
  ArrowRight, 
  CheckCircle2, 
  AlertCircle,
  ShieldAlert,
  User,
  Wallet,
  ArrowLeft
} from 'lucide-vue-next';

const auth = useAuthStore();
const router = useRouter();

const step = ref(1);
const phone = ref('');
const amount = ref(0);
const recipient = ref(null);
const senderAccount = ref(null);
const error = ref('');
const isLoading = ref(false);

const findRecipient = async () => {
  error.value = '';
  isLoading.value = true;
  try {
    const res = await api.searchUserByPhone(phone.value);
    if (!res) throw new Error('User not found.');
    
    if (res.kycLevel !== 'VERIFIED') {
      error.value = 'Recipient is not KYC verified. They must verify their identity to receive funds.';
      return;
    }

    recipient.value = res;
    step.value = 2;
  } catch (err) {
    error.value = err.message || 'User not found. Please check the phone number.';
  } finally {
    isLoading.value = false;
  }
};

const processTransfer = async () => {
  error.value = '';
  isLoading.value = true;
  try {
    // Get sender's TND account
    const accounts = await api.getAccounts(auth.userId);
    const tndAcc = accounts.find(a => a.currency === 'TND');
    if (!tndAcc) throw new Error('You need a TND wallet to send money.');
    if (tndAcc.balance < amount.value) throw new Error('Insufficient funds.');

    // Get recipient's TND account
    const recAccs = await api.getAccounts(recipient.value.userId);
    const recTnd = recAccs.find(a => a.currency === 'TND');
    if (!recTnd) throw new Error('Recipient does not have a TND wallet.');

    await api.transferP2P(tndAcc.id, recTnd.id, amount.value);
    step.value = 3;
  } catch (err) {
    error.value = err.message || 'Transfer failed';
  } finally {
    isLoading.value = false;
  }
};

const reset = () => {
  step.value = 1;
  phone.value = '';
  amount.value = 0;
  recipient.value = null;
  error.value = '';
};
</script>

<template>
  <div class="transfer-view flex-center">
    <div class="transfer-card glass-card">
      <!-- KYC Required Overlay -->
      <div v-if="!auth.isVerified" class="kyc-block-overlay">
        <div class="kyc-block-content flex-center flex-column">
          <div class="kyc-icon-wrapper">
            <ShieldAlert :size="64" class="text-danger" />
          </div>
          <h2 class="text-gradient">Verification Required</h2>
          <p class="text-secondary text-center">
            P2P transfers are restricted to verified users. Please complete your identity verification to start sending money instantly.
          </p>
          <button class="btn btn-primary" @click="router.push('/kyc')">
            Get Verified Now
          </button>
        </div>
      </div>

      <template v-else>
        <div v-if="step !== 3" class="header">
          <button v-if="step === 2" @click="step = 1" class="back-btn">
            <ArrowLeft :size="20" />
          </button>
          <h1 class="text-gradient">Instant Transfer</h1>
        </div>

        <div v-if="error" class="error-badge">
          <AlertCircle :size="18" /> {{ error }}
        </div>

        <!-- Step 1: Search Recipient -->
        <div v-if="step === 1" class="step">
          <p class="text-secondary">Send money instantly to any Fintun user across Tunisia.</p>
          
          <div class="input-group">
            <label class="input-label">Recipient Phone Number</label>
            <div class="input-wrapper">
              <Search class="input-icon" :size="20" />
              <input 
                v-model="phone" 
                type="tel" 
                placeholder="216 11111111" 
                @keyup.enter="findRecipient"
              >
            </div>
          </div>

          <button class="btn btn-primary flow-btn" @click="findRecipient" :disabled="isLoading || !phone">
            {{ isLoading ? 'Searching...' : 'Find Recipient' }}
            <ArrowRight v-if="!isLoading" :size="18" />
          </button>
        </div>

        <!-- Step 2: Enter Amount & Confirm -->
        <div v-if="step === 2" class="step">
          <div class="recipient-box glass">
            <div class="user-avatar"><User :size="24" /></div>
            <div class="user-info">
              <p class="user-name">{{ recipient?.fullName }}</p>
              <p class="user-phone">{{ phone }}</p>
            </div>
          </div>

          <div class="input-group">
            <label class="input-label">Amount (TND)</label>
            <div class="input-wrapper">
              <Wallet class="input-icon" :size="20" />
              <input 
                v-model.number="amount" 
                type="number" 
                placeholder="0.00" 
                step="0.01"
                min="0.01"
                required
              >
            </div>
          </div>

          <button class="btn btn-primary flow-btn" @click="processTransfer" :disabled="isLoading || !amount">
            {{ isLoading ? 'Processing...' : `Send ${amount || 0} TND` }}
            <ArrowRight v-if="!isLoading" :size="18" />
          </button>
        </div>

        <!-- Step 3: Success -->
        <div v-if="step === 3" class="success-step">
          <div class="success-icon"><CheckCircle2 :size="64" /></div>
          <h2>Transfer Successful!</h2>
          <p class="text-secondary">
            You've sent <strong>{{ amount }} TND</strong> to {{ recipient?.fullName }}.
          </p>
          
          <div class="success-actions">
            <button class="btn btn-primary" @click="router.push('/')">Go to Dashboard</button>
            <button class="btn btn-glass" @click="reset">New Transfer</button>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped>
.transfer-view {
  min-height: calc(100vh - 100px);
}

.transfer-card {
  width: 100%;
  max-width: 500px;
  padding: 40px;
}

.header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 32px;
}

.back-btn {
  background: none;
  border: none;
  color: var(--text-dim);
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  transition: var(--transition);
}

.back-btn:hover {
  background: var(--glass);
  color: var(--text-primary);
}

.header h1 {
  font-size: 1.8rem;
  margin: 0;
}

.step p {
  margin-bottom: 24px;
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
  font-size: 1.1rem;
}

.flow-btn {
  width: 100%;
  justify-content: center;
  margin-top: 16px;
  padding: 14px;
}

.recipient-box {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border-radius: 16px;
  margin-bottom: 32px;
  background: rgba(139, 92, 246, 0.05);
  border: 1px solid rgba(139, 92, 246, 0.1);
}

.user-avatar {
  width: 48px;
  height: 48px;
  background: var(--primary);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.user-name {
  font-weight: 700;
  font-size: 1.1rem;
}

.user-phone {
  font-size: 0.85rem;
  color: var(--text-dim);
}

.success-step {
  text-align: center;
  animation: scaleIn 0.5s cubic-bezier(0.23, 1, 0.32, 1);
}

.success-icon {
  color: var(--success);
  margin-bottom: 24px;
}

.success-step h2 {
  font-size: 1.8rem;
  margin-bottom: 12px;
}

.success-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 32px;
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
  margin-bottom: 24px;
}

@keyframes scaleIn {
  from { opacity: 0; transform: scale(0.9); }
  to { opacity: 1; transform: scale(1); }
}

.kyc-block-overlay {
  padding: 20px 0;
}

.kyc-block-content h2 {
  font-size: 2rem;
  margin: 24px 0 12px;
}

.kyc-block-content p {
  max-width: 320px;
  margin-bottom: 32px;
  line-height: 1.6;
}

.kyc-icon-wrapper {
  padding: 24px;
  background: rgba(239, 68, 68, 0.05);
  border-radius: 50%;
  border: 1px solid rgba(239, 68, 68, 0.1);
}
</style>
