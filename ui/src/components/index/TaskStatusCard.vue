<template>
  <el-dialog
      :visible="visible"
      @close="handleClose()"
      @change="execForceUpdate()"
      :width="adaptWidth"
  >
    <el-card shadow="never" class="status-card">
      <div slot="header" class="card-header">
        <span>{{ dataObj.title }}</span>
        <slot name="extra"></slot>
      </div>

      <el-descriptions :border="true" :column="Number('1')" style="margin-top:6px;">
        <el-descriptions-item label="状态">
          <el-tag :style="statusTagStyle" effect="dark">{{ dataObj.status || '未知' }}</el-tag>
        </el-descriptions-item>

        <el-descriptions-item label="上次触发时间">
          <span>{{ dataObj.lastFireTime || '未知' }}</span>
        </el-descriptions-item>

        <el-descriptions-item label="下次触发时间">
          <span>{{ dataObj.nextFireTime || '未知' }}</span>
        </el-descriptions-item>

        <el-descriptions-item label="操作">
          <el-button type="primary" round size="mini" @click="startNow()">立即执行</el-button>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </el-dialog>
</template>

<script>
import axios from "@/axios";
import {adaptWidth} from '@/utils/utils';
export default {
  name: 'TaskStatusCard',
  props: {
    uuid: {
      type: String,
      default: null
    },
    visible:{
      type: Boolean,
      default: false,
    }
  },
  watch: {
    uuid: {
      handler(uuid) {
        this.getData(uuid);
      },
      deep: true
    },
    visible: {
      handler(visible){
        if (visible){
          this.getData(null)
        }
      }
    }
  },
  data(){
    return {
      rsData: null
    }
  },
  computed: {
    dataObj() {
      return this.rsData || {};
    },
    adaptWidth() {
      return adaptWidth();
    },
    statusTagStyle() {
      const bg = (this.dataObj && this.dataObj.statusColor) || '#909399';
      const color = this.getReadableTextColor(bg);
      return {
        backgroundColor: bg,
        color,
        padding: '1px 6px',
        borderRadius: '4px',
        display: 'inline-block'
      };
    }
  },
  methods: {
    getReadableTextColor(bg) {
      try {
        let r, g, b;
        if (typeof bg !== 'string') return '#fff';
        if (bg.startsWith('#')) {
          let hex = bg.replace('#', '');
          if (hex.length === 3) {
            r = parseInt(hex[0] + hex[0], 16);
            g = parseInt(hex[1] + hex[1], 16);
            b = parseInt(hex[2] + hex[2], 16);
          } else if (hex.length === 6) {
            r = parseInt(hex.slice(0, 2), 16);
            g = parseInt(hex.slice(2, 4), 16);
            b = parseInt(hex.slice(4, 6), 16);
          } else {
            return '#fff';
          }
        } else if (bg.startsWith('rgb')) {
          const parts = bg.match(/\d+/g);
          if (!parts) return '#fff';
          r = +parts[0]; g = +parts[1]; b = +parts[2];
        } else {
          return '#fff';
        }
        const yiq = ((r * 299) + (g * 587) + (b * 114)) / 1000;
        return yiq >= 128 ? '#000' : '#fff';
      } catch (e) {
        return '#fff';
      }
    },
    getData(uuid){
      if (uuid == null){
        uuid = this.uuid;
      }
      axios.get('/api/sub/status/' + uuid)
          .then(res => {
            if (res.data.code == '1'){
              this.rsData = res.data.data;
            }else {
              this.$message.error(res.data.msg)
            }
          }).catch(error => {
        console.log(error)
        this.$message.error('未知错误！')
      })
    },
    //立即执行
    startNow(uuid){
      axios.post('/api/sub/status/' + this.uuid)
          .then(res => {
            if (res.data.code == '1'){
              this.getData(uuid);
              this.$message.success("操作成功！")
            }else {
              this.$message.error(res.data.msg)
            }
          }).catch(error => {
        console.log(error)
        this.$message.error('未知错误！')
      })
    },
    handleClose() {
      this.$emit('update:visible', false); // 通知父组件关闭弹窗
    },
    execForceUpdate() {
      this.$forceUpdate();
    }
  }
};
</script>

<style scoped>
.card-header {
  font-weight: 600;
  font-size: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.status-card { max-width: 520px; }
</style>