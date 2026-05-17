import { createRouter, createWebHistory } from 'vue-router'
import UserLayout from '@/views/layout/user.vue'
import AdminLayout from '@/views/layout/admin.vue'

import Login from '@/views/login/index.vue'

import Home from '@/views/user/home/index.vue'
import Physics from '@/views/user/physics/index.vue'
import Chemistry from '@/views/user/chemistry/index.vue'
import Biology from '@/views/user/biology/index.vue'
import Borrow from '@/views/user/borrow/index.vue'
import Profile from '@/views/user/profile/index.vue'

import AdminHome from '@/views/admin/home/index.vue'
import AdminEquipment from '@/views/admin/equipment/index.vue'
import AdminBorrow from '@/views/admin/borrow/index.vue'
import AdminUser from '@/views/admin/user/index.vue'

import NotFound from '@/views/layout/404.vue'

const routes = [
  {
    path:'/',
    component:Login
  },
  {
    path: '/userLayout',
    component: UserLayout,
    children: [
      {
        path: '',
        component: Home
      },
      {
        path: 'physics',
        component: Physics
      },
      {
        path: 'chemistry',
        component: Chemistry
      },
      {
        path: 'biology',
        component: Biology
      },
      {
        path: 'borrow',
        component: Borrow
      },
      {
        path: 'profile',
        component: Profile
      }
    ]
  },
  {
    path: '/adminLayout',
    component: AdminLayout,
    children: [
      {
        path: '',
        component: AdminHome
      },
      {
        path: 'equipment',
        component: AdminEquipment
      }, 
      {
        path: 'borrow',
        component: AdminBorrow
      },
      {
        path: 'user',
        component: AdminUser
      },
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    component: NotFound
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
