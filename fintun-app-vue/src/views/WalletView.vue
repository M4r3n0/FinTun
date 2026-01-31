<script setup>
import { ref, onMounted } from 'vue';
import { useAuthStore } from '../stores/auth';
import api from '../services/api';
import { 
  Plus, 
  History, 
  ArrowDownLeft, 
  ArrowUpRight, 
  CreditCard,
  AlertCircle
} from 'lucide-vue-next';

const auth = useAuthStore();
const accounts = ref([]);
const history = ref([]);
const isLoading = ref(true);

const fetchWalletData = async () => {
  isLoading.value = true;
  try {
    const [accs, hist] = await Promise.all([
      api.getAccounts(auth.userId),
      api.getHistory(auth.userId)
    ]);
    accounts.value = accs;
    history.value = hist;
  } catch (err) {
    console.error('Failed to fetch wallet data:', err);
  } finally {
    isLoading.value = false;
  }
};

const createAccount = async () => {
  const currency = prompt('Enter currency code (e.g. TND, USD):', 'TND');
  if (!currency) return;
  try {
    await api.createAccount(auth.userId, currency);
    fetchWalletData();
  } catch (err) {
    alert(err.message);
  }
};

onMounted(fetchWalletData);
</script>

<template>
  <div class="wallet-view">
    <header class="view-header">
      <h1 class="text-gradient">Your Digital Wallets</h1>
      <button class="btn btn-primary" @click="createAccount">
        <Plus :size="18" /> New Account
      </button>
    </header>

    <div class="accounts-grid">
      <div v-if="isLoading" v-for="i in 2" :key="i" class="glass-card skeleton-card"></div>
      
      <div v-else v-for="acc in accounts" :key="acc.id" class="virtual-card-wrapper">
        <div class="virtual-card glass">
          <div class="card-bg-glow"></div>
          <div class="card-content">
            <div class="card-top">
              <div class="chip-wrapper">
                <div class="chip"></div>
                <div class="chip-lines"></div>
              </div>
              <div class="vendor-logo">
                <div class="logo-circle"></div>
                <span class="vendor-name">FINTUN</span>
              </div>
            </div>

            <div class="card-number-section">
              <span class="card-number">{{ acc.cardNumber || '•••• •••• •••• ••••' }}</span>
            </div>

            <div class="card-info-row">
              <div class="info-item">
                <span class="info-label">EXPIRY</span>
                <span class="info-value">{{ acc.expiryDate || 'MM/YY' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">CVV</span>
                <span class="info-value">•••</span>
              </div>
              <div class="info-item right">
                <span class="info-label">CURRENCY</span>
                <span class="info-value">{{ acc.currency }}</span>
              </div>
            </div>

            <div class="card-bottom">
              <div class="balance-section">
                <span class="balance-label">CURRENT BALANCE</span>
                <span class="balance-amount">
                  {{ acc.balance.toLocaleString('en-US', { minimumFractionDigits: 2 }) }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <section class="history-section">
      <div class="section-header">
        <h2 class="flex-center" style="gap: 12px;"><History :size="24" /> Recent Activity</h2>
      </div>

      <div class="glass-card history-container">
        <div v-if="history.length === 0" class="empty-state">
          No transactions found yet.
        </div>
        
        <div v-else v-for="tx in history" :key="tx.id" class="history-item">
          <div class="tx-type-icon" :class="tx.amount < 0 ? 'debit' : 'credit'">
            <ArrowDownLeft v-if="tx.amount >= 0" :size="18" />
            <ArrowUpRight v-else :size="18" />
          </div>
          
          <div class="tx-details">
            <p class="tx-title">{{ tx.description || 'Transaction' }}</p>
            <p class="tx-date">{{ new Date(tx.createdAt).toLocaleString() }}</p>
          </div>

          <div class="tx-amount" :class="tx.amount < 0 ? 'debit' : 'credit'">
            {{ tx.amount >= 0 ? '+' : '' }}{{ tx.amount.toLocaleString('en-US', { minimumFractionDigits: 2 }) }} 
            <span class="tx-currency">TND</span>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.wallet-view {
  animation: fadeIn 0.4s ease-out;
}

.view-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40px;
}

.accounts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(380px, 1fr));
  gap: 32px;
  margin-bottom: 48px;
}

.virtual-card-wrapper {
  perspective: 1000px;
}

.virtual-card {
  height: 240px;
  border-radius: 24px;
  position: relative;
  overflow: hidden;
  padding: 24px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  transition: transform 0.3s ease;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.08) 0%, rgba(255, 255, 255, 0.03) 100%);
  border: 1px solid rgba(255, 255, 255, 0.1);
  cursor: pointer;
}

.virtual-card:hover {
  transform: translateY(-5px) rotateX(2deg) rotateY(-2deg);
}

.card-bg-glow {
  position: absolute;
  top: -20%;
  right: -20%;
  width: 60%;
  height: 60%;
  background: radial-gradient(circle, var(--primary) 0%, transparent 70%);
  opacity: 0.15;
  filter: blur(40px);
  z-index: 0;
}

.card-content {
  position: relative;
  z-index: 1;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.chip-wrapper {
  width: 45px;
  height: 35px;
  background: linear-gradient(135deg, #ffd700 0%, #daa520 100%);
  border-radius: 6px;
  position: relative;
}

.chip-lines {
  position: absolute;
  inset: 5px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 4px;
}

.vendor-logo {
  display: flex;
  align-items: center;
  gap: 8px;
}

.vendor-name {
  font-family: 'Outfit', sans-serif;
  font-weight: 800;
  letter-spacing: 0.1em;
  font-size: 0.9rem;
  color: var(--text-primary);
}

.logo-circle {
  width: 24px;
  height: 24px;
  background: var(--primary);
  border-radius: 50%;
  box-shadow: 0 0 10px var(--primary);
}

.card-number-section {
  margin: 12px 0 24px;
}

.card-number {
  font-family: 'Outfit', sans-serif;
  font-size: 1.6rem;
  letter-spacing: 0.15em;
  font-weight: 500;
  color: var(--text-primary);
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.card-info-row {
  display: flex;
  gap: 32px;
  margin-bottom: auto;
}

.info-item {
  display: flex;
  flex-direction: column;
}

.info-item.right {
  margin-left: auto;
  text-align: right;
}

.info-label {
  font-size: 0.6rem;
  color: var(--text-dim);
  letter-spacing: 0.1em;
  font-weight: 700;
  margin-bottom: 4px;
}

.info-value {
  font-family: monospace;
  font-size: 0.9rem;
  color: var(--text-primary);
  font-weight: 600;
}

.balance-section {
  display: flex;
  flex-direction: column;
}

.balance-label {
  font-size: 0.7rem;
  color: var(--text-dim);
  font-weight: 600;
  letter-spacing: 0.05em;
  margin-bottom: 4px;
}

.balance-amount {
  font-family: 'Outfit', sans-serif;
  font-size: 1.8rem;
  font-weight: 800;
  color: var(--text-primary);
}

.history-section {
  margin-top: 60px;
}

.section-header {
  margin-bottom: 24px;
}

.history-container {
  padding: 0;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid var(--glass-border);
}

.history-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px;
  border-bottom: 1px solid var(--glass-border);
  transition: background 0.2s ease;
}

.history-item:last-child {
  border-bottom: none;
}

.history-item:hover {
  background: rgba(255, 255, 255, 0.04);
}

.tx-type-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tx-type-icon.credit {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.tx-type-icon.debit {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.tx-details {
  flex: 1;
}

.tx-title {
  font-weight: 600;
  font-size: 1rem;
  margin-bottom: 4px;
}

.tx-date {
  font-size: 0.8rem;
  color: var(--text-dim);
}

.tx-amount {
  font-weight: 800;
  font-size: 1.2rem;
}

.tx-amount.credit { color: #10b981; }
.tx-amount.debit { color: var(--text-primary); }

.tx-currency {
  font-size: 0.8rem;
  color: var(--text-dim);
  font-weight: 500;
}

.empty-state {
  padding: 60px;
  text-align: center;
  color: var(--text-dim);
  font-size: 1.1rem;
}

.skeleton-card {
  height: 240px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.05);
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0% { opacity: 0.6; }
  50% { opacity: 0.3; }
  100% { opacity: 0.6; }
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
