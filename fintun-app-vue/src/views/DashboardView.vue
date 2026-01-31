<script setup>
import { ref, onMounted } from 'vue';
import { useAuthStore } from '../stores/auth';
import api from '../services/api';
import { 
  TrendingUp, 
  ArrowUpRight, 
  Plus, 
  Search, 
  Clock,
  ChevronRight
} from 'lucide-vue-next';

const auth = useAuthStore();
const accounts = ref([]);
const totalBalance = ref(0);
const isLoading = ref(true);

onMounted(async () => {
  try {
    const res = await api.getAccounts(auth.userId);
    accounts.value = res;
    totalBalance.value = res.reduce((sum, acc) => sum + acc.balance, 0);
  } catch (err) {
    console.error('Failed to fetch accounts:', err);
  } finally {
    isLoading.value = false;
  }
});
</script>

<template>
  <div class="dashboard-view">
    <header class="view-header">
      <div class="welcome">
        <h1 class="text-gradient">Portfolio Overview</h1>
        <p class="text-secondary">Welcome back, {{ auth.userId?.substring(0, 8) }}</p>
      </div>
      <div class="header-actions">
        <button class="btn btn-primary" @click="$router.push('/wallet')">
          <Plus :size="18" /> Manage Wallets
        </button>
      </div>
    </header>

    <div class="stats-grid">
      <div class="glass-card stat-card main-stat">
        <h3 class="stat-label">Total Portfolio Value</h3>
        <div class="stat-value">
          <span v-if="isLoading" class="skeleton-text">Loading...</span>
          <template v-else>
            {{ totalBalance.toLocaleString('en-US', { minimumFractionDigits: 2 }) }}
            <span class="currency">TND</span>
          </template>
        </div>
        <div class="stat-meta text-success">
          <TrendingUp :size="16" /> Live from Ledger
        </div>
      </div>

      <div class="glass-card stat-card actions-card">
        <h3 class="stat-label">Fast Actions</h3>
        <div class="action-list">
          <button class="action-item" @click="$router.push('/transfer')">
            <div class="action-icon"><ArrowUpRight :size="20" /></div>
            <span>Send Money</span>
            <ChevronRight :size="16" class="chevron" />
          </button>
          <button class="action-item" @click="$router.push('/kyc')">
            <div class="action-icon"><Search :size="20" /></div>
            <span>Verify Identity</span>
            <ChevronRight :size="16" class="chevron" />
          </button>
        </div>
      </div>
    </div>

    <!-- More sections like Recent Transactions could go here -->
  </div>
</template>

<style scoped>
.dashboard-view {
  animation: fadeIn 0.4s ease-out;
}

.view-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 48px;
}

.view-header h1 {
  font-size: 2.5rem;
  line-height: 1.2;
}

.stats-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
}

.stat-card {
  display: flex;
  flex-direction: column;
}

.stat-label {
  text-transform: uppercase;
  letter-spacing: 0.1em;
  font-size: 0.8rem;
  color: var(--text-dim);
  margin-bottom: 16px;
}

.stat-value {
  font-family: 'Outfit', sans-serif;
  font-size: 3.5rem;
  font-weight: 700;
  margin-bottom: 16px;
}

.currency {
  font-size: 1.5rem;
  color: var(--text-dim);
  margin-left: 8px;
}

.stat-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.9rem;
  font-weight: 600;
}

.action-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid transparent;
  border-radius: 12px;
  color: var(--text-primary);
  cursor: pointer;
  transition: var(--transition);
  text-align: left;
}

.action-item:hover {
  background: rgba(139, 92, 246, 0.1);
  border-color: rgba(139, 92, 246, 0.2);
}

.action-icon {
  width: 40px;
  height: 40px;
  background: var(--glass);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary);
}

.action-item span {
  flex: 1;
  font-weight: 500;
}

.chevron {
  color: var(--text-dim);
  opacity: 0.5;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 1024px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
