<template>
  <div>
    <!-- 使用 el-tabs 实现顶部导航切换 -->
    <el-tabs v-model="activeTab" type="card">
      <!-- 正在下载标签页 -->
      <el-tab-pane :label="downloadUploadLabel" name="Downloading">
        <el-table :data="download.progress" stripe style="width: 100%">
          <el-table-column prop="channelName" label="频道名称" width="180"></el-table-column>
          <el-table-column prop="itemName" label="节目名称" width="180"></el-table-column>
          <el-table-column label="进度">
            <template slot-scope="{ row }">
              <el-progress :text-inside="true" :stroke-width="26" :percentage="row.downloadProgress"></el-progress>
            </template>
          </el-table-column>
          <el-table-column prop="downloadSpeed" label="速度"></el-table-column>
          <el-table-column prop="downloadTimeLeft" label="剩余时间"></el-table-column>
          <el-table-column label="操作">
            <template slot-scope="{ row }">
              <el-dropdown trigger="click">
                <el-button type="primary" round size="mini" icon="el-icon-more"></el-button>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item @click.native="downloadDetail(row.uuid)"><i class="el-icon-info"></i>详细</el-dropdown-item>
                  <el-dropdown-item @click.native="deleteDownloading(row.uuid)"><i class="el-icon-delete"></i>删除</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 错误标签页 -->
      <el-tab-pane :label="downloadErrorLabel" name="Error">
        <el-table :data="download.error" stripe style="width: 100%">
          <el-table-column label="状态" width="180">
            <template slot-scope="{ row }">
              <i :class="{
                'el-icon-success': row.status === '5' || row.status === '24',
                'el-icon-error': row.status !== '5' && row.status !== '24'
              }" :style="{
                fontSize: '45px',
                color: row.status === '5' || row.status === '24' ? '#54AC1C' : '#F95C61'
              }"></i>
            </template>
          </el-table-column>
          <el-table-column prop="channelName" label="频道名称" width="180"></el-table-column>
          <el-table-column prop="itemName" label="节目标题"></el-table-column>
          <el-table-column label="进度">
            <template slot-scope="{ row }">
              <el-progress :status="row.status !== '5' && row.status !== '24' ? 'exception' : 'success'"
                :text-inside="true" :stroke-width="26" :percentage="row.downloadProgress"></el-progress>
            </template>
          </el-table-column>
          <el-table-column label="操作">
            <template slot-scope="{ row }">
              <el-dropdown trigger="click">
                <el-button type="primary" round size="mini" icon="el-icon-more"></el-button>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item @click.native="reDownload(row.uuid)"><i class="el-icon-refresh"></i>重新</el-dropdown-item>
                  <el-dropdown-item @click.native="downloadDetail(row.uuid)"><i class="el-icon-info"></i>详细</el-dropdown-item>
                  <el-dropdown-item @click.native="(download.conf.uuid = row.uuid) && (download.conf.visible = true)"><i class="el-icon-edit-outline"></i>配置</el-dropdown-item>
                  <el-dropdown-item @click.native="downloadDelete(row.uuid)"><i class="el-icon-delete"></i>删除</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 已完成标签页 -->
      <el-tab-pane :label="doneLabel" name="Done">
        <div style="height: 90vh; overflow-y: scroll">
          <el-button type="danger" round @click="batchDeleteDownloadDone()"
            v-if="download.selectionDownloadDone.length > 1" size="mini">
            批量删除
          </el-button>
          <el-button size="mini" type="primary" round @click="getDownloadDone() && $message.success('刷新成功！')">刷新
      </el-button>
          <el-table :data="download.done" stripe style="width: 100%" ref="selectDownloadDone"
            @selection-change="handleSelectionDownloadDone">
            <el-table-column type="selection" width="55"></el-table-column>
            <el-table-column type="index" width="50"></el-table-column>
            <el-table-column label="状态" width="180">
              <i slot-scope="scope"
                :class="{ 'el-icon-success': scope.row.status === 5 || scope.row.status === 24, 'el-icon-error': scope.row.status !== 5 && scope.row.status !== 24 }"
                :style="{ fontSize: '45px', color: scope.row.status === 5 || scope.row.status === 24 ? '#54AC1C' : '#F95C61' }">
              </i>
            </el-table-column>
            <el-table-column prop="channelName" label="频道名称" width="180"></el-table-column>
            <el-table-column prop="itemName" label="节目标题"></el-table-column>
            <el-table-column label="进度">
              <el-progress :status="scope.row.status != '5' && scope.row.status != '24' ? 'exception' : 'success'"
                :text-inside="true" :stroke-width="26" slot-scope="scope" :percentage="scope.row.downloadProgress">
              </el-progress>
            </el-table-column>
            <el-table-column label="操作">
              <template slot-scope="{ row }">
                <el-dropdown trigger="click">
                  <el-button type="primary" round size="mini" icon="el-icon-more"></el-button>
                  <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item @click.native="reDownload(row.uuid)"><i class="el-icon-refresh"></i>重新</el-dropdown-item>
                    <el-dropdown-item @click.native="downloadDetail(row.uuid)"><i class="el-icon-info"></i>详细</el-dropdown-item>
                    <el-dropdown-item @click.native="(download.conf.uuid = row.uuid) && (download.conf.visible = true)"><i class="el-icon-edit-outline"></i>配置</el-dropdown-item>
                    <el-dropdown-item @click.native="downloadDelete(row.uuid)"><i class="el-icon-delete"></i>删除</el-dropdown-item>
                  </el-dropdown-menu>
                </el-dropdown>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <!-- 下载器信息标签页 -->
      <el-tab-pane label="下载器信息" name="DownloaderInfo">
        <el-table :data="download.info" stripe style="width: 100%">
          <el-table-column prop="name" label="名称"></el-table-column>
          <el-table-column prop="version" label="版本"></el-table-column>
          <el-table-column prop="updateTime" label="更新时间"></el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 下载信息 -->
    <div>
      <el-dialog title="详细" :visible.sync="download.downloadDetailVisible" :width="adaptWidth()" label-position="top">
        <el-form ref="form" :model="download.detail" label-width="auto">
          <el-form-item label="下载时间">
            <div @click="copy(download.detail.createTime)">
              <el-input v-model="download.detail.createTime"></el-input>
            </div>
          </el-form-item>
          <el-form-item label="状态">
            <div @click="copy(download.detail.status)">
              <el-input v-model="download.detail.status"></el-input>
            </div>
          </el-form-item>
          <el-form-item label="下载器">
            <div @click="copy(download.detail.downloaderName)">
              <el-input v-model="download.detail.downloaderName"></el-input>
            </div>
          </el-form-item>
          <el-form-item label="下载器版本">
            <div @click="copy(download.detail.downloaderVersion)">
              <el-input v-model="download.detail.downloaderVersion"></el-input>
            </div>
          </el-form-item>
          <el-form-item label="文件名称">
            <div @click="copy(download.detail.fileName)">
              <el-input v-model="download.detail.fileName"></el-input>
            </div>
          </el-form-item>
          <el-form-item label="频道名称">
            <div @click="copy(download.detail.channelName)">
              <el-input v-model="download.detail.channelName"></el-input>
            </div>
          </el-form-item>
          <el-form-item label="节目标题">
            <div @click="copy(download.detail.itemTitle)">
              <el-input v-model="download.detail.itemTitle"></el-input>
            </div>
          </el-form-item>
          <el-form-item label="节目链接">
            <div @click="copy(download.detail.itemLink)">
              <el-input v-model="download.detail.itemLink"></el-input>
            </div>
          </el-form-item>
          <el-form-item label="订阅链接">
            <div @click="copy(download.detail.subLink)">
              <el-input v-model="download.detail.subLink"></el-input>
            </div>
          </el-form-item>
        </el-form>
      </el-dialog>
    </div>
    <!--  下载配置  -->
    <DownloadConf :uuid="download.conf.uuid" :visible.sync="download.conf.visible"/>
  </div>
</template>

<script>
import axios from 'axios';
import DownloadConf from "@/components/manager/DownloadConf.vue";
import { globalStore } from '@/store';
export default {
  components: {
    DownloadConf
  },
  computed: {
    downloadUploadLabel() {
      return this.download.progress.length > 0 ? '正在上传/下载 ' + this.download.progress.length : '正在上传/下载';
    },
    downloadErrorLabel() {
      return this.download.error.length > 0 ? '错误 ' + this.download.error.length : '错误';
    },
    doneLabel() {
      return this.download.done.length > 0 ? '完成 ' + this.download.done.length : '完成';
    },
    token() {
      return globalStore.token
    }
  },
  mounted() {
    this.getDownloaderInfo();
    this.getDownloadDone();
    this.getDownloadError();
  },
  watch: {
    token: {
      handler(newToken) {
        if (newToken) {
          this.setupDownloadSocket();
        } else if(this.websocket){
          this.websocket.close();
        }
      },
      immediate: true
    }
  },
  data() {
    return {
      activeTab: 'Downloading',
      download: {
        progress: [],
        error: [],
        done: [],
        info: [],
        detail: {},
        downloadDetailVisible: false,
        selectionDownloadDone: [],
        conf: {
          uuid: '',
          visible: false
        }
      },
      upload: {
        progress: [],
      },
      websocket: null
    };
  },
  methods: {
    //重新下载
    reDownload(uuid) {
      this.$confirm('此操作将重新处理该节目, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        axios.get('/api/download/reDownload/' + uuid)
          .then(res => {
            if (res.data.code == '1') {
              this.$message.success('正在重新处理中');
              //重新获取完成下载信息
              this.getDownloadDone();
            } else {
              this.$message.error(res.data.msg);
            }
          }).catch(err => {
            this.$message.error('重新处理失败！');
            console.log(err)
          })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消重新处理'
        });
      });

    },
    //获取下载详细信息
    downloadDetail(uuid) {
      this.download.detail = {}
      this.download.downloadDetailVisible = true
      axios.get('/api/download/detail/' + uuid)
        .then(res => {
          if (res.data.code == '1') {
            this.download.detail = res.data.data;
          } else {
            this.$message.error(res.data.msg);
          }
        }).catch(err => {
          this.$message.error('获取下载详细信息失败！');
          console.log(err);
        })
    },
    downloadDelete(uuid) {
      this.$confirm('此操作将删除该下载, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        axios.delete('/api/download?uuids=' + uuid)
          .then(res => {
            if (res.data.code == '1') {
              this.$message.success('删除成功！')
              this.getDownloadDone();
            } else {
              this.$message.error(res.data.msg)
            }
          }).catch(err => {
            console.log(err)
            this.$message.error('删除失败！')
          })
      }).catch(() => {
        this.$message.info('已取消删除')
      });
    },
    deleteDownloading(uuid) {
      this.$confirm('此操作将删除该下载, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        axios.delete('/api/download/downloading?uuids=' + uuid)
          .then(res => {
            if (res.data.code == '1') {
              this.$message.success('删除成功！')
            } else {
              this.$message.error(res.data.msg)
            }
          }).catch(err => {
            console.log(err)
            this.$message.error('删除失败！')
          })
      }).catch(() => {
        this.$message.info('已取消删除')
      });
    },
    batchDeleteDownloadDone() {
      this.$confirm('此操作将批量删除下载, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        const uuids = this.download.selectionDownloadDone.map(item => item.uuid);
        axios.delete('/api/download?uuids=' + uuids)
          .then(res => {
            if (res.data.code == '1') {
              this.$message.success('批量删除成功！')
              this.getDownloadDone();
            } else {
              this.$message.error(res.data.msg)
            }
          }).catch(err => {
            console.log(err)
            this.$message.error('批量删除失败！')
          })
      }).catch(() => {
        this.$message.info('已取消批量删除')
      });
    },
    handleSelectionDownloadDone(val) {
      this.download.selectionDownloadDone = val;
    },
    //获取下载器信息
    getDownloaderInfo() {
      axios.get('/api/download/info')
        .then(res => {
          if (res.data.code == '1') {
            this.download.info = res.data.data;
          } else {
            this.$message.error('获取下载器信息失败！')
          }
        }).catch(err => {
          this.$message.error('获取下载器信息失败！')
          console.log(err)
        })
    },
    //获取下载完成的信息
    async getDownloadDone() {
      axios.get('/api/download/completed')
        .then(res => {
          if (res.data.code == '1') {
            this.download.done = res.data.data;
          } else {
            this.$message.error(res.data.msg)
          }
        }).catch(err => {
          console.log(err)
          this.$message.error('获取下载完成的信息失败！')
        })
    },
    //获取下载错误的信息
    getDownloadError() {
      axios.get('/api/download/error')
        .then(res => {
          if (res.data.code == '1') {
            this.download.error = res.data.data;
          } else {
            this.$message.error(res.data.msg)
          }
        }).catch(err => {
          console.log(err)
          this.$message.error('获取下载完成的信息失败！')
        })
    },
    //复制内容到粘贴板
    copy(content) {
      const textarea = document.createElement('textarea');

      console.log('复制到粘贴板')

      textarea.value = content;
      textarea.setAttribute('readonly', '');
      textarea.style.position = 'absolute';
      textarea.style.left = '-9999px';
      document.body.appendChild(textarea);
      textarea.select();
      document.execCommand('copy');
      document.body.removeChild(textarea);

      // 延迟显示复制成功提示
      setTimeout(() => {
        this.$message({
          message: '复制成功！',
          type: 'success'
        });
      }, 100);

      console.log('内容已成功复制到剪贴板');
    },
    //适配宽度
    adaptWidth() {
      let type = this.$deviceType;
      console.log("deviceType: " + type);
      if (type == 'mobile') {
        return '80%';
      } else if (type == 'tablet') {
        return '50%';
      } else {
        return '40%';
      }
    },
    //下载进度展示
    setupDownloadSocket() {
      let clientId = Math.random().toString(36).substr(2);
      const token = localStorage.getItem('token');
      let wsUrl = `/ws/download/${clientId}?token=${token}`;

      if ('WebSocket' in window) {
        this.websocket = new WebSocket(wsUrl);
      } else {
        alert('Not support websocket')
      }

      //连接发生错误的回调方法
      this.websocket.onerror = function () {
        console.log('下载ws连接错误')
      };

      //连接成功建立的回调方法
      this.websocket.onopen = function () {
        console.log('下载ws连接成功')
      }

      //接收到消息的回调方法
      var vm = this;
      this.websocket.onmessage = function (event) {
        let message = event.data;
        let object = JSON.parse(message);
        console.log(object);
        if (vm.download.progress.length != object.length) {
          vm.getDownloadDone();
          vm.getDownloadError();
        }
        vm.download.progress = object;
      }

      //连接关闭的回调方法
      this.websocket.onclose = function () {
        console.log('下载ws关闭')
      }

      window.onbeforeunload = function () {
        this.websocket.close();
      }
    },
  },
};
</script>

<style scoped>
.el-tabs {
  margin-bottom: 20px;
}

/* 小屏幕适配 */
@media (max-width: 600px) {
  .el-table {
    font-size: 12px;
  }

  .el-table-column {
    font-size: 12px;
  }

  .el-button {
    font-size: 12px;
  }

  /* 简化显示，隐藏某些列 */
  .el-table-column:nth-child(3),
  .el-table-column:nth-child(4),
  .el-table-column:nth-child(5) {
    display: none;
  }
}

/* 中等屏幕适配 */
@media (min-width: 600px) and (max-width: 1024px) {
  .el-table {
    font-size: 14px;
  }

  .el-table-column {
    font-size: 14px;
  }

  .el-button {
    font-size: 14px;
  }
}

/* 大屏幕适配 */
@media (min-width: 1024px) {
  .el-table {
    font-size: 16px;
  }

  .el-table-column {
    font-size: 16px;
  }

  .el-button {
    font-size: 16px;
  }
}
</style>
