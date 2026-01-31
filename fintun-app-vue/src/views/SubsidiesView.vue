<script setup>
import { ref, onMounted } from 'vue';
import { useAuthStore } from '../stores/auth';
import api from '../services/api';
import { 
  Coins, 
  CheckCircle, 
  ChevronRight, 
  Info,
  Calendar,
  Wallet
} from 'lucide-vue-next';

const auth = useAuthStore();
const programs = ref([]);
const isLoading = ref(true);

const fetchSubsidies = async () => {
  isLoading.value = true;
  try {
    const res = await api.getSubsidies();
    programs.value = res;
  } catch (err) {
    console.error('Failed to fetch subsidies:', err);
  } finally {
    isLoading.value = false;
  }
};

const claim = async (id) => {
  try {
    const res = await api.claimSubsidy(id, auth.userId);
    alert(`Success! Claim status: ${res.status}. Funds will be disbursed shortly.`);
  } catch (err) {
    alert(err.message || 'You might not be eligible for this program.');
  }
};

onMounted(fetchSubsidies);
</script>

<template>
  <div class="subsidies-view">
    <header class="view-header">
      <h1 class="text-gradient">Government Benefits</h1>
      <p class="text-secondary">Manage and claim your eligible subsidies through Fintun.</p>
    </header>

    <div class="programs-grid">
      <div v-if="isLoading" v-for="i in 3" :key="i" class="glass-card skeleton-card"></div>
      
      <div v-else v-for="p in programs" :key="p.id" class="glass-card program-card">
        <div class="program-header">
          <div class="program-icon"><Coins :size="24" /></div>
          <div class="program-amount">{{ p.amountPerUser }}<span>TND</span></div>
        </div>

        <h3 class="program-name">{{ p.name }}</h3>
        <p class="program-desc">{{ p.description }}</p>

        <div class="program-meta">
          <div class="meta-item">
            <Calendar :size="14" /> Active Program
          </div>
          <div class="meta-item">
            <Wallet :size="14" /> Direct Disbursement
          </div>
        </div>

        <button class="btn btn-primary claim-btn" @click="claim(p.id)">
          Check Eligibility & Claim <ChevronRight :size="18" />
        </button>
      </div>
    </div>

    <div class="info-section glass-card">
      <div class="info-header">
        <Info :size="20" class="text-primary" />
        <h4>How it works</h4>
      </div>
      <p>Fintun partners with the Tunisian government to ensure fast and transparent disbursement of social benefits. Eligibility is checked automatically based on your verified identity and socio-economic data.</p>
    </div>
  </div>
</template>

<style scoped>
.subsidies-view {
  animation: fadeIn 0.4s ease-out;
}

.view-header {
  margin-bottom: 40px;
}

.programs-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(340px, 1fr));
  gap: 24px;
  margin-bottom: 48px;
}

.program-card {
  display: flex;
  flex-direction: column;
  transition: var(--transition);
}

.program-card:hover {
  transform: translateY(-5px);
  border-color: var(--primary);
}

.program-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.program-icon {
  width: 48px;
  height: 48px;
  background: var(--primary-glow);
  color: var(--primary);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.program-amount {
  font-family: 'Outfit', sans-serif;
  font-size: 1.8rem;
  font-weight: 700;
  color: var(--success);
}

.program-amount span {
  font-size: 0.9rem;
  margin-left: 4px;
  color: var(--text-dim);
}

.program-name {
  font-size: 1.3rem;
  margin-bottom: 12px;
}

.program-desc {
  color: var(--text-secondary);
  font-size: 0.95rem;
  margin-bottom: 24px;
  flex: 1;
}

.program-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  padding-top: 20px;
  border-top: 1px solid var(--glass-border);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.8rem;
  color: var(--text-dim);
  font-weight: 500;
}

.claim-btn {
  width: 100%;
  justify-content: center;
}

.info-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: rgba(139, 92, 246, 0.05);
  border-color: rgba(139, 92, 246, 0.1);
}

.info-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.info-header h4 {
  margin: 0;
}

.skeleton-card {
  height: 300px;
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
