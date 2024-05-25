import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    component: () => import('../views/IndexView.vue')
  },
  {
    path: '/index',
    component: () => import('../views/IndexView.vue')
  },
  {
    path: '/login',
    component: () => import('../views/LoginView.vue')
  },
  {
    path: '/qr',
    component: () => import('../views/QuickResponseCode.vue')
  },
  {
    path: '/manager',
    component: () => import('../views/ManagerView.vue')
  },
  {
    path: '/404',
    component: () => import( '../views/404View.vue')
  },
  {
    path: '*',
    redirect: '/404'
  }

]

const router = new VueRouter({
  routes
})

export default router
