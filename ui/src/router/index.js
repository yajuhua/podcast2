import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    meta: { showOperationButton: true },
    component: () => import('../views/IndexView.vue')
  },
  {
    path: '/index',
    meta: { showOperationButton: true },
    component: () => import('../views/IndexView.vue')
  },
  {
    path: '/login',
    meta: { showOperationButton: false },
    component: () => import('../views/LoginView.vue')
  },
  {
    path: '/qr',
    component: () => import('../views/QuickResponseCode.vue')
  },
  {
    path: '/manager',
    meta: { showOperationButton: true },
    component: () => import('../views/ManagerView.vue')
  },
  {
    path: '/404',
    meta: { showOperationButton: false },
    component: () => import( '../views/404View.vue')
  },
  {
    path: '*',
    meta: { showOperationButton: false },
    redirect: '/404'
  }

]

const router = new VueRouter({
  routes
})

export default router
