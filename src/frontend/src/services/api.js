export class ApiService {
    constructor() {
        this.token = localStorage.getItem('token');
        this.userId = localStorage.getItem('userId');
        this.role = localStorage.getItem('role');
    }

    setAuth(token, userId, role) {
        this.token = token;
        this.userId = userId;
        this.role = role;
        localStorage.setItem('token', token);
        localStorage.setItem('userId', userId);
        localStorage.setItem('role', role);
    }

    clearAuth() {
        this.token = null;
        this.userId = null;
        this.role = null;
        localStorage.removeItem('token');
        localStorage.removeItem('userId');
        localStorage.removeItem('role');
    }

    getHeaders() {
        const headers = { 'Content-Type': 'application/json' };
        if (this.token) {
            headers['Authorization'] = `Bearer ${this.token}`;
        }
        if (this.userId) {
            headers['X-User-Id'] = this.userId;
        }
        return headers;
    }

    async fetch(url, options = {}) {
        const response = await fetch(url, {
            ...options,
            headers: { ...this.getHeaders(), ...options.headers }
        });

        if (!response.ok) {
            const error = await response.json().catch(() => ({ message: response.statusText }));
            throw error;
        }

        const text = await response.text();
        return text ? JSON.parse(text) : {};
    }

    // --- Auth ---
    login(phoneNumber, password) {
        return this.fetch('/api/auth/login', {
            method: 'POST',
            body: JSON.stringify({ phoneNumber, password })
        });
    }

    register(data) {
        return this.fetch('/api/auth/register', {
            method: 'POST',
            body: JSON.stringify(data)
        });
    }

    // --- Wallet ---
    getAccounts() {
        return this.fetch(`/api/wallet/accounts/user/${this.userId}`);
    }

    getAccountsByUserId(userId) {
        return this.fetch(`/api/wallet/accounts/user/${userId}`);
    }

    createAccount(currency) {
        return this.fetch('/api/wallet/accounts', {
            method: 'POST',
            body: JSON.stringify({ userId: this.userId, currency })
        });
    }

    transfer(fromId, toId, amount) {
        return this.fetch('/api/wallet/ledger/transfer', {
            method: 'POST',
            body: JSON.stringify({ sourceAccountId: fromId, destinationAccountId: toId, amount })
        });
    }

    getHistory() {
        return this.fetch(`/api/wallet/ledger/history/${this.userId}`);
    }

    // --- KYC ---
    getKycDocs() {
        return this.fetch(`/api/kyc/documents?userId=${this.userId}`);
    }

    uploadKycDoc(type, file) {
        const formData = new FormData();
        formData.append('type', type);
        formData.append('file', file);

        const headers = {};
        if (this.token) headers['Authorization'] = `Bearer ${this.token}`;

        return fetch(`/api/kyc/upload/${this.userId}`, {
            method: 'POST',
            body: formData,
            headers
        }).then(res => res.json());
    }

    getKycToken() {
        return this.fetch(`/api/kyc/token?userId=${this.userId}`);
    }

    mockVerify() {
        return this.fetch(`/api/kyc/review/${this.userId}?approve=true`, {
            method: 'POST'
        });
    }

    // --- Subsidies ---
    getSubsidies() {
        return this.fetch('/api/subsidies');
    }

    checkEligibility(programId) {
        return this.fetch(`/api/subsidies/eligibility/${programId}?userId=${this.userId}`);
    }

    claimSubsidy(programId) {
        return this.fetch('/api/subsidies/claim', {
            method: 'POST',
            body: JSON.stringify({ programId, userId: this.userId })
        });
    }

    // --- Disputes ---
    fileDispute(paymentId, reason) {
        return this.fetch('/api/disputes', {
            method: 'POST',
            body: JSON.stringify({ paymentId, reason })
        });
    }

    getMyDisputes() {
        return this.fetch('/api/disputes/my-disputes');
    }

    // --- Users ---
    searchUserByPhone(phoneNumber) {
        return this.fetch(`/api/users/search?phoneNumber=${encodeURIComponent(phoneNumber)}`);
    }

    transferP2P(senderAccountId, receiverAccountId, amount) {
        return this.fetch('/api/payment/p2p', {
            method: 'POST',
            body: JSON.stringify({ senderAccountId, receiverAccountId, amount })
        });
    }

    // --- Notifications ---
    updateFcmToken(token) {
        return this.fetch(`/api/users/fcm-token?userId=${this.userId}&token=${encodeURIComponent(token)}`, {
            method: 'POST'
        });
    }
}

export const api = new ApiService();
