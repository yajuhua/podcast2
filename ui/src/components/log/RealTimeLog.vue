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
      realTimeLogs: []
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
      let clientId = Math.random().toString(36).substr(2);
      // let wsProtocol = window.location.protocol.includes("https") ? "wss" : "ws";
      // let wsHost = window.location.hostname; // 使用前端主机名
      // let wsPort = window.location.port ? `:${window.location.port}` : ''; // 使用前端端口，如果有的话
      // let wsUrl = `${wsProtocol}://${wsHost}${wsPort}/ws/logs/${clientId}`;
      let wsUrl = `/ws/logs/${clientId}`;

      let websocket = null;
      //判断当前浏览器是否支持WebSocket
      if ("WebSocket" in window) {
        //连接WebSocket节点
        websocket = new WebSocket(wsUrl);
      } else {
        alert("Not support websocket");
      }

      //连接发生错误的回调方法
      websocket.onerror = function () {
        console.log("日志ws连接错误");
      };

      //连接成功建立的回调方法
      websocket.onopen = function () {
        console.log("日志ws连接成功");
      };

      //接收到消息的回调方法
      var vm = this; // 保存对Vue实例的引用
      websocket.onmessage = function (event) {
        let message = event.data;
        vm.realTimeLogs.push(message);
      };

      //连接关闭的回调方法
      websocket.onclose = function () {
        console.log("日志ws关闭");
      };

      //监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
      window.onbeforeunload = function () {
        websocket.close();
      };
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
