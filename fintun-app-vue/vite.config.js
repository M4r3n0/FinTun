import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [vue()],
    server: {
        port: 3000,
        proxy: {
            '/api/auth': {
                target: 'http://localhost:8081',
                changeOrigin: true
            },
            '/api/users': {
                target: 'http://localhost:8081',
                changeOrigin: true
            },
            '/api/wallet': {
                target: 'http://localhost:8082',
                changeOrigin: true
            },
            '/api/payment': {
                target: 'http://localhost:8083',
                changeOrigin: true
            },
            '/api/qr': {
                target: 'http://localhost:8084',
                changeOrigin: true
            },
            '/api/kyc': {
                target: 'http://localhost:8081',
                changeOrigin: true
            },
            '/api/subsidies': {
                target: 'http://localhost:8082',
                changeOrigin: true
            }
        }
    }
})
