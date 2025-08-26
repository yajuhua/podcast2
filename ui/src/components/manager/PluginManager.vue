<template>
  <div class="plugin-container">

    <!-- 上传插件 -->
    <el-upload class="upload-demo" ref="upload" :auto-upload="false" :limit="1">
      <el-button slot="trigger" size="small" type="primary" round>选取插件文件</el-button>
      <el-button style="margin-left: 10px;" size="small" type="success" round @click="submitUpload">上传
      </el-button>
      <el-button style="margin-left: 10px;" size="small" type="primary" round @click="fetchPluginList()">刷新
      </el-button>
      <div slot="tip" class="el-upload__tip">只能上传jar文件</div>
    </el-upload>

    <!-- 分类导航 -->
    <el-tabs v-model="activeTab" @tab-click="onTabClick" type="card" class="tabs">
      <el-tab-pane label="所有插件" name="all"></el-tab-pane>
      <el-tab-pane label="已安装" name="installed"></el-tab-pane>
      <el-tab-pane label="待安装" name="uninstalled"></el-tab-pane>
      <el-tab-pane label="有更新" name="update"></el-tab-pane>
    </el-tabs>

    <!-- 插件列表 -->
    <el-table :data="filteredPluginList" stripe :row-key="row => row.uuid" class="plugin-table">
      <el-table-column prop="name" label="名称" min-width="120" />
      <el-table-column prop="version" label="版本" min-width="100" />
      <el-table-column prop="update" label="更新时间" min-width="160" />
      <el-table-column prop="keyInfo" label="提醒" min-width="120" />
      <el-table-column fixed="right" label="操作" min-width="150">
        <template v-slot="scope">
          <el-dropdown>
            <el-button type="primary" size="mini" round
              :icon="scope.row.installing ? 'el-icon-loading' : 'el-icon-more'">
            </el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item @click.native="pluginDetail(scope.row.uuid)">详细</el-dropdown-item>
              <el-dropdown-item v-if="scope.row.install" @click.native="pluginDelete(scope.row.uuid)">
                <i class="el-icon-delete"></i> 卸载
              </el-dropdown-item>
              <el-dropdown-item v-if="!scope.row.install && !scope.row.hasUpdate"
                @click.native="pluginInstall(scope.row)">
                <span v-if="!scope.row.installing"><i class="el-icon-download"></i> 安装</span>
                <span v-if="scope.row.installing">
                  <i class="el-icon-loading el-icon--right"></i> 安装中...
                </span>
              </el-dropdown-item>
              <el-dropdown-item v-if="scope.row.hasUpdate" @click.native="pluginUpdate(scope.row)">
                <i class="el-icon-refresh"></i> 更新
                <span v-if="scope.row.installing">
                  <i class="el-icon-loading el-icon--right"></i> 更新中...
                </span>
              </el-dropdown-item>
              <el-dropdown-item @click.native="getPluginSettings(scope.row.name)">
                <i class="el-icon-setting"></i> 设置
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>

    <!-- 插件设置 -->
    <el-dialog title="设置" :visible.sync="settingsVisible" :width="adaptWidth()">
      <div v-if="settings.length > 0">
        <el-form ref="form" label-width="auto">
          <div v-for="(item, key) in settings" :key="key">
            <el-form-item :label="item.name">
              <el-input v-model="item.content"></el-input>
              <el-tooltip class="item" effect="dark" :content="item.tip" placement="top-start">
                <i class="el-icon-question"></i>
              </el-tooltip>
            </el-form-item>
          </div>
        </el-form>
      </div>
      <span v-if="settings.length == 0">暂无设置</span>
      <span slot="footer" class="dialog-footer">
        <el-button @click="settingsVisible = false">取 消</el-button>
        <el-button type="primary" @click="updatePluginSettings()">确 定</el-button>
      </span>
    </el-dialog>

    <!-- 插件详细信息 -->
    <el-dialog title="详细信息" :visible.sync="detailVisible" :width="adaptWidth()">
      <el-table :data="detail" stripe style="width: 100%">
        <el-table-column prop="name">
        </el-table-column>
        <el-table-column prop="content">
        </el-table-column>
      </el-table>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="detailVisible = false">确 定</el-button>
      </span>
    </el-dialog>

  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'PluginManager',
  data() {
    return {
      activeTab: 'all', // 默认选择所有插件
      plugin: {
        list: []
      },
      settings: [],
      settingsVisible: false,
      detailVisible: false,
      detail: []
    };
  },
  mounted() {
    this.fetchPluginList();
  },
  computed: {
    // 根据激活的标签过滤插件列表
    filteredPluginList() {
      if (this.activeTab === 'installed') {
        return this.plugin.list.filter(plugin => plugin.install);
      } else if (this.activeTab === 'uninstalled') {
        return this.plugin.list.filter(plugin => !plugin.install);
      } else if (this.activeTab === 'update') {
        return this.plugin.list.filter(plugin => plugin.hasUpdate);
      }
      return this.plugin.list;
    }
  },
  methods: {
    async fetchPluginList() {
      try {
        const response = await axios.get('/api/plugin/list');
        if (response.data.code === 1) {
          this.plugin.list = response.data.data;
        } else {
          this.$message.error('获取插件列表失败');
        }
      } catch (error) {
        console.error(error);
        this.$message.error('获取插件列表失败');
      }
    },
    onTabClick(tab) {
      console.log('Tab clicked:', tab.name);
    },
    pluginInstall(plugin) {
      if (plugin.installing) {
        this.$message.warning('插件正在安装，请稍候...');
        return;
      }
      this.$confirm('此操作将安装该插件, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        plugin.installing = true;
        this.$message.success(`开始安装插件：${plugin.name}`);
        axios.get('/api/plugin/install?uuids=' + plugin.uuid)
          .then(res => {
            if (res.data.code == '1') {
              this.$message.success('插件安装成功！');
              this.fetchPluginList();
            } else {
              this.$message.error(res.data.msg);
            }
            plugin.installing = false;
          }).catch(err => {
            this.$message.error('插件安装失败！');
            console.log(err);
            plugin.installing = false;
          });

        // 定时查询安装状态
        const intervalId = setInterval(() => {
          axios.get('/api/plugin/install/status/' + plugin.uuid)
            .then(res => {
              if (res.data.data.install === true) {
                this.plugin.installStatus = ''; // 清除安装状态
                clearInterval(intervalId); // 停止定时查询

              }
            }).catch(err => {
              console.error('查询安装状态失败：', err);
              clearInterval(intervalId); // 停止定时查询
            });
        }, 2000); // 每2秒查询一次安装状态
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消删除'
        });
      });

    },
    async pluginUpdate(plugin) {
      if (plugin.installing) {
        this.$message.warning('插件正在更新，请稍候...');
        return;
      }
      plugin.installing = true;
      this.$message.success(`开始更新插件：${plugin.name}`);

      try {
        const response = await axios.post('/api/plugin/update', { names: [plugin.name] });

        if (response.data.code === 1) {
          // 轮询更新状态
          const checkUpdateStatus = setInterval(async () => {
            const statusResponse = await axios.get(`/api/plugin/update/status/${plugin.name}`);

            if (statusResponse.data.code === 1) {
              const pluginStatus = statusResponse.data.data;
              if (pluginStatus.install && !pluginStatus.hasUpdate) {
                clearInterval(checkUpdateStatus);
                plugin.installing = false;
                plugin.hasUpdate = false;
                this.$message.success(`${plugin.name} 更新成功`);
              }
            }
          }, 1000); // 每1秒检查一次
        } else {
          plugin.installing = false;
          this.$message.error('更新失败，请重试');
        }
      } catch (error) {
        plugin.installing = false;
        this.$message.error('更新请求失败');
      }
    },
    pluginDelete(uuid) {
      this.$confirm('此操作将永久删除该插件, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        axios.delete('/api/plugin?uuids=' + uuid)
          .then(res => {
            if (res.data.code == '1') {
              this.$message.success("插件删除成功！");
              this.fetchPluginList();

            } else {
              this.$message.error(res.data.msg);
            }
          }).catch(err => {
            this.$message.error("插件删除错误！")
            console.log(err)
          })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消删除'
        });
      });
    },
    //获取插件设置
    getPluginSettings(name) {
      this.settings = []
      this.settingsVisible = true;
      axios.get('/api/plugin/settings/' + name)
        .then(res => {
          if (res.data.code == '1') {
            this.settings = res.data.data;
          } else {
            this.$message.error(res.data.msg);
          }
        }).catch(err => {
          console.log(err);
          this.$message.error('获取插件设置失败！')
        })
    },
    //更新插件设置
    updatePluginSettings() {
      axios.put('/api/plugin/settings', this.settings)
        .then(res => {
          if (res.data.code == '1') {
            this.$message.success('更新插件设置成功！')
          } else {
            this.$message.error(res.data.mgs);
          }
          this.settingsVisible = false;
        }).catch(err => {
          console.log(err);
          this.$message.error('更新插件设置错误！');
        });
    },
    //获取插件详细信息
    pluginDetail(uuid) {
      axios.get('/api/plugin/detail/' + uuid)
        .then(res => {
          if (res.data.code == '1') {
            this.detail = res.data.data;
            this.detailVisible = true;
          } else {
            this.$message.warning('请先安装插件！')
          }
        }).catch(err => {
          console.log('获取插件详细信息失败!')
          console.log(err)
        })
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
    //上传插件
    submitUpload() {
      let name = this.$refs.upload.uploadFiles[0].name;
      let s = name.split(".");
      let ext = s[s.length - 1];
      console.log('文件格式' + ext)
      if (ext == 'jar') {
        //构建一个表单把文件传进去
        let param = new FormData()
        param.append("files", this.$refs.upload.uploadFiles[0].raw)
        axios.post('/api/common/upload/plugin', param, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        }).then(res => {
          if (res.data.code == '1') {
            this.$message.success('上传插件成功');
            this.$refs.upload.uploadFiles = [];
            this.fetchPluginList();
          } else {
            this.$message.error(res.data.msg);
          }

        })
          .catch(error => {
            console.error(error);
          });
      } else {
        this.$message.error('只能上传插件jar包！')
      }

    },
  },
  created() {
    this.fetchPluginList();
  }
};
</script>

<style scoped>
.plugin-container {
  padding: 15px;
  display: flex;
  flex-direction: column;
}

.tabs {
  margin-bottom: 15px;
}

.el-table {
  margin-top: 15px;
}

.el-table th {
  background-color: #f5f7fa;
}

.el-table .el-button {
  width: 100%;
  text-align: center;
}

.el-dropdown-menu {
  display: block;
}

.el-dropdown-menu i.el-icon-loading {
  animation: spin 1.5s infinite linear;
}

/* Spin animation for loading icon */
@keyframes spin {
  0% {
    transform: rotate(0deg);
  }

  100% {
    transform: rotate(360deg);
  }
}
</style>
