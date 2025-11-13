<template>
  <div class="page-wrapper">
    <div class="page-content">
      <operation-button :data.sync="operationButton" v-if="false"></operation-button>
      <!-- 搜索订阅 -->
      <div style="text-align: center;">
        <el-input v-model="searchQuery" style="width: 70%" placeholder="搜索订阅" @input="handleSearchInput()">
        </el-input>
      </div>

      <!-- 订阅详细信息 -->
      <sub-detail :data="subDetail.detail" :visible.sync="subDetail.visible"></sub-detail>

      <!-- 订阅列表展示 -->
      <div style="display: flex;justify-content: center; padding-left: 5%;padding-right: 5%">
        <el-table ref="multipleTable" :data="filteredSubListData" tooltip-effect="dark" style="width: 100%"
          @selection-change="handleSelectionChange" :header-cell-style="{ textAlign: 'center' }"
          :cell-style="{ 'text-align': 'center' }" empty-text="暂无订阅">
          <el-table-column type="selection" width="auto" v-if="selectionVisible"></el-table-column>
          <el-table-column type="index"></el-table-column>
          <el-table-column label="更新" prop="updateTime"></el-table-column>
          <el-table-column label="名称" prop="title" show-overflow-tooltip></el-table-column>

          <!-- 相关操作 -->
          <el-table-column label="操作">
            <template slot-scope="scope">
              <el-dropdown trigger="click">
                <span class="el-dropdown-link">
                  <el-button type="primary" plain round icon="el-icon-more" size="mini"></el-button>
                </span>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item icon="el-icon-document-copy"
                    @click.native="copyUrl(scope.row.uuid)">复制URL</el-dropdown-item>
                  <el-dropdown-item icon="el-icon-full-screen"
                    @click.native="qrcode(scope.row.uuid)">二维码</el-dropdown-item>
                  <el-dropdown-item icon="el-icon-delete"
                    @click.native="batchDelete(scope.row.uuid)">删除</el-dropdown-item>
                  <el-dropdown-item icon="el-icon-edit-outline"
                    @click.native="(editSubUuid = scope.row.uuid) && (editSubVisible = true)">编辑</el-dropdown-item>
                  <el-dropdown-item icon="el-icon-info"
                    @click.native="subDetailShow(scope.row.uuid)">详细</el-dropdown-item>
                  <el-dropdown-item icon="el-icon-circle-plus"
                    @click.native="(appendItem.channelUuid = scope.row.uuid) && (appendItem.visible = true)">追加节目</el-dropdown-item>
                  <el-dropdown-item icon="el-icon-time"
                                    @click.native="(taskStatus.uuid = scope.row.uuid) && (taskStatus.visible = true)">状态</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!--  task状态展示  -->
      <TaskStatusCard :uuid="taskStatus.uuid" :visible.sync="taskStatus.visible"/>

      <!-- 展示二维码 -->
      <quick-code :url="url" :visible.sync="qrcodeVisible"></quick-code>

      <!-- 展示组订阅二维码 -->
      <el-dialog title="二维码" :visible.sync="subGroupData.qrcodeVisible" width="350px">
        <div>
          <vue-qr :text="subGroupData.url + encodeURI(subGroupData.group) + '&xmlConfName=' + subGroupData.xmlConfName" :size="300"></vue-qr>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-input v-model="subGroupData.group" placeholder="请输入组名"></el-input><br><br>
          <el-input v-model="subGroupData.xmlConfName" placeholder="xml配置名称"></el-input><br><br>
          <el-button @click="subGroupData.qrcodeVisible = false">取 消</el-button>
          <el-button type="primary" @click="copy(subGroupData.url + encodeURI(subGroupData.group) + '&xmlConfName=' + subGroupData.xmlConfName)">复 制 URL</el-button>
        </span>
      </el-dialog>

      <!-- 右下角菜单 -->
      <div class="floating-menu">
        <el-dropdown trigger="click">
          <span class="el-dropdown-link">
            <el-button type="primary" icon="el-icon-s-tools" circle></el-button>
          </span>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item icon="el-icon-circle-plus" @click.native="addSubVisible = true">添加订阅</el-dropdown-item>
            <el-dropdown-item icon="el-icon-position"
              @click.native="selectionVisible = !selectionVisible">选择</el-dropdown-item>
            <el-dropdown-item icon="el-icon-delete" @click.native="batchDelete('')">批量删除</el-dropdown-item>
            <el-dropdown-item icon="el-icon-document-add" @click.native="downloadOPML">生成OPML</el-dropdown-item>
            <el-dropdown-item icon="el-icon-folder-add" @click.native="subGroup()">订阅组</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>

      <!-- 添加订阅 -->
      <add-sub :visible.sync="addSubVisible" :subData.sync="subData"></add-sub>

      <!-- 订阅追加节目 -->
      <append-item :visible.sync="appendItem.visible" :channelUuid="appendItem.channelUuid"></append-item>

      <!-- 编辑订阅 -->
      <edit-sub :visible.sync="editSubVisible" :uuid="editSubUuid" @getSubList="getSubList()"></edit-sub>
    </div>

    <!-- 页面底部的控制按钮 -->
    <page-footer></page-footer>
  </div>
</template>

<script>
import axios from 'axios'
import VueQr from 'vue-qr'
import SubDetail from '../components/index/SubDetail'
import QuickCode from '../components/index/QuickCode'
import PageFooter from '../components/index/PageFooter'
import AddSub from '../components/index/AddSub'
import AppendItem from '../components/index/AppendItem'
import EditSub from '../components/index/EditSub'
import OperationButton from '../components/operation/OperationButton'
import { copy } from '@/utils/utils';
import { debounce } from 'lodash';
import TaskStatusCard from "@/components/index/TaskStatusCard.vue";

export default {
  components: {
    VueQr,
    SubDetail,
    QuickCode,
    PageFooter,
    AddSub,
    AppendItem,
    EditSub,
    OperationButton,
    TaskStatusCard
  },
  data() {
    return {
      searchQuery: '',
      searchIng: '',
      subData: [],
      qrcodeVisible: false,
      addSubVisible: false,
      editSubVisible: false,
      editSubUuid: '',
      url: '',
      multipleSelection: [],
      delele: [],
      loading: false,
      addSubStatus: '',
      subDetail: {
        detail: {},
        visible: false
      },
      subGroupData: {
        url: '',
        group: '',
        qrcodeVisible: false,
        uuids: [],
        xmlConfName: ''
      },
      appendItem: {
        channelUuid: '',
        visible: false
      },
      selectionVisible: false,
      operationButton: {
        realTimelogVisible: false,
        historyLogVisible: false
      },
      taskStatus: {
        uuid: null,
        visible: false
      }
    }
  },
  computed: {
    filteredSubListData() {
      return this.subData.filter(item => {
        const titleMatch = item.title.includes(this.searchQuery);
        const updateTimeMatch = item.updateTime.includes(this.searchQuery);
        const uuidMatch = item.uuid.includes(this.searchQuery);
        return titleMatch || updateTimeMatch || uuidMatch;
      });
    },
  },
  mounted() {
    this.getSubList();
  },
  methods: {
    //获取订阅列表
    getSubList() {
      var _this = this;
      axios({
        method: "get",
        url: "/api/sub/list"
      }).then(function (resp) {
        _this.subData = resp.data.data;
      })
    },
    toggleSelection(rows) {
      if (rows) {
        rows.forEach(row => {
          this.$refs.multipleTable.toggleRowSelection(row);
        });
      } else {
        this.$refs.multipleTable.clearSelection();
      }
    },
    handleSelectionChange(val) {
      this.multipleSelection = val;
    },
    //复制URL
    copyUrl(uuid) {
      if (uuid == null || uuid == '') {
        this.copy(this.url);
        this.qrcodeVisible = false;
      } else {
        this.copy(this.generateUrl(uuid));
      }
    },
    //生成链接
    generateUrl(uuid) {
      // 优先使用 .env 文件里的端口
      const port = process.env.VUE_APP_API_PORT || window.location.port || "80";

      // 如果端口为空（默认 80/443），就不要拼接 ":"
      const portPart = port && !["80", "443"].includes(port) ? `:${port}` : "";

      const url =
          window.location.protocol +
          "//" +
          window.location.hostname +
          portPart +
          "/sub/xml/" +
          uuid;

      return url;
    },
    //生成二维码
    qrcode(uuid) {
      this.url = this.generateUrl(uuid);
      this.qrcodeVisible = true;
    },
    batchDelete(uuid) {
      if (uuid == null || uuid == '') {
        if (this.multipleSelection.length == 0) {
          this.$message({
            message: '请先选择！',
            type: 'warning'
          });
          return;
        } else {
          this.delele = [];
          for (var i = 0; i < this.multipleSelection.length; i++) {
            this.delele.push(this.multipleSelection[i].uuid)
          }
        }
      } else {
        this.delele = []
        this.delele.push(uuid)
      }

      this.$confirm('此操作将永久删除选择的订阅, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        axios.delete('/api/sub?uuids=' + this.delele).then(res => {
          if (res.data.code == '1') {
            this.$message.success("删除成功")
            this.getSubList();
          } else if (res.data.code == '0') {
            this.$message.error("删除失败");
          }
        }).catch(error => {
          this.$message.error("未知错误");
          console.log(error)
        })
      }).catch(() => {
        this.$message.info('已取消删除')
      });
    },
    //生成OPML文件
    downloadOPML() {
      if (this.multipleSelection.length != 0) {
        let text = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        text += "<opml version=\"1.0\">\n";
        text += "  <head>\n";
        text += "    <title>OPML</title>\n";
        text += "  </head>\n";
        text += "  <body>\n";
        for (let i = 0; i < this.multipleSelection.length; i++) {
          text += "    <outline type=\"rss\"  xmlUrl=\"" + this.generateUrl(this.multipleSelection[i].uuid) + "\" />\n"
        }
        text += "  </body>\n";
        text += "</opml>\n";

        const blob = new Blob([text], { type: 'text/plain' });
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', 'opml.opml');
        document.body.appendChild(link);
        link.click();
      } else {
        this.$message.error('不能为空！请先选择！');
      }
    },
    //搜索订阅
    subSearch() {
      this.searchIng = 'el-icon-loading';
      axios.get('/api/sub/search?keywords=' + this.searchKeyword)
        .then(res => {
          if (res.data.code == '1') {
            this.subData = res.data.data;
            this.searchIng = '';
          } else {
            this.$message.error(res.data.msg);
            this.searchIng = '';
          }
        })
        .catch(err => {
          this.$message.error(err);
          this.searchIng = '';
        })
    },
    //获取订阅详细信息
    subDetailShow(uuid) {
      this.subDetail.detail = {}
      this.subDetail.visible = true
      axios.get('/api/sub/detail/' + uuid)
        .then(res => {
          if (res.data.code == '1') {
            this.subDetail.detail = res.data.data;
          } else {
            this.$message.error(res.data.msg);
          }
        }).catch(err => {
          this.$message.error('获取订阅详细信息失败！');
          console.log(err);
        })
    },
    //复制内容到粘贴板
    copy(content) {
      copy(content)
      this.$message.success('复制成功！')
    },
    //组订阅
    subGroup() {
      if (this.multipleSelection.length == 0) {
        this.$message({
          message: '请先选择！',
          type: 'warning'
        });
        return;
      } else {
        this.subGroupData.uuids = [];
        for (var i = 0; i < this.multipleSelection.length; i++) {
          this.subGroupData.uuids.push(this.multipleSelection[i].uuid)
        }
        // 优先使用 .env 文件里的端口
        const port = process.env.VUE_APP_API_PORT || window.location.port || "80";

        // 如果端口为空（默认 80/443），就不要拼接 ":"
        const portPart = port && !["80", "443"].includes(port) ? `:${port}` : "";
        this.subGroupData.url = window.location.protocol + '//' + window.location.hostname +  portPart + '/sub/xml?uuids=' + this.subGroupData.uuids + '&group=';
        this.subGroupData.qrcodeVisible = true;
      }
    },
    handleSearchInput: debounce(function() {
      console.log('搜索关键词:', this.searchQuery);
    }, 500), 
  }
}
</script>

<style scoped>
.page-wrapper {
  display: flex;
  flex-direction: column;
  min-height: 95vh;
  /* 页面至少占满屏幕高度 */
}

.page-content {
  flex: 1;
  /* 内容区撑开，footer 被推到底部 */
}

.el-tag+.el-tag {
  margin-left: 10px;
}

.button-new-tag {
  margin-left: 10px;
  height: 32px;
  line-height: 30px;
  padding-top: 0;
  padding-bottom: 0;
}

.input-new-tag {
  width: 90px;
  margin-left: 10px;
  vertical-align: bottom;
}

.floating-menu {
  position: fixed;
  bottom: 20px;
  right: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  z-index: 999;
}
</style>
