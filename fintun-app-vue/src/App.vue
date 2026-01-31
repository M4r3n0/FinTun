<script setup>
import { ref, onMounted } from 'vue';
import { useAuthStore } from './stores/auth';
import { useRouter, useRoute } from 'vue-router';
import { 
  LayoutDashboard, 
  Wallet, 
  UserCheck, 
  Coins, 
  MessageSquare, 
  Settings, 
  LogOut,
  ShieldHalf
} from 'lucide-vue-next';

const auth = useAuthStore();
const router = useRouter();
const route = useRoute();

onMounted(() => {
  if (auth.isAuthenticated) {
    auth.refreshUserStatus();
  }
});

const handleLogout = () => {
  auth.logout();
  router.push('/login');
};

const menuItems = [
  { name: 'Dashboard', path: '/', icon: LayoutDashboard },
  { name: 'Wallet', path: '/wallet', icon: Wallet },
  { name: 'Transfer', path: '/transfer', icon: Coins },
  { name: 'Verification', path: '/kyc', icon: UserCheck },
  { name: 'Subsidies', path: '/subsidies', icon: Coins },
  { name: 'Support', path: '/disputes', icon: MessageSquare },
];
</script>

<template>
  <div class="app-container" v-if="auth.isAuthenticated && route.meta.auth">
    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="logo">
          <span class="logo-dot"></span>
          Fintun
        </div>
      </div>

      <nav class="sidebar-nav">
        <router-link 
          v-for="item in menuItems" 
          :key="item.path" 
          :to="item.path"
          class="nav-item"
          :class="{ active: route.path === item.path }"
        >
          <component :is="item.icon" :size="20" />
          {{ item.name }}
        </router-link>

        <router-link 
          v-if="auth.isAdmin" 
          to="/admin" 
          class="nav-item admin-item"
          :class="{ active: route.path === '/admin' }"
        >
          <ShieldHalf :size="20" />
          Admin Center
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <button @click="handleLogout" class="btn btn-glass logout-btn">
          <LogOut :size="18" />
          Logout
        </button>
      </div>
    </aside>

    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
  
  <div v-else class="auth-layout">
    <router-view />
  </div>
</template>

<style scoped>
.app-container {
  display: flex;
  min-height: 100vh;
}

.sidebar {
  width: 280px;
  background: var(--bg-secondary);
  border-right: 1px solid var(--glass-border);
  display: flex;
  flex-direction: column;
  padding: 32px 24px;
  position: fixed;
  height: 100vh;
}

.sidebar-header {
  margin-bottom: 48px;
}

.logo {
  font-family: 'Outfit', sans-serif;
  font-size: 24px;
  font-weight: 800;
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-dot {
  width: 12px;
  height: 12px;
  background-color: var(--primary);
  border-radius: 50%;
  box-shadow: 0 0 10px var(--primary);
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 12px;
  color: var(--text-secondary);
  font-weight: 500;
  transition: var(--transition);
}

.nav-item:hover {
  background: var(--glass);
  color: var(--text-primary);
}

.nav-item.active {
  background: var(--glass);
  color: var(--primary);
  box-shadow: inset 0 0 0 1px var(--glass-border);
}

.admin-item {
  margin-top: 16px;
  color: var(--primary);
}

.sidebar-footer {
  margin-top: auto;
}

.logout-btn {
  width: 100%;
  justify-content: flex-start;
  color: var(--text-dim);
}

.logout-btn:hover {
  color: var(--danger);
  border-color: rgba(239, 68, 68, 0.2);
}

.main-content {
  flex: 1;
  margin-left: 280px;
  padding: 48px;
  max-width: 1200px;
}

.auth-layout {
  min-height: 100vh;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>
