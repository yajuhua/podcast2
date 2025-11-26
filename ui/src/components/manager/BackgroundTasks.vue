<template>
<div>
  <el-table
      :data="list"
      border
      stripe
      style="width: 100%"
      :row-key="row => row.title"
  >
    <el-table-column
        prop="title"
        label="任务"
        min-width="220"
    >
      <template slot-scope="scope">
        <div style="font-weight:500">{{ scope.row.title }}</div>
      </template>
    </el-table-column>

    <el-table-column
        label="状态"
        width="140"
    >
      <template slot-scope="scope">
        <div class="status-cell">
          <el-tag :style="statusTagStyle(scope.row.statusColor)" effect="dark">{{ scope.row.status || 未知 }}</el-tag>
        </div>
      </template>
    </el-table-column>

    <el-table-column
        prop="lastFireTime"
        label="上次触发"
        min-width="160"
    >
      <template slot-scope="scope">
        <div class="time">{{ scope.row.lastFireTime }}</div>
      </template>
    </el-table-column>

    <el-table-column
        prop="nextFireTime"
        label="下次触发"
        min-width="160"
    >
      <template slot-scope="scope">
        <div class="time">{{ scope.row.nextFireTime }}</div>
      </template>
    </el-table-column>

    <el-table-column
        label="操作"
        fixed="right"
        width="160"
    >
      <template slot-scope="scope">
        <div>
          <el-button size="mini" type="primary" round @click="startNow(scope.row.uuid)">立即执行</el-button>
        </div>
      </template>
    </el-table-column>
  </el-table>
</div>
</template>

<script>
import axios from "@/axios";

export default {
  name: "BackgroundTasks",
  data() {
    return {
      list: []
    };
  },
  created() {
    this.getList();
  },
  methods: {
    //立即执行
    startNow(uuid){
      axios.post('/api/system/backgroundTasks/' + uuid)
          .then(res => {
            if (res.data.code == '1'){
              this.$message.success("操作成功！")
            }else {
              this.$message.error(res.data.msg)
            }
          }).catch(error => {
        console.log(error)
        this.$message.error(error);
      })
    },
    statusTagStyle(statusColor) {
      const bg = statusColor || '#909399';
      const color = this.getReadableTextColor(bg);
      return {
        backgroundColor: bg,
        color,
        padding: '1px 6px',
        borderRadius: '4px',
        display: 'inline-block'
      };
    },
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
    getList(){
      axios.get('/api/system/backgroundTasks/')
          .then(res => {
            if (res.data.code == '1'){
              this.list = res.data.data;
            }else {
              this.$message.error(res.data.msg)
            }
          }).catch(error => {
        console.log(error)
        this.$message.error('无法获取后台任务列表！')
      })
    }
  }
};
</script>