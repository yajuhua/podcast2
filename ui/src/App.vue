<!-- App.vue -->
<template>
  <div id="app">
    <transition name="fade" mode="out-in">
      <router-view></router-view>
    </transition>
    <operation-button v-if="$route.meta.showOperationButton"></operation-button>
    <real-time-log></real-time-log>
    <history-log></history-log>
  </div>
</template>
<script>
import RealTimeLog from './components/log/RealTimeLog.vue'
import HistoryLog from './components/log/HistoryLog.vue'
import OperationButton from './components/operation/OperationButton.vue'

export default {
  components: { RealTimeLog, OperationButton, HistoryLog },
}
</script>
<style>
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.1s;
}
.fade-enter, .fade-leave-to {
  opacity: 0;
}
</style>
