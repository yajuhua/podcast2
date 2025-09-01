import Vue from 'vue'

// 定义全局响应式对象
export const globalStore = Vue.observable({
  operationButton:{
    realTimelogVisible: false,
    historyLogVisible: false
  }
})
