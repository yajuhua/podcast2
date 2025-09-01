import Vue from 'vue'
import App from './App.vue'
import router from './router'
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import AxiosHander from './axios';
import { globalStore } from './store'

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
Vue.config.productionTip = false
Vue.prototype.$http = AxiosHander;
Vue.use(ElementUI);
// 挂载全局变量
Vue.prototype.$store = globalStore

new Vue({
  router,
  render: h => h(App)
}).$mount('#app')
