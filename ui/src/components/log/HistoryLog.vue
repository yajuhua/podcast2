<template>
  <!-- 历史日志 -->
  <div>
    <el-dialog
      title="历史日志"
      :visible="$store.operationButton.historyLogVisible"
      width="95%"
      @close="$store.operationButton.historyLogVisible = false"
    >
      <el-form ref="form" label-width="auto" label-position="top">
        <el-form-item label="自定义日志时间范围" v-if="historyLogsLatest == -1">
          <!-- 自定义日志时间范围 -->
          <div style="width: 100%">
            <!-- <el-date-picker
            v-model="selectHistoryByTime"
            type="datetimerange"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            :default-time="['00:00:00']"
            format="yyyy-MM-dd HH:mm:ss"
            value-format="yyyy-MM-dd HH:mm:ss"
            align="center"
            style="width:100%"
          >
          </el-date-picker> -->
            <!-- 开始时间 -->
            <el-date-picker
              v-model="startTime"
              type="datetime"
              placeholder="开始日期时间"
              format="yyyy-MM-dd HH:mm:ss"
              value-format="yyyy-MM-dd HH:mm:ss"
              default-time="00:00:00"
              style="width: 100%"
            />
            <!-- 结束时间 -->
            <el-date-picker
              v-model="endTime"
              type="datetime"
              placeholder="结束日期时间"
              format="yyyy-MM-dd HH:mm:ss"
              value-format="yyyy-MM-dd HH:mm:ss"
              default-time="23:59:59"
              style="width: 100%"
            />
          </div>
        </el-form-item>
        <el-form-item label="查看最近">
          <el-select v-model="historyLogsLatest" placeholder="查看最近">
            <el-option label="最近10分钟" value="10"></el-option>
            <el-option label="最近20分钟" value="20"></el-option>
            <el-option label="最近30分钟" value="30"></el-option>
            <el-option label="最近60分钟" value="60"></el-option>
            <el-option label="最近120分钟" value="120"></el-option>
            <el-option label="自定义" value="-1"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="级别">
          <el-select v-model="historyLogsLevel" placeholder="级别">
            <el-option label="error" value="error"></el-option>
            <el-option label="info" value="info"></el-option> </el-select
        ></el-form-item>
        <el-form-item label="操作">
          <el-row>
            <el-button
              type="primary"
              v-if="historyLogsLatest != -1"
              @click="getHistoryLogs()"
              >查看</el-button
            >
            <el-button
              type="primary"
              v-if="
                startTime.length != 0 && endTime.length != 0 && historyLogsLatest == -1
              "
              @click="getHistoryLogsByTime()"
              >查看</el-button
            >
            <el-button type="success" @click="downloadLogs()">下载</el-button>
            <el-button type="danger" @click="historyLogs = []">清空</el-button>
          </el-row>
        </el-form-item>
      </el-form>
      <div class="logs">
        <div v-for="log in historyLogs" :key="log.id">
          <LogMessage :message="formatLog(log)" />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import LogMessage from "../log/LogMessage";
import axios from "axios";
export default {
  components: {
    LogMessage,
  },
  data() {
    return {
      historyLogs: [],
      historyLogsLatest: 10,
      historyLogsLevel: "error",
      selectHistoryByTime: [],
      startTime: "",
      endTime: "",
    };
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
    //查看最近历史日志
    getHistoryLogs() {
      axios
        .get(
          "/api/system/logs/history/latest?" +
            "minutes=" +
            this.historyLogsLatest +
            "&level=" +
            this.historyLogsLevel
        )
        .then((res) => {
          if (res.data.code == "1") {
            this.historyLogs = res.data.data;
          } else {
            this.$message.error(res.data.msg);
          }
        })
        .catch((err) => {
          console.log(err);
          this.$message.error("最近日志失败！");
        });
    },
    getHistoryLogsByTime() {
      axios
        .get(
          "/api/system/logs/history/between?start=" +
            this.startTime +
            "&end=" +
            this.endTime +
            "&level=" +
            this.historyLogsLevel
        )
        .then((res) => {
          if (res.data.code == "1") {
            this.historyLogs = res.data.data;
          } else {
            this.$message.error(res.data.msg);
          }
        })
        .catch((err) => {
          console.log(err);
          this.$message.error("获取日志失败！");
        });
    },
    //下载日志
    downloadLogs() {
      //不能为空
      if (this.historyLogs.length != 0) {
        const blob = new Blob([this.historyLogs.join("\n")], { type: "text/plain" });
        const url = URL.createObjectURL(blob);
        const link = document.createElement("a");
        link.href = url;
        link.setAttribute("download", this.historyLogsLevel + ".log");
        document.body.appendChild(link);
        link.click();
      } else {
        this.$message.error("不能下载空日志！");
      }
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
