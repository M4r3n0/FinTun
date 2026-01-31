import { defineStore } from 'pinia';
import api from '../services/api';

export const useAuthStore = defineStore('auth', {
    state: () => ({
        token: localStorage.getItem('token') || null,
        userId: localStorage.getItem('userId') || null,
        role: localStorage.getItem('role') || null,
        kycLevel: localStorage.getItem('kycLevel') || 'UNVERIFIED',
        user: null,
    }),
    getters: {
        isAuthenticated: (state) => !!state.token,
        isAdmin: (state) => state.role === 'ROLE_ADMIN',
        isVerified: (state) => state.kycLevel === 'VERIFIED',
    },
    actions: {
        async login(phoneNumber, password) {
            try {
                const res = await api.login(phoneNumber, password);
                this.setAuth(res.token, res.userId, res.role, res.kycLevel, phoneNumber);
                return res;
            } catch (error) {
                throw error;
            }
        },
        async register(data) {
            return await api.register(data);
        },
        async refreshUserStatus() {
            if (!this.userId) return;
            try {
                const res = await api.searchUserByPhone(localStorage.getItem('userPhone') || ''); // Fallback or use a proper profile endpoint
                if (res && res.kycLevel) {
                    this.kycLevel = res.kycLevel;
                    localStorage.setItem('kycLevel', res.kycLevel);
                }
            } catch (error) {
                console.error('Failed to refresh user status:', error);
            }
        },
        setAuth(token, userId, role, kycLevel, phoneNumber) {
            this.token = token;
            this.userId = userId;
            this.role = role;
            this.kycLevel = kycLevel;
            localStorage.setItem('token', token);
            localStorage.setItem('userId', userId);
            localStorage.setItem('role', role);
            localStorage.setItem('kycLevel', kycLevel);
            if (phoneNumber) localStorage.setItem('userPhone', phoneNumber);
        },
        logout() {
            this.token = null;
            this.userId = null;
            this.role = null;
            this.kycLevel = 'UNVERIFIED';
            localStorage.removeItem('token');
            localStorage.removeItem('userId');
            localStorage.removeItem('role');
            localStorage.removeItem('kycLevel');
            this.user = null;
        }
    }
});
