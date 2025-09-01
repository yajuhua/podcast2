<template>
  <div>
    <!-- 操作按钮 -->
    <el-button type="success" round size="mini" @click="handleExport">导出</el-button>
    <el-button type="primary" round size="mini" @click="triggerImport">
      导入
      <input
        type="file"
        class="custom-file-input"
        ref="fileInput"
        @change="handleFileSelect"
      />
    </el-button>
    <el-button type="success" round size="mini" @click="getSubList()">刷新</el-button>

    <!-- 当前订阅列表 -->
    <el-table
      ref="exportTable"
      :data="subData"
      tooltip-effect="dark"
      style="width: 100%"
      @selection-change="handleExportSelectionChange"
    >
      <el-table-column type="selection" width="55"></el-table-column>
      <el-table-column type="index" width="50"></el-table-column>
      <el-table-column label="更新时间" prop="updateTime" width="120" />
      <el-table-column label="频道名称" prop="title" show-overflow-tooltip />
    </el-table>

    <!-- 导入数据选择弹窗 -->
    <el-dialog title="导入" :visible.sync="dataImportVisible" width="40%">
      <el-table
        ref="importTable"
        :data="importData"
        tooltip-effect="dark"
        style="width: 100%"
        @selection-change="handleImportSelectionChange"
      >
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column type="index" width="50"></el-table-column>
        <el-table-column label="频道名称" prop="sub.title" show-overflow-tooltip />
      </el-table>

      <span slot="footer" class="dialog-footer">
        <el-button @click="dataImportVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitImport">导 入</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import axios from "axios";

export default {
  name: "DataMigration",
  data() {
    return {
      // 当前表格数据
      subData: [],

      // 导出选中的项
      exportData: [],

      // 导入数据
      importData: [],
      selectImport: [],

      // 导入弹窗
      dataImportVisible: false
    };
  },
  created() {
    this.getSubList();  // 在组件创建时获取订阅列表数据
  },
  methods: {
    /** ===== 获取订阅列表数据 ===== */
   getSubList() {
      axios.get('/api/sub/list')
          .then(res => {
            this.subData = res.data.data;
          }).catch(err => {
        console.log(err)
        this.$message.error('获取订阅列表数据失败！')
      })
    },

    /** ===== 导出 ===== */
    handleExport() {
      if (this.exportData.length === 0) {
        this.$message.error("请先选择订阅！");
        return;
      }
      this.$confirm("此操作将导出所选订阅，是否继续？", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      })
        .then(() => {
          const uuids = this.exportData.map(item => item.uuid);
          axios
            .get("/api/user/dataExport?uuids=" + uuids.join(","))
            .then(res => {
              if (res.data.code == "1") {
                const blob = new Blob([JSON.stringify(res.data.data)], {
                  type: "text/plain"
                });
                const url = URL.createObjectURL(blob);
                const link = document.createElement("a");
                link.href = url;
                link.setAttribute("download", "dataExport.json");
                document.body.appendChild(link);
                link.click();
                this.$message.success("导出成功！");
              } else {
                this.$message.error(res.data.msg);
              }
            })
            .catch(err => {
              console.error(err);
              this.$message.error("导出失败！");
            });
        })
        .catch(() => {
          this.$message.info("已取消导出");
        });
    },

    handleExportSelectionChange(val) {
      this.exportData = val;
    },

    /** ===== 导入 ===== */
    triggerImport() {
      this.$refs.fileInput.value = "";
      this.$refs.fileInput.click();
    },

    handleFileSelect(event) {
      const file = event.target.files[0];
      const reader = new FileReader();

      reader.onload = () => {
        try {
          this.importData = JSON.parse(reader.result);
          this.dataImportVisible = true;
        } catch (err) {
          this.$message.error("请正确上传 JSON 文件！");
        }
      };
      reader.readAsText(file);
    },

    handleImportSelectionChange(val) {
      this.selectImport = val;
    },

    submitImport() {
      axios
        .post("/api/user/dataImport", this.selectImport)
        .then(res => {
          if (res.data.code == "1") {
            this.$message.success("导入成功！");
            this.getSubList(); // 导入成功后刷新订阅列表
            this.dataImportVisible = false;
          } else {
            this.$message.error(res.data.msg);
          }
        })
        .catch(err => {
          console.error(err);
          this.$message.error("导入失败！");
        });
    }
  }
};
</script>

<style scoped>
.custom-file-input {
  display: none;
}
</style>
