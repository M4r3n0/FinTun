import { api } from './services/api.js';

const app = document.getElementById('app');

// State Management
let currentState = {
  user: null,
  view: 'login', // login, dashboard, wallet, kyc, subsidies, disputes, admin
  isLoading: false
};

// Route Definitions
const routes = {
  login: renderLogin,
  register: renderRegister,
  dashboard: renderDashboard,
  wallet: renderWallet,
  kyc: renderKyc,
  subsidies: renderSubsidies,
  disputes: renderDisputes,
  admin: renderAdmin,
  p2p: renderP2pTransfer
};

function navigate(view) {
  currentState.view = view;
  const renderer = routes[view] || renderLogin;
  app.innerHTML = '';
  renderer();
}

// Admin View Implementation
async function renderAdmin() {
  renderDashboard();
  const grid = document.getElementById('dashboard-widgets');
  grid.innerHTML = `
    <div class="glass-card" style="grid-column: span 3;">
      <h2 style="margin-bottom: 32px;">System Administration</h2>
      <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 32px;">
        <div class="glass" style="padding: 24px;">
          <h3 style="margin-bottom: 16px;">Pending Disputes</h3>
          <div id="admin-disputes-list" class="text-muted">Loading queue...</div>
        </div>
        <div class="glass" style="padding: 24px;">
          <h3 style="margin-bottom: 16px;">User Activity</h3>
          <p class="text-dim">User management and KYC approval tools.</p>
          <button class="btn btn-primary" style="margin-top: 16px;">Export System Audit</button>
        </div>
      </div>
    </div>
  `;

  try {
    const apiService = await import('./services/api.js').then(m => m.api);
    // Note: This would typically hit /api/admin/all/disputes or similar
    // For now, illustrating the UI structure
    document.getElementById('admin-disputes-list').innerHTML = '<p>No pending escalations.</p>';
  } catch (e) {
    console.error(e);
  }
}

// Initial Load
window.addEventListener('load', () => {
  if (api.token) {
    navigate('dashboard');
  } else {
    navigate('login');
  }
});

// Renderers (Placeholders to be implemented in separate modules)
function renderLogin() {
  app.innerHTML = `
    <div class="flex-center" style="min-height: 100vh;">
      <div class="glass-card" style="width: 400px;">
        <h1 style="margin-bottom: 8px;">Welcome to TunFin</h1>
        <p class="text-muted" style="margin-bottom: 32px;">Secure banking for the modern age.</p>
        
        <form id="login-form">
          <div class="input-group">
            <span class="input-label">Phone Number</span>
            <input type="tel" id="phoneNumber" placeholder="216 12345678" required>
          </div>
          <div class="input-group">
            <span class="input-label">Password</span>
            <input type="password" id="password" placeholder="••••••••" required>
          </div>
          <button type="submit" class="btn btn-primary" style="width: 100%; justify-content: center;">Sign In</button>
        </form>
        
        <p style="margin-top: 24px; text-align: center; font-size: 0.9rem;">
          <span class="text-muted">New here?</span> 
          <a href="#" class="text-primary" style="text-decoration: none; font-weight: 500;" onclick="window.navigate('register'); return false;">Create account</a>
        </p>
      </div>
    </div>
  `;

  document.getElementById('login-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const phoneNumber = document.getElementById('phoneNumber').value;
    const password = document.getElementById('password').value;

    try {
      const res = await api.login(phoneNumber, password);
      api.setAuth(res.token, res.userId, res.role);
      initNotifications();
      navigate('dashboard');
    } catch (err) {
      alert(err.message || 'Login failed');
    }
  });
}

function renderRegister() {
  app.innerHTML = `
    <div class="flex-center" style="min-height: 100vh; padding: 40px 0;">
      <div class="glass-card" style="width: 480px;">
        <h1 style="margin-bottom: 8px;">Create Your Account</h1>
        <p class="text-muted" style="margin-bottom: 32px;">Join TunFin for a premium banking experience.</p>
        
        <form id="register-form">
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
            <div class="input-group">
              <span class="input-label">Full Name</span>
              <input type="text" id="fullName" placeholder="John Doe" required>
            </div>
            <div class="input-group">
              <span class="input-label">Phone Number</span>
              <input type="tel" id="regPhoneNumber" placeholder="216 12345678" required>
            </div>
          </div>
          
          <div class="input-group">
            <span class="input-label">Email Address</span>
            <input type="email" id="email" placeholder="john@example.com" required>
          </div>
          
          <div class="input-group">
            <span class="input-label">National ID</span>
            <input type="text" id="nationalId" placeholder="ID Number" required>
          </div>
          
          <div class="input-group">
             <span class="input-label">Password</span>
             <input type="password" id="reg-password" placeholder="••••••••" required>
          </div>
           <div class="input-group">
             <span class="input-label">Birth Date</span>
             <input type="date" id="dateOfBirth" required>
          </div>
          
          <p class="text-dim" style="font-size: 0.75rem; margin-bottom: 24px;">
            Tip: Use "AdminAcc" in your Name or Email to enable Admin privileges.
          </p>

          <button type="submit" class="btn btn-primary" style="width: 100%; justify-content: center;">Create Account</button>
        </form>
        
        <p style="margin-top: 24px; text-align: center; font-size: 0.9rem;">
          <span class="text-muted">Already have an account?</span> 
          <a href="#" class="text-primary" style="text-decoration: none; font-weight: 500;" onclick="window.navigate('login'); return false;">Sign In</a>
        </p>
      </div>
    </div>
  `;

  document.getElementById('register-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const data = {
      fullName: document.getElementById('fullName').value,
      phoneNumber: document.getElementById('regPhoneNumber').value,
      email: document.getElementById('email').value,
      nationalId: document.getElementById('nationalId').value,
      password: document.getElementById('reg-password').value,
      dateOfBirth: document.getElementById('dateOfBirth').value,
      address: 'Tunisia Central' // Default address for POC
    };

    try {
      const res = await api.register(data);
      alert('Registration successful! Please login with your phone number.');
      navigate('login');
    } catch (err) {
      alert(err.message || 'Registration failed');
    }
  });
}

function renderDashboard() {
  app.innerHTML = `
    <div style="display: flex; min-height: 100vh;">
      <!-- Sidebar -->
      <nav class="glass" style="width: 260px; margin: 16px; padding: 24px; display: flex; flex-direction: column;">
        <h2 style="margin-bottom: 40px; display: flex; align-items: center; gap: 10px;">
          <div style="width: 32px; height: 32px; background: var(--primary); border-radius: 8px;"></div>
          TunFin
        </h2>
        
        <div style="display: flex; flex-direction: column; gap: 8px; flex: 1;">
          <button class="btn btn-glass" style="width: 100%; border: none;" onclick="window.navigate('dashboard')">Dashboard</button>
          <button class="btn btn-glass" style="width: 100%; border: none;" onclick="window.navigate('wallet')">Wallet</button>
          <button class="btn btn-glass" style="width: 100%; border: none;" onclick="window.navigate('kyc')">Verification</button>
          <button class="btn btn-glass" style="width: 100%; border: none;" onclick="window.navigate('subsidies')">Subsidies</button>
          <button class="btn btn-glass" style="width: 100%; border: none;" onclick="window.navigate('disputes')">Support</button>
          ${api.role === 'ROLE_ADMIN' ? `<button class="btn btn-glass" style="width: 100%; border: none; color: var(--primary);" onclick="window.navigate('admin')">Admin Center</button>` : ''}
        </div>
        
        <button class="btn btn-glass" style="width: 100%; margin-top: auto;" id="logout-btn">Logout</button>
      </nav>

      <!-- Main Content -->
      <main style="flex: 1; padding: 40px; overflow-y: auto;">
        <header style="display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 48px;">
          <div>
            <h1 class="text-gradient" style="font-size: 2.5rem; margin-bottom: 8px;">Portfolio Overview</h1>
            <p class="text-muted">Welcome back, ${api.userId.substring(0, 8)}</p>
          </div>
          <button class="btn btn-primary" onclick="window.navigate('wallet')">Manage Wallets</button>
        </header>

        <div id="dashboard-widgets" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 24px;">
           <div class="glass-card" style="grid-column: span 2;">
              <h3 class="text-muted" style="margin-bottom: 16px; font-size: 0.9rem; text-transform: uppercase; letter-spacing: 0.05em;">Total Portfolio Value</h3>
              <div id="total-balance-display">
                <p style="font-size: 3rem; font-weight: 700; font-family: 'Outfit';">Loading...</p>
              </div>
              <div style="margin-top: 24px; display: flex; gap: 16px;">
                <div class="text-success" style="font-size: 0.9rem; font-weight: 600;">↑ Live from Ledger</div>
              </div>
           </div>
           
           <div class="glass-card">
              <h3 style="margin-bottom: 16px;">Fast Actions</h3>
              <div style="display: flex; flex-direction: column; gap: 12px;">
                <button class="btn btn-glass" style="justify-content: flex-start;" onclick="window.navigate('p2p')">Send Money</button>
                <button class="btn btn-glass" style="justify-content: flex-start;" onclick="window.navigate('kyc')">Verify Identity</button>
              </div>
           </div>
        </div>
      </main>
    </div>
  `;

  // Fetch real balance for dashboard
  api.getAccounts().then(accounts => {
    const total = accounts.reduce((sum, acc) => sum + acc.balance, 0);
    const display = document.getElementById('total-balance-display');
    if (display) {
      display.innerHTML = `<p style="font-size: 3rem; font-weight: 700; font-family: 'Outfit';">${total.toLocaleString('en-US', { minimumFractionDigits: 2 })} <span style="font-size: 1.5rem; color: var(--text-muted);">TND</span></p>`;
    }
  }).catch(() => {
    const display = document.getElementById('total-balance-display');
    if (display) display.innerHTML = '<p class="text-muted">Balance unavailable</p>';
  });

  document.getElementById('logout-btn').addEventListener('click', () => {
    api.clearAuth();
    navigate('login');
  });
}

// Global scope access for simple event handlers in HTML strings
window.navigate = navigate;

// Module level functions for specific views (will be moved to files later)
async function renderWallet() {
  renderDashboard();
  const grid = document.getElementById('dashboard-widgets');
  grid.innerHTML = '<div class="glass-card" style="grid-column: span 3;">Loading Wallet...</div>';

  try {
    const accounts = await api.getAccounts();
    grid.innerHTML = `
      <div class="glass-card" style="grid-column: span 3;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 32px;">
          <h2>Your Digital Wallets</h2>
          <button class="btn btn-primary" id="open-wallet-btn">+ New Account</button>
        </div>
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 24px;">
          ${accounts.map(acc => `
            <div class="glass" style="padding: 32px; background: linear-gradient(135deg, rgba(255,255,255,0.03) 0%, rgba(255,255,255,0.01) 100%);">
              <div style="display: flex; justify-content: space-between; margin-bottom: 24px;">
                <span class="text-muted" style="font-weight: 500;">${acc.currency} / TUNISIA</span>
                <span style="font-size: 1.2rem;">💰</span>
              </div>
              <p style="font-size: 2.2rem; font-weight: 700; font-family: 'Outfit'; margin-bottom: 8px;">${acc.balance.toLocaleString('en-US', { minimumFractionDigits: 2 })}</p>
              <p class="text-dim" style="font-size: 0.8rem; letter-spacing: 0.1em;">•••• •••• ${acc.id.substring(acc.id.length - 4)}</p>
            </div>
          `).join('')}
        </div>
        
        <div style="margin-top: 48px;">
          <h3 style="margin-bottom: 20px;">Recent Activity</h3>
          <div id="transaction-feed" class="glass" style="padding: 8px;"></div>
        </div>
      </div>
    `;

    document.getElementById('open-wallet-btn').onclick = async () => {
      const currency = prompt('Enter currency code (e.g. TND, USD):', 'TND');
      if (!currency) return;
      try {
        await api.createAccount(currency);
        renderWallet();
      } catch (e) { alert(e.message); }
    };

    renderHistory();
  } catch (err) {
    grid.innerHTML = `<div class="glass-card" style="color: var(--accent-red);">Failed to load wallet.</div>`;
  }
}

async function renderHistory() {
  const container = document.getElementById('transaction-feed');
  if (!container) return;
  try {
    const history = await api.getHistory();
    if (history.length === 0) {
      container.innerHTML = '<p class="text-muted" style="padding: 24px; text-align: center;">No transactions yet.</p>';
      return;
    }
    container.innerHTML = history.map(tx => `
      <div style="padding: 16px 24px; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--glass-border);">
        <div style="display: flex; align-items: center; gap: 16px;">
          <div class="flex-center" style="width: 40px; height: 40px; border-radius: 10px; background: ${tx.type === 'DEBIT' ? 'rgba(255,75,75,0.1)' : 'rgba(0,255,136,0.1)'};">
            <span style="color: ${tx.type === 'DEBIT' ? 'var(--accent-red)' : 'var(--accent-green)'}; font-weight: bold;">
              ${tx.type === 'DEBIT' ? '↓' : '↑'}
            </span>
          </div>
          <div>
            <p style="font-weight: 500;">${tx.type === 'DEBIT' ? 'Outgoing Payment' : 'Received Funds'}</p>
            <p class="text-dim" style="font-size: 0.75rem;">${new Date(tx.timestamp).toLocaleString()}</p>
          </div>
        </div>
        <div style="text-align: right;">
          <p style="font-weight: 700; color: ${tx.type === 'DEBIT' ? 'var(--text-main)' : 'var(--accent-green)'};">
            ${tx.type === 'DEBIT' ? '-' : '+'}${tx.amount.toFixed(2)} TND
          </p>
          <button class="btn" style="padding: 2px 8px; font-size: 0.7rem; border: 1px solid var(--primary); color: var(--primary); height: auto; margin-top: 4px;" onclick="window.reportIssue('${tx.id}')">Report</button>
        </div>
      </div>
    `).join('');
  } catch (e) {
    container.innerHTML = 'Error loading history.';
  }
}

window.reportIssue = (paymentId) => {
  const reason = prompt('What is the issue with this transaction?');
  if (!reason) return;
  api.fileDispute(paymentId, reason).then(() => {
    alert('Dispute filed successfully.');
    navigate('disputes');
  }).catch(e => alert(e.message));
};

async function renderKyc() {
  renderDashboard();
  const grid = document.getElementById('dashboard-widgets');
  grid.innerHTML = `
    <div class="glass-card" style="grid-column: span 3;" id="kyc-wizard">
      <h2 style="margin-bottom: 24px;">Identity Verification</h2>
      
      <div id="kyc-status" class="glass" style="padding: 16px; margin-bottom: 24px; border-left: 4px solid var(--text-muted);">
        Loading status...
      </div>

      <div class="glass" style="padding: 32px; text-align: center; border: 1px dashed rgba(255,255,255,0.1);">
        <p class="text-muted" style="margin-bottom: 24px;">To access all banking features, we need to verify your identity using our secure Sumsub integration.</p>
        <button class="btn btn-primary" id="kyc-submit" style="margin: 0 auto; display: flex;">Start Verification</button>
      </div>
    </div>
  `;

  const statusContainer = document.getElementById('kyc-status');

  const updateStatus = async () => {
    try {
      const docs = await api.getKycDocs();
      if (docs.length === 0) {
        statusContainer.innerHTML = '<span class="text-muted">Status:</span> <strong>UNVERIFIED</strong>';
        statusContainer.style.borderLeftColor = 'var(--text-muted)';
      } else {
        const status = docs[0].status;
        statusContainer.innerHTML = `<span class="text-muted">Status:</span> <strong class="${status === 'VERIFIED' ? 'text-success' : 'text-primary'}">${status}</strong>`;
        statusContainer.style.borderLeftColor = status === 'VERIFIED' ? 'var(--accent-green)' : 'var(--primary)';
      }
    } catch (e) {
      statusContainer.innerHTML = 'Error fetching status';
    }
  };

  updateStatus();


  document.getElementById('kyc-submit').onclick = async () => {
    try {
      const { token } = await api.getKycToken();
      launchSumsub(token);
    } catch (e) {
      alert('Failed to start verification: ' + e.message);
    }
  };
}

function launchSumsub(accessToken) {
  const container = document.getElementById('kyc-wizard');

  // Detect if we are in MOCK mode (backend returned a mock token)
  if (accessToken.startsWith('MOCK_TOKEN_')) {
    container.innerHTML = `
      <div class="glass" style="padding: 40px; text-align: center;">
        <div style="font-size: 48px; margin-bottom: 24px;">🧪</div>
        <h3 style="color: var(--accent-gold); margin-bottom: 16px;">Verification Demo Mode</h3>
        <p class="text-muted" style="margin-bottom: 24px;">Backend is not configured with real Sumsub keys. <br> Use this mock interface to test your integration.</p>
        <button class="btn btn-primary" onclick="window.simulateKycSuccess()" style="margin: 0 auto;">Simulate Manual Approval</button>
      </div>
    `;
    return;
  }

  container.innerHTML = '<div id="sumsub-websdk-container"></div>';

  if (window.snsWebSdk) {
    const snsWebSdkInstance = window.snsWebSdk.init(
      accessToken,
      () => api.getKycToken().then(res => res.token)
    )
      .withConf({ lang: 'en' })
      .on('onMessage', (type, payload) => {
        console.log('Sumsub Message:', type, payload);
        if (type === 'idCheck.onStepCompleted') {
          alert('Step completed! Verification processing...');
        }
      })
      .build();

    snsWebSdkInstance.launch('#sumsub-websdk-container');
  } else {
    container.innerHTML = `
      <div class="glass" style="padding: 40px; text-align: center;">
        <h3 style="color: var(--accent-gold); margin-bottom: 16px;">Sumsub SDK Missing</h3>
        <p class="text-muted" style="margin-bottom: 24px;">The Sumsub Web SDK library failed to load or is not included.</p>
        <button class="btn btn-primary" onclick="window.simulateKycSuccess()" style="margin: 0 auto;">Complete Verification (Bypass)</button>
      </div>
    `;
  }
}

window.simulateKycSuccess = async () => {
  try {
    await api.mockVerify();
    alert('Verification Complete! (Mock)');
    navigate('dashboard');
  } catch (e) {
    alert('Mock verification failed: ' + e.message);
  }
};

async function initNotifications() {
  if (!('Notification' in window)) return;

  try {
    const permission = await Notification.requestPermission();
    if (permission === 'granted') {
      // In production, use firebase.messaging().getToken()
      const mockFcmToken = 'mock_fcm_token_' + api.userId;
      await api.updateFcmToken(mockFcmToken);
      console.log('>>> Push Notifications Active (Mock Token Registered)');
    }
  } catch (e) {
    console.warn('>>> Notification initialization failed:', e);
  }
}

async function renderSubsidies() {
  renderDashboard();
  const grid = document.getElementById('dashboard-widgets');
  grid.innerHTML = '<div class="glass-card" style="grid-column: span 3;">Scanning for eligible programs...</div>';

  try {
    const programs = await api.getSubsidies();
    grid.innerHTML = `
      <div class="glass-card" style="grid-column: span 3;">
        <h2 style="margin-bottom: 24px;">Government Benefits & Subsidies</h2>
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); gap: 24px;">
          ${programs.map(p => `
            <div class="glass" style="padding: 24px; display: flex; flex-direction: column;">
              <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 16px;">
                <h3 style="color: var(--accent-gold);">${p.name}</h3>
                <span class="text-success" style="font-weight: 700; font-size: 1.1rem;">${p.amountPerUser} TND</span>
              </div>
              <p class="text-muted" style="font-size: 0.9rem; margin-bottom: 24px; flex: 1;">${p.description}</p>
              <button class="btn btn-glass" onclick="window.claimSubsidy('${p.id}')" style="width: 100%; justify-content: center;">Check Eligibility & Claim</button>
            </div>
          `).join('')}
        </div>
      </div>
    `;
  } catch (err) {
    grid.innerHTML = `<div class="glass-card" style="color: var(--accent-red);">Failed to load subsidies.</div>`;
  }
}

window.claimSubsidy = async (id) => {
  try {
    const res = await api.claimSubsidy(id);
    alert(`Success! Claim status: ${res.status}. Funds will be disbursed shortly.`);
  } catch (e) {
    alert(e.message || 'You might not be eligible for this program.');
  }
};

async function renderDisputes() {
  renderDashboard();
  const grid = document.getElementById('dashboard-widgets');
  grid.innerHTML = `
    <div class="glass-card" style="grid-column: span 3;">
      <h2 style="margin-bottom: 24px;">Dispute Resolution Center</h2>
      <div id="disputes-list" style="display: flex; flex-direction: column; gap: 16px;">
        <p class="text-muted">Loading your claims...</p>
      </div>
    </div>
  `;

  try {
    const disputes = await api.getMyDisputes();
    const list = document.getElementById('disputes-list');
    if (disputes.length === 0) {
      list.innerHTML = '<div class="glass" style="padding: 32px; text-align: center; color: var(--text-dim);">No active disputes found.</div>';
    } else {
      list.innerHTML = disputes.map(d => `
        <div class="glass" style="padding: 20px; display: flex; justify-content: space-between; align-items: center;">
          <div>
            <div style="display: flex; align-items: center; gap: 12px; margin-bottom: 8px;">
              <span class="text-primary" style="font-weight: 600;">${d.category}</span>
              <span class="glass" style="padding: 2px 8px; font-size: 0.75rem; background: ${d.status === 'AUTO_RESOLVED' ? 'rgba(0,255,136,0.1)' : 'rgba(196,161,255,0.1)'}; color: ${d.status === 'AUTO_RESOLVED' ? 'var(--accent-green)' : 'var(--primary)'};">${d.status}</span>
            </div>
            <p style="font-style: italic; font-size: 0.9rem;">"${d.reason}"</p>
          </div>
          <div style="text-align: right;">
            <p class="text-dim" style="font-size: 0.75rem;">Filed on ${new Date(d.createdAt).toLocaleDateString()}</p>
            ${d.aiRecommendation ? `<p class="text-success" style="font-size: 0.8rem; margin-top: 4px;">AI: ${d.aiRecommendation}</p>` : ''}
          </div>
        </div>
      `).join('');
    }
  } catch (e) {
    document.getElementById('disputes-list').innerHTML = 'Error loading disputes.';
  }
}

async function renderP2pTransfer() {
  renderDashboard();
  const grid = document.getElementById('dashboard-widgets');
  grid.innerHTML = `
    <div class="glass-card" style="grid-column: span 3;">
      <h2 style="margin-bottom: 32px;">P2P Instant Transfer</h2>
      
      <div style="max-width: 500px; margin: 0 auto;">
        <div id="p2p-step-1">
          <p class="text-muted" style="margin-bottom: 24px;">Enter the recipient's phone number to find their wallet.</p>
          <div class="input-group">
            <span class="input-label">Recipient Phone Number</span>
            <input type="tel" id="p2p-phone" placeholder="216 12345678" required>
          </div>
          <button class="btn btn-primary" style="width: 100%;" id="p2p-search-btn">Find Recipient</button>
        </div>

        <div id="p2p-step-2" style="display: none;">
          <div class="glass" style="padding: 24px; margin-bottom: 32px;">
            <p class="text-dim" style="font-size: 0.8rem; margin-bottom: 4px;">Sending to:</p>
            <h3 id="recipient-name">---</h3>
            <p class="text-muted" id="recipient-phone-display">---</p>
          </div>

          <div class="input-group">
            <span class="input-label">Amount (TND)</span>
            <input type="number" id="p2p-amount" placeholder="0.00" step="0.01" min="0.01" required>
          </div>

          <button class="btn btn-primary" style="width: 100%;" id="p2p-send-btn">Confirm Transfer</button>
          <button class="btn btn-glass" style="width: 100%; margin-top: 12px; border: none;" onclick="window.renderP2pTransfer()">Cancel</button>
        </div>
      </div>
    </div>
  `;

  let recipientData = null;

  document.getElementById('p2p-search-btn').onclick = async () => {
    const phone = document.getElementById('p2p-phone').value;
    if (!phone) return alert('Please enter a phone number');

    try {
      const result = await api.searchUserByPhone(phone);
      recipientData = result;

      document.getElementById('recipient-name').innerText = result.fullName;
      document.getElementById('recipient-phone-display').innerText = phone;
      document.getElementById('p2p-step-1').style.display = 'none';
      document.getElementById('p2p-step-2').style.display = 'block';
    } catch (e) {
      alert('User not found. Please check the phone number.');
    }
  };

  document.getElementById('p2p-send-btn').onclick = async () => {
    const amount = parseFloat(document.getElementById('p2p-amount').value);
    if (!amount || amount <= 0) return alert('Please enter a valid amount');

    try {
      // 1. Get sender's account (using the first TND account found)
      const accounts = await api.getAccounts();
      const senderAcc = accounts.find(a => a.currency === 'TND');
      if (!senderAcc) throw new Error('You need a TND account to send money.');
      if (senderAcc.balance < amount) throw new Error('Insufficient funds.');

      // 2. Get receiver's account
      const receiverAccounts = await api.getAccountsByUserId(recipientData.userId);

      const receiverAcc = receiverAccounts.find(a => a.currency === 'TND');
      if (!receiverAcc) throw new Error('Recipient does not have a TND wallet.');

      // 3. Process transfer
      await api.transferP2P(senderAcc.id, receiverAcc.id, amount);

      alert(`Successfully sent ${amount} TND to ${recipientData.fullName}!`);
      navigate('dashboard');
    } catch (e) {
      alert(e.message || 'Transfer failed');
    }
  };
}
