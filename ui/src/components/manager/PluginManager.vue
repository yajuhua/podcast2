<template>
    <div class="plugin-container">
  
      <!-- 插件详情 -->
      <el-dialog title="详细信息" :visible.sync="plugin.detailVisible" width="30%">
        <el-table :data="plugin.detail" stripe>
          <el-table-column prop="name"></el-table-column>
          <el-table-column prop="content"></el-table-column>
        </el-table>
        <span slot="footer" class="dialog-footer">
          <el-button type="primary" @click="plugin.detailVisible = false">确定</el-button>
        </span>
      </el-dialog>
  
      <!-- 搜索区 -->
      <el-row :gutter="10" type="flex" wrap class="toolbar search-bar">
        <!-- 输入框 -->
        <el-col :xs="24" :sm="12" :md="8" :lg="6" class="search-input">
          <el-input v-model="plugin.search" placeholder="搜索插件" clearable />
        </el-col>
        <!-- 按钮 -->
        <el-col :xs="24" :sm="12" :md="8" :lg="6" class="search-buttons">
          <el-button type="primary" @click="pluginSearch" icon="el-icon-search">搜索</el-button>
          <el-button type="primary" @click="getPluginList">全部</el-button>
        </el-col>
      </el-row>
  
      <!-- 上传区 -->
      <el-row type="flex" wrap class="toolbar upload-bar">
        <el-upload ref="upload" :auto-upload="false" :limit="1" accept=".jar">
          <el-button slot="trigger" size="small" type="primary">上传本地插件</el-button>
        </el-upload>
        <el-button size="small" type="success" @click="submitUpload">上传到服务器</el-button>
        <el-button size="small" type="primary" @click="getPluginList">刷新插件列表</el-button>
      </el-row>
  
      <!-- 表格 -->
      <el-table :data="plugin.list" stripe :row-key="row => row.uuid">
        <el-table-column prop="name" label="名称" min-width="120"/>
        <el-table-column prop="version" label="版本" min-width="100"/>
        <el-table-column prop="update" label="更新时间" min-width="160"/>
        <el-table-column prop="keyInfo" label="提醒" min-width="120"/>
        <el-table-column fixed="right" label="操作" min-width="150">
          <template v-slot="scope">
            <el-dropdown>
              <el-button type="primary" size="mini">
                操作 <i class="el-icon-arrow-down el-icon--right"></i>
              </el-button>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item @click.native="pluginDetail(scope.row.uuid)">详细</el-dropdown-item>
                <el-dropdown-item v-if="scope.row.install" @click.native="pluginDelete(scope.row.uuid)">卸载</el-dropdown-item>
                <el-dropdown-item v-if="!scope.row.install" @click.native="pluginInstall(scope.row.uuid)">安装</el-dropdown-item>
                <el-dropdown-item v-if="scope.row.hasUpdate" @click.native="pluginUpdate(scope.row.name)">更新</el-dropdown-item>
                <el-dropdown-item @click.native="getPluginSettings(scope.row.name)">设置</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
  
    </div>
  </template>
  
  <script>
  export default {
    name: 'PluginManager',
    data() {
      return {
        plugin: {
          search: '',
          searchIng: false,
          list: [
            { uuid: '1', name: '插件A', version: '1.0', update: '2025-08-18', keyInfo: '无', install: true, hasUpdate: false },
            { uuid: '2', name: '插件B', version: '1.1', update: '2025-08-10', keyInfo: '可更新', install: true, hasUpdate: true },
            { uuid: '3', name: '插件C', version: '0.9', update: '2025-07-01', keyInfo: '未安装', install: false, hasUpdate: false }
          ],
          detail: [],
          detailVisible: false
        }
      }
    },
    methods: {
      pluginSearch() {
        this.$message.info(`搜索：${this.plugin.search}`)
        // TODO: 调接口搜索插件
      },
      getPluginList() {
        this.$message.success('刷新插件列表')
        // TODO: 调接口获取插件列表
      },
      submitUpload() {
        this.$refs.upload.submit()
        this.$message.success('上传成功')
        // TODO: 调接口上传插件
      },
      pluginDetail(uuid) {
        this.plugin.detailVisible = true
        this.plugin.detail = [
          { name: 'UUID', content: uuid },
          { name: '示例字段', content: 'xxx' }
        ]
        // TODO: 调接口获取插件详情
      },
      pluginInstall(uuid) {
        this.$message.success(`安装插件：${uuid}`)
        // TODO: 调接口安装插件
      },
      pluginDelete(uuid) {
        this.$message.warning(`卸载插件：${uuid}`)
        // TODO: 调接口卸载插件
      },
      pluginUpdate(name) {
        this.$message.info(`更新插件：${name}`)
        // TODO: 调接口更新插件
      },
      getPluginSettings(name) {
        this.$message.info(`打开插件设置：${name}`)
        // TODO: 打开插件设置
      }
    }
  }
  </script>
  
  <style scoped>
  .plugin-container {
    padding: 15px;
    display: flex;
    flex-direction: column;
  }
  
  .el-table {
    margin-top: 15px;
  }
  
  /* 工具栏基础间距 */
  .toolbar {
    margin: 10px 0;
    align-items: center;
  }
  
  /* 上传区：始终紧凑排列 */
  .upload-bar > * {
    margin-right: 8px;
    margin-bottom: 8px;
  }
  
  /* 搜索区：小屏幕时输入框在第一行，按钮在第二行 */
  @media screen and (max-width: 768px) {
    .search-bar {
      flex-direction: column; /* 垂直排列 */
    }
    .search-bar .search-input,
    .search-bar .search-buttons {
      width: 100%;
      margin-bottom: 10px;
    }
    .search-bar .search-buttons .el-button {
      margin-right: 8px;
      margin-bottom: 8px;
    }
  }
  </style>
  