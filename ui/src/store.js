import Vue from 'vue'

// 定义全局响应式对象
export const globalStore = Vue.observable({
  operationButton:{
    realTimelogVisible: false,
    historyLogVisible: false
  },
  "token": localStorage.getItem("token")
})

export const setGlobalStore = (key, value) => {
  Vue.set(globalStore, key, value)
}
