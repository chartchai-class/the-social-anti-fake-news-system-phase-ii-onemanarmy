import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import { inject } from '@vercel/analytics'
import { useAuthStore, type Role } from './stores/auth'
inject()

import '@/services/AxiosInterceptor';

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)

const authStore = useAuthStore(pinia)

router.beforeEach((to, _from, next) => {
  const requiredRoles = to.meta.requiredRoles as Role[] | undefined
  if (!requiredRoles || requiredRoles.length === 0) {
    next()
    return
  }

  if (authStore.hasAnyRole(requiredRoles)) {
    next()
  } else {
    next({ name: 'home', query: { denied: 'true' } })
  }
})

app.use(router)

app.mount('#app')
