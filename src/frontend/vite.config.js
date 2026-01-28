import { defineConfig } from 'vite'

export default defineConfig({
    root: '.',
    server: {
        port: 3000,
        proxy: {
            '/api/auth': 'http://localhost:8081',
            '/api/admin': 'http://localhost:8081',
            '/api/kyc': 'http://localhost:8081',
            '/api/users': 'http://localhost:8081',
            '/api/wallet': 'http://localhost:8082',
            '/api/subsidies': 'http://localhost:8082',
            '/api/payment': 'http://localhost:8083',
            '/api/disputes': 'http://localhost:8083'
        }
    }
})
