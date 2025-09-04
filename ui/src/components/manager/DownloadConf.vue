<template>
  <div>
    <el-dialog
      title="更新下载配置"
      :visible="visible"
      width="30%"
      :before-close="handleClose"
    >
      <span>
        <!-- 动态表单 -->
        <el-form ref="formRef" label-position="top" v-if="downloadConfData.extendList">
          <!-- type 下拉框 -->
          <el-form-item label="类型">
            <el-select v-model="downloadConfData.type" placeholder="请选择类型">
              <el-option label="视频" value="Video"></el-option>
              <el-option label="音频" value="Audio"></el-option>
            </el-select>
          </el-form-item>

          <!-- 动态生成 select 下拉框 -->
          <div
            v-for="(select, selectIndex) in downloadConfData.extendList.selectList"
            :key="'select-' + selectIndex"
          >
            <el-form-item :label="select.name">
              <el-select
                v-model="downloadConfData.selectListData[selectIndex].content"
                placeholder="请选择"
              >
                <span v-show="false">
                  {{ (downloadConfData.selectListData[selectIndex].name = select.name) }}
                </span>
                <el-option
                  v-for="(option, i) in select.options"
                  :key="i"
                  :label="option"
                  :value="option"
                ></el-option>
              </el-select>
            </el-form-item>
          </div>

          <!-- 动态生成 input 输入框 -->
          <div
            v-for="(input, inputIndex) in downloadConfData.extendList.inputList"
            :key="'input-' + inputIndex"
          >
            <span v-show="false">
              {{ (downloadConfData.inputListData[inputIndex].name = input.name) }}
            </span>
            <el-form-item :label="input.name">
              <el-input
                v-model="downloadConfData.inputListData[inputIndex].content"
                placeholder="请输入"
              ></el-input>
            </el-form-item>
          </div>
        </el-form>
      </span>
      <span slot="footer" class="dialog-footer">
        <el-button @click="handleClose()">取消</el-button>
        <el-button type="primary" @click="handleSubmit">更新</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import axios from "axios";
export default {
  name: "DownloadConf",
  props: {
    uuid: {
      type: String,
      required: true,
    },
    visible: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      downloadConfData: {
        isExtend: 1,
        extendList: {
          inputList: [],
          selectList: [],
        },
        inputListData: [],
        selectListData: [],
        type: "",
      },
    };
  },
  created() {
    this.fetchDownloadConf();
  },
  methods: {
    /** 获取配置数据 */
    async fetchDownloadConf() {
      try {
        const res = await axios.get("/api/download/conf/" + this.uuid);
        if (res.data.code === 1) {
          this.downloadConfData = res.data.data;

          // 初始化 selectListData
          if (
            !this.downloadConfData.selectListData ||
            this.downloadConfData.selectListData.length === 0
          ) {
            this.downloadConfData.selectListData = this.downloadConfData.extendList.selectList.map(
              (item) => ({
                name: item.name,
                content: "",
              })
            );
          }

          // 初始化 inputListData
          if (
            !this.downloadConfData.inputListData ||
            this.downloadConfData.inputListData.length === 0
          ) {
            this.downloadConfData.inputListData = this.downloadConfData.extendList.inputList.map(
              (item) => ({
                name: item.name,
                content: "",
              })
            );
          }
        } else {
          this.$message.error("获取配置失败：" + res.data.msg);
        }
      } catch (error) {
        console.error(error);
        this.$message.error("请求出错，请稍后再试");
      }
    },

    /** 提交配置 */
    async handleSubmit() {
      try {
        const submitData = {
          type: this.downloadConfData.type,
          inputListData: this.downloadConfData.inputListData,
          selectListData: this.downloadConfData.selectListData,
          uuid: this.uuid,
        };

        const res = await axios.post("/api/download/conf", submitData);
        if (res.data.code === 1) {
          this.handleClose();
          this.$message.success("修改成功！");
        } else {
          this.$message.error(res.data.msg);
        }
      } catch (error) {
        console.error(error);
        this.$message.error("修改出错，请稍后再试");
      }
    },
    handleClose() {
      this.$emit("update:visible", false); // 通知父组件关闭弹窗
    },
  },
};
</script>
