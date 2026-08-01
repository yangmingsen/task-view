import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/task/new',
    name: 'TaskCreate',
    component: () => import('@/views/TaskForm.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/task/:id',
    name: 'TaskDetail',
    component: () => import('@/views/TaskDetail.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/task/:id/edit',
    name: 'TaskEdit',
    component: () => import('@/views/TaskForm.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/share/:id',
    name: 'ShareDetail',
    component: () => import('@/views/ShareDetail.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/',
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true'
  if (to.meta.requiresAuth && !isLoggedIn) {
    next('/login')
  } else if (to.path === '/login' && isLoggedIn) {
    next('/')
  } else {
    next()
  }
})

export default router
