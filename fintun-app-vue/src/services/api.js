import axios from 'axios';

const api = axios.create({
    baseURL: '', // Proxied by Vite
    headers: {
        'Content-Type': 'application/json',
    },
});

api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    const userId = localStorage.getItem('userId');

    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    if (userId) {
        config.headers['X-User-Id'] = userId;
    }
    return config;
});

api.interceptors.response.use(
    (response) => response.data,
    (error) => {
        const message = error.response?.data?.message || error.message || 'Something went wrong';
        return Promise.reject({ message });
    }
);

export default {
    // Auth
    login: (phoneNumber, password) => api.post('/api/auth/login', { phoneNumber, password }),
    register: (data) => api.post('/api/auth/register', data),

    // Wallet
    getAccounts: (userId) => api.get(`/api/wallet/accounts/user/${userId}`),
    createAccount: (userId, currency) => api.post('/api/wallet/accounts', { userId, currency }),
    getHistory: (userId) => api.get(`/api/wallet/ledger/history/${userId}`),

    // KYC
    getKycDocs: (userId) => api.get(`/api/kyc/documents?userId=${userId}`),
    getKycToken: (userId) => api.get(`/api/kyc/token?userId=${userId}`),
    uploadKycDoc: (userId, type, file) => {
        const formData = new FormData();
        formData.append('userId', userId);
        formData.append('type', type);
        formData.append('file', file);
        return api.post('/api/kyc/upload', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        });
    },
    getPendingKycUsers: () => api.get('/api/kyc/pending'),
    mockVerify: (userId) => api.post(`/api/kyc/review/${userId}?approve=true`),
    processKycReview: (userId, approve) => api.post(`/api/kyc/review/${userId}?approve=${approve}`),

    // Subsidies
    getSubsidies: () => api.get('/api/subsidies'),
    claimSubsidy: (programId, userId) => api.post('/api/subsidies/claim', { programId, userId }),

    // Disputes
    fileDispute: (paymentId, reason) => api.post('/api/disputes', { paymentId, reason }),
    getMyDisputes: () => api.get('/api/disputes/my-disputes'),

    // Users & P2P
    searchUserByPhone: (phoneNumber) => api.get(`/api/users/search?phoneNumber=${encodeURIComponent(phoneNumber)}`),
    transferP2P: (senderAccountId, receiverAccountId, amount) =>
        api.post('/api/payment/p2p', { senderAccountId, receiverAccountId, amount }),

    // Notifications
    updateFcmToken: (userId, token) => api.post(`/api/users/fcm-token?userId=${userId}&token=${encodeURIComponent(token)}`),
};
