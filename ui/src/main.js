import Vue from 'vue'
import App from './App.vue'
import router from './router'
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import AxiosHander from './axios';
import { globalStore } from './store'
import { MessageBox } from 'element-ui'
import './styles/mobile-confirm.css'

// 获取设备类型
function getDeviceType() {
  const width = window.innerWidth;
  if (width < 768) {
    return 'mobile';
  } else if (width >= 768 && width < 1024) {
    return 'tablet';
  } else {
    return 'desktop';
  }
}
// 初始化全局设备类型
Vue.prototype.$deviceType = getDeviceType();
// 监听窗口变化实时更新
window.addEventListener('resize', () => {
  Vue.prototype.$deviceType = getDeviceType();
});

// 保存原始 confirm 方法
const originConfirm = MessageBox.confirm

// 重写 confirm
MessageBox.confirm = function(message, title, options = {}) {
  const isMobile = window.innerWidth <= 768

  return originConfirm.call(this, message, title, {
    // 默认配置
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
    ...options,
    customClass: isMobile
      ? `mobile-confirm ${options.customClass || ''}`
      : (options.customClass || '')
  })
}

Vue.config.productionTip = false
Vue.prototype.$http = AxiosHander;
Vue.use(ElementUI);
// 挂载全局变量
Vue.prototype.$store = globalStore
Vue.prototype.$confirm = MessageBox.confirm

new Vue({
  router,
  render: h => h(App)
}).$mount('#app')
