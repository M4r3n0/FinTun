import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '../stores/auth';

const routes = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('../views/LoginView.vue'),
        meta: { guest: true }
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('../views/RegisterView.vue'),
        meta: { guest: true }
    },
    {
        path: '/',
        name: 'Dashboard',
        component: () => import('../views/DashboardView.vue'),
        meta: { auth: true }
    },
    {
        path: '/wallet',
        name: 'Wallet',
        component: () => import('../views/WalletView.vue'),
        meta: { auth: true }
    },
    {
        path: '/kyc',
        name: 'Verification',
        component: () => import('../views/KycView.vue'),
        meta: { auth: true }
    },
    {
        path: '/subsidies',
        name: 'Subsidies',
        component: () => import('../views/SubsidiesView.vue'),
        meta: { auth: true }
    },
    {
        path: '/transfer',
        name: 'Transfer',
        component: () => import('../views/TransferView.vue'),
        meta: { auth: true }
    },
    {
        path: '/disputes',
        name: 'Support',
        component: () => import('../views/DisputesView.vue'),
        meta: { auth: true }
    },
    {
        path: '/admin',
        name: 'Admin',
        component: () => import('../views/AdminView.vue'),
        meta: { auth: true, admin: true }
    }
    // Add other routes as needed
];

const router = createRouter({
    history: createWebHistory(),
    routes
});

router.beforeEach((to, from, next) => {
    const auth = useAuthStore();

    if (to.meta.auth && !auth.isAuthenticated) {
        next('/login');
    } else if (to.meta.admin && !auth.isAdmin) {
        next('/');
    } else if (to.meta.guest && auth.isAuthenticated) {
        next('/');
    } else {
        next();
    }
});

export default router;
