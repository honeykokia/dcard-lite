import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../views/Home.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/Login.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/Register.vue'),
      meta: { requiresAuth: false }
    },
  ],
})

// Navigation guard
router.beforeEach((to, from, next) => {
  const accessToken = localStorage.getItem('accessToken')
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)

  if (requiresAuth && !accessToken) {
    // 需要登入但未登入，導向登入頁
    next({ name: 'login' })
  } else if (!requiresAuth && accessToken && (to.name === 'login' || to.name === 'register')) {
    // 已登入但訪問登入/註冊頁，導向首頁
    next({ name: 'home' })
  } else {
    next()
  }
})

export default router
