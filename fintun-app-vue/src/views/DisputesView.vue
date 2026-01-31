<script setup>
import { ref, onMounted } from 'vue';
import { useAuthStore } from '../stores/auth';
import api from '../services/api';
import { 
  MessageSquare, 
  Search, 
  CheckCircle, 
  AlertTriangle,
  Clock,
  ChevronRight
} from 'lucide-vue-next';

const auth = useAuthStore();
const disputes = ref([]);
const isLoading = ref(true);

const fetchDisputes = async () => {
  isLoading.value = true;
  try {
    const res = await api.getMyDisputes();
    disputes.value = res;
  } catch (err) {
    console.error('Failed to fetch disputes:', err);
  } finally {
    isLoading.value = false;
  }
};

onMounted(fetchDisputes);
</script>

<template>
  <div class="disputes-view">
    <header class="view-header">
      <h1 class="text-gradient">Support Center</h1>
      <p class="text-secondary">Track and manage your transaction disputes and resolutions.</p>
    </header>

    <div class="disputes-container glass-card">
      <div class="section-header">
        <h2 class="flex-center" style="gap: 12px;"><MessageSquare :size="24" /> Your Claims</h2>
      </div>

      <div v-if="isLoading" class="loading-state">
        <div class="skeleton-line" v-for="i in 3" :key="i"></div>
      </div>

      <div v-else-if="disputes.length === 0" class="empty-state">
        <Search :size="48" class="text-dim" style="margin-bottom: 20px;" />
        <p>No active disputes found. If you have an issue with a transaction, you can file a dispute directly from your wallet history.</p>
      </div>

      <div v-else class="disputes-list">
        <div v-for="d in disputes" :key="d.id" class="dispute-item glass">
          <div class="dispute-main">
            <div class="dispute-header">
              <span class="category">{{ d.category }}</span>
              <span class="status" :class="d.status.toLowerCase()">{{ d.status }}</span>
            </div>
            <p class="reason">"{{ d.reason }}"</p>
            <div class="meta">
              <Clock :size="14" /> Filed on {{ new Date(d.createdAt).toLocaleDateString() }}
            </div>
          </div>

          <div v-if="d.aiRecommendation" class="ai-box">
            <div class="ai-header">
              <CheckCircle :size="16" /> Fintun AI Recommendation
            </div>
            <p>{{ d.aiRecommendation }}</p>
          </div>
        </div>
      </div>
    </div>

    <div class="faq-section glass-card">
      <h3>Need help?</h3>
      <div class="faq-list">
        <div class="faq-item">
          <span>How long does resolution take?</span>
          <ChevronRight :size="16" />
        </div>
        <div class="faq-item">
          <span>What documents are required?</span>
          <ChevronRight :size="16" />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.disputes-view {
  animation: fadeIn 0.4s ease-out;
}

.view-header {
  margin-bottom: 40px;
}

.disputes-container {
  margin-bottom: 24px;
}

.section-header {
  margin-bottom: 32px;
}

.dispute-item {
  padding: 24px;
  border-radius: 16px;
  margin-bottom: 16px;
}

.dispute-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.category {
  font-weight: 700;
  color: var(--primary);
  text-transform: uppercase;
  font-size: 0.8rem;
  letter-spacing: 0.05em;
}

.status {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
}

.status.auto_resolved { background: rgba(16, 185, 129, 0.1); color: var(--success); }
.status.pending { background: rgba(255, 255, 255, 0.05); color: var(--text-secondary); }

.reason {
  font-style: italic;
  font-size: 1rem;
  color: var(--text-primary);
  margin-bottom: 16px;
}

.meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.85rem;
  color: var(--text-dim);
}

.ai-box {
  margin-top: 20px;
  padding: 16px;
  background: rgba(16, 185, 129, 0.05);
  border: 1px dashed rgba(16, 185, 129, 0.2);
  border-radius: 12px;
}

.ai-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.85rem;
  font-weight: 700;
  color: var(--success);
  margin-bottom: 8px;
}

.ai-box p {
  font-size: 0.9rem;
  color: var(--text-secondary);
  line-height: 1.5;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-dim);
}

.faq-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 20px;
}

.faq-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: var(--glass);
  border-radius: 12px;
  cursor: pointer;
  transition: var(--transition);
}

.faq-item:hover {
  background: rgba(255,255,255,0.05);
  color: var(--primary);
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
