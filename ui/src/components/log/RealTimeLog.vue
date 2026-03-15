<template>
  <!-- 实时日志 -->
  <div>
    <el-dialog
      title="实时日志"
      :visible="$store.operationButton.realTimelogVisible"
      width="95%"
      @close="$store.operationButton.realTimelogVisible = false"
    >
      <div class="logs">
        <div v-for="log in realTimeLogs" :key="log.id">
          <LogMessage :message="formatLog(log)" />
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button type="danger" @click="clearRealTimeLog()">清空</el-button>
        <el-button type="primary" @click="copyRealTimeLogs()">复制</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import LogMessage from "../log/LogMessage";
import { copy } from "../../utils/utils";
export default {
  components: {
    LogMessage,
  },
  data() {
    return {
      realTimeLogs: [],
    };
  },
  mounted() {
    this.setupLogsSocket();
  },
  methods: {
    //格式化日志，避免过长
    formatLog(log) {
      // 将日志按照每120个字符进行分割
      const maxLength = 120;
      let formattedLog = "";
      for (let i = 0; i < log.length; i += maxLength) {
        formattedLog += log.substring(i, i + maxLength) + "\n";
      }
      return formattedLog;
    },
    setupLogsSocket() {
      const clientId = Math.random().toString(36).substr(2);
      const token = localStorage.getItem('token');
      const wsUrl = `/ws/logs/${clientId}?token=${token}`;

      let websocket = null;
      let reconnectTimer = null;

      const createWS = () => {
        websocket = new WebSocket(wsUrl);

        websocket.onopen = () => {
          console.log("日志ws连接成功");
        };

        websocket.onmessage = (event) => {
          this.realTimeLogs.push(event.data);
        };

        websocket.onerror = () => {
          console.error("日志ws错误");
          reconnect();
        };

        websocket.onclose = () => {
          console.warn("日志ws关闭");
          reconnect();
        };
      };

      const reconnect = () => {
        if (reconnectTimer) return;
        reconnectTimer = setTimeout(() => {
          console.log("尝试重连日志ws");
          createWS();
          reconnectTimer = null;
        }, 3000);
      };

      createWS();

      // 页面关闭时断开
      window.addEventListener("beforeunload", () => websocket.close());
    },
    //清空实时日志
    clearRealTimeLog() {
      this.realTimeLogs = [];
    },
    copyRealTimeLogs() {
      let tmp = this.realTimeLogs;
      copy(tmp.join("\n"));
      this.$message.success("复制日志成功！");
    },
  },
};
</script>

<style>
.operation-button {
  position: fixed;
  top: 20px;
  right: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  z-index: 9999;
}

.logs {
  height: 60vh;
  /* 将高度设置为页面高度的 80% */
  padding: 0px;
  overflow-y: scroll;
  background-color: #f0f0f0;
}
</style>
