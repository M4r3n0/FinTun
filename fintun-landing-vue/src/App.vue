<script setup>
import { onMounted } from 'vue'
import { 
  Send, 
  CreditCard, 
  QrCode, 
  Globe, 
  ShieldCheck, 
  ArrowRight, 
  Menu, 
  X,
  Smartphone,
  Zap,
  Lock
} from 'lucide-vue-next'
import gsap from 'gsap'
import { ScrollTrigger } from 'gsap/ScrollTrigger'

gsap.registerPlugin(ScrollTrigger)

const features = [
  {
    title: 'Instant Transfers',
    description: 'Send money to any Fintun user instantly with zero fees.',
    icon: Send
  },
  {
    title: 'Bill Payments',
    description: 'Pay your STEG, SONEDE, and other utilities directly from the app.',
    icon: CreditCard
  },
  {
    title: 'QR Payments',
    description: 'Scan and pay at thousands of merchants across Tunisia.',
    icon: QrCode
  },
  {
    title: 'Multi-Currency',
    description: 'Manage TND and other currencies with real-time exchange rates.',
    icon: Globe
  }
]

const steps = [
  { title: 'Download', desc: 'Get the Fintun app from Play Store or App Store.' },
  { title: 'Verify', desc: 'Securely verify your identity in minutes.' },
  { title: 'Transact', desc: 'Start sending, receiving, and paying instantly.' }
]

onMounted(() => {
  gsap.from('.hero-content > *', {
    y: 50,
    opacity: 0,
    duration: 1,
    stagger: 0.2,
    ease: 'power3.out'
  })

  gsap.utils.toArray('.reveal').forEach(el => {
    gsap.to(el, {
      scrollTrigger: {
        trigger: el,
        start: 'top 85%',
      },
      y: 0,
      opacity: 1,
      duration: 1,
      ease: 'power3.out'
    })
  })
})
</script>

<template>
  <div class="landing-wrapper">
    <!-- Navbar -->
    <nav class="navbar">
      <div class="nav-container">
        <div class="logo">
          <span class="logo-dot"></span>
          Fintun
        </div>
        <div class="nav-links">
          <a href="#features">Features</a>
          <a href="#security">Security</a>
          <a href="#how-it-works">How it works</a>
        </div>
        <div class="nav-auth">
          <a href="http://localhost:3000" class="btn btn-outline">Log In</a>
          <a href="http://localhost:3000" class="btn btn-primary">Get Started</a>
        </div>
      </div>
    </nav>

    <!-- Hero Section -->
    <header class="hero">
      <div class="hero-bg"></div>
      <div class="hero-content">
        <div class="badge">Innovation & Trust</div>
        <h1 class="hero-title">Secure Digital Payments <br/> <span class="text-gradient">for Tunisia</span></h1>
        <p class="hero-subtitle">The modern way to manage your finances. Fast, secure, and built for the future of Tunisian economy.</p>
        <div class="hero-actions">
          <a href="http://localhost:3000" class="btn btn-primary btn-lg">
            Download Now <ArrowRight :size="20" />
          </a>
          <a href="#features" class="btn btn-outline btn-lg">Explore Features</a>
        </div>
      </div>
    </header>

    <!-- Features Section -->
    <section id="features" class="features">
      <div class="section-header reveal">
        <h2 class="section-title">Built for Modern Needs</h2>
        <p class="section-subtitle">Everything you need to manage your money in one place.</p>
      </div>
      <div class="features-grid">
        <div v-for="feature in features" :key="feature.title" class="glass-card reveal">
          <div class="feature-icon">
            <component :is="feature.icon" :size="32" color="#8b5cf6" />
          </div>
          <h3 class="feature-title">{{ feature.title }}</h3>
          <p class="feature-desc">{{ feature.description }}</p>
        </div>
      </div>
    </section>

    <!-- Security Section -->
    <section id="security" class="security">
      <div class="security-container glass-card reveal">
        <div class="security-info">
          <div class="badge-purple">Security First</div>
          <h2 class="section-title">World-Class Encryption</h2>
          <p>Your data and transactions are protected by bank-grade AES-256 encryption. We use multi-factor authentication and biometric security to ensure only you can access your funds.</p>
          <ul class="security-list">
            <li><ShieldCheck :size="20" color="#8b5cf6" /> Real-time fraud monitoring</li>
            <li><Lock :size="20" color="#8b5cf6" /> Biometric Authentication</li>
            <li><Smartphone :size="20" color="#8b5cf6" /> Secure Device Pairing</li>
          </ul>
        </div>
        <div class="security-visual">
          <div class="security-glow"></div>
          <ShieldCheck :size="120" stroke-width="1" color="#8b5cf6" />
        </div>
      </div>
    </section>

    <!-- How it Works -->
    <section id="how-it-works" class="how-it-works">
      <div class="section-header reveal">
        <h2 class="section-title">Get Started in Minutes</h2>
      </div>
      <div class="steps-container">
        <div v-for="(step, index) in steps" :key="step.title" class="step-item reveal">
          <div class="step-number">{{ index + 1 }}</div>
          <h3 class="step-title">{{ step.title }}</h3>
          <p class="step-desc">{{ step.desc }}</p>
        </div>
      </div>
    </section>

    <!-- Footer -->
    <footer class="footer">
      <div class="footer-container">
        <div class="footer-brand">
          <div class="logo">Fintun</div>
          <p>Digitizing payments for everyone in Tunisia.</p>
        </div>
        <div class="footer-links">
          <div class="link-group">
            <h4>Product</h4>
            <a href="#">Features</a>
            <a href="#">Security</a>
            <a href="#">Pricing</a>
          </div>
          <div class="link-group">
            <h4>Company</h4>
            <a href="#">About Us</a>
            <a href="#">Contact</a>
            <a href="#">Legal</a>
          </div>
        </div>
      </div>
      <div class="footer-bottom">
        <p>&copy; 2026 Fintun. All rights reserved.</p>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.landing-wrapper {
  background-color: var(--bg-primary);
}

/* Navbar */
.navbar {
  position: fixed;
  top: 0;
  width: 100%;
  z-index: 1000;
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--glass-border);
  padding: 20px 0;
}

.nav-container {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  font-family: 'Outfit', sans-serif;
  font-size: 24px;
  font-weight: 800;
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-dot {
  width: 12px;
  height: 12px;
  background-color: var(--purple);
  border-radius: 50%;
  box-shadow: 0 0 10px var(--purple);
}

.nav-links {
  display: flex;
  gap: 30px;
}

.nav-links a:hover {
  color: var(--purple);
}

.nav-auth {
  display: flex;
  gap: 15px;
}

/* Hero */
.hero {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  position: relative;
  overflow: hidden;
  padding-top: 80px;
}

.hero-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: url('./assets/hero-bg.png') no-repeat center center;
  background-size: cover;
  opacity: 0.4;
  z-index: 0;
}

.hero-bg::after {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at center, transparent 0%, var(--bg-primary) 80%);
}

.hero-content {
  position: relative;
  z-index: 1;
  max-width: 800px;
  padding: 0 20px;
}

.badge {
  display: inline-block;
  padding: 6px 16px;
  background: rgba(139, 92, 246, 0.1);
  border: 1px solid rgba(139, 92, 246, 0.2);
  color: var(--purple);
  border-radius: 50px;
  font-size: 0.9rem;
  font-weight: 600;
  margin-bottom: 24px;
}

.hero-title {
  font-size: clamp(2.5rem, 8vw, 4.5rem);
  line-height: 1.1;
  margin-bottom: 24px;
}

.hero-subtitle {
  font-size: 1.2rem;
  color: var(--text-secondary);
  margin-bottom: 40px;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}

.hero-actions {
  display: flex;
  gap: 20px;
  justify-content: center;
}

/* Features */
.section-header {
  text-align: center;
  margin-bottom: 60px;
}

.section-title {
  font-size: 2.5rem;
  margin-bottom: 16px;
}

.section-subtitle {
  color: var(--text-secondary);
  font-size: 1.1rem;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 30px;
}

.feature-icon {
  width: 60px;
  height: 60px;
  background: rgba(139, 92, 246, 0.1);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
}

.feature-title {
  font-size: 1.5rem;
  margin-bottom: 12px;
}

.feature-desc {
  color: var(--text-secondary);
}

/* Security */
.security-container {
  display: flex;
  align-items: center;
  gap: 60px;
  padding: 60px;
}

.security-info {
  flex: 1;
}

.badge-purple {
  color: var(--purple);
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 2px;
  font-size: 0.8rem;
  margin-bottom: 16px;
}

.security-list {
  margin-top: 30px;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.security-list li {
  display: flex;
  align-items: center;
  gap: 12px;
}

.security-visual {
  flex: 1;
  display: flex;
  justify-content: center;
  position: relative;
}

.security-glow {
  position: absolute;
  width: 200px;
  height: 200px;
  background: var(--purple);
  filter: blur(100px);
  opacity: 0.3;
  z-index: -1;
}

/* How it Works */
.steps-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 40px;
  margin-top: 40px;
}

.step-item {
  text-align: center;
}

.step-number {
  width: 50px;
  height: 50px;
  background: var(--purple);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 1.2rem;
  margin: 0 auto 24px;
  box-shadow: 0 0 20px var(--purple-glow);
}

/* Footer */
.footer {
  padding: 80px 20px 40px;
  border-top: 1px solid var(--glass-border);
  background: var(--bg-secondary);
}

.footer-container {
  max-width: var(--max-width);
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 40px;
}

.footer-brand p {
  color: var(--text-secondary);
  margin-top: 16px;
  max-width: 200px;
}

.footer-links {
  display: flex;
  gap: 80px;
}

.link-group h4 {
  margin-bottom: 24px;
  font-size: 1.1rem;
}

.link-group a {
  display: block;
  color: var(--text-secondary);
  margin-bottom: 12px;
}

.link-group a:hover {
  color: var(--purple);
}

.footer-bottom {
  max-width: var(--max-width);
  margin: 60px auto 0;
  text-align: center;
  padding-top: 40px;
  border-top: 1px solid var(--glass-border);
  color: #666;
  font-size: 0.9rem;
}

/* Responsive */
@media (max-width: 968px) {
  .nav-links { display: none; }
  .security-container { flex-direction: column; padding: 40px 20px; }
  .footer-links { gap: 40px; }
}

@media (max-width: 640px) {
  .hero-actions { flex-direction: column; }
  .hero-title { font-size: 2.8rem; }
}
</style>
