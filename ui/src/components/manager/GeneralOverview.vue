<template>
  <div class="dashboard">
    <!-- 顶部操作区 -->
    <div class="actions">
      <el-button type="primary" @click="handleRestart" round>重启</el-button>
      <el-button type="success" @click="handleCheckUpdate" round>检查更新</el-button>
    </div>

    <!-- 系统信息卡片 -->
    <el-row :gutter="20" class="info-row">
      <!-- 一组 if/else -->
      <template v-if="loading">
        <el-col :span="24">
          <div class="loading-box">
            <el-spin size="large">加载中...</el-spin>
          </div>
        </el-col>
      </template>

      <template v-else-if="error">
        <el-col :span="24">
          <el-alert type="error" :closable="false" :title="error" />
        </el-col>
      </template>

      <template v-else>
        <el-col :xs="24" :sm="12" :md="8" v-for="item in infoList" :key="item.name">
          <el-card shadow="hover" class="info-card">
            <div class="info-content">
              <h3>{{ item.name }}</h3>
              <p>{{ item.content }}</p>
            </div>
          </el-card>
        </el-col>
      </template>
    </el-row>

    <!-- 检查更新窗口 -->
    <div>
      <el-dialog
        title="检查更新"
        :visible.sync="system.update.windowsVisible"
        :width="adaptWidth"
        v-loading="system.update.windowsLoading"
        :before-close="handleCloseUpdateWindow"
        :element-loading-text="system.update.loadingTip"
      >
        <div v-html="system.update.info.desc"></div>
        <span slot="footer" class="dialog-footer">
          <el-button @click="system.update.windowsVisible = false">关闭窗口</el-button>
          <el-button
            type="primary"
            v-if="
              system.update.info.hasUpdate &&
              !system.update.status.download &&
              !system.update.status.downloading
            "
            @click="downloadJarFile()"
            >下载最新Jar包</el-button
          >
          <el-button
            type="primary"
            :loading="system.update.status.downloading"
            v-if="system.update.status.downloading"
            >下载中 {{ system.update.status.percent }}%</el-button
          >
          <el-button
            type="primary"
            v-if="system.update.status.downloading"
            @click="cancelDownloadJarFile()"
            >取消下载</el-button
          >
          <el-button
            v-if="system.update.status.download"
            type="danger"
            @click="deleteDownloadLatestJarFile()"
            >删除</el-button
          >
          <el-button
            v-if="system.update.status.download"
            type="primary"
            @click="handleRestart()"
            >立即重启</el-button
          >
        </span>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import axios from "axios";
import { adaptWidth } from '@/utils/utils';
export default {
  computed: {
    adaptWidth() {
      return adaptWidth();
    }
  },
  data() {
    return {
      infoList: [],
      loading: false,
      error: null,
      system: {
        //在线更新
        update: {
          info: {
            hasUpdate: false,
            version: '',
            desc: ''
          },
          status:{
            percent: 0.0,
            completed: false,
            error: false,
            download: false,
            downloading: true,
            version: ''
          },
          windowsVisible: false,
          windowsLoading: false,
          loadingTip: ''
        }
      },
    };
  },
  created() {
    this.fetchInfo();
  },
  methods: {
    async fetchInfo() {
      this.loading = true;
      this.error = null;
      try {
        const res = await axios.get("/api/system/info");
        if (res.data.code === 1) {
          this.infoList = res.data.data;
        } else {
          this.error = "获取系统信息失败";
        }
      } catch (err) {
        this.error = "请求出错，请检查网络或服务器";
        console.error(err);
      } finally {
        this.loading = false;
      }
    },
    handleRestart() {
      this.$confirm("此操作将重启项目, 是否继续?", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          axios
            .get("/api/system/restart")
            .then((res) => {
              if (res.data.code == "1") {
                this.$message({
                  showClose: true,
                  message: "请等待3-4分钟，正在重启中...",
                  type: "success",
                });
              } else {
                this.$message({
                  showClose: true,
                  message: res.data.msg,
                  type: "error",
                });
              }
            })
            .catch((err) => {
              console.log(err);
              this.$message({
                showClose: true,
                message: "重启失败!",
                type: "error",
              });
            });
        })
        .catch(() => {
          this.$message({
            showClose: true,
            message: "已取消",
            type: "info",
          });
        });
    },

    //检查更新
    handleCheckUpdate() {
      this.system.update.windowsVisible = false;
      this.system.update.windowsLoading = false;
      this.system.update.loadingTip = "正在获取更新信息中...";
      this.system.update.windowsVisible = true;
      this.system.update.windowsLoading = true;
      this.getDownloadJarFileStatus();
      axios
        .get("/api/system/update/has")
        .then((res) => {
          if (res.data.code == 1) {
            this.system.update.info = res.data.data;
            this.system.update.windowsLoading = false;
          } else {
            this.system.update.windowsLoading = false;
            this.$message({
              showClose: true,
              message: res.data.msg,
              type: "error",
            });
          }
        })
        .catch((err) => {
          console.log(err);
          this.system.update.windowsLoading = false;
          this.$message({
            showClose: true,
            message: "无法获取更新信息！",
            type: "error",
          });
        });
    },

    //获取Jar包文件下载状态
    getDownloadJarFileStatus() {
      axios
        .get("/api/system/update/jarStatus?version=" + this.system.update.info.version)
        .then((res) => {
          if (res.data.code == 1) {
            this.system.update.status = res.data.data;
          } else {
            this.$message({
              showClose: true,
              message: res.data.msg,
              type: "error",
            });
          }
        })
        .catch((err) => {
          console.log(err);
          this.$message({
            showClose: true,
            message: "无法获取Jar包下载状态！",
            type: "error",
          });
        });
    },

    //删除最新版本的Jar文件
    deleteDownloadLatestJarFile() {
      this.$confirm("此操作将删除该更新Jar包, 是否继续?", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          axios
            .get("/api/system/update/delete?version=" + this.system.update.info.version)
            .then((res) => {
              if (res.data.code == 1) {
                if (res.data.data == true) {
                  this.$message({
                    showClose: true,
                    message: "删除成功",
                    type: "success",
                  });
                  //刷新状态
                  this.getDownloadJarFileStatus();
                } else {
                  this.$message({
                    showClose: true,
                    message: "删除最新版本Jar包失败!",
                    type: "error",
                  });
                }
              } else {
                this.$message({
                  showClose: true,
                  message: res.data.msg,
                  type: "error",
                });
              }
            })
            .catch((err) => {
              console.log(err);
              this.$message({
                showClose: true,
                message: "删除最新版本Jar包失败！",
                type: "error",
              });
            });
        })
        .catch(() => {
          this.$message({
            showClose: true,
            message: "已取消",
            type: "info",
          });
        });
    },

    //下载jar包
    downloadJarFile() {
      this.system.update.loadingTip = "正在发送下载请求中，请稍等...";
      this.system.update.windowsLoading = true;
      axios
        .get("/api/system/update/download?version=" + this.system.update.info.version)
        .then((res) => {
          if (res.data.code == 1) {
            this.system.update.windowsLoading = false;
            this.$message({
              showClose: true,
              message: "开始下载Jar包",
              type: "success",
            });
            //获取下载状态
            let interval = setInterval(() => {
              this.getDownloadJarFileStatus();
              let status = this.system.update.status;
              if (status.download || status.error || status.completed) {
                clearInterval(interval);
                interval = null;
                return;
              }
            }, 1000); // 每1秒发送一次请求
          } else {
            this.system.update.windowsLoading = false;
            this.$message({
              showClose: true,
              message: res.data.msg,
              type: "error",
            });
          }
        })
        .catch((err) => {
          console.log(err);
          this.system.update.loading = false;
          this.$message({
            showClose: true,
            message: "无法下载Jar包！",
            type: "error",
          });
        });
    },

    //关闭更新窗口前清空数据
    handleCloseUpdateWindow(done) {
      console.log(done);
      this.system.update.info.hasUpdate = false;
      this.system.update.info.version = "";
      this.system.update.info.desc = "";
      this.system.update.status.percent = 0.0;
      this.system.update.status.completed = false;
      this.system.update.status.error = false;
      this.system.update.status.download = false;
      this.system.update.status.downloading = true;
      this.system.update.status.version = "";
      this.system.update.windowsVisible = false;
      this.system.update.windowsLoading = false;
      this.system.update.loadingTip = "";
    },

    //取消Jar文件下载
    cancelDownloadJarFile() {
      this.$confirm("此操作将取消下载Jar文件, 是否继续?", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          axios
            .get("/api/system/update/cancel")
            .then((res) => {
              if (res.data.code == 1) {
                this.$message({
                  showClose: true,
                  message: "取消成功！",
                  type: "success",
                });
                //刷新状态
                this.getDownloadJarFileStatus();
              } else {
                this.$message({
                  showClose: true,
                  message: res.data.msg,
                  type: "error",
                });
              }
            })
            .catch((err) => {
              console.log(err);
              this.$message({
                showClose: true,
                message: "取消下载Jar文件失败",
                type: "error",
              });
            });
        })
        .catch(() => {
          this.$message({
            showClose: true,
            message: "已取消",
            type: "info",
          });
        });
    },
  },
};
</script>

<style>
.dashboard {
  padding: 20px;
}
.actions {
  margin-bottom: 30px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.info-row {
  margin-bottom: 20px;
}
.info-card {
  margin-bottom: 20px;
  min-height: 140px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.info-content {
  width: 100%;
}
h3 {
  margin: 0 0 12px;
  font-size: 16px;
}
p {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
  word-break: break-all;
}
.loading-box {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}
</style>
