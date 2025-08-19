<template>
  <div>
    <!-- 使用 el-tabs 实现顶部导航切换 -->
    <el-tabs v-model="activeTab" type="card">
      <!-- 正在下载标签页 -->
      <el-tab-pane label="正在下载" name="Downloading">
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
                <el-button type="primary">更多</el-button>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item @click="downloadDetail(row.uuid)">详细</el-dropdown-item>
                  <el-dropdown-item @click="deleteDownloading(row.uuid)">删除</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 正在上传标签页 -->
      <el-tab-pane label="正在上传" name="Uploading">
        <el-table :data="upload.progress" stripe style="width: 100%">
          <el-table-column prop="channelName" label="频道名称" width="180"></el-table-column>
          <el-table-column prop="itemName" label="节目名称" width="180"></el-table-column>
          <el-table-column label="进度">
            <template slot-scope="{ row }">
              <el-progress :text-inside="true" :stroke-width="26" :percentage="row.uploadProgress"></el-progress>
            </template>
          </el-table-column>
          <el-table-column prop="uploadSpeed" label="速度"></el-table-column>
          <el-table-column prop="uploadTimeLeft" label="剩余时间"></el-table-column>
          <el-table-column label="操作">
            <template slot-scope="{ row }">
              <el-dropdown trigger="click">
                <el-button type="primary">更多</el-button>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item @click="uploadDetail(row.uuid)">详细</el-dropdown-item>
                  <el-dropdown-item @click="deleteUploading(row.uuid)">删除</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 错误标签页 -->
      <el-tab-pane label="错误" name="Error">
        <el-table :data="download.error" stripe style="width: 100%">
          <el-table-column label="状态" width="180">
            <template slot-scope="{ row }">
              <i
                :class="{
                  'el-icon-success': row.status === '5' || row.status === '24',
                  'el-icon-error': row.status !== '5' && row.status !== '24'
                }"
                :style="{
                  fontSize: '45px',
                  color: row.status === '5' || row.status === '24' ? '#54AC1C' : '#F95C61'
                }"
              ></i>
            </template>
          </el-table-column>
          <el-table-column prop="channelName" label="频道名称" width="180"></el-table-column>
          <el-table-column prop="itemName" label="节目标题"></el-table-column>
          <el-table-column label="进度">
            <template slot-scope="{ row }">
              <el-progress
                :status="row.status !== '5' && row.status !== '24' ? 'exception' : 'success'"
                :text-inside="true"
                :stroke-width="26"
                :percentage="row.downloadProgress"
              ></el-progress>
            </template>
          </el-table-column>
          <el-table-column label="操作">
            <template slot-scope="{ row }">
              <el-dropdown trigger="click">
                <el-button type="primary">更多</el-button>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item @click="reDownload(row.uuid)">重新</el-dropdown-item>
                  <el-dropdown-item @click="downloadDetail(row.uuid)">详细</el-dropdown-item>
                  <el-dropdown-item @click="downloadDelete(row.uuid)">删除</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 已完成标签页 -->
      <el-tab-pane label="已完成" name="Done">
        <div style="height: 90vh; overflow-y: scroll">
          <el-button type="danger" round @click="batchDeleteDownloadDone()" v-if="download.selectionDownloadDone.length > 1">
            批量删除
          </el-button>
          <el-button type="primary" round @click="getDownloadDone()">刷新列表</el-button>
          <el-table :data="download.done" stripe style="width: 100%" ref="selectDownloadDone" @selection-change="handleSelectionDownloadDone">
            <el-table-column type="selection" width="55"></el-table-column>
            <el-table-column type="index" width="50"></el-table-column>
            <el-table-column label="状态" width="180">
              <template slot-scope="{ row }">
                <i
                  :class="{
                    'el-icon-success': row.status === '5' || row.status === '24',
                    'el-icon-error': row.status !== '5' && row.status !== '24'
                  }"
                  :style="{
                    fontSize: '45px',
                    color: row.status === '5' || row.status === '24' ? '#54AC1C' : '#F95C61'
                  }"
                ></i>
              </template>
            </el-table-column>
            <el-table-column prop="channelName" label="频道名称" width="180"></el-table-column>
            <el-table-column prop="itemName" label="节目标题"></el-table-column>
            <el-table-column label="进度">
              <template slot-scope="{ row }">
                <el-progress
                  :status="row.status !== '5' && row.status !== '24' ? 'exception' : 'success'"
                  :text-inside="true"
                  :stroke-width="26"
                  :percentage="row.downloadProgress"
                ></el-progress>
              </template>
            </el-table-column>
            <el-table-column label="操作">
              <template slot-scope="{ row }">
                <el-dropdown trigger="click">
                  <el-button type="primary">更多</el-button>
                  <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item @click="reDownload(row.uuid)">重新</el-dropdown-item>
                    <el-dropdown-item @click="downloadDetail(row.uuid)">详细</el-dropdown-item>
                    <el-dropdown-item @click="downloadDelete(row.uuid)">删除</el-dropdown-item>
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
  </div>
</template>

<script>
export default {
  data() {
    return {
      activeTab: 'Downloading', // 默认显示“正在下载”标签页
      download: {
        progress: [
          {
            uuid: '1',
            channelName: '新闻频道',
            itemName: '每日新闻摘要',
            downloadProgress: 50,
            downloadSpeed: '1MB/s',
            downloadTimeLeft: '5min',
          },
          {
            uuid: '2',
            channelName: '科技频道',
            itemName: '最新科技报道',
            downloadProgress: 20,
            downloadSpeed: '500KB/s',
            downloadTimeLeft: '10min',
          },
        ],
        error: [
          {
            uuid: '3',
            channelName: '娱乐频道',
            itemName: '明星八卦',
            downloadProgress: 0,
            status: 'error',
          },
          {
            uuid: '4',
            channelName: '体育频道',
            itemName: '足球直播',
            downloadProgress: 0,
            status: '5',
          },
        ],
        done: [
          {
            uuid: '7',
            channelName: '历史频道',
            itemName: '古代战争纪录片',
            downloadProgress: 100,
            status: '5',
          },
          {
            uuid: '8',
            channelName: '教育频道',
            itemName: '数学教程',
            downloadProgress: 100,
            status: '24',
          },
        ],
        info: [
          {
            name: 'Downloader A',
            version: '1.0.0',
            updateTime: '2023-10-01',
          },
          {
            name: 'Downloader B',
            version: '2.0.1',
            updateTime: '2023-09-15',
          },
        ],
        selectionDownloadDone: [],
      },
      upload: {
        progress: [
          {
            uuid: '5',
            channelName: '艺术频道',
            itemName: '画作上传',
            uploadProgress: 30,
            uploadSpeed: '2MB/s',
            uploadTimeLeft: '2min',
          },
          {
            uuid: '6',
            channelName: '游戏频道',
            itemName: '游戏录像',
            uploadProgress: 80,
            uploadSpeed: '3MB/s',
            uploadTimeLeft: '30sec',
          },
        ],
      },
    };
  },
  methods: {
    // 操作方法
    reDownload(uuid) {
      console.log('重新下载', uuid);
    },
    downloadDetail(uuid) {
      console.log('查看详情', uuid);
    },
    downloadDelete(uuid) {
      console.log('删除任务', uuid);
    },
    deleteDownloading(uuid) {
      console.log('删除下载任务', uuid);
    },
    deleteUploading(uuid) {
      console.log('删除上传任务', uuid);
    },
    batchDeleteDownloadDone() {
      console.log('批量删除已完成任务');
    },
    getDownloadDone() {
      console.log('刷新已完成任务列表');
    },
    handleSelectionDownloadDone(val) {
      this.download.selectionDownloadDone = val;
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
