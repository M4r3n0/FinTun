<script setup>
import { ref, onMounted } from 'vue';
import { 
  ShieldCheck, 
  Activity, 
  Users, 
  AlertCircle, 
  TrendingUp, 
  ChevronRight,
  UserCheck,
  XCircle,
  Eye
} from 'lucide-vue-next';
import api from '../services/api';

const pendingUsers = ref([]);
const selectedUser = ref(null);
const userDocs = ref([]);
const isLoadingUsers = ref(false);
const isLoadingDocs = ref(false);
const isReviewing = ref(false);

const fetchPendingUsers = async () => {
  isLoadingUsers.value = true;
  try {
    pendingUsers.value = await api.getPendingKycUsers();
  } catch (err) {
    console.error('Failed to fetch pending users:', err);
  } finally {
    isLoadingUsers.value = false;
  }
};

const selectUser = async (user) => {
  selectedUser.value = user;
  isLoadingDocs.value = true;
  userDocs.value = [];
  try {
    userDocs.value = await api.getKycDocs(user.id);
  } catch (err) {
    console.error('Failed to fetch user documents:', err);
  } finally {
    isLoadingDocs.value = false;
  }
};

const processReview = async (approve) => {
  if (!selectedUser.value) return;
  isReviewing.value = true;
  try {
    await api.processKycReview(selectedUser.value.id, approve);
    alert(`User ${approve ? 'Approved' : 'Rejected'} successfully.`);
    selectedUser.value = null;
    fetchPendingUsers();
  } catch (err) {
    alert('Failed to process review: ' + err.message);
  } finally {
    isReviewing.value = false;
  }
};

onMounted(fetchPendingUsers);
</script>

<template>
  <div class="admin-view">
    <header class="view-header">
      <h1 class="text-gradient">Admin Center</h1>
      <p class="text-secondary">System-wide monitoring and identity verification.</p>
    </header>

    <div class="admin-grid">
      <div class="glass-card stat-card">
        <div class="card-header">
          <Activity :size="20" class="text-primary" />
          <span>System Health</span>
        </div>
        <div class="card-value">Operational</div>
        <div class="card-meta">All services responding</div>
      </div>

      <div class="glass-card stat-card">
        <div class="card-header">
          <Users :size="20" class="text-primary" />
          <span>Pending KYC</span>
        </div>
        <div class="card-value">{{ pendingUsers.length }}</div>
        <div class="card-meta">Awaiting manual review</div>
      </div>
    </div>

    <div class="admin-main">
      <div class="glass-card queue-card">
        <div class="section-header">
          <h3>KYC Verification Queue</h3>
          <button class="btn btn-secondary btn-sm" @click="fetchPendingUsers">Refresh</button>
        </div>

        <div v-if="isLoadingUsers" class="loading-state">Loading users...</div>
        
        <div v-else-if="pendingUsers.length === 0" class="empty-state">
          <ShieldCheck :size="48" class="text-success" style="margin-bottom: 16px;" />
          <p>Great! No pending KYC requests.</p>
        </div>

        <div v-else class="user-list">
          <div 
            v-for="user in pendingUsers" 
            :key="user.id" 
            class="user-item"
            :class="{ active: selectedUser?.id === user.id }"
            @click="selectUser(user)"
          >
            <div class="user-info">
              <span class="user-name">{{ user.phoneNumber }}</span>
              <span class="user-id">ID: {{ user.id.slice(0, 8) }}</span>
            </div>
            <ChevronRight :size="18" />
          </div>
        </div>
      </div>

      <div class="glass-card details-card">
        <div v-if="!selectedUser" class="details-placeholder">
          <UserCheck :size="48" class="text-dim" />
          <p>Select a user from the queue to review their documents.</p>
        </div>

        <div v-else class="user-details">
          <h3>Review: {{ selectedUser.phoneNumber }}</h3>

          <!-- User Profile Section -->
          <div class="user-profile-info glass">
            <div class="profile-grid">
              <div class="profile-item">
                <label>Full Name</label>
                <span>{{ selectedUser.fullName }}</span>
              </div>
              <div class="profile-item">
                <label>National ID</label>
                <span>{{ selectedUser.nationalId || 'N/A' }}</span>
              </div>
              <div class="profile-item">
                <label>Email</label>
                <span>{{ selectedUser.email }}</span>
              </div>
              <div class="profile-item">
                <label>Date of Birth</label>
                <span>{{ selectedUser.dateOfBirth || 'N/A' }}</span>
              </div>
              <div class="profile-item full-width">
                <label>Address</label>
                <span>{{ selectedUser.address || 'N/A' }}</span>
              </div>
            </div>
          </div>
          
          <div v-if="isLoadingDocs" class="loading-state">Loading documents...</div>
          
          <div v-else class="document-viewer">
            <div v-for="doc in userDocs" :key="doc.id" class="doc-item">
              <div class="doc-header">
                <span class="doc-type-badge">{{ doc.type }}</span>
                <span :class="doc.status.toLowerCase()" class="doc-status">{{ doc.status }}</span>
              </div>
              <div class="doc-preview">
                <img :src="'http://localhost:8081' + doc.fileUrl" alt="KYC Document" @error="e => e.target.src = 'https://via.placeholder.com/300?text=Doc+Preview'" />
                <div class="overlay">
                  <Eye :size="24" />
                </div>
              </div>
            </div>
          </div>

          <div class="review-actions">
            <button 
              class="btn btn-danger" 
              @click="processReview(false)" 
              :disabled="isReviewing"
            >
              <XCircle :size="18" /> Reject
            </button>
            <button 
              class="btn btn-success" 
              @click="processReview(true)" 
              :disabled="isReviewing"
            >
              <ShieldCheck :size="18" /> Approve
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-view {
  animation: fadeIn 0.4s ease-out;
}

.view-header {
  margin-bottom: 40px;
}

.admin-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.stat-card {
  padding: 24px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 0.9rem;
  color: var(--text-dim);
  margin-bottom: 16px;
}

.card-value {
  font-family: 'Outfit', sans-serif;
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 8px;
}

.card-meta {
  font-size: 0.85rem;
  color: var(--text-dim);
}

.admin-main {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.queue-card, .details-card {
  min-height: 480px;
}

.user-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.user-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid var(--glass-border);
  border-radius: 12px;
  cursor: pointer;
  transition: var(--transition);
}

.user-item:hover, .user-item.active {
  background: rgba(139, 92, 246, 0.08);
  border-color: var(--primary);
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-name {
  font-weight: 600;
  font-size: 0.95rem;
}

.user-id {
  font-size: 0.8rem;
  color: var(--text-dim);
}

.user-profile-info {
  margin: 20px 0;
  padding: 20px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid var(--glass-border);
  border-radius: 12px;
}

.profile-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.profile-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.profile-item.full-width {
  grid-column: span 2;
}

.profile-item label {
  font-size: 0.75rem;
  text-transform: uppercase;
  color: var(--text-dim);
  letter-spacing: 0.05em;
  font-weight: 700;
}

.profile-item span {
  font-size: 0.95rem;
  color: var(--text-primary);
  word-break: break-all;
}

.details-placeholder {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  color: var(--text-dim);
  text-align: center;
}

.document-viewer {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
  margin: 24px 0;
}

.doc-item {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--glass-border);
}

.doc-header {
  padding: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(255, 255, 255, 0.02);
}

.doc-type-badge {
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
  color: var(--text-dim);
}

.doc-status {
  font-size: 0.75rem;
  padding: 2px 8px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.05);
}

.doc-status.approved { color: var(--success); }
.doc-status.pending { color: var(--primary); }
.doc-status.rejected { color: var(--danger); }

.doc-preview {
  position: relative;
  aspect-ratio: 16/10;
  background: #111;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: zoom-in;
}

.doc-preview img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.review-actions {
  display: flex;
  gap: 16px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--glass-border);
}

.review-actions button {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.loading-state {
  padding: 40px;
  text-align: center;
  color: var(--text-dim);
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 1024px) {
  .admin-main {
    grid-template-columns: 1fr;
  }
}
</style>
