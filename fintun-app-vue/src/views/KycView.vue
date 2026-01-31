<script setup>
import { ref, onMounted } from 'vue';
import { useAuthStore } from '../stores/auth';
import api from '../services/api';
import { 
  ShieldCheck, 
  ShieldAlert, 
  ShieldClose,
  ChevronRight,
  UserCheck,
  Smartphone,
  Info
} from 'lucide-vue-next';

const auth = useAuthStore();
const status = ref('UNVERIFIED');
const isLoading = ref(true);
const isUploading = ref(false);
const currentStep = ref(1); // 1: ID Card, 2: Selfie

const idFile = ref(null);
const selfieFile = ref(null);

const checkVerificationStatus = async () => {
  isLoading.value = true;
  try {
    const docs = await api.getKycDocs(auth.userId);
    if (docs.length > 0) {
      // Find latest status
      status.value = docs[0].status;
    }
  } catch (err) {
    console.error('Failed to fetch KYC status:', err);
  } finally {
    isLoading.value = false;
  }
};

const handleFileChange = (e, type) => {
  const file = e.target.files[0];
  if (type === 'id') idFile.value = file;
  if (type === 'selfie') selfieFile.value = file;
};

const uploadDocuments = async () => {
  if (!idFile.value || !selfieFile.value) {
    alert('Please select both documents.');
    return;
  }

  isUploading.value = true;
  try {
    // Sequential uploads for demo simplicity
    await api.uploadKycDoc(auth.userId, 'ID_CARD_FRONT', idFile.value);
    await api.uploadKycDoc(auth.userId, 'SELFIE', selfieFile.value);
    
    status.value = 'PENDING';
    alert('Documents uploaded successfully! Waiting for admin review.');
  } catch (err) {
    alert('Upload failed: ' + err.message);
  } finally {
    isUploading.value = false;
  }
};

onMounted(checkVerificationStatus);
</script>

<template>
  <div class="kyc-view">
    <header class="view-header">
      <h1 class="text-gradient">Identity Verification</h1>
      <p class="text-secondary">Secure your account by verifying your identity.</p>
    </header>

    <div class="kyc-container">
      <div class="glass-card status-card">
        <div class="status-info">
          <div class="status-header">
            <span class="text-dim">Current Status:</span>
            <strong :class="status.toLowerCase()" class="status-badge">{{ status }}</strong>
          </div>
          <p v-if="status === 'UNVERIFIED'" class="status-desc">
            To access all features and higher transaction limits, please complete your identity check.
          </p>
          <p v-else-if="status === 'PENDING'" class="status-desc">
            Our team is currently reviewing your documents. This usually takes less than 24 hours.
          </p>
          <p v-else-if="status === 'REJECTED'" class="status-desc">
            Your verification was declined. Please review the requirements and try again.
          </p>
          <p v-else class="status-desc">
            Your identity is verified. You have full access to Fintun services.
          </p>
        </div>
        
        <div class="status-visual">
          <ShieldCheck v-if="status === 'VERIFIED' || status === 'APPROVED'" :size="60" class="text-success" />
          <ShieldAlert v-else-if="status === 'PENDING'" :size="60" class="text-primary" />
          <ShieldAlert v-else-if="status === 'REJECTED'" :size="60" class="text-danger" />
          <ShieldClose v-else :size="60" class="text-dim" />
        </div>
      </div>

      <div v-if="status === 'REJECTED'" class="glass-card retry-card">
        <button class="btn btn-primary" @click="status = 'UNVERIFIED'; currentStep = 1; idFile = null; selfieFile = null;">
          Restart Verification
        </button>
      </div>

      <div v-if="status === 'UNVERIFIED'" class="glass-card flow-card">
        <div class="upload-wrapper">
          <div class="upload-steps">
            <div :class="{ active: currentStep === 1 }" class="step-item">
              <div class="step-number">1</div>
              <div class="step-text">National ID</div>
            </div>
            <div :class="{ active: currentStep === 2 }" class="step-item">
              <div class="step-number">2</div>
              <div class="step-text">Selfie</div>
            </div>
          </div>

          <div v-if="currentStep === 1" class="upload-content">
            <ShieldAlert :size="48" class="text-primary" />
            <h3>Upload National ID</h3>
            <p>Please upload a clear photo of your ID Card (Front).</p>
            <div class="file-input-wrapper">
              <input 
                type="file" 
                id="id-upload" 
                accept="image/*" 
                @change="e => handleFileChange(e, 'id')" 
                class="file-input"
              />
              <label for="id-upload" class="btn btn-secondary">
                {{ idFile ? idFile.name : 'Select File' }}
              </label>
            </div>
            <button v-if="idFile" class="btn btn-primary" @click="currentStep = 2">
              Next Step <ChevronRight :size="18" />
            </button>
          </div>

          <div v-if="currentStep === 2" class="upload-content">
            <UserCheck :size="48" class="text-primary" />
            <h3>Upload Selfie</h3>
            <p>Take a selfie holding your ID to confirm identity.</p>
            <div class="file-input-wrapper">
              <input 
                type="file" 
                id="selfie-upload" 
                accept="image/*" 
                @change="e => handleFileChange(e, 'selfie')" 
                class="file-input"
              />
              <label for="selfie-upload" class="btn btn-secondary">
                {{ selfieFile ? selfieFile.name : 'Select File' }}
              </label>
            </div>
            <div class="action-buttons">
              <button class="btn btn-dim" @click="currentStep = 1">Back</button>
              <button 
                v-if="selfieFile" 
                class="btn btn-primary" 
                @click="uploadDocuments"
                :disabled="isUploading"
              >
                {{ isUploading ? 'Uploading...' : 'Submit Verification' }}
              </button>
            </div>
          </div>
        </div>
      </div>

      <div class="glass-card info-card">
        <h3>Why verify?</h3>
        <div class="benefits-list">
          <div class="benefit-item">
            <Smartphone :size="20" />
            <div>
              <p class="benefit-title">Higher Limits</p>
              <p class="benefit-desc">Increase your daily transfer and withdrawal limits.</p>
            </div>
          </div>
          <div class="benefit-item">
            <ShieldCheck :size="20" />
            <div>
              <p class="benefit-title">Enhanced Security</p>
              <p class="benefit-desc">Protect your account against unauthorized access.</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.kyc-view {
  animation: fadeIn 0.4s ease-out;
}

.view-header {
  margin-bottom: 32px;
}

.kyc-container {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 24px;
}

.status-card {
  grid-column: span 2;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32px;
}

.status-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.9rem;
  background: rgba(255, 255, 255, 0.05);
}

.status-badge.verified, .status-badge.approved { color: var(--success); background: rgba(16, 185, 129, 0.1); }
.status-badge.pending { color: var(--primary); background: rgba(139, 92, 246, 0.1); }

.status-desc {
  color: var(--text-secondary);
  max-width: 500px;
}

.flow-card {
  padding: 0;
  overflow: hidden;
  min-height: 400px;
}

.upload-wrapper {
  padding: 40px;
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.upload-steps {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-bottom: 8px;
}

.step-item {
  display: flex;
  align-items: center;
  gap: 12px;
  color: var(--text-dim);
  opacity: 0.5;
  transition: var(--transition);
}

.step-item.active {
  color: var(--primary);
  opacity: 1;
}

.step-number {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--glass);
  border: 1px solid var(--glass-border);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 0.9rem;
}

.step-item.active .step-number {
  background: var(--primary);
  border-color: var(--primary);
  color: white;
}

.upload-content {
  text-align: center;
  max-width: 400px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.file-input-wrapper {
  margin: 16px 0;
  width: 100%;
}

.file-input {
  display: none;
}

.action-buttons {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

.info-card h3 {
  font-size: 1.1rem;
  margin-bottom: 24px;
}

.benefits-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.benefit-item {
  display: flex;
  gap: 16px;
}

.benefit-item svg {
  color: var(--primary);
  flex-shrink: 0;
}

.benefit-title {
  font-weight: 600;
  margin-bottom: 4px;
}

.benefit-desc {
  font-size: 0.85rem;
  color: var(--text-dim);
}

.mock-card {
  border-color: rgba(139, 92, 246, 0.3);
  background: rgba(139, 92, 246, 0.05);
  text-align: center;
}

.mock-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 16px;
  color: var(--primary);
}

.mock-card p {
  margin-bottom: 24px;
  color: var(--text-secondary);
}

@media (max-width: 1024px) {
  .kyc-container {
    grid-template-columns: 1fr;
  }
  .status-card {
    grid-column: span 1;
  }
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
